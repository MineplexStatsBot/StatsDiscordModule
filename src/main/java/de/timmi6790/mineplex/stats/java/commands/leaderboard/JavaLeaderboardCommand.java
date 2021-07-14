package de.timmi6790.mineplex.stats.java.commands.leaderboard;

import de.timmi6790.discord_framework.module.modules.command.CommandModule;
import de.timmi6790.discord_framework.module.modules.command.models.CommandParameters;
import de.timmi6790.discord_framework.module.modules.command.property.properties.controll.MinArgProperty;
import de.timmi6790.mineplex.stats.common.commands.leaderboard.LeaderboardCommand;
import de.timmi6790.mineplex.stats.common.utilities.ArgumentParsingUtilities;
import de.timmi6790.mpstats.api.client.common.BaseApiClient;
import de.timmi6790.mpstats.api.client.java.player.models.JavaPlayer;

public class JavaLeaderboardCommand extends LeaderboardCommand<JavaPlayer> {
    public JavaLeaderboardCommand(final BaseApiClient<JavaPlayer> baseApiClient, final CommandModule commandModule) {
        this(
                baseApiClient,
                "leaderboard",
                commandModule,
                "lb"
        );
    }

    public JavaLeaderboardCommand(final BaseApiClient<JavaPlayer> baseApiClient,
                                  final String name,
                                  final CommandModule commandModule,
                                  final String... aliasNames) {
        super(
                baseApiClient,
                commandModule,
                3,
                "Java",
                name,
                "Java",
                "<game> <stat> [board]",
                aliasNames
        );

        this.addProperties(
                new MinArgProperty(2)
        );
    }


    @Override
    protected String getStat(final CommandParameters commandParameters) {
        return commandParameters.getArg(1);
    }

    @Override
    protected String getBoard(final CommandParameters commandParameters) {
        return commandParameters.getArgOrDefault(2, ArgumentParsingUtilities.getDefaultBoard());
    }
}
