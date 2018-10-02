package eu.fbk.das.challenge.gui.rs;

import com.google.common.base.Throwables;
import eu.trentorise.game.challenges.ChallengeInstanceFactory;
import eu.trentorise.game.challenges.ChallengesRulesGenerator;
import eu.trentorise.game.challenges.model.ChallengeDataDTO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static eu.fbk.das.rs.Utils.err;

/**
 * {@link Runnable} class for running challenge generation
 */
public class RSGenerate implements Runnable {

    private static final Logger logger = LogManager.getLogger(RSGenerate.class);

    private RecommenderSystemController controller;
    private Map<String, String> conf;
    private String log = "";

    RSGenerate(RecommenderSystemController controller, Map<String, String> conf) {
        this.controller = controller;
        this.conf = conf;
    }

    @Override
    public void run() {

        try {

            Map<String, List<ChallengeDataDTO>> res = controller.rs.recommendation(conf);

            /*
            for (String s: res.keySet()) {
                for (ChallengeDataDTO cha : res.get(s)) {
                    cha.setStart(controller.getDateFormat().parseDateTime("28/09/2018 00:00"));
                    cha.setEnd(controller.getDateFormat().parseDateTime("29/09/2018 23:59"));
                }
            } */


            //  saveChallanges(res);

            controller.challanges = res;

            controller.setStatusBar("Challenge generation completed", false);
            controller.addLog(log);
            if (!log.contains("Error") && !log.contains("exception")) {

                // TODO remove
//                 controller.enableUpload(true);
//                controller.updateChart("generated-rules-report.csv");
               controller.updateList(res);
                logger.info("Challenge generation completed");
            } else {

                controller
                        .setStatusBar(
                                "Error during challenge generation, please see the log",
                                true);
                logger.error("Error during challenge generation");
            }

        } catch (Exception e) {
            controller.setStatusBar("Challenge generation error", true);
            log = Throwables.getStackTraceAsString(e);
            controller.addLog(log);
            logger.error(e.getMessage(), e);
        }
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

}
