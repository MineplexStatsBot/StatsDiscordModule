package de.timmi6790.mineplex.stats.common.commands.managment;

import de.timmi6790.discord_framework.module.modules.command.CommandModule;
import de.timmi6790.discord_framework.module.modules.command.exceptions.CommandReturnException;
import de.timmi6790.discord_framework.module.modules.command.models.BaseCommandResult;
import de.timmi6790.discord_framework.module.modules.command.models.CommandParameters;
import de.timmi6790.discord_framework.module.modules.command.models.CommandResult;
import de.timmi6790.discord_framework.module.modules.command.property.properties.controll.MinArgProperty;
import de.timmi6790.discord_framework.module.modules.command.property.properties.info.AliasNamesProperty;
import de.timmi6790.discord_framework.module.modules.command.property.properties.info.CategoryProperty;
import de.timmi6790.discord_framework.module.modules.command.property.properties.info.DescriptionProperty;
import de.timmi6790.discord_framework.module.modules.command.property.properties.info.SyntaxProperty;
import de.timmi6790.discord_framework.module.modules.command.utilities.ArgumentUtilities;
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
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.MarkdownUtil;

import java.awt.*;
import java.time.Instant;
import java.util.Optional;

@Log4j2
public abstract class FilterCommand<P extends Player> extends BaseStatsCommand<P> {
    private static final int GAME_POSITION = 0;
    private static final int STAT_POSITION = 1;
    private static final int BOARD_POSITION = 2;

    // TODO: Move into config
    private static final long FILTER_ANNOUNCEMENT_CHANNEL = 787325600196853790L;

    private final ShardManager shardManager;

    protected FilterCommand(final BaseApiClient<P> apiClient,
                            @NonNull final String name,
                            final CommandModule commandModule,
                            final ShardManager shardManager,
                            @NonNull final String category,
                            final String... aliasNames) {
        super(
                apiClient,
                name,
                commandModule
        );
     
        this.addProperties(
                new MinArgProperty(5),
                new CategoryProperty(category),
                new DescriptionProperty("Filter"),
                new SyntaxProperty("<game> <stat> <board> <player> <reason>"),
                new AliasNamesProperty(aliasNames)
        );

        this.shardManager = shardManager;
    }

    private Filter<P> createFilter(final CommandParameters commandParameters,
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
                    stat,
                    STAT_POSITION,
                    "stat",
                    null,
                    new String[0],
                    exception.getSuggestedStats(),
                    Stat::getStatName
            );
        } catch (final InvalidGameNameRestException exception) {
            this.throwArgumentCorrectionMessage(
                    commandParameters,
                    game,
                    GAME_POSITION,
                    "game",
                    null,
                    new String[0],
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
                    board,
                    BOARD_POSITION,
                    "board",
                    null,
                    new String[0],
                    exception.getSuggestedBoards(),
                    Board::getBoardName
            );
        }

        throw new CommandReturnException();
    }

    private void sendFilterLogMessage(final CommandParameters commandParameters, final Filter<P> filter) {
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
                        commandParameters.getUser().getAsMention(),
                        this.getFormattedPlayer(filter.getPlayer()),
                        MarkdownUtil.monospace(
                                leaderboard.getGame().getGameName() + "-" +
                                        leaderboard.getStat().getStatName() + "-" +
                                        leaderboard.getBoard().getBoardName()
                        )
                )
                .setTimestamp(Instant.now());
        for (final MessageEmbed embedMessage : message.build()) {
            channel
                    .sendMessage(embedMessage)
                    .flatMap(Message::crosspost)
                    .queue();
        }
    }

    protected abstract String getFormattedPlayer(P player);

    protected abstract String getSchemaName();

    @Override
    protected CommandResult onStatsCommand(final CommandParameters commandParameters) {
        final String game = commandParameters.getArg(GAME_POSITION);
        final String stat = commandParameters.getArg(STAT_POSITION);
        final String board = commandParameters.getArg(BOARD_POSITION);
        // TODO: We need to think about a way for bedrock
        final String player = commandParameters.getArg(3);
        final Reason reason = ArgumentUtilities.getFromEnumIgnoreCaseOrThrow(
                commandParameters,
                this.getClass(),
                4,
                Reason.class
        );

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
