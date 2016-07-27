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
			crr.setName((String) v.get(0));
			crr.setType((String) v.get(1));
			crr.setGoalType((String) v.get(2));
			crr.setTarget(v.get(3));
			if (v.get(4) != null && v.get(4) instanceof Integer) {
				crr.setBonus((Integer) v.get(4));
			} else {
				crr.setBonus(0);
			}
			crr.setPointType((String) v.get(6));
			crr.setBaselineVar((String) v.get(7));
			crr.setSelectionCriteriaCustomData((String) v.get(8));
			crr.setSelectionCriteriaBadges((String) v.get(9));
			crr.setSelectionCriteriaPoints((String) v.get(10));
			result.getChallenges().add(crr);
		}
		return result;
	}
}
