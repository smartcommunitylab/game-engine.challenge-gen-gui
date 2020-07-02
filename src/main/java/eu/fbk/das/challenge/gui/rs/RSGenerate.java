package eu.fbk.das.challenge.gui.rs;

import eu.fbk.das.GamificationEngineRestFacade;
import eu.fbk.das.api.exec.RecommenderSystemWeekly;
import eu.fbk.das.model.ChallengeExpandedDTO;
import eu.fbk.das.rs.challenges.evaluation.ChallengeDataGuru;
import it.smartcommunitylab.model.PlayerStateDTO;
import it.smartcommunitylab.model.ext.ChallengeConcept;
import it.smartcommunitylab.model.ext.GameConcept;
import org.apache.log4j.Logger;
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

    private static final Logger logger = Logger.getLogger(RSGenerate.class);
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
            PlayerStateDTO player = facade.getPlayerState(conf.get("gameId"), pId);

            // p(player.getState());

            // p(player.getState().getChallengeConcept());

            Set<GameConcept> scores =  player.getState().get("ChallengeConcept");
            if (scores == null) continue;
            for (GameConcept gc : scores) {
                ChallengeConcept cha = (ChallengeConcept) gc;

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
            List<ChallengeExpandedDTO> lcha = res.get(s);
            if (lcha == null || lcha.isEmpty())
                continue;

                for (ChallengeExpandedDTO cha : lcha) {
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

    private void recommendation() {

            dbg(logger, "Reading players from game");

            DateTime date = stringToDate(conf.get("date"));
            if (date == null) {
                err(logger, "Invalid date! %s", conf.get("date"));
            }

            monday = jumpToMonday(date);

            currentPlayer = 1;

            controller.resetChallenges();

            String playerIdsValue = conf.get("playerIds");
            Set<String> pIds = controller.playerIds;

            if (!("".equals(playerIdsValue))) {
            String[] spl = playerIdsValue.split("\\s+");
                pIds = new HashSet<>(Arrays.asList(spl));
            }

            controller.totPlayers = pIds.size();
            controller.rs.preprocess(pIds);

        RecommenderSystemWeekly rsw = new RecommenderSystemWeekly();

            // generate for all player ids
            for (String pId : pIds) {
                    if (!controller.playerIds.contains(pId))
                        throw new IllegalArgumentException(f("Given PlayerStateDTO id %s is nowhere to be found in the game", pId));

                List<ChallengeExpandedDTO> res = rsw.go(conf, pId);

                if (res != null && !res.isEmpty()) {
                    controller.addChallenges(pId, res);
                }

                controller.setStatusBar(false, "\rPlayerStateDTO considered: %d / %d", currentPlayer++, controller.totPlayers);

            }

        }

/*
    private void saveChallanges(Map<String, List<ChallengeExpandedDTO>> res) {

        ChallengesRulesGenerator crg;
        try {

            crg = new ChallengesRulesGenerator(new ChallengeInstanceFactory(), "generated-rules-report.csv", conf.get("OUTPUT"));

            // TO REMOVE

            crg.setChallenges(res, conf.get("gameId"));

            crg.writeChallengesToFile();

            // controller.rs.writeToFile(res);

        } catch (IOException e) {
            err(logger, "Error in saving challenges. Exception: %s", e.getMessage());
        }


    }*/

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

        Map<String, List<ChallengeExpandedDTO>> challenges = controller.challenges;

        try {
            BufferedWriter w = new BufferedWriter(new FileWriter(fileName));
            wf(w, ",%s\n", joinArray(ChallengeModel.challengeColNames));
            w.flush();

            for (String playerId : challenges.keySet()) {

                List<ChallengeExpandedDTO> lcha = challenges.get(playerId);

                if (lcha == null || lcha.isEmpty())
                    continue;

                for (ChallengeExpandedDTO cha : lcha) {
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
