package de.timmi6790.mineplex_stats.commands.java.player;

import de.timmi6790.discord_framework.modules.command.properties.ExampleCommandsCommandProperty;
import de.timmi6790.discord_framework.modules.command.properties.MinArgCommandProperty;
import de.timmi6790.discord_framework.modules.command.properties.RequiredDiscordBotPermsCommandProperty;
import de.timmi6790.mineplex_stats.commands.java.AbstractJavaPlayerStatsCommand;
import de.timmi6790.mineplex_stats.statsapi.models.java.JavaPlayerStats;
import net.dv8tion.jda.api.Permission;


public class JavaPlayerStatsCommand extends AbstractJavaPlayerStatsCommand {
    public JavaPlayerStatsCommand() {
        super(true, "player", "Java player stats", "<player> <game> [board] [date]", "pl");

        this.addProperties(
                new MinArgCommandProperty(2),
                new RequiredDiscordBotPermsCommandProperty(Permission.MESSAGE_ATTACH_FILES),
                new ExampleCommandsCommandProperty(
                        "nwang888 Global",
                        "nwang888 Global yearly",
                        "nwang888 Global global 1/25/2020"
                )
        );
    }

    @Override
    protected String[] getHeader(final JavaPlayerStats.Info playerStatsInfo) {
        return new String[]{playerStatsInfo.getName(), playerStatsInfo.getGame(), playerStatsInfo.getBoard()};
    }
}
