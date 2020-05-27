package eu.fbk.das.challenge.gui.rs;

import eu.fbk.das.rs.challenges.evaluation.ChallengeDataGuru;
import eu.trentorise.game.challenges.ChallengeInstanceFactory;
import eu.trentorise.game.challenges.ChallengesRulesGenerator;
import eu.trentorise.game.challenges.model.ChallengeDataDTO;
import eu.trentorise.game.challenges.rest.ChallengeConcept;
import eu.trentorise.game.challenges.rest.Player;
import eu.fbk.das.GamificationEngineRestFacade;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import javax.swing.*;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import static eu.fbk.das.rs.utils.Utils.*;

/**
 * {@link Runnable} class for running challenge generation
 */
public class RSGenerate  extends SwingWorker<String, Object> {

    private final ChallengeDataGuru cdg;

    private String fileName;

    private static final Logger logger = LogManager.getLogger(RSGenerate.class);
    private final GamificationEngineRestFacade facade;

    private RecommenderSystemController controller;
    private Map<String, String> conf;
    private String log = "";

    private int currentPlayer;
    private DateTime monday;

    RSGenerate(RecommenderSystemController controller, Map<String, String> conf) {
        this.controller = controller;
        this.conf = conf;

        this.facade = controller.getFacade();

        cdg = new ChallengeDataGuru(controller.rs);
    }

    @Override
    public String doInBackground() {

        try {

            if (!generate())
                return "";

            if (!writeCompleted())
                return "";

            if (!writeChallengesSimple())
                return "";

            if (!writeChallengesComplete())
                return "";

            String s = f("Challenges succesfully written to disk; %s \n", fileName );
            controller.newMessage(s);


        } catch (Exception e) {
e.printStackTrace();
            p(e.getMessage());
            controller.newError("Error during challenge generation: %s", e.getMessage());
        }

        return "done";
    }

    private boolean writeCompleted() throws IOException {
        fileName = f("completed.csv");
        BufferedWriter w = new BufferedWriter(new FileWriter(fileName));

        Map<String, Integer> completed =  new HashMap<>();

        for (String pId : controller.playerIds) {
            Player player = facade.getPlayerState(conf.get("GAME_ID"), pId);

            // p(player.getState());

            // p(player.getState().getChallengeConcept());

            if (player.getState().getChallengeConcept() == null)
                continue;

            for (ChallengeConcept cha: player.getState().getChallengeConcept()) {
                String s = cha.getName();
                if (s.contains("survey") || s.contains("initial"))
                    continue;

                completed.put(cha.getName(), cha.isCompleted() ? 1 : 0);

            }
        }

        SortedSet<Map.Entry<String, Integer>> ordered = entriesSortedByKey(completed);

        for (Map.Entry<String, Integer> e: ordered) {
                wf(w, "%s,%d\n", e.getKey(), e.getValue());
                w.flush();
        }
        w.close();

        return true;
    }


    private boolean generate() {
        recommendation();

                /*
        for (String s: res.keySet()) {
            List<ChallengeDataDTO> lcha = res.get(s);
            if (lcha == null || lcha.isEmpty())
                continue;

                for (ChallengeDataDTO cha : lcha) {
                    cha.setStart(Utils.parseDateTime("26/10/2018 00:00"));
                    cha.setEnd(Utils.parseDateTime("28/10/2018 23:59"));
                }
        }*/

        //  saveChallanges(res);

        controller.setStatusBar(false, "Challenge generation completed");
        controller.addLog(log);
        if (log.contains("Error") || log.contains("exception")) {
            controller.newError("Error during challenge generation, please see the log");
            return false;
        }

            logger.info("Challenge generation completed");
        return true;

    }

    private Map<String, List<ChallengeDataDTO>> recommendation() {

            dbg(logger, "Reading players from game");

            DateTime date = stringToDate(conf.get("DATE"));
            if (date == null) {
                err(logger, "Invalid date! %s", conf.get("DATE"));
            }

            monday = jumpToMonday(date);

            Map<String, List<ChallengeDataDTO>> challenges = new HashMap<>();

            currentPlayer = 1;

            controller.resetChallenges();

            String playerIds = conf.get("PLAYER_IDS");
            if ("".equals(playerIds)) {
                controller.totPlayers = controller.playerIds.size();
                preprocessChallenges(controller.playerIds);
                // generate for all player ids!
                for (String pId : controller.playerIds) {
                    addChallenge(date, pId);
                }
            } else {
                // check if given ids exists
                String[] splited = playerIds.split("\\s+");
                controller.totPlayers = splited.length;
                for (String pId : splited)
                    if (!controller.playerIds.contains(pId))
                        throw new IllegalArgumentException(f("Given player id %s is nowhere to be found in the game", pId));

                for (String pId : splited) {
                    addChallenge(date, pId);
                }
            }

            return challenges;
        }

    private void preprocessChallenges(Set<String> playerIds) {
        controller.rs.preprocess(playerIds);
    }

    private void addChallenge(DateTime date, String pId) {

        List<ChallengeDataDTO> res = controller.rs.recommend(pId, null, null);
        if (res != null && !res.isEmpty()) {
            controller.addChallenges(pId, res);
        }

        controller.setStatusBar(false, "\rPlayer considered: %d / %d", currentPlayer++, controller.totPlayers);
    }


    private void saveChallanges(Map<String, List<ChallengeDataDTO>> res) {

        ChallengesRulesGenerator crg;
        try {

            crg = new ChallengesRulesGenerator(new ChallengeInstanceFactory(), "generated-rules-report.csv", conf.get("OUTPUT"));

            // TO REMOVE

            crg.setChallenges(res, conf.get("GAME_ID"));

            crg.writeChallengesToFile();

            // controller.rs.writeToFile(res);

        } catch (IOException e) {
            err(logger, "Error in saving challenges. Exception: %s", e.getMessage());
        }


    }

    private boolean writeChallengesComplete() {
        fileName = f("challenges-%s-complete", formatDateTimeFileName(new DateTime()));

        try {
            cdg.generate(fileName, controller.challenges, monday, ChallengeModel.challengeColNames);
        } catch (IOException e) {
            controller.newError(f("COULD NOT WRITE GURU CHALLENGES TO DISK: %s", e.getMessage()));
            return false;
        }

        return true;
    }


    private boolean writeChallengesSimple() {

        fileName = f("challenges-%s.csv", formatDateTimeFileName(new DateTime()));

        Map<String, List<ChallengeDataDTO>> challenges = controller.challenges;

        try {
            BufferedWriter w = new BufferedWriter(new FileWriter(fileName));
            wf(w, ",%s\n", joinArray(ChallengeModel.challengeColNames));
            w.flush();

            for (String playerId : challenges.keySet()) {

                List<ChallengeDataDTO> lcha = challenges.get(playerId);

                if (lcha == null || lcha.isEmpty())
                    continue;

                for (ChallengeDataDTO cha : lcha) {
                    wf(w, "%s\n", cha.printData());
                    w.flush();
                }
            }

            w.close();

        }  catch (IOException e) {
            controller.newError(f("COULD NOT WRITE CHALLENGES TO DISK: %s", e.getMessage()));
            return false;
        }

        return true;
    }

}
