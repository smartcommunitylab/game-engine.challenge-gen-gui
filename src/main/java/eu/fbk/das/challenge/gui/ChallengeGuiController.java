package eu.fbk.das.challenge.gui;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.fbk.das.challenge.gui.util.ConvertUtil;
import eu.fbk.das.challenge.gui.util.PropertiesUtil;
import eu.trentorise.game.challenges.util.ChallengeRuleRow;
import eu.trentorise.game.challenges.util.ChallengeRules;
import eu.trentorise.game.challenges.util.ChallengeRulesLoader;

public class ChallengeGuiController {

	private static final Logger logger = LogManager
			.getLogger(ChallengeGuiController.class);

	private ChallengeRules challenges;
	private ChallengeGeneratorGui window;

	private String input;
	private String templateDir;
	private String output;

	public ChallengeGuiController() {
		init();
	}

	private void init() {
		challenges = new ChallengeRules();
		challenges.getChallenges().add(new ChallengeRuleRow());
	}

	/**
	 * Reset current state, without save anything and refresh interface
	 */
	public void newSession() {
		// reset data
		init();
		// set data
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
			challenges.getChallenges().clear();
			List<ChallengeRuleRow> rows = new ArrayList<ChallengeRuleRow>();
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
			window.enableGenerate(true);
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

	public void generate() {
		String host = window.getHost();
		String gameId = window.getGameId();
		String username = window.getUser();
		String password = window.getPassword();
		window.setStatusBar("Challenge generation in progress", false);
		SwingUtilities.invokeLater(new ChallengeRunnable(this, host, gameId,
				challenges, templateDir, output, username, password));
	}

	public void setStatusBar(String text, boolean b) {
		window.setStatusBar(text, b);
	}

	public void addLog(String log) {
		window.addLog(log);
	}

	public void addChallenge(int index) {
		ChallengeRuleRow row = new ChallengeRuleRow();
		challenges.getChallenges().add(index, row);
		window.setChallenges(challenges);
	}

	public void removeChallenge(int index) {
		challenges.getChallenges().remove(index);
		window.setChallenges(challenges);
	}

	public void saveChallenges(File f, DefaultTableModel defaultTableModel) {
		try {
			ChallengeRulesLoader.write(f,
					ConvertUtil.convertTable(defaultTableModel));
		} catch (IllegalArgumentException | IOException e) {
			logger.error(e);
		}
	}
}
