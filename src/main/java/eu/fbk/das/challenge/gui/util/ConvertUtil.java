package eu.fbk.das.challenge.gui.util;

import java.util.Vector;

import javax.swing.table.DefaultTableModel;

import eu.trentorise.game.challenges.util.ChallengeRuleRow;
import eu.trentorise.game.challenges.util.ChallengeRules;

public class ConvertUtil {

	private ConvertUtil() {
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Vector<Object> convertChallenge(ChallengeRuleRow crr) {
		Vector<Object> result = new Vector<Object>();
		result.add(crr.getName());
		result.add(crr.getType());
		result.add(crr.getGoalType());
		result.add(crr.getTarget());
		result.add(crr.getBonus());
		result.add(crr.getPointType());
		result.add("");
		result.add(crr.getBaselineVar());
		result.add(crr.getSelectionCriteriaCustomData());
		result.add(crr.getSelectionCriteriaBadges());
		result.add(crr.getSelectionCriteriaPoints());
		return result;
	}

	public static ChallengeRules convertTable(DefaultTableModel model) {
		ChallengeRules result = new ChallengeRules();
		Vector<Vector> vec = model.getDataVector();
		for (Vector v : vec) {
			int i = 0;
			ChallengeRuleRow crr = new ChallengeRuleRow();
			if (v.get(0) != null) {
				crr.setName((String) v.get(0));
			} else {
				crr.setName("");
			}
			if (v.get(1) != null) {
				crr.setType((String) v.get(1));
			} else {
				crr.setType("");
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
			if (v.get(4) != null && v.get(4) instanceof Integer) {
				crr.setBonus((Integer) v.get(4));
			} else {
				crr.setBonus(0);
			}
			// v.get(5) is difficulty, for now blank
			if (v.get(6) != null) {
				crr.setPointType((String) v.get(6));
			} else {
				crr.setPointType("");
			}
			if (v.get(7) != null) {
				crr.setBaselineVar((String) v.get(7));
			} else {
				crr.setBaselineVar("");
			}
			if (v.get(8) != null) {
				crr.setSelectionCriteriaCustomData((String) v.get(8));
			} else {
				crr.setSelectionCriteriaCustomData("");
			}
			if (v.get(9) != null) {
				crr.setSelectionCriteriaBadges((String) v.get(9));
			} else {
				crr.setSelectionCriteriaBadges("");
			}
			if (v.get(10) != null) {
				crr.setSelectionCriteriaPoints((String) v.get(10));
			} else {
				crr.setSelectionCriteriaPoints("");
			}
			result.getChallenges().add(crr);
		}
		return result;
	}
}
