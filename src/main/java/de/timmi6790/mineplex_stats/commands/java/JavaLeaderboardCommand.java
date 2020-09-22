package de.timmi6790.mineplex_stats.commands.java;

import de.timmi6790.commons.builders.ListBuilder;
import de.timmi6790.mineplex_stats.statsapi.models.java.JavaLeaderboard;
import de.timmi6790.mineplex_stats.statsapi.models.java.JavaStat;
import lombok.EqualsAndHashCode;

import java.util.stream.Collectors;

@EqualsAndHashCode(callSuper = true)
public class JavaLeaderboardCommand extends AbstractJavaLeaderboardCommand {
    public JavaLeaderboardCommand() {
        super(true, "leaderboard", "Java Leaderboard", "<game> <stat> [board] [start] [end] [date]", "lb");
    }

    @Override
    protected String[][] parseLeaderboard(final JavaStat stat, final JavaLeaderboard leaderboardResponse) {
        return ListBuilder.<String[]>ofArrayList(leaderboardResponse.getLeaderboard().size() + 1)
                .add(new String[]{"Player", "Score", "Position"})
                .addAll(leaderboardResponse.getLeaderboard()
                        .stream()
                        .map(data -> new String[]{data.getName(), this.getFormattedScore(stat, data.getScore()), String.valueOf(data.getPosition())})
                        .collect(Collectors.toList()))
                .build()
                .toArray(new String[0][3]);
    }

    @Override
    protected String[] getHeader(final JavaLeaderboard.Info leaderboardInfo) {
        return new String[]{leaderboardInfo.getGame(), leaderboardInfo.getStat(), leaderboardInfo.getBoard()};
    }
}
