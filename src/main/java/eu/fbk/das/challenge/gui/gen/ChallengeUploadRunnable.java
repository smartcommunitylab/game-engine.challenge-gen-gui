package eu.fbk.das.challenge.gui.gen;

import eu.fbk.das.old.UploaderTool;
import org.apache.log4j.Logger;

/**
 * {@link Runnable} class for challenge upload into GamificationEngine
 */
public class ChallengeUploadRunnable implements Runnable {

    final static Logger logger = Logger.getLogger(ChallengeUploadRunnable.class);

    private ChallengeGuiController controller;
    private String host;
    private String gameId;
    private String username;
    private String password;
    private String output;

    public ChallengeUploadRunnable(ChallengeGuiController controller,
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
        String log = UploaderTool.upload(host, gameId, output, username,
                password);
        controller.setStatusBar("Challenge upload completed", false);
        controller.addLog(log);
        controller.enableUpload(true);
        logger.info("Challenge upload completed");
    }
}
