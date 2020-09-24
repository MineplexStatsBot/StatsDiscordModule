package de.timmi6790.mineplex_stats.commands.java;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public class JavaLeaderboardCommand extends AbstractJavaLeaderboardCommand {
    public JavaLeaderboardCommand() {
        super("leaderboard", "Java Leaderboard", "lb");
    }
}
