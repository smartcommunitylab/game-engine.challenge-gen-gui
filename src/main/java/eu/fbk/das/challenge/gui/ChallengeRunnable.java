package eu.fbk.das.challenge.gui;

import eu.trentorise.game.challenges.ChallengeGeneratorTool;
import eu.trentorise.game.challenges.util.ChallengeRules;

public class ChallengeRunnable implements Runnable {

	private ChallengeGuiController controller;
	private String host;
	private String gameId;
	private String templateDir;
	private String output;
	private String username;
	private String password;
	private ChallengeRules challenges;

	public ChallengeRunnable(ChallengeGuiController controller, String host,
			String gameId, ChallengeRules challenges, String templateDir,
			String output, String username, String password) {
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
		String log = ChallengeGeneratorTool.generate(host, gameId, challenges,
				templateDir, output, username, password);
		controller.setStatusBar("Challenge generation completed", false);
		controller.addLog(log);
	}
}
