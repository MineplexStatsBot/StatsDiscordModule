package de.timmi6790.mineplex.stats.bedrock.commands.leaderboard;

import de.timmi6790.discord_framework.module.modules.slashcommand.SlashCommandModule;
import de.timmi6790.discord_framework.module.modules.slashcommand.parameters.SlashCommandParameters;
import de.timmi6790.mineplex.stats.common.commands.leaderboard.LeaderboardCommand;
import de.timmi6790.mpstats.api.client.bedrock.player.models.BedrockPlayer;
import de.timmi6790.mpstats.api.client.common.BaseApiClient;

public class BedrockLeaderboardCommand extends LeaderboardCommand<BedrockPlayer> {
    public BedrockLeaderboardCommand(final BaseApiClient<BedrockPlayer> baseApiClient, final SlashCommandModule commandModule) {
        this(
                baseApiClient,
                commandModule,
                "bedrockLeaderboard",
                "blb"
        );
    }

    public BedrockLeaderboardCommand(final BaseApiClient<BedrockPlayer> baseApiClient,
                                     final SlashCommandModule commandModule,
                                     final String name,
                                     final String... aliasNames) {
        super(
                baseApiClient,
                commandModule,
                "Bedrock",
                name,
                "Check the leaderboard",
                "Bedrock",
                "<game>",
                aliasNames
        );
    }

    @Override
    protected String getStat(final SlashCommandParameters commandParameters) {
        return "Wins";
    }

    @Override
    protected String getBoard(final SlashCommandParameters commandParameters) {
        return "All";
    }
}
