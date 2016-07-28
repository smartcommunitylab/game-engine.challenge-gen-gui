package eu.fbk.das.challenge.gui;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import javax.swing.SwingUtilities;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.fbk.das.challenge.gui.util.PropertiesUtil;
import eu.trentorise.game.challenges.util.ChallengeRuleRow;
import eu.trentorise.game.challenges.util.ChallengeRules;
import eu.trentorise.game.challenges.util.ChallengeRulesLoader;

public class ChallengeGuiController {

	private static final Logger logger = LogManager
			.getLogger(ChallengeGuiController.class);

	private ChallengeGeneratorGui window;

	private String templateDir;
	private String output;

	public ChallengeGuiController() {
	}

	/**
	 * Reset current state, without save anything and refresh interface
	 */
	public void newSession() {
		ChallengeRules challenges = new ChallengeRules();
		challenges.getChallenges().add(new ChallengeRuleRow());
		window.setChallenges(challenges);
	}

	public void setWindow(ChallengeGeneratorGui window) {
		this.window = window;
	}

	public void openChallenges(File f) {
		// load basic properties from file
		loadPropertiesFromFile();
		// unlock check connection button
		window.enableCheckConnection(true);
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
			ChallengeRules challenges = new ChallengeRules();
			for (ChallengeRuleRow crr : temp.getChallenges()) {
				challenges.getChallenges().add(crr);
			}
			// set data and refresh window
			window.setChallenges(challenges);
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
		String gameId = PropertiesUtil.get(PropertiesUtil.GAMEID);
		window.setGameId(gameId);
		templateDir = PropertiesUtil.get(PropertiesUtil.TEMPLATE_DIR);
		output = "output.json";
	}

	public void checkConnection(String host, String user, char[] password) {
		String msg = "";
		String psw = String.valueOf(password);
		boolean result = false;
		if (!host.isEmpty() && user.isEmpty() && psw.isEmpty()) {
			result = checkConnection(host, user, psw, false);
		} else if (host != null && user != null && psw != null
				&& !host.isEmpty() && !user.isEmpty() && !psw.isEmpty()) {
			msg = "Trying to connect with host " + host + " with user " + user;
			logger.debug(msg);
			addLog(msg);
			result = checkConnection(host, user, psw, true);
		} else {
			result = false;
			msg = "Gamification engine connection parameters are invalid";
			logger.warn(msg);
			addLog(msg);
		}
		if (result) {
			msg = "Connection parameters to gamification engine are ok";
			window.setStatusBar(msg, false);
			logger.info(msg);
			addLog(msg);
			window.enableGenerate(true);
		} else {
			msg = "Error in connection parameters to gamification engine";
			window.setStatusBar(msg, true);
			logger.warn(msg);
			addLog(msg);
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

	public void generate() {
		logger.info("Challenge generation in progress");
		String host = window.getHost();
		String gameId = window.getGameId();
		String username = window.getUser();
		String password = window.getPassword();
		window.setStatusBar("Challenge generation in progress", false);
		SwingUtilities.invokeLater(new ChallengeGenerationRunnable(this, host,
				gameId, window.getChallenges(), templateDir, output, username,
				password));
	}

	public void setStatusBar(String text, boolean b) {
		window.setStatusBar(text, b);
	}

	public void addLog(String log) {
		window.addLog(log);
	}

	public void addChallenge(int index) {
		ChallengeRules challenges = window.getChallenges();
		ChallengeRuleRow row = new ChallengeRuleRow();
		challenges.getChallenges().add(index, row);
		window.setChallenges(challenges);
		window.setStatusBar("Added new challenge ", false);
	}

	public void removeChallenge(int index) {
		ChallengeRules challenges = window.getChallenges();
		challenges.getChallenges().remove(index);
		window.setChallenges(challenges);
		window.setStatusBar("Removed challenge ", false);
	}

	public void saveChallenges(File f, ChallengeRules ch) {
		try {
			ChallengeRulesLoader.write(f, ch);
			window.setStatusBar("File saved " + f.getAbsolutePath(), false);
		} catch (IllegalArgumentException | IOException e) {
			window.setStatusBar("Error in saving file " + f.getAbsolutePath(),
					true);
			logger.error(e);
		}
	}

	public void upload() {
		String host = window.getHost();
		String gameId = window.getGameId();
		String username = window.getUser();
		String password = window.getPassword();
		window.setStatusBar("Challenge upload in progress", false);
		SwingUtilities.invokeLater(new ChallengeUploadRunnable(this, host,
				gameId, username, password, "output.json"));
	}

	public void enableUpload(boolean b) {
		window.enableUpload(b);
	}

}
