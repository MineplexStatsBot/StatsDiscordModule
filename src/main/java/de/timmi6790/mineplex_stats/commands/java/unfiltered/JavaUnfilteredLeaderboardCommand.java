package de.timmi6790.mineplex_stats.commands.java.unfiltered;

import de.timmi6790.mineplex_stats.commands.java.AbstractJavaLeaderboardCommand;

public class JavaUnfilteredLeaderboardCommand extends AbstractJavaLeaderboardCommand {
    public JavaUnfilteredLeaderboardCommand() {
        super("unfilteredLeaderboard", "Java Unfiltered Leaderboard", "<game> <stat> [board] [start] [end] [date]", "ulb");

        this.setFilteredStats(false);
    }
}