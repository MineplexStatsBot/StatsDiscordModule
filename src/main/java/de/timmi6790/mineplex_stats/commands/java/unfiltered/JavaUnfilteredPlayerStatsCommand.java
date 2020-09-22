package de.timmi6790.mineplex_stats.commands.java.unfiltered;

import de.timmi6790.mineplex_stats.commands.java.AbstractJavaPlayerStatsCommand;

public class JavaUnfilteredPlayerStatsCommand extends AbstractJavaPlayerStatsCommand {
    public JavaUnfilteredPlayerStatsCommand() {
        super(false, "unfilteredPlayer", "Java unfiltered player stats", "<player> <game> [board] [date]", "upl");
    }
}
