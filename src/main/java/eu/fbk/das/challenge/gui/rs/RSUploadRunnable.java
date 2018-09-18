package eu.fbk.das.challenge.gui.rs;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * {@link Runnable} class for challenge upload into GamificationEngine
 */
public class RSUploadRunnable implements Runnable {

    private static final Logger logger = LogManager
            .getLogger(RSUploadRunnable.class);

    private RecommenderSystemController controller;
    private String host;
    private String gameId;
    private String username;
    private String password;
    private String output;

    public RSUploadRunnable(RecommenderSystemController controller,
                            String host, String gameId, String username, String password,
                            String output) {
        this.controller = controller;
        this.host = host;
        this.gameId = gameId;
        this.output = output;
        this.username = username;
        this.password = password;
    }

    @Override
    public void run() {

        // TODO
//        String log = ChallengeUploadTool.upload(host, gameId, output, username,
//                password);
//        controller.setStatusBar("Challenge upload completed", false);
//        controller.addLog(log);
//        controller.enableUpload(true);
//        logger.info("Challenge upload completed");
    }
}
