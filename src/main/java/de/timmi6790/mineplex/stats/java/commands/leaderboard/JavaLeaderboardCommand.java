package de.timmi6790.mineplex.stats.java.commands.leaderboard;

import de.timmi6790.discord_framework.module.modules.command.CommandParameters;
import de.timmi6790.discord_framework.module.modules.command.property.properties.MinArgCommandProperty;
import de.timmi6790.mineplex.stats.common.commands.leaderboard.LeaderboardCommand;
import de.timmi6790.mineplex.stats.common.utilities.ArgumentParsingUtilities;
import de.timmi6790.mpstats.api.client.common.BaseApiClient;
import de.timmi6790.mpstats.api.client.java.player.models.JavaPlayer;

public class JavaLeaderboardCommand extends LeaderboardCommand<JavaPlayer> {
    public JavaLeaderboardCommand(final BaseApiClient<JavaPlayer> baseApiClient) {
        super(
                baseApiClient,
                3,
                "Java",
                "leaderboard",
                "Java",
                "<game> <stat> [board]",
                "lb"
        );

        this.addProperties(
                new MinArgCommandProperty(2)
        );
    }

    @Override
    protected String getStat(final CommandParameters commandParameters) {
        return this.getArg(commandParameters, 1);
    }

    @Override
    protected String getBoard(final CommandParameters commandParameters) {
        return this.getArgOrDefault(commandParameters, 2, ArgumentParsingUtilities.getDefaultBoard());
    }
}
