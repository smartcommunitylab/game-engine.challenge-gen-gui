package eu.fbk.das.challenge.gui.rs;

import eu.fbk.das.GamificationEngineRestFacade;
import eu.fbk.das.challenge.gui.util.PropertiesUtil;
import eu.fbk.das.model.ChallengeExpandedDTO;
import eu.fbk.das.old.ChallengeRules;
import eu.fbk.das.old.ChallengeRulesLoader;
import eu.fbk.das.rs.challenges.generation.RecommendationSystem;
import eu.fbk.das.GamificationConfig;
import eu.fbk.das.rs.utils.ArrayUtils;
import it.smartcommunitylab.model.PlayerStateDTO;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.*;

import static eu.fbk.das.rs.challenges.evaluation.ChallengeAnalyzer.an;
import static eu.fbk.das.rs.utils.Utils.f;
import static eu.fbk.das.rs.utils.Utils.p;
import static javax.swing.JOptionPane.showMessageDialog;

/**
 * Controller class for RecommenderSystemGui
 */
class RecommenderSystemController {

    private static final Logger logger = Logger
            .getLogger(RecommenderSystemController.class);
    protected final RecommenderSystemAnalyzer rsa;
    public final HashMap<String, String> cfg;

    public int totPlayers;

    Map<String, List<ChallengeExpandedDTO>> challenges;

    private RecommenderSystemGui window;

    RecommendationSystem rs;

    private final static String OUTPUT = "challenges-rs.json";

    private GamificationEngineRestFacade facade;

    protected Set<String> playerIds;

    protected GamificationEngineRestFacade fac_copy;

    int[] order = new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8};

    RecommenderSystemController() {
        cfg = new GamificationConfig(true).extract();
        rs = new RecommendationSystem(cfg);
        rsa = new RecommenderSystemAnalyzer(rs);

        ArrayUtils.shuffleArray(order, new Random(System.currentTimeMillis()));
    }

    /**
     * Reset current state, without save anything and refresh interface
     */
    void newSession() {
        window.resetChallenges();
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

    /*
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

    } */

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

        // String startDate = window.getStartDate();
        // String endDate = window.getEndDate();
        // Boolean useRs = window.getUseRs();

        window.setStatusBar("Challenge generation in progress", false);

        // saveConf(host, gameId, username, password, startDate, endDate, useRs);


        new RSGenerate(this, prepareConf()).execute();
    }

    protected Map<String, String> prepareConf() {
        Map<String, String> conf = new HashMap<>();
        conf.put("host", window.getHost());
        conf.put("gameId", window.getGameId());
        conf.put("user", window.getUser());
        conf.put("pass", window.getPassword());
        conf.put("date", window.getTextDate());
        conf.put("playerIds", window.getPlayerIds());
        conf.put("OUTPUT", OUTPUT);
        return conf;
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


	/*
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
    } */

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

        RSUploader up = new RSUploader(this, prepareConf(), "output.json");
        up.execute();
    }

    Set<String> getPlayerList(String gameId) {
        // p(facade);
        return facade.getGamePlayers(gameId);
    }

    boolean checkFacade(String host, String user, char[] pass, String gameId) {
        try {
            setFacade(new GamificationEngineRestFacade(host, user, String.valueOf(pass)));

            playerIds = getPlayerList(gameId);
            if (playerIds == null) {
                return false;
            }

            String msg = "Connection parameters to gamification engine are ok";
            window.setStatusBar(msg, false);
            logger.info(msg);
            addLog(msg);

            // setFacadeCopy(new GamificationEngineRestFacade(host.replace("2/", "-copia/"), user, String.valueOf(pass)));

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

    private void setFacadeCopy(GamificationEngineRestFacade facade) {
        this.fac_copy = facade;
    }

    private void setFacade(GamificationEngineRestFacade facade) {
        this.facade = facade;
    }

    public int newDialog(String msg) {
        return JOptionPane.showConfirmDialog(window.app, msg,
                "alert", JOptionPane.OK_CANCEL_OPTION);
    }

    public void newError(String format, Object... args) {
        String msg = String.format(format, args);
        showMessageDialog(window.app, msg,
                "Failure", JOptionPane.ERROR_MESSAGE);
        setStatusBar(true, msg);
    }

    public RecommenderSystemGui getWindow() {
        return window;
    }

    public void newMessage(String msg) {
        showMessageDialog(window.app, msg,
                "Dialog", JOptionPane.INFORMATION_MESSAGE);
        addLog(msg);
    }

    public GamificationEngineRestFacade getFacade() {
        return facade;
    }

    void setStatusBar(boolean b, String format, Object... args) {
        String text = String.format(format, args);

        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> {
                window.setStatusBar(text, b);
                // addLog(text);
            });
        }

    }

    void addLog(String format, Object... args) {
        String log = String.format(format, args);
        window.addLog(log);
    }

    public void resetChallenges() {
        challenges = new HashMap<>();
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> {
                window.resetChallenges();
            });
        }
    }

    public void addChallenges(String pId, List<ChallengeExpandedDTO> res) {
        challenges.put(pId, res);
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> {
                window.addChallenges(res);
            });
        }

    }

    public PlayerStateDTO getPlayer(String pId) {
        return facade.getPlayerState(window.getGameId(), pId);
    }

    public String checkImprovement() {
         Map<String, List<Double>> incrByCounter = new HashMap<>();
         ArrayList<List<Double>> incrByLvl = new ArrayList<>();
         ArrayList<Double> incrTot = new ArrayList<>();

         for (String pId: challenges.keySet())
             for (ChallengeExpandedDTO ch: challenges.get(pId)) {
                 double impr = Double.valueOf((Double) ch.getInfo("improvement"));
                 String counter = (String) ch.getData("counterName");
                 int lvl = Integer.valueOf((Integer) ch.getInfo("playerLevel"));

                 incrTot.add(impr);

                 while (incrByLvl.size() <= lvl)
                     incrByLvl.add(new ArrayList<>());
                 incrByLvl.get(lvl).add(impr);

                 if (!incrByCounter.containsKey(counter))
                     incrByCounter.put(counter, new ArrayList<>());
                 incrByCounter.get(counter).add(impr);
         }

         StringBuilder s = new StringBuilder();

        sf(s, "\n\n# General improvement: %s", an(incrTot));

        sf(s,"\n\n");
        for (int i = 0; i < incrByLvl.size(); i++) {
            sf(s,"# Level %d: %s\n", i, an(incrByLvl.get(i)));
        }

        sf(s,"\n\n");
        for (String c: incrByCounter.keySet()) {
            sf(s,"# Counter %s: %s\n", c, an(incrByCounter.get(c)));
        }

        return s.toString();
    }

    private void sf(StringBuilder s, String format, Object... args) {
        s.append( String.format(format, args));
    }
}

