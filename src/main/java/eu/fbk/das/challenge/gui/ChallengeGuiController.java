package eu.fbk.das.challenge.gui;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.swing.SwingUtilities;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jfree.data.general.DefaultPieDataset;

import eu.fbk.das.challenge.gui.util.ConvertUtil;
import eu.fbk.das.challenge.gui.util.PropertiesUtil;
import eu.trentorise.game.challenges.util.ChallengeRuleRow;
import eu.trentorise.game.challenges.util.ChallengeRules;
import eu.trentorise.game.challenges.util.ChallengeRulesLoader;

public class ChallengeGuiController {

	private static final Logger logger = LogManager
			.getLogger(ChallengeGuiController.class);

	private ChallengeGeneratorGui window;

	private final static String OUTPUT = "output.json";

	private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

	public ChallengeGuiController() {
	}

	/**
	 * Reset current state, without save anything and refresh interface
	 */
	public void newSession() {
		ChallengeRules challenges = new ChallengeRules();
		challenges.getChallenges().add(new ChallengeRuleRow());
		window.setChallenges(challenges);
		window.resetAnalytics();
		window.enableCheckConnection(false);
		window.enableGenerate(false);
		window.enableUpload(false);
		window.enableUseRecommendationsystem(false);
		window.setStatusBar(
				"To start, create a new challenge definition using the table or open a new challenge definition file using File -> Open",
				false);
	}

	public void setWindow(ChallengeGeneratorGui window) {
		this.window = window;
	}

	public void openChallenges(File f) {
		// load basic properties from file
		loadPropertiesFromFile();
		// set default window status
		window.enableCheckConnection(true);
		window.enableUpload(false);
		window.enableGenerate(false);
		window.resetAnalytics();
		// set default start date as today and end to one week
		Calendar c = Calendar.getInstance();
		c.set(Calendar.DATE, c.get(Calendar.DATE) + 1);
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		Date now = c.getTime();
		window.setStartDate(now);
		c.add(Calendar.DAY_OF_MONTH, 7);
		window.setEndDate(c.getTime());
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
	}

	public void checkConnection(String host, String user, char[] password,
			String gameId) {
		String msg = "";
		String psw = String.valueOf(password);
		boolean result = false;
		if (!host.isEmpty() && user.isEmpty() && psw.isEmpty()) {
			result = checkConnection(host, user, psw, false, gameId);
		} else if (host != null && user != null && psw != null
				&& !host.isEmpty() && !user.isEmpty() && !psw.isEmpty()) {
			msg = "Trying to connect with host " + host + " with user " + user;
			logger.debug(msg);
			addLog(msg);
			result = checkConnection(host, user, psw, true, gameId);
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
			boolean auth, String gameId) {
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
		String startDate = window.getStartDate();
		String endDate = window.getEndDate();
		String filterIds = PropertiesUtil.get(PropertiesUtil.FILTERING);
		Boolean useRs = window.getUseRs();
		Boolean useFiltering = Boolean.valueOf(PropertiesUtil
				.get(PropertiesUtil.FILTERING_ENABLED));
		window.setStatusBar("Challenge generation in progress", false);
		try {
			SwingUtilities.invokeLater(new ChallengeGenerationRunnable(this,
					host, gameId, window.getChallenges(), username, password,
					OUTPUT, sdf.parse(startDate), sdf.parse(endDate),
					filterIds, useRs, useFiltering));
		} catch (ParseException e) {
			String msg = "Error in parsing start or end date";
			logger.error(msg);
			window.addLog(msg);
			window.setStatusBar(msg, true);
		}
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

	public void updateChart(String file) {
		try {
			// read generate challenges data from file, remove first ( labels )
			List<String> lines = IOUtils.readLines(new FileInputStream(file));
			lines.remove(0);
			// convert and group by challenge type
			List<ChallengeReport> report = ConvertUtil
					.convertChallengeReport(lines);
			Map<String, List<ChallengeReport>> map = report.stream().collect(
					Collectors.groupingBy(ChallengeReport::getChallengeName));
			// update chart data
			DefaultPieDataset pieDataSet = new DefaultPieDataset();
			for (String key : map.keySet()) {
				pieDataSet.setValue(key, map.get(key).size());
			}
			// update challenge for player count

			Map<String, List<ChallengeReport>> mapPlayer = report.stream()
					.collect(Collectors.groupingBy(ChallengeReport::getPlayer));
			List<Integer> values = new ArrayList<Integer>();
			for (String key : mapPlayer.keySet()) {
				if (!values.contains(mapPlayer.get(key).size())) {
					values.add(mapPlayer.get(key).size());
				}
			}
			// update chart area
			window.updateChart(pieDataSet, values, mapPlayer.size());
		} catch (IOException e) {
			logger.error(e);
		}
	}

	public List<ChallengeRuleRow> getChallenges() {
		return window.getChallenges().getChallenges();
	}

}
