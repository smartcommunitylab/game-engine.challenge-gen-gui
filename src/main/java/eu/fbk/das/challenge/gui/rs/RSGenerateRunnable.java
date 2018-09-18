package eu.fbk.das.challenge.gui.rs;

import com.google.common.base.Throwables;
import eu.fbk.das.rs.challengeGeneration.RecommendationSystem;
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
public class RSGenerateRunnable implements Runnable {

    private static final Logger logger = LogManager.getLogger(RSGenerateRunnable.class);

    private RecommenderSystemController controller;
    private Map<String, String> conf;

    public RSGenerateRunnable(RecommenderSystemController controller, Map<String, String> conf) {
        this.controller = controller;
        this.conf = conf;
    }

    @Override
    public void run() {
        String log = "";
        try {

            Map<String, List<ChallengeDataDTO>> res = generate(conf, controller.rs);

            controller.setStatusBar("Challenge generation completed", false);
            controller.addLog(log);
            if (!log.contains("Error") && !log.contains("exception")) {

                // TODO
//                controller.enableUpload(true);
//                controller.updateChart("generated-rules-report.csv");
//                controller.updateList(res);
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

    public Map<String, List<ChallengeDataDTO>> generate(Map<String, String> conf, RecommendationSystem rs) {
        Map<String, List<ChallengeDataDTO>> res = rs.recommendation(conf);

        ChallengesRulesGenerator crg;
        try {
            crg = new ChallengesRulesGenerator(new ChallengeInstanceFactory(), "generated-rules-report.csv", "challenge.json");

            crg.setChallenges(res, conf.get("GAME_ID"));

            rs.writeToFile(res);

        } catch (IOException e) {
            err (logger, "Error in saving challenges. Exception: %s", e.getMessage());
            return null;
        }

        return res;
    }

}
