package eu.fbk.das.challenge.gui.rs;

import eu.fbk.das.challenge.gui.util.PropertiesUtil;
import eu.fbk.das.rs.challengeGeneration.RecommendationSystem;
import eu.trentorise.game.challenges.model.ChallengeDataDTO;
import eu.trentorise.game.challenges.rest.GamificationEngineRestFacade;
import eu.trentorise.game.challenges.util.ChallengeRuleRow;
import eu.trentorise.game.challenges.util.ChallengeRules;
import eu.trentorise.game.challenges.util.ChallengeRulesLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.*;

import static eu.fbk.das.rs.Utils.f;
import static eu.fbk.das.rs.Utils.p;

/**
 * Controller class for RecommenderSystemGui
 */
class RecommenderSystemController {

    private static final Logger logger = LogManager
            .getLogger(RecommenderSystemController.class);

    Map<String, List<ChallengeDataDTO>> challanges;

    private RecommenderSystemGui window;

    RecommendationSystem rs;

    private final static String OUTPUT = "challenges-rs.json";

    DateTimeFormatter sdf = DateTimeFormat.forPattern("dd/MM/yyyy");

    DateTimeFormatter df = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm");

    RecommenderSystemController() {
        rs = new RecommendationSystem();
    }

    /**
     * Reset current state, without save anything and refresh interface
     */
    void newSession() {
        ChallengeRules challenges = new ChallengeRules();
        challenges.getChallenges().add(new ChallengeRuleRow());
        window.setChallenges(challenges);
//        window.resetAnalytics();
        window.enableCheckConnection(false);
        window.enableGenerate(false);
        window.enableUpload(false);
        window.setStatusBar(
                "To start, create a new challenge definition using the table or open a new challenge definition file using File -> Open",
                false);
    }

    void setWindow(RecommenderSystemGui window) {
        this.window = window;
    }

    void openChallenges(File f) {
        // load basic properties from file
        loadPropertiesFromFile();
        // set default window status
        window.enableCheckConnection(true);
        window.enableUpload(false);
        window.enableGenerate(false);
        // window.resetAnalytics();
        // set default start date as today and end to one week
        Calendar c = Calendar.getInstance();
        c.set(Calendar.DATE, c.get(Calendar.DATE) + 1);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        // Date now = c.getTime();
        // window.setStartDate(now);
        c.add(Calendar.DAY_OF_MONTH, 7);
        // window.setEndDate(c.getTime());
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

    /*
    boolean checkHost(String host, String user, char[] password) {
        String msg;
        String psw = String.valueOf(password);
        boolean result = false;
        if (!host.isEmpty() && user.isEmpty() && psw.isEmpty()) {
            result = checkHost(host);
        } else if (user != null && !host.isEmpty() && !user.isEmpty() && !psw.isEmpty()) {
            msg = "Trying to connect with host " + host + " with user " + user;
            logger.debug(msg);
            addLog(msg);
            result = checkHost(host);
        } else {
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
            rs.setFacade(new GamificationEngineRestFacade(host, user, psw));
        } else {
            msg = "Error in connection parameters to gamification engine";
            window.setStatusBar(msg, true);
            logger.warn(msg);
            addLog(msg);
        }

        return result;
    }

    private boolean checkHost(String host) {
        try {
            URL url = new URL(host);
            URLConnection conn = url.openConnection();
            conn.connect();
            return true;
        } catch (IOException e) {
            return false;
        }

    }*/

    void generate() {
        logger.info("Challenge generation in progress");
        Map<String, String> conf = new HashMap<>();
        conf.put("HOST", window.getHost());
        conf.put("GAME_ID", window.getGameId());
        conf.put("USERNAME", window.getUser());
        conf.put("PASSWORD", window.getPassword());
        conf.put("DATE", window.getTextDate());
        conf.put("PLAYER_IDS", window.getPlayerIds());

        // String startDate = window.getStartDate();
        // String endDate = window.getEndDate();
        // Boolean useRs = window.getUseRs();

        window.setStatusBar("Challenge generation in progress", false);

        // saveConf(host, gameId, username, password, startDate, endDate, useRs);

        conf.put("OUTPUT", OUTPUT);

        SwingUtilities.invokeLater(new RSGenerate(this, conf));
    }

	/*
	private void saveConf(String host, String gameId, String username, String password, String startDate, String endDate, Boolean useRs) {

            try {
                BufferedWriter wr = new BufferedWriter(new FileWriter("conf"));
                wr.write(host + "\n");
                wr.write(gameId + "\n");
                wr.write(username + "\n");
                wr.write(password + "\n");
                wr.write(startDate + "\n");
                wr.write(endDate + "\n");
                wr.write(useRs + "\n");
                wr.close();
            } catch (IOException e) {
                addLog("Not possible to write configuration");
            }
        } */


    void setStatusBar(String text, boolean b) {
        window.setStatusBar(text, b);
    }

    void addLog(String log) {
        window.addLog(log);
    }

    void addChallenge(int index) {
        ChallengeRules challenges = window.getChallenges();
        ChallengeRuleRow row = new ChallengeRuleRow();
        challenges.getChallenges().add(index, row);
        window.setChallenges(challenges);
        window.setStatusBar("Added new challenge ", false);
    }

    void removeChallenge(int index) {
        ChallengeRules challenges = window.getChallenges();
        challenges.getChallenges().remove(index);
        window.setChallenges(challenges);
        window.setStatusBar("Removed challenge ", false);
    }

    void saveChallenges(File f, ChallengeRules ch) {
        try {
            ChallengeRulesLoader.write(f, ch);
            window.setStatusBar("File saved " + f.getAbsolutePath(), false);
        } catch (IllegalArgumentException | IOException e) {
            window.setStatusBar("Error in saving file " + f.getAbsolutePath(),
                    true);
            logger.error(e);
        }
    }

    void upload() {
        window.setStatusBar("Challenge upload in progress", false);
        SwingUtilities.invokeLater(new RSUploader(this,
                window.getGameId(), window.getDate(), "output.json"));
    }


    List<ChallengeRuleRow> getChallenges() {
        return window.getChallenges().getChallenges();
    }


    void updateList(Map<String, List<ChallengeDataDTO>> res) {
        window.setChallenges(res);
    }

    Set<String> getPlayerList() {
        GamificationEngineRestFacade facade = rs.getFacade();
        // p(facade);
        return facade.getGamePlayers(window.getGameId());
    }

    boolean checkFacade(String host, String user, char[] pass, String gameId) {
        try {
            rs.setFacade(new GamificationEngineRestFacade(host, user, String.valueOf(pass)));

            // TODO check gameId

            String msg = "Connection parameters to gamification engine are ok";
            window.setStatusBar(msg, false);
            logger.info(msg);
            addLog(msg);

            return true;
        } catch (Exception ex) {
            p(ex);

            String msg = f("Error in connection parameters to gamification engine: %s", ex.getMessage());
            window.setStatusBar(msg, true);
            logger.warn(msg);
            addLog(msg);
        }

        return false;
    }

    DateTimeFormatter getDateFormat() {
        return df;
    }

    DateTimeFormatter getSimpledate() {
        return sdf;
    }

    public int newDialog(String msg) {
        return JOptionPane.showConfirmDialog(window.app, msg,
                "alert", JOptionPane.OK_CANCEL_OPTION);
    }

    public void newError(String msg) {
        JOptionPane.showMessageDialog(window.app, msg,
                "Failure", JOptionPane.ERROR_MESSAGE);
    }

    public RecommenderSystemGui getWindow() {
        return window;
    }

    public void newMessage(String msg) {
        JOptionPane.showMessageDialog(window.app, msg,
                "Dialog", JOptionPane.INFORMATION_MESSAGE);
    }
}
