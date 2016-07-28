package eu.fbk.das.challenge.gui;

public class ChallengeReport {

	private String player;
	private String challengeName;
	private String challengeType;
	private String transportMode;
	private String baselineValue;
	private String targetValue;
	private String prize;
	private String pointType;
	private String chId;

	public void setPlayer(String player) {
		this.player = player;
	}

	public void setChallengeName(String challengeName) {
		this.challengeName = challengeName;
	}

	public void setChallengeType(String challengeType) {
		this.challengeType = challengeType;
	}

	public void setTransportMode(String transportMode) {
		this.transportMode = transportMode;
	}

	public void setBaselineValue(String baselineValue) {
		this.baselineValue = baselineValue;

	}

	public void setTargetValue(String targetValue) {
		this.targetValue = targetValue;
	}

	public void setPrize(String prize) {
		this.prize = prize;
	}

	public void setPointType(String pointType) {
		this.pointType = pointType;
	}

	public void setChId(String chId) {
		this.chId = chId;
	}

	public Object getTransportMode() {
		return transportMode;
	}

	public String getPlayer() {
		return player;
	}

	public String getChallengeName() {
		return challengeName;
	}

	public String getChallengeType() {
		return challengeType;
	}

	public String getBaselineValue() {
		return baselineValue;
	}

	public String getTargetValue() {
		return targetValue;
	}

	public String getPrize() {
		return prize;
	}

	public String getPointType() {
		return pointType;
	}

	public String getChId() {
		return chId;
	}

}
