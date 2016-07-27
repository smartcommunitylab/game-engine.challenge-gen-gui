package eu.fbk.das.challenge.gui;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.trentorise.game.challenges.UploaderTool;

public class ChallengeUploadRunnable implements Runnable {

	private static final Logger logger = LogManager
			.getLogger(ChallengeUploadRunnable.class);

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
		logger.info("Challenge generation completed");
	}
}
