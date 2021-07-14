package de.timmi6790.mineplex.stats.common.commands.leaderboard;

import com.google.common.collect.Lists;
import de.timmi6790.discord_framework.module.modules.command.CommandModule;
import de.timmi6790.discord_framework.module.modules.command.exceptions.CommandReturnException;
import de.timmi6790.discord_framework.module.modules.command.models.BaseCommandResult;
import de.timmi6790.discord_framework.module.modules.command.models.CommandParameters;
import de.timmi6790.discord_framework.module.modules.command.models.CommandResult;
import de.timmi6790.discord_framework.module.modules.command.property.properties.info.AliasNamesProperty;
import de.timmi6790.discord_framework.module.modules.command.property.properties.info.CategoryProperty;
import de.timmi6790.discord_framework.module.modules.command.property.properties.info.DescriptionProperty;
import de.timmi6790.discord_framework.module.modules.command.property.properties.info.SyntaxProperty;
import de.timmi6790.discord_framework.module.modules.command.utilities.ArgumentUtilities;
import de.timmi6790.discord_framework.module.modules.command.utilities.ArrayUtilities;
import de.timmi6790.discord_framework.module.modules.reactions.button.actions.ButtonAction;
import de.timmi6790.discord_framework.module.modules.reactions.button.actions.CommandButtonAction;
import de.timmi6790.discord_framework.utilities.discord.DiscordEmotes;
import de.timmi6790.mineplex.stats.common.commands.BaseStatsCommand;
import de.timmi6790.mineplex.stats.common.generators.picture.PictureTable;
import de.timmi6790.mineplex.stats.common.utilities.ArgumentParsingUtilities;
import de.timmi6790.mineplex.stats.common.utilities.ErrorMessageUtilities;
import de.timmi6790.mineplex.stats.common.utilities.FormationUtilities;
import de.timmi6790.mpstats.api.client.common.BaseApiClient;
import de.timmi6790.mpstats.api.client.common.board.exceptions.InvalidBoardNameException;
import de.timmi6790.mpstats.api.client.common.board.models.Board;
import de.timmi6790.mpstats.api.client.common.filter.models.Reason;
import de.timmi6790.mpstats.api.client.common.game.exceptions.InvalidGameNameRestException;
import de.timmi6790.mpstats.api.client.common.game.models.Game;
import de.timmi6790.mpstats.api.client.common.leaderboard.exceptions.InvalidLeaderboardCombinationRestException;
import de.timmi6790.mpstats.api.client.common.leaderboard.models.LeaderboardPositionEntry;
import de.timmi6790.mpstats.api.client.common.leaderboard.models.LeaderboardPositionSave;
import de.timmi6790.mpstats.api.client.common.player.models.Player;
import de.timmi6790.mpstats.api.client.common.stat.exceptions.InvalidStatNameRestException;
import de.timmi6790.mpstats.api.client.common.stat.models.Stat;
import lombok.NonNull;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.interactions.components.ButtonStyle;

import java.time.ZonedDateTime;
import java.util.*;

// TODO: Leaps incorrectly, it only jumps 15 entries instead of 16 and includes the last page first entry on the new page
public abstract class LeaderboardCommand<P extends Player> extends BaseStatsCommand<P> {
    private static final int ROW_COUNT = 15;

    private static final int GAME_POSITION = 0;
    private static final int STAT_POSITION = 1;
    private static final int BOARD_POSITION = 2;

    private final int positionIndex;
    private final String schemaName;

    protected LeaderboardCommand(final BaseApiClient<P> apiClient,
                                 final CommandModule commandModule,
                                 final int positionIndex,
                                 final String schemaName,
                                 @NonNull final String name,
                                 @NonNull final String category,
                                 final String syntax,
                                 final String... aliasNames) {
        super(
                apiClient,
                name,
                commandModule
        );

        this.addProperties(
                new AliasNamesProperty(aliasNames),
                new SyntaxProperty(syntax + " [startPosition] [dateTime]"),
                new DescriptionProperty("Check the leaderboard"),
                new CategoryProperty(category)
        );

        this.positionIndex = positionIndex;
        this.schemaName = schemaName;
    }

    protected abstract String getStat(CommandParameters commandParameters);

    protected abstract String getBoard(CommandParameters commandParameters);

    protected LeaderboardPositionSave<P> getSave(final CommandParameters commandParameters,
                                                 final String game,
                                                 final String stat,
                                                 final String board,
                                                 final ZonedDateTime zonedDateTime,
                                                 final Set<Reason> filterReasons) {
        try {
            final Optional<LeaderboardPositionSave<P>> leaderboardSaveOpt = this.getApiClient().getLeaderboardClient().getLeaderboardSave(
                    game,
                    stat,
                    board,
                    zonedDateTime,
                    filterReasons
            );

            if (leaderboardSaveOpt.isPresent()) {
                return leaderboardSaveOpt.get();
            }

            // SEND ERROR;
            commandParameters.sendMessage(
                    commandParameters.getEmbedBuilder()
                            .setTitle("No data found")
                            .setDescription("No data was found.")
            );
            throw new CommandReturnException(BaseCommandResult.SUCCESSFUL);

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
        } catch (final InvalidLeaderboardCombinationRestException exception) {
            ErrorMessageUtilities.sendInvalidLeaderboardCombinationMessage(commandParameters, exception);
        }

        // This line should never be called
        throw new CommandReturnException(BaseCommandResult.INVALID_ARGS);
    }

    protected int getMaxRenderCount() {
        return ROW_COUNT;
    }

    protected int getPositionIndex() {
        return this.positionIndex;
    }

    protected int getTimeStartIndex() {
        return this.positionIndex + 1;
    }

    protected String[][] parseLeaderboard(final LeaderboardPositionSave<P> leaderboardSave,
                                          final int startPosition,
                                          final int endPosition) {
        final List<String[]> parsed = Lists.newArrayListWithCapacity(endPosition - startPosition + 1);
        parsed.add(new String[]{"Player", "Score", "Position"});

        final List<LeaderboardPositionEntry<P>> entries = leaderboardSave.getEntries();
        for (int index = startPosition - 1; endPosition > index; index++) {
            final LeaderboardPositionEntry<P> row = entries.get(index);
            parsed.add(
                    new String[]{
                            row.getPlayer().getName(),
                            FormationUtilities.getFormattedScore(leaderboardSave.getLeaderboard().getStat(), row.getScore()),
                            FormationUtilities.getFormattedPosition(row.getPosition())
                    }
            );
        }

        return parsed.toArray(new String[0][3]);
    }

    protected String getGame(final CommandParameters commandParameters) {
        return commandParameters.getArg(GAME_POSITION);
    }

    protected Set<Reason> getFilterReasons(final CommandParameters commandParameters) {
        return ArgumentParsingUtilities.getFilterReasons(commandParameters);
    }

    protected int getStartPosition(final CommandParameters commandParameters) {
        if (this.getPositionIndex() >= commandParameters.getArgs().length) {
            return 1;
        }

        final int startPosition = ArgumentUtilities.getIntOrThrow(commandParameters, this.getPositionIndex());
        if (startPosition > 0) {
            return startPosition;
        }

        commandParameters.sendMessage(
                commandParameters.getEmbedBuilder()
                        .setTitle("Invalid start position")
                        .setDescription("The start position must be bigger than 0.")
        );
        throw new CommandReturnException();
    }

    protected int getEndPosition(final LeaderboardPositionSave<P> leaderboardSave, final int startPosition) {
        return Math.min(leaderboardSave.getEntries().size(), startPosition + this.getMaxRenderCount());
    }

    protected CommandParameters getModifiedPositionParameter(final CommandParameters commandParameters,
                                                             int newStartPosition) {
        // This would show an error for the client
        if (newStartPosition <= 0) {
            newStartPosition = 1;
        }

        return CommandParameters.of(
                ArrayUtilities.modifyArrayAtPosition(commandParameters.getArgs(),
                        String.valueOf(newStartPosition),
                        this.getPositionIndex()
                ),
                commandParameters.isGuildCommand(),
                commandParameters.getCommandCause(),
                commandParameters.getCommandModule(),
                commandParameters.getChannelDb(),
                commandParameters.getUserDb()
        );
    }

    protected Map<Button, ButtonAction> getButtonActions(final CommandParameters commandParameters,
                                                         final int startPosition,
                                                         final int endPosition,
                                                         final int maxEntries) {
        final Map<Button, ButtonAction> buttonActions = new LinkedHashMap<>();
        final int bigJump = (int) Math.max(Math.floor(maxEntries / 10D), this.getMaxRenderCount() * 2);

        // Fast back button
        if (startPosition > this.getMaxRenderCount()) {
            final int distance = Math.min(startPosition - 1, bigJump);
            buttonActions.put(
                    Button.of(ButtonStyle.SECONDARY, "fast-back", String.valueOf(distance))
                            .withEmoji(Emoji.fromUnicode(DiscordEmotes.FAR_LEFT_ARROW.getEmote())),
                    new CommandButtonAction(
                            this.getClass(),
                            this.getModifiedPositionParameter(
                                    commandParameters,
                                    startPosition - distance
                            )
                    )
            );
        }

        // Back button
        if (startPosition > 1) {
            final int distance = Math.min(startPosition - 1, this.getMaxRenderCount());
            buttonActions.put(
                    Button.of(ButtonStyle.SECONDARY, "back", String.valueOf(distance))
                            .withEmoji(Emoji.fromUnicode(DiscordEmotes.LEFT_ARROW.getEmote())),
                    new CommandButtonAction(
                            this.getClass(),
                            this.getModifiedPositionParameter(
                                    commandParameters,
                                    startPosition - distance
                            )
                    )
            );
        }

        // Forward button
        if (maxEntries > endPosition) {
            final int distance = Math.min(maxEntries - startPosition, this.getMaxRenderCount());
            buttonActions.put(
                    Button.of(ButtonStyle.SECONDARY, "forward", String.valueOf(distance))
                            .withEmoji(Emoji.fromUnicode(DiscordEmotes.RIGHT_ARROW.getEmote())),
                    new CommandButtonAction(
                            this.getClass(),
                            this.getModifiedPositionParameter(
                                    commandParameters,
                                    startPosition + distance
                            )
                    )
            );
        }

        // Fast forward button
        if (maxEntries - this.getMaxRenderCount() >= endPosition) {
            final int distance = Math.min(maxEntries - startPosition, bigJump);
            buttonActions.put(
                    Button.of(ButtonStyle.SECONDARY, "fast-forward", String.valueOf(distance))
                            .withEmoji(Emoji.fromUnicode(DiscordEmotes.FAR_RIGHT_ARROW.getEmote())),
                    new CommandButtonAction(
                            this.getClass(),
                            this.getModifiedPositionParameter(
                                    commandParameters,
                                    startPosition + distance
                            )
                    )
            );
        }

        return buttonActions;
    }

    @Override
    protected CommandResult onStatsCommand(final CommandParameters commandParameters) {
        final String game = this.getGame(commandParameters);
        final String stat = this.getStat(commandParameters);
        final String board = this.getBoard(commandParameters);
        int startPosition = this.getStartPosition(commandParameters);
        final ZonedDateTime time = ArgumentParsingUtilities.getDateTimeOrThrow(commandParameters, this.getTimeStartIndex());

        final Set<Reason> filterReasons = this.getFilterReasons(commandParameters);

        final LeaderboardPositionSave<P> leaderboardSave = this.getSave(
                commandParameters,
                game,
                stat,
                board,
                time,
                filterReasons
        );
        // Catch 0 entry leaderboards. This should never be the case but I wanna make sure here.
        if (leaderboardSave.getEntries().isEmpty()) {
            ErrorMessageUtilities.sendNotDataFoundMessage(commandParameters);
            return BaseCommandResult.SUCCESSFUL;
        }

        // Fall back into the leaderboard if the selected start position with the entries is outside of the possible area
        final int endPosition = this.getEndPosition(leaderboardSave, startPosition);
        startPosition = Math.max(1, endPosition - this.getMaxRenderCount());

        final String[][] parsedLeaderboard = this.parseLeaderboard(leaderboardSave, startPosition, endPosition);
        final String[] header = new String[]{
                leaderboardSave.getLeaderboard().getGame().getGameName(),
                leaderboardSave.getLeaderboard().getStat().getStatName(),
                leaderboardSave.getLeaderboard().getBoard().getBoardName()
        };

        final String formattedSaveTime = FormationUtilities.getFormattedTime(leaderboardSave.getSaveTime());
        final String subHeader = this.schemaName + " - " + formattedSaveTime;

        final Map<Button, ButtonAction> buttonActions = this.getButtonActions(
                commandParameters,
                startPosition,
                endPosition,
                leaderboardSave.getEntries().size()
        );
        return this.sendPicture(
                commandParameters,
                new PictureTable(
                        header,
                        subHeader,
                        parsedLeaderboard
                ).generatePicture(),
                String.format("%s-%s", String.join("-", header), subHeader),
                buttonActions
        );
    }
}
