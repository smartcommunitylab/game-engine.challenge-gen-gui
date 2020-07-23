package eu.fbk.das.challenge.gui.old;

import com.google.common.base.Throwables;
import eu.fbk.das.old.ChallengeGeneratorTool;
import eu.fbk.das.old.ChallengeRules;
import org.apache.log4j.Logger;

import java.util.Date;

/**
 * {@link Runnable} class for running challenge generation
 */
public class ChallengeGenerationRunnable implements Runnable {

    private static final Logger logger = Logger
            .getLogger(ChallengeGenerationRunnable.class);

    private ChallengeGuiController controller;
    private String host;
    private String gameId;
    private String output;
    private String username;
    private String password;
    private ChallengeRules challenges;
    private Date startDate;
    private Date endDate;
    private String filterIds;
    private Boolean useFiltering;

    public ChallengeGenerationRunnable(ChallengeGuiController controller,
                                       String host, String gameId, ChallengeRules challenges,
                                       String username, String password, String output, Date startDate,
                                       Date endDate, String filterIds,
                                       Boolean useFiltering) {
        this.controller = controller;
        this.host = host;
        this.gameId = gameId;
        this.challenges = challenges;
        this.output = output;
        this.username = username;
        this.password = password;
        this.startDate = startDate;
        this.endDate = endDate;
        this.filterIds = filterIds;
        this.useFiltering = useFiltering;
    }

    @Override
    public void run() {
        String log = "";
        try {
            log = ChallengeGeneratorTool.generate(host, gameId, challenges,
                    username, password, output, startDate, endDate, filterIds, useFiltering);
            controller.setStatusBar("Challenge generation completed", false);
            controller.addLog(log);
            if (!log.contains("Error") && !log.contains("exception")) {
                controller.enableUpload(true);
                controller.updateChart("generated-rules-report.csv");
                logger.info("Challenge generation completed");
            } else {
                controller.enableUpload(false);
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
}
