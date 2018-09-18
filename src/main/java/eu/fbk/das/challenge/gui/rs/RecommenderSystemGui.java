package eu.fbk.das.challenge.gui.rs;

import eu.fbk.das.rs.challengeGeneration.RecommendationSystem;
import eu.fbk.das.rs.challengeGeneration.RecommendationSystemConfig;
import eu.trentorise.game.challenges.api.Constants;
import eu.trentorise.game.challenges.model.ChallengeDataDTO;
import eu.trentorise.game.challenges.util.CalendarUtil;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class RecommenderSystemGui {

    public RecommenderSystemGui() {

        /*

        // recommandationsystem integration
        if (useRecommendationSystem) {
            RecommendationSystem rs = new RecommendationSystem(
                    new RecommendationSystemConfig(useFiltering, filterIds));
            Map<String, List<ChallengeDataDTO>> rsChallenges = rs
                    .recommendation(users, CalendarUtil.getStart().getTime(),
                            CalendarUtil.getEnd().getTime());
            if (rsChallenges == null
                    || (rsChallenges != null && rsChallenges.isEmpty())) {
                msg = "Warning: no challenges generated using recommendation system, even if is enabled";
                System.out.println(msg);
                log += msg + Constants.LINE_SEPARATOR;
                return log;
            }
            try {
                crg.setChallenges(rsChallenges, gameId);
                msg = "Generated challenges using recommandation system for "
                        + rsChallenges.size() + " players";
                System.out.println(msg);
                log += msg + Constants.LINE_SEPARATOR;
                // write configuration file to filesystem
                rs.writeToFile(rsChallenges);
            } catch (IOException e) {
                msg = "Error in challenge generation : " + e.getMessage();
                System.err.println(msg);
                log += msg + Constants.LINE_SEPARATOR;
                return log;
            }
        }

        */

    }

}
