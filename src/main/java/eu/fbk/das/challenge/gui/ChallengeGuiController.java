package eu.fbk.das.challenge.gui;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.fbk.das.challenge.gui.util.PropertiesUtil;
import eu.trentorise.game.challenges.util.ChallengeRuleRow;
import eu.trentorise.game.challenges.util.ChallengeRules;
import eu.trentorise.game.challenges.util.ChallengeRulesLoader;

public class ChallengeGuiController {

	private static final Logger logger = LogManager
			.getLogger(ChallengeGuiController.class);

	private ChallengeRules challenges;
	private ChallengeGeneratorGui window;

	public ChallengeGuiController() {
		init();
	}

	private void init() {
		challenges = new ChallengeRules();
	}

	/**
	 * Reset current state, without save anything and refresh interface
	 */
	public void newSession() {
		// reset data
		init();
		// set data
		window.setChallenges(challenges);
		// refresh window
		window.refresh();
	}

	public void setWindow(ChallengeGeneratorGui window) {
		this.window = window;
	}

	public void openChallenges(File f) {
		// load basic properties from file
		loadPropertiesFromFile();
		// open challenges file
		ChallengeRules temp = null;
		try {
			temp = ChallengeRulesLoader.load(f.getAbsolutePath());
		} catch (NullPointerException | IllegalArgumentException | IOException e) {
			logger.error(
					"Error in opening challenge file: " + f.getAbsolutePath(),
					e);
		}
		if (temp != null) {
			logger.info("Challenges loaded from file " + f.getAbsolutePath());
			challenges.getChallenges().clear();
			List<ChallengeRuleRow> rows = new ArrayList<ChallengeRuleRow>();
			for (ChallengeRuleRow crr : temp.getChallenges()) {
				challenges.getChallenges().add(crr);
			}
			// set data and refresh window
			window.setChallenges(challenges);
			window.refresh();
		} else {
			logger.info("Challenges not loaded from file "
					+ f.getAbsolutePath());
		}

	}

	private void loadPropertiesFromFile() {
		String host = PropertiesUtil.get(PropertiesUtil.HOST);
		window.setHost(host);
		String user = PropertiesUtil.get(PropertiesUtil.USERNAME);
		window.setUser(user);
		String psw = PropertiesUtil.get(PropertiesUtil.PASSWORD);
		window.setPassword(psw);
	}

	public void checkConnection(String host, String user, char[] password) {
		System.out.println("check");
		String psw = String.valueOf(password);
		boolean result = false;
		if (!host.isEmpty() && user.isEmpty() && psw.isEmpty()) {
			result = checkConnection(host, user, psw, false);
		} else if (host != null && user != null && psw != null
				&& !host.isEmpty() && !user.isEmpty() && !psw.isEmpty()) {
			System.out.println("con auth");
			logger.debug("Trying to connect with host " + host + " with user "
					+ user);
			result = checkConnection(host, user, psw, true);
		} else {
			result = false;
			logger.warn("Gamification engine connection parameters are invalid");
		}
		if (result) {
			window.setStatusBar(
					"Connection parameters to gamification engine are ok",
					false);
		} else {
			window.setStatusBar(
					"Error in connection parameters to gamification engine",
					true);
		}

	}

	private boolean checkConnection(String host, String user, String psw,
			boolean auth) {
		try {
			URL url = new URL(host);
			URLConnection conn = url.openConnection();
			conn.connect();
			return true;
		} catch (MalformedURLException e) {
			return false;
		} catch (IOException e) {
			return false;
		}

	}
}
