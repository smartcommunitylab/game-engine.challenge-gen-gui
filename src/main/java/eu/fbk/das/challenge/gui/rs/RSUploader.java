package eu.fbk.das.challenge.gui.rs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import eu.trentorise.game.challenges.api.Constants;
import eu.trentorise.game.challenges.model.ChallengeDataDTO;
import eu.trentorise.game.challenges.model.ChallengeDataInternalDto;
import eu.trentorise.game.challenges.rest.GamificationEngineRestFacade;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import java.io.*;
import java.util.*;

import static eu.fbk.das.rs.Utils.*;

/**
 * {@link Runnable} class for challenge upload into GamificationEngine
 */
public class RSUploader implements Runnable {

    private static final Logger logger = LogManager
            .getLogger(RSUploader.class);

    private final DateTime date;

    private RecommenderSystemController controller;
    private String gameId;
    private String output;
    private GamificationEngineRestFacade facade;

    private String fileName;

    RSUploader(RecommenderSystemController controller, String gameId, DateTime date, String output) {
        this.controller = controller;
        this.gameId = gameId;
        this.output = output;
        this.date = date;
    }

    @Override
    public void run() {

        if (output == null) {
            addLog("output file cannot be null");
            return;
        }

        facade = controller.rs.getFacade();
        if (facade == null) {
            addLog("ERROR! output file cannot be null");
            return;
        }

        // TODO fixare lettura da disco
        // List<ChallengeDataInternalDto> challenges = readChallenges();
        // List<ChallengeDataInternalDto> challenges = prepareChallenges();

        Map<String, List<ChallengeDataDTO>> challenges = controller.challanges;
        if (challenges == null) {
            addLog("ERROR! challenges cannot be null");
            return;
        }

        // Write challenges to disk
        if (!writeChallenges(challenges))  {
            String s = "COULD NOT WRITE CHALLENGES TO DISK";
            addLog(s);
            controller.newError(s);
            return;
        } else {
            String s = f("Challenges succesfully written to disk; %s \n", fileName );
            addLog(s);
            controller.newMessage(s);
        }


        if (upload(facade, challenges)) {
            controller.setStatusBar("Challenge upload completed", false);
            addLog("Challenge upload completed");
        }
    }

    private boolean writeChallenges(Map<String, List<ChallengeDataDTO>> challenges) {

        fileName = f("challenges-%s.csv", slug(formatDateTime(new DateTime()).replace("/", "-").replace(":", "-")));

        try {
            BufferedWriter w = new BufferedWriter(new FileWriter(fileName));
            RecommenderSystemGui wi = controller.getWindow();
            wf(w, "%s\n", joinArray(wi.challengeColNames));
            w.flush();

            for (String playerId : challenges.keySet()) {

                List<ChallengeDataDTO> lcha = challenges.get(playerId);

                if (lcha == null || lcha.isEmpty())
                    continue;

                for (ChallengeDataDTO cha : lcha) {
                    wf(w, "%s\n", cha.getWriteData().toString().replace("[", "").replace("]", ""));
                    w.flush();
                }
            }

            w.close();

        }  catch (IOException e) {
            addLog("Error in writing challenges: %s", e.getMessage());
            return false;
        }

        return true;
    }

    private boolean upload(GamificationEngineRestFacade facade, Map<String, List<ChallengeDataDTO>> challenges) {

        // int tot = 0;

        addLog("Read challenges: %d", challenges.size());

        boolean success;

        for (String playerId : challenges.keySet()) {

            List<ChallengeDataDTO> lcha = challenges.get(playerId);

            if (lcha == null || lcha.isEmpty())
                continue;


            if (existsPlayerChallenge(gameId, playerId, date)) {
                addLog("ERROR: this user already has challenges this week");

                int result = controller.newDialog(f("User '%s' has already at least one challenge ending this week. Do you wish to add another?", playerId));

                if (result != 0)
                    continue;
            }

            for (ChallengeDataDTO cha : lcha) {

                // upload every challenge

                cha.setOrigin("rs");

                addLog("Inserting challenge: %s\n", cha.getInstanceName());
                success = facade.assignChallengeToPlayer(cha, gameId, playerId);

                if (!success) {
                    addLog("ERROR", cha.getInstanceName());
                    return false;
                }
            }
        }

        addLog("Challenges upload completed!");
        return true;
    }

    private boolean existsPlayerChallenge(String gameId, String playerId, DateTime date) {

        DateTime monday = jumpToMonday(date);

        // TODO controlla non abbia già challenges
        List<LinkedHashMap<String, Object>> currentChallenges = facade.getChallengesPlayer(gameId, playerId);
        for (LinkedHashMap<String, Object> cha: currentChallenges) {
            DateTime newMonday = new DateTime(cha.get("end"));

            newMonday = jumpToMonday(newMonday);
            int v = Math.abs(daysApart(monday, newMonday));
            if (v < 1) {
                pf("There is already a challenge ending this week! Name: %s\n", cha.get("name"));
                return true;
            }
        }

        return false;
    }


    private List<ChallengeDataInternalDto> prepareChallenges() {
        List<ChallengeDataInternalDto> challenges = new ArrayList<>();

        for (String user : controller.challanges.keySet())
            for (ChallengeDataDTO cha : controller.challanges.get(user)) {
                ChallengeDataInternalDto chaInt = new ChallengeDataInternalDto();
                chaInt.setPlayerId(user);
                chaInt.setGameId(gameId);
                chaInt.setDto(cha);

                challenges.add(chaInt);
            }
        return challenges;
    }

    private void addLog(String format, Object... args) {
        String msg = String.format(format, args);
        controller.addLog(msg);
        p(msg);
    }

    private List<ChallengeDataInternalDto> readChallenges() {
        // read output file
        ObjectMapper mapper = new ObjectMapper();
        TypeFactory typeFactory = mapper.getTypeFactory();
        String jsonString;
        List<ChallengeDataInternalDto> challenges;
        try {
            jsonString = IOUtils.toString(new FileInputStream(output));
            challenges = mapper.readValue(jsonString, typeFactory
                    .constructCollectionType(List.class,
                            ChallengeDataInternalDto.class));

        } catch (IOException e1) {
            addLog("Error in reading output file for uploader " + output);
            return null;
        }

        return challenges;
    }
}
