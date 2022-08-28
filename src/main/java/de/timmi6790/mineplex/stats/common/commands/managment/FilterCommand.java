package de.timmi6790.mineplex.stats.common.commands.managment;

import de.timmi6790.discord_framework.module.modules.slashcommand.SlashCommandModule;
import de.timmi6790.discord_framework.module.modules.slashcommand.exceptions.CommandReturnException;
import de.timmi6790.discord_framework.module.modules.slashcommand.option.Option;
import de.timmi6790.discord_framework.module.modules.slashcommand.option.options.EnumOption;
import de.timmi6790.discord_framework.module.modules.slashcommand.parameters.SlashCommandParameters;
import de.timmi6790.discord_framework.module.modules.slashcommand.property.properties.info.CategoryProperty;
import de.timmi6790.discord_framework.module.modules.slashcommand.property.properties.info.SyntaxProperty;
import de.timmi6790.discord_framework.module.modules.slashcommand.result.BaseCommandResult;
import de.timmi6790.discord_framework.module.modules.slashcommand.result.CommandResult;
import de.timmi6790.discord_framework.utilities.MultiEmbedBuilder;
import de.timmi6790.mineplex.stats.common.commands.BaseStatsCommand;
import de.timmi6790.mineplex.stats.common.utilities.ErrorMessageUtilities;
import de.timmi6790.mpstats.api.client.common.BaseApiClient;
import de.timmi6790.mpstats.api.client.common.board.exceptions.InvalidBoardNameException;
import de.timmi6790.mpstats.api.client.common.board.models.Board;
import de.timmi6790.mpstats.api.client.common.filter.models.Filter;
import de.timmi6790.mpstats.api.client.common.filter.models.Reason;
import de.timmi6790.mpstats.api.client.common.game.exceptions.InvalidGameNameRestException;
import de.timmi6790.mpstats.api.client.common.game.models.Game;
import de.timmi6790.mpstats.api.client.common.leaderboard.exceptions.InvalidLeaderboardCombinationRestException;
import de.timmi6790.mpstats.api.client.common.leaderboard.models.Leaderboard;
import de.timmi6790.mpstats.api.client.common.player.exceptions.InvalidPlayerNameRestException;
import de.timmi6790.mpstats.api.client.common.player.models.Player;
import de.timmi6790.mpstats.api.client.common.stat.exceptions.InvalidStatNameRestException;
import de.timmi6790.mpstats.api.client.common.stat.models.Stat;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.MarkdownUtil;

import java.awt.*;
import java.time.Instant;
import java.util.Optional;

@Log4j2
public abstract class FilterCommand<P extends Player> extends BaseStatsCommand<P> {
    private static final Option<Reason> REASON_OPTION = new EnumOption<>(Reason.class, "reason", "Filter reason").setRequired(true);

    // TODO: Move into config
    private static final long FILTER_ANNOUNCEMENT_CHANNEL = 787325600196853790L;

    private final ShardManager shardManager;

    protected FilterCommand(final BaseApiClient<P> apiClient,
                            @NonNull final String name,
                            final SlashCommandModule commandModule,
                            final ShardManager shardManager,
                            @NonNull final String category,
                            final String... aliasNames) {
        super(
                apiClient,
                name,
                "Filter",
                commandModule
        );

        this.addProperties(
                new CategoryProperty(category),
                new SyntaxProperty("<game> <stat> <board> <player> <reason>")
        );

        this.shardManager = shardManager;

        this.addOptions(
                GAME_OPTION_REQUIRED,
                STAT_OPTION_REQUIRED,
                BOARD_OPTION_REQUIRED,
                JAVA_PLAYER_NAME_REQUIRED,
                REASON_OPTION
        );
    }

    private Filter<P> createFilter(final SlashCommandParameters commandParameters,
                                   final String game,
                                   final String stat,
                                   final String board,
                                   final String player,
                                   final Reason reason) {
        try {
            final Optional<Filter<P>> filterOpt = this.getApiClient().getFilterClient().createPermanentFilter(
                    game,
                    stat,
                    board,
                    player,
                    reason
            );

            if (filterOpt.isPresent()) {
                return filterOpt.get();
            }

            commandParameters.sendMessage(
                    commandParameters.getEmbedBuilder()
                            .setTitle("Error")
                            .setDescription(
                                    "Are you sure that the player with the name %s exists inside a leaderboard?",
                                    MarkdownUtil.monospace(player)
                            )
            );
        } catch (final InvalidStatNameRestException exception) {
            this.throwArgumentCorrectionMessage(
                    commandParameters,
                    STAT_OPTION,
                    null,
                    exception.getSuggestedStats(),
                    Stat::getStatName
            );
        } catch (final InvalidGameNameRestException exception) {
            this.throwArgumentCorrectionMessage(
                    commandParameters,
                    GAME_OPTION,
                    null,
                    exception.getSuggestedGames(),
                    Game::getGameName
            );
        } catch (final InvalidPlayerNameRestException exception) {
            commandParameters.sendMessage(
                    commandParameters.getEmbedBuilder()
                            .setTitle("Error")
                            .setDescription(
                                    "Are you sure that the player name %s is valid?",
                                    MarkdownUtil.monospace(player)
                            )
            );
        } catch (final InvalidLeaderboardCombinationRestException exception) {
            ErrorMessageUtilities.sendInvalidLeaderboardCombinationMessage(commandParameters, exception);
        } catch (final InvalidBoardNameException exception) {
            this.throwArgumentCorrectionMessage(
                    commandParameters,
                    BOARD_OPTION,
                    null,
                    exception.getSuggestedBoards(),
                    Board::getBoardName
            );
        }

        throw new CommandReturnException();
    }

    private void sendFilterLogMessage(final SlashCommandParameters commandParameters, final Filter<P> filter) {
        final TextChannel channel = this.shardManager.getTextChannelById(FILTER_ANNOUNCEMENT_CHANNEL);
        if (channel == null) {
            log.warn("Can't find filter announcement channel");
            return;
        }

        final Leaderboard leaderboard = filter.getLeaderboard();
        final MultiEmbedBuilder message = new MultiEmbedBuilder()
                .setAuthor("FilterSystem")
                .setColor(Color.CYAN)
                .setDescription(
                        "[%s]%s added a new filter entry for %s in %s",
                        this.getSchemaName(),
                        commandParameters.getUserDb().getUser().getAsMention(),
                        this.getFormattedPlayer(filter.getPlayer()),
                        MarkdownUtil.monospace(
                                leaderboard.getGame().getGameName() + "-" +
                                        leaderboard.getStat().getStatName() + "-" +
                                        leaderboard.getBoard().getBoardName()
                        )
                )
                .setTimestamp(Instant.now());

        channel.sendMessageEmbeds(message.build())
                .flatMap(Message::crosspost)
                .queue();
    }

    protected abstract String getFormattedPlayer(P player);

    protected abstract String getSchemaName();

    @Override
    protected CommandResult onStatsCommand(final SlashCommandParameters commandParameters) {
        final String game = commandParameters.getOptionOrThrow(GAME_OPTION_REQUIRED);
        final String stat = commandParameters.getOptionOrThrow(STAT_OPTION_REQUIRED);
        final String board = commandParameters.getOptionOrThrow(BOARD_OPTION_REQUIRED);
        final String player = commandParameters.getOptionOrThrow(JAVA_PLAYER_NAME_REQUIRED);
        final Reason reason = commandParameters.getOptionOrThrow(REASON_OPTION);

        final Filter<P> filter = this.createFilter(
                commandParameters,
                game,
                stat,
                board,
                player,
                reason
        );

        // Inform executor
        final Leaderboard leaderboard = filter.getLeaderboard();
        commandParameters.sendMessage(
                commandParameters.getEmbedBuilder()
                        .setTitle("Added filter")
                        .setDescription(
                                "Added new permanent filter for %s in %s",
                                this.getFormattedPlayer(filter.getPlayer()),
                                MarkdownUtil.monospace(
                                        leaderboard.getGame().getGameName() + "-" +
                                                leaderboard.getStat().getStatName() + "-" +
                                                leaderboard.getBoard().getBoardName()
                                )
                        )
        );

        // Send global log
        this.sendFilterLogMessage(commandParameters, filter);

        return BaseCommandResult.SUCCESSFUL;
    }
}
