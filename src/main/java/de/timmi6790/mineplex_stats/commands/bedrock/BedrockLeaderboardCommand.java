package de.timmi6790.mineplex_stats.commands.bedrock;

import de.timmi6790.mineplex_stats.statsapi.models.bedrock.BedrockLeaderboard;

import java.util.ArrayList;
import java.util.List;

public class BedrockLeaderboardCommand extends AbstractBedrockLeaderboardCommand {
    public BedrockLeaderboardCommand() {
        super("bleaderboard", "Bedrock Leaderboard", "bl", "blb");
    }

    @Override
    protected String[][] parseLeaderboard(final List<BedrockLeaderboard.Leaderboard> leaderboardResponse) {
        final List<String[]> parsed = new ArrayList<>(leaderboardResponse.size() + 1);
        parsed.add(new String[]{"Player", "Score", "Position"});

        for (final BedrockLeaderboard.Leaderboard data : leaderboardResponse) {
            parsed.add(new String[]{
                    data.getName(),
                    this.getFormattedNumber(data.getScore()),
                    String.valueOf(data.getPosition())
            });
        }

        return parsed.toArray(new String[0][0]);
    }
}

