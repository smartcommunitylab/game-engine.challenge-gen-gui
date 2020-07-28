package eu.fbk.das.challenge.gui.rs;

import eu.fbk.das.GamificationEngineRestFacade;
import eu.fbk.das.api.exec.RecommenderSystemWeekly;
import eu.fbk.das.model.ChallengeExpandedDTO;
import it.smartcommunitylab.model.ChallengeConcept;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static eu.fbk.das.utils.Utils.*;


/**
 * {@link Runnable} class for challenge upload into GamificationEngine
 */
public class RSUploader extends SwingWorker<String, Object> {

    private static final Logger logger = Logger
            .getLogger(RSUploader.class);

    private final Map<String, String> conf;

    private RecommenderSystemController controller;
    private String output;
    private GamificationEngineRestFacade facade;

    Map<String, List<ChallengeExpandedDTO>> challenges;

    private String gameId;

    private DateTime monday;

    RSUploader(RecommenderSystemController controller,  Map<String, String> conf, String output) {
        this.controller = controller;
        this.conf = conf;
        this.output = output;

        this.facade  = controller.getFacade();
    }


    @Override
    public String doInBackground() {

        p("Start upload");

        challenges = controller.challenges;

        gameId = conf.get("GAMEID");
        DateTime date = stringToDate(conf.get("date"));
        if (date == null) {
            err(logger, "Invalid date! %s", conf.get("date"));
        }

        monday = jumpToMonday(date);

        if (!check())
            return "";

        if (!upload())
            return "";

            controller.setStatusBar(true,"Challenge upload completed");

            return "done";
    }



    private boolean check() {

        if (facade == null) {
            controller.addLog("ERROR! facade cannot be null");
            return false;
        }

        if (output == null) {
            controller.addLog("output file cannot be null");
            return false;
        }

        if (controller.challenges == null) {
            controller.addLog("ERROR! challenges cannot be null");
            return false;
        }

        return true;
    }

    private boolean upload() {

        // int tot = 0;

        // controller.addLog("Read challenges: %d", controller.challenges.size());

        boolean success;

        RecommenderSystemWeekly rsw = new RecommenderSystemWeekly();

        for (String playerId : challenges.keySet()) {

            List<ChallengeExpandedDTO> lcha = challenges.get(playerId);

            if (lcha == null || lcha.isEmpty())
                continue;

            String already = existsPlayerChallenge(gameId, playerId, lcha.get(0));
            if (!"".equals(already)) {
                controller.addLog("ERROR: this user already has challenges this week: %s", already);

                int result = controller.newDialog(f("User '%s' has already the following challenges in this week. Do you wish to add another? \n \n %s",  playerId, already));

                if (result != 0)
                    continue;
            }

            int tot = lcha.size();
            int ix = 0;

            for (ChallengeExpandedDTO cha : lcha) {

                // upload every challenge

                cha.setOrigin("rs");

                controller.setStatusBar(false, "Inserting challenge: %d / %d - %s\n", ++ix, tot, cha.getInstanceName());

                success = rsw.upload(conf, cha);

                if (!success) {
                    controller.addLog("ERROR", cha.getInstanceName());
                    return false;
                }
            }
        }

        controller.addLog("Challenges upload completed!");
        return true;
    }

    private String  existsPlayerChallenge(String gameId, String playerId, ChallengeExpandedDTO old) {

        DateTime currentChaEnd = jumpToMonday(new DateTime(old.getEnd()));

        List<String> already = new ArrayList<>();
        List<ChallengeConcept> currentChallenges = facade.getChallengesPlayer(gameId, playerId);
        for (ChallengeConcept cha: currentChallenges) {
            DateTime existingChaEnd = jumpToMonday(new DateTime(cha.getEnd()));

            String s = (String) cha.getName();
            if (s.contains("survey") || s.contains("initial") || s.contains("bonus") || s.contains("group") || s.contains("recommend"))
                continue;

            int v = Math.abs(daysApart(currentChaEnd, existingChaEnd));
            if (v < 1) {
                already.add(s);
            }
        }

        return String.join("\n", already);
    }

    /*
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
            controller.addLog("Error in reading output file for uploader " + output);
            return null;
        }

        return challenges;
    } */
}
