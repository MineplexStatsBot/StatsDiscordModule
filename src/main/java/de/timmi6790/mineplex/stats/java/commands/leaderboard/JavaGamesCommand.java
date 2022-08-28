package de.timmi6790.mineplex.stats.java.commands.leaderboard;

import com.google.common.collect.Lists;
import de.timmi6790.discord_framework.module.modules.slashcommand.SlashCommandModule;
import de.timmi6790.discord_framework.module.modules.slashcommand.parameters.SlashCommandParameters;
import de.timmi6790.discord_framework.module.modules.slashcommand.result.BaseCommandResult;
import de.timmi6790.discord_framework.module.modules.slashcommand.result.CommandResult;
import de.timmi6790.discord_framework.utilities.MultiEmbedBuilder;
import de.timmi6790.mineplex.stats.common.commands.leaderboard.GamesCommand;
import de.timmi6790.mpstats.api.client.common.BaseApiClient;
import de.timmi6790.mpstats.api.client.common.game.exceptions.InvalidGameNameRestException;
import de.timmi6790.mpstats.api.client.common.game.models.Game;
import de.timmi6790.mpstats.api.client.common.leaderboard.models.Leaderboard;
import de.timmi6790.mpstats.api.client.common.stat.exceptions.InvalidStatNameRestException;
import de.timmi6790.mpstats.api.client.common.stat.models.Stat;
import de.timmi6790.mpstats.api.client.java.player.models.JavaPlayer;

import java.util.*;

public class JavaGamesCommand extends GamesCommand<JavaPlayer> {
    public JavaGamesCommand(final BaseApiClient<JavaPlayer> apiClient, final SlashCommandModule commandModule) {
        super(
                apiClient,
                commandModule,
                "games",
                "Java",
                "Java games",
                "[game] [stat]"
        );

        this.addOptions(
                GAME_OPTION,
                STAT_OPTION
        );
    }

    private CommandResult handleGamesCommand(final SlashCommandParameters commandParameters) {
        final List<Game> games = this.getApiClient().getGameClient().getGames();
        final MultiEmbedBuilder message = commandParameters.getEmbedBuilder()
                .setTitle("Java Games")
                .setFooterFormat(
                        "TIP: Run /%s <game> to see more details",
                        this.getName()
                );

        commandParameters.sendMessage(this.parseGames(games, message));
        return BaseCommandResult.SUCCESSFUL;
    }

    private CommandResult handleGameInfoCommand(final SlashCommandParameters commandParameters, final String gameName) {
        // Get leaderboards
        final List<Leaderboard> gameLeaderboards;
        try {
            gameLeaderboards = this.getApiClient().getLeaderboardClient().getLeaderboards(gameName);
        } catch (final InvalidGameNameRestException exception) {
            this.throwArgumentCorrectionMessage(
                    commandParameters,
                    GAME_OPTION_REQUIRED,
                    null,
                    exception.getSuggestedGames(),
                    Game::getGameName
            );
            return BaseCommandResult.INVALID_ARGS;
        }

        if (gameLeaderboards.isEmpty()) {
            commandParameters.sendMessage(
                    commandParameters.getEmbedBuilder()
                            .setTitle("No Stats Found")
                            .setDescription("The game has no stats")
            );
            return BaseCommandResult.SUCCESSFUL;
        }

        // Parse found leaderboards
        final Leaderboard firstLeaderboard = gameLeaderboards.get(0);
        final Game game = firstLeaderboard.getGame();

        // We need to use a set here, because each stat is included with all its boards
        final Set<String> statNames = new HashSet<>();
        for (final Leaderboard leaderboard : gameLeaderboards) {
            statNames.add(leaderboard.getStat().getStatName());
        }
        final List<String> sortedStatNames = new ArrayList<>(statNames);
        sortedStatNames.sort(Comparator.naturalOrder());

        commandParameters.sendMessage(
                commandParameters.getEmbedBuilder()
                        .setTitle("Java Games - " + game.getCleanName())
                        .addField(
                                "Wiki",
                                "[" + game.getCleanName() + "](" + game.getWikiUrl() + ")",
                                false,
                                game.getWikiUrl() != null
                        )
                        .addField(
                                "Description",
                                Objects.requireNonNullElse(game.getDescription(), ""),
                                false,
                                game.getDescription() != null
                        )
                        .addField(
                                "Alias names",
                                String.join(", ", game.getAliasNames()),
                                false,
                                !game.getAliasNames().isEmpty()
                        )
                        .addField(
                                "Stats",
                                String.join(", ", sortedStatNames)
                        )
                        .setFooterFormat(
                                "TIP: Run /%s %s <stat> to see more details",
                                this.getName(),
                                gameName
                        )
        );
        return BaseCommandResult.SUCCESSFUL;
    }

    private CommandResult handleStatInfoCommand(final SlashCommandParameters commandParameters,
                                                final String gameName,
                                                final String statName) {
        // Get leaderboards
        final List<Leaderboard> statLeaderboards;
        try {
            statLeaderboards = this.getApiClient().getLeaderboardClient().getLeaderboards(gameName, statName);
        } catch (final InvalidGameNameRestException exception) {
            this.throwArgumentCorrectionMessage(
                    commandParameters,
                    GAME_OPTION_REQUIRED,
                    null,
                    exception.getSuggestedGames(),
                    Game::getGameName
            );
            return BaseCommandResult.INVALID_ARGS;
        } catch (final InvalidStatNameRestException exception) {
            this.throwArgumentCorrectionMessage(
                    commandParameters,
                    STAT_OPTION_REQUIRED,
                    null,
                    exception.getSuggestedStats(),
                    Stat::getStatName
            );
            return BaseCommandResult.INVALID_ARGS;
        }

        if (statLeaderboards.isEmpty()) {
            // TODO: Add better support for the wrong combination
            commandParameters.sendMessage(
                    commandParameters.getEmbedBuilder()
                            .setTitle("No Stat Info Found")
                            .setDescription("Are you sure that the inputted combination is correct?")
            );
            return BaseCommandResult.SUCCESSFUL;
        }

        // Parse found leaderboards
        final Leaderboard firstLeaderboard = statLeaderboards.get(0);
        final Game game = firstLeaderboard.getGame();
        final Stat stat = firstLeaderboard.getStat();

        final List<String> sortedBoardNames = Lists.newArrayListWithCapacity(statLeaderboards.size());
        for (final Leaderboard leaderboard : statLeaderboards) {
            sortedBoardNames.add(leaderboard.getBoard().getBoardName());
        }
        sortedBoardNames.sort(Comparator.naturalOrder());

        commandParameters.sendMessage(
                commandParameters.getEmbedBuilder()
                        .setTitleFormat(
                                "Java Games - %s - %s",
                                game.getCleanName(),
                                stat.getCleanName()
                        )
                        .addField(
                                "Description",
                                Objects.requireNonNullElse(stat.getDescription(), ""),
                                false,
                                stat.getDescription() != null
                        )
                        .addField(
                                "Alias names",
                                String.join(", ", stat.getAliasNames()),
                                false,
                                !stat.getAliasNames().isEmpty()
                        )
                        .addField(
                                "Boards",
                                String.join(", ", sortedBoardNames),
                                false
                        )
        );
        return BaseCommandResult.SUCCESSFUL;
    }

    @Override
    protected CommandResult onStatsCommand(final SlashCommandParameters commandParameters) {
        final Optional<String> gameOpt = commandParameters.getOption(GAME_OPTION_REQUIRED);
        final Optional<String> statOpt = commandParameters.getOption(STAT_OPTION_REQUIRED);

        if (gameOpt.isEmpty()) {
            return this.handleGamesCommand(commandParameters);
        }

        if (statOpt.isEmpty()) {
            return this.handleGameInfoCommand(commandParameters, gameOpt.get());
        }

        return this.handleStatInfoCommand(commandParameters, gameOpt.get(), statOpt.get());
    }
}
