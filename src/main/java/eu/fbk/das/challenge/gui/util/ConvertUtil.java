package eu.fbk.das.challenge.gui.util;

import eu.fbk.das.challenge.gui.gen.ChallengeReport;
import eu.trentorise.game.challenges.util.ChallengeRuleRow;
import eu.trentorise.game.challenges.util.ChallengeRules;

import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * Utility class for conversion between formats
 */
public class ConvertUtil {

    private ConvertUtil() {
    }

    public static Vector<Object> convertChallenge(ChallengeRuleRow crr) {
        Vector<Object> result = new Vector<Object>();
        result.add(crr.getName());
        result.add(crr.getModelName());
        result.add(crr.getGoalType());
        result.add(crr.getTarget());
        result.add(crr.getBonus());
        result.add(crr.getPointType());
        result.add(crr.getPeriodName());
        result.add(crr.getPeriodTarget());
        result.add("");
        result.add(crr.getBaselineVar());
        result.add(crr.getSelectionCriteriaPoints());
        result.add(crr.getSelectionCriteriaBadges());
        return result;
    }


    public static ChallengeRules convertTable(DefaultTableModel model)
            throws NumberFormatException {
        ChallengeRules result = new ChallengeRules();
        Vector<Vector> vec = model.getDataVector();
        for (Vector v : vec) {
            ChallengeRuleRow crr = new ChallengeRuleRow();
            if (v.get(0) != null) {
                crr.setName((String) v.get(0));
            } else {
                crr.setName("");
            }
            if (v.get(1) != null) {
                crr.setModelName((String) v.get(1));
            } else {
                crr.setModelName("");
            }
            if (v.get(2) != null) {
                crr.setGoalType((String) v.get(2));
            } else {
                crr.setGoalType("");
            }
            if (v.get(3) != null) {
                crr.setTarget(v.get(3));
            } else {
                crr.setTarget("");
            }
            if (v.get(4) != null) {
                if (v.get(4) instanceof Double) {
                    crr.setBonus((Double) v.get(4));
                } else {
                    crr.setBonus(Double.valueOf((String) v.get(4)));
                }
            } else {
                crr.setBonus(0.0);
            }

            if (v.get(5) != null) {
                crr.setPointType((String) v.get(5));
            } else {
                crr.setPointType("");
            }

            if (v.get(6) != null) {
                crr.setPeriodName((String) v.get(6));
            } else {
                crr.setPeriodName("");
            }

            if (v.get(7) != null) {
                crr.setPeriodTarget((String) v.get(7));
            } else {
                crr.setPeriodTarget("");
            }

            // v.get(8) is difficulty, for now blank
            if (v.get(9) != null) {
                crr.setBaselineVar((String) v.get(9));
            } else {
                crr.setBaselineVar("");
            }
            if (v.get(10) != null) {
                crr.setSelectionCriteriaPoints((String) v.get(10));
            } else {
                crr.setSelectionCriteriaPoints("");
            }
            if (v.get(11) != null) {
                crr.setSelectionCriteriaBadges((String) v.get(11));
            } else {
                crr.setSelectionCriteriaBadges("");
            }
            result.getChallenges().add(crr);
        }
        return result;
    }

    public static List<ChallengeReport> convertChallengeReport(List<String> lines) {
        List<ChallengeReport> reports = new ArrayList<ChallengeReport>();
        for (String line : lines) {
            ChallengeReport cr = new ChallengeReport();
            String[] elem = line.split(";");
            if (elem[0] != null) {
                cr.setPlayer(elem[0]);
            }
            if (elem[1] != null) {
                cr.setChallengeName(elem[1]);
            }
            if (elem[2] != null) {
                cr.setChallengeType(elem[2]);
            }
            if (elem[3] != null) {
                cr.setTransportMode(elem[3]);
            }
            if (elem[4] != null) {
                cr.setBaselineValue(elem[4]);
            }
            if (elem[5] != null) {
                cr.setTargetValue(elem[5]);
            }
            if (elem[6] != null) {
                cr.setPrize(elem[6]);
            }
            if (elem[7] != null) {
                cr.setPointType(elem[7]);
            }
            if (elem[8] != null) {
                cr.setChId(elem[8]);
            }
            reports.add(cr);
        }
        return reports;
    }
}
