package de.timmi6790.mineplex.stats.bedrock.commands.leaderboard;

import de.timmi6790.discord_framework.module.modules.command.CommandParameters;
import de.timmi6790.discord_framework.module.modules.command.CommandResult;
import de.timmi6790.discord_framework.utilities.MultiEmbedBuilder;
import de.timmi6790.mineplex.stats.common.commands.leaderboard.GamesCommand;
import de.timmi6790.mpstats.api.client.bedrock.player.models.BedrockPlayer;
import de.timmi6790.mpstats.api.client.common.BaseApiClient;
import de.timmi6790.mpstats.api.client.common.game.models.Game;

import java.util.List;

public class BedrockGamesCommand extends GamesCommand<BedrockPlayer> {
    public BedrockGamesCommand(final BaseApiClient<BedrockPlayer> apiClient) {
        super(
                apiClient,
                "bedrockGames",
                "Bedrock",
                "Bedrock games",
                "",
                "bgames"
        );
    }

    @Override
    protected CommandResult onStatsCommand(final CommandParameters commandParameters) {
        final List<Game> games = this.getApiClient().getGameClient().getGames();
        final MultiEmbedBuilder message = this.getEmbedBuilder(commandParameters)
                .setTitle("Bedrock Games");

        this.sendTimedMessage(
                commandParameters,
                this.parseGames(games, message),
                600
        );
        return CommandResult.SUCCESS;
    }
}
