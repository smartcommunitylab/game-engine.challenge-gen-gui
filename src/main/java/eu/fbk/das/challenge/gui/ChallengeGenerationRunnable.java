package eu.fbk.das.challenge.gui;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.base.Throwables;

import eu.trentorise.game.challenges.ChallengeGeneratorTool;
import eu.trentorise.game.challenges.util.ChallengeRules;

public class ChallengeGenerationRunnable implements Runnable {

	private static final Logger logger = LogManager
			.getLogger(ChallengeGenerationRunnable.class);

	private ChallengeGuiController controller;
	private String host;
	private String gameId;
	private String templateDir;
	private String output;
	private String username;
	private String password;
	private ChallengeRules challenges;

	public ChallengeGenerationRunnable(ChallengeGuiController controller,
			String host, String gameId, ChallengeRules challenges,
			String templateDir, String output, String username, String password) {
		this.controller = controller;
		this.host = host;
		this.gameId = gameId;
		this.challenges = challenges;
		this.templateDir = templateDir;
		this.output = output;
		this.username = username;
		this.password = password;
	}

	@Override
	public void run() {
		String log = "";
		try {
			log = ChallengeGeneratorTool.generate(host, gameId, challenges,
					templateDir, output, username, password);
			controller.setStatusBar("Challenge generation completed", false);
			controller.addLog(log);
			controller.enableUpload(true);
			logger.info("Challenge generation completed");
			controller.updateChart("generated-rules-report.csv");
		} catch (Exception e) {
			controller.setStatusBar("Challenge generation error", true);
			log = Throwables.getStackTraceAsString(e);
			controller.addLog(log);
			logger.error(e.getMessage(), e);
		}
	}
}
