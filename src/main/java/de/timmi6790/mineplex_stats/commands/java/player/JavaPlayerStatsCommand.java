package de.timmi6790.mineplex_stats.commands.java.player;

import de.timmi6790.mineplex_stats.commands.java.AbstractJavaPlayerStatsCommand;


public class JavaPlayerStatsCommand extends AbstractJavaPlayerStatsCommand {
    public JavaPlayerStatsCommand() {
        super(true, "player", "Java player stats", "<player> <game> [board] [date]", "pl");
    }
}
