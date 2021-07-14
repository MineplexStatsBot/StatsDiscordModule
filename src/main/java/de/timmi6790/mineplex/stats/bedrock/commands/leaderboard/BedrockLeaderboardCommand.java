package de.timmi6790.mineplex.stats.bedrock.commands.leaderboard;

import de.timmi6790.discord_framework.module.modules.command.CommandModule;
import de.timmi6790.discord_framework.module.modules.command.models.CommandParameters;
import de.timmi6790.discord_framework.module.modules.command.property.properties.controll.MinArgProperty;
import de.timmi6790.mineplex.stats.common.commands.leaderboard.LeaderboardCommand;
import de.timmi6790.mpstats.api.client.bedrock.player.models.BedrockPlayer;
import de.timmi6790.mpstats.api.client.common.BaseApiClient;

public class BedrockLeaderboardCommand extends LeaderboardCommand<BedrockPlayer> {
    public BedrockLeaderboardCommand(final BaseApiClient<BedrockPlayer> baseApiClient, final CommandModule commandModule) {
        this(
                baseApiClient,
                commandModule,
                "bedrockLeaderboard",
                "blb"
        );
    }

    public BedrockLeaderboardCommand(final BaseApiClient<BedrockPlayer> baseApiClient,
                                     final CommandModule commandModule,
                                     final String name,
                                     final String... aliasNames) {
        super(
                baseApiClient,
                commandModule,
                1,
                "Bedrock",
                name,
                "Bedrock",
                "<game>",
                aliasNames
        );

        this.addProperties(
                new MinArgProperty(1)
        );
    }

    @Override
    protected String getStat(final CommandParameters commandParameters) {
        return "Wins";
    }

    @Override
    protected String getBoard(final CommandParameters commandParameters) {
        return "All";
    }
}
