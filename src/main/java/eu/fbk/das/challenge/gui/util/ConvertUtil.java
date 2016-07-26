package eu.fbk.das.challenge.gui.util;

import java.util.Vector;

import eu.trentorise.game.challenges.util.ChallengeRuleRow;

public class ConvertUtil {

	private ConvertUtil() {
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Vector convertChallenge(ChallengeRuleRow crr) {
		Vector result = new Vector();
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

}
