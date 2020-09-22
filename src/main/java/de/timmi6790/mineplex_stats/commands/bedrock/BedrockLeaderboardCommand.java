package de.timmi6790.mineplex_stats.commands.bedrock;

import de.timmi6790.commons.builders.ListBuilder;
import de.timmi6790.mineplex_stats.statsapi.models.bedrock.BedrockLeaderboard;

import java.util.ArrayList;
import java.util.List;

public class BedrockLeaderboardCommand extends AbstractBedrockLeaderboardCommand {
    public BedrockLeaderboardCommand() {
        super("bleaderboard", "Bedrock Leaderboard", "<game> [start] [end] [date]", "bl", "blb");
    }

    @Override
    protected String[][] parseLeaderboard(final List<BedrockLeaderboard.Leaderboard> leaderboardResponse) {
        return new ListBuilder<String[]>(() -> new ArrayList<>(leaderboardResponse.size() + 1))
                .add(new String[]{"Player", "Score", "Position"})
                .addAll(leaderboardResponse.stream()
                        .map(data -> new String[]{data.getName(), this.getFormattedNumber(data.getScore()), String.valueOf(data.getPosition())}))
                .build()
                .toArray(new String[0][3]);
    }

    @Override
    protected String[] getHeader(final BedrockLeaderboard.Info leaderboardInfo) {
        return new String[]{"Bedrock " + leaderboardInfo.getGame()};
    }
}

