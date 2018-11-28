package eu.fbk.das.challenge.gui.rs;

import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.util.Vector;

class ChallengeModel extends DefaultTableModel {

    public static  String[] challengeColNames = {"Player", "Level", "Id", "Experiment", "Model", "Counter", "Baseline",
            "Target", "Improvement", "Difficulty", "Bonus", "State", "Priority", "Start", "End"};

    public static Class[] challengeColTypes = { Integer.class, Integer.class, Integer.class, String.class, String.class,
            String.class, Double.class, Double.class, Double.class, Double.class, Integer.class, Integer.class, String.class, String.class, String.class};

    public int getColumnCount() {
        return challengeColNames.length;
    }

    public String getColumnName(int col) {
        return challengeColNames[col];
    }

    public Class getColumnClass(int col) {
        return challengeColTypes[col];
    }
}