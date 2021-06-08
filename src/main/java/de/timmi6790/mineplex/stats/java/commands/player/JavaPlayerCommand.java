package de.timmi6790.mineplex.stats.java.commands.player;

import com.google.common.collect.Lists;
import de.timmi6790.discord_framework.module.modules.command.CommandParameters;
import de.timmi6790.discord_framework.module.modules.command.CommandResult;
import de.timmi6790.discord_framework.module.modules.command.exceptions.CommandReturnException;
import de.timmi6790.discord_framework.module.modules.command.property.properties.MinArgCommandProperty;
import de.timmi6790.minecraft.utilities.JavaUtilities;
import de.timmi6790.mineplex.stats.common.commands.BaseStatsCommand;
import de.timmi6790.mineplex.stats.common.generators.picture.PictureTable;
import de.timmi6790.mineplex.stats.common.models.ParserResult;
import de.timmi6790.mineplex.stats.common.utilities.ArgumentParsingUtilities;
import de.timmi6790.mineplex.stats.common.utilities.ErrorMessageUtilities;
import de.timmi6790.mineplex.stats.common.utilities.FormationUtilities;
import de.timmi6790.mineplex.stats.java.utilities.JavaArgumentParsingUtilities;
import de.timmi6790.mpstats.api.client.common.BaseApiClient;
import de.timmi6790.mpstats.api.client.common.board.exceptions.InvalidBoardNameException;
import de.timmi6790.mpstats.api.client.common.board.models.Board;
import de.timmi6790.mpstats.api.client.common.filter.models.Reason;
import de.timmi6790.mpstats.api.client.common.game.exceptions.InvalidGameNameRestException;
import de.timmi6790.mpstats.api.client.common.game.models.Game;
import de.timmi6790.mpstats.api.client.common.leaderboard.exceptions.InvalidLeaderboardCombinationRestException;
import de.timmi6790.mpstats.api.client.common.player.models.GeneratedPlayerEntry;
import de.timmi6790.mpstats.api.client.common.player.models.PlayerEntry;
import de.timmi6790.mpstats.api.client.common.player.models.PlayerStats;
import de.timmi6790.mpstats.api.client.common.stat.models.Stat;
import de.timmi6790.mpstats.api.client.java.JavaMpStatsApiClient;
import de.timmi6790.mpstats.api.client.java.player.models.JavaPlayer;
import lombok.SneakyThrows;

import java.awt.image.BufferedImage;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class JavaPlayerCommand extends BaseStatsCommand<JavaPlayer> {
    private static final int ROW_SOFT_LIMIT = 20;

    private static final int GAME_POSITION = 1;
    private static final int BOARD_POSITION = 2;

    public JavaPlayerCommand(final BaseApiClient<JavaPlayer> baseApiClient) {
        this(
                baseApiClient,
                "player",
                "Check player stats",
                "pl"
        );
    }

    public JavaPlayerCommand(final BaseApiClient<JavaPlayer> baseApiClient,
                             final String name,
                             final String description, final String... aliasNames) {
        super(
                baseApiClient,
                name,
                "Java",
                description,
                "<player> <game> [board] [dateTime]",
                aliasNames
        );

        this.addProperties(
                new MinArgCommandProperty(2)
        );
    }

    protected Optional<PlayerStats<JavaPlayer>> getPlayerStats(final CommandParameters commandParameters,
                                                               final UUID playerUUID,
                                                               final String game,
                                                               final String board,
                                                               final ZonedDateTime zonedDateTime,
                                                               final Set<Reason> filterReasons) {
        try {
            return ((JavaMpStatsApiClient) this.getApiClient()).getPlayerClient().getPlayerGameStats(
                    playerUUID,
                    game,
                    board,
                    true,
                    zonedDateTime,
                    filterReasons
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

    protected Set<Reason> getFilterReasons(final CommandParameters commandParameters) {
        return ArgumentParsingUtilities.getFilterReasons(commandParameters);
    }

    protected String[] getTableHeader(final PlayerStats<JavaPlayer> playerStats) {
        PlayerEntry foundEntry = null;
        // Find one entry from set
        for (final PlayerEntry entry : playerStats.getStats()) {
            foundEntry = entry;
            break;
        }

        if (foundEntry == null) {
            return new String[]{
                    playerStats.getPlayer().getName(),
                    "Unknown",
                    "Unknown"
            };
        } else {
            return new String[]{
                    playerStats.getPlayer().getName(),
                    foundEntry.getLeaderboard().getGame().getGameName(),
                    foundEntry.getLeaderboard().getBoard().getBoardName()
            };
        }
    }

    protected ParserResult parsePlayerStats(final PlayerStats<JavaPlayer> playerStats) {
        final List<String[]> parsed = Lists.newArrayListWithCapacity(playerStats.getGeneratedStats().size() + playerStats.getStats().size() + 1);
        parsed.add(new String[]{"Stat", "Score", "Position"});

        final List<GeneratedPlayerEntry> generatedStats = new ArrayList<>(playerStats.getGeneratedStats());
        generatedStats.sort(Comparator.comparing(GeneratedPlayerEntry::getCleanStatName));
        for (final GeneratedPlayerEntry generatedEntry : generatedStats) {
            parsed.add(
                    new String[]{
                            generatedEntry.getCleanStatName(),
                            FormationUtilities.getFormattedNumber(generatedEntry.getScore()),
                            ""
                    }
            );
        }

        final List<PlayerEntry> statEntries = new ArrayList<>(playerStats.getStats());
        statEntries.sort((object1, object2) -> {
            // Achievements are always at the bottom
            // Highest sorting priority at the top
            // If same priority lexicographic

            final Stat stat1 = object1.getLeaderboard().getStat();
            final Stat stat2 = object2.getLeaderboard().getStat();

            final int stat1Priority = stat1.getSortingPriority();
            final int stat2Priority = stat2.getSortingPriority();
            if (stat1Priority != stat2Priority) {
                return Integer.compare(stat2Priority, stat1Priority);
            }

            if (!(stat1.isAchievement() && stat2.isAchievement())) {
                return stat1.isAchievement() ? 1 : -1;
            }

            return stat1.getCleanName().compareTo(stat2.getCleanName());
        });

        final int totalEntries = generatedStats.size() + statEntries.size();
        final boolean aboveLimit = totalEntries > ROW_SOFT_LIMIT;
        ZonedDateTime highestTime = LocalDateTime.MIN.atZone(ZoneId.systemDefault());
        for (final PlayerEntry entry : statEntries) {
            // Don't show empty rows if we are above the limit
            if (aboveLimit && entry.getScore() == -1) {
                continue;
            }

            if (entry.getSaveTime().isAfter(highestTime)) {
                highestTime = entry.getSaveTime();
            }

            parsed.add(
                    new String[]{
                            FormationUtilities.getFormattedStat(entry.getLeaderboard().getStat()),
                            FormationUtilities.getFormattedScore(entry.getLeaderboard().getStat(), entry.getScore()),
                            FormationUtilities.getFormattedPosition(entry.getPosition())
                    }
            );
        }

        final String[] tableHeader = this.getTableHeader(playerStats);
        final String[][] leaderboard = parsed.toArray(new String[0][3]);
        return new ParserResult(
                leaderboard,
                tableHeader,
                highestTime
        );
    }

    @SneakyThrows
    @Override
    protected CommandResult onStatsCommand(final CommandParameters commandParameters) {
        final String playerName = JavaArgumentParsingUtilities.getJavaPlayerNameOrThrow(commandParameters, 0);
        final UUID playerUUID = JavaArgumentParsingUtilities.getPlayerUUIDOrThrow(commandParameters, playerName);
        final String game = this.getArg(commandParameters, GAME_POSITION);
        final String board = this.getArgOrDefault(commandParameters, BOARD_POSITION, ArgumentParsingUtilities.getDefaultBoard());
        final ZonedDateTime zonedDateTime = ArgumentParsingUtilities.getDateTimeOrThrow(commandParameters, 3);
        final Set<Reason> filterReasons = this.getFilterReasons(commandParameters);

        final CompletableFuture<BufferedImage> skinFuture = JavaUtilities.getPlayerSkin(playerUUID);
        final Optional<PlayerStats<JavaPlayer>> playerStatsOpt = this.getPlayerStats(
                commandParameters,
                playerUUID,
                game,
                board,
                zonedDateTime,
                filterReasons
        );
        if (playerStatsOpt.isEmpty()) {
            ErrorMessageUtilities.sendNotDataFoundMessage(commandParameters);
            return CommandResult.SUCCESS;
        }

        final PlayerStats<JavaPlayer> playerStats = playerStatsOpt.get();
        final ParserResult parserResult = this.parsePlayerStats(playerStats);

        final String formattedSaveTime = FormationUtilities.getFormattedTime(parserResult.getHighestTime());
        final String subHeader = "Java - " + formattedSaveTime;

        BufferedImage skin;
        try {
            skin = skinFuture.get(10, TimeUnit.SECONDS);
        } catch (final ExecutionException | TimeoutException e) {
            skin = null;
        }

        return this.sendPicture(
                commandParameters,
                new PictureTable(
                        parserResult.getHeader(),
                        subHeader,
                        parserResult.getLeaderboard(),
                        skin
                ).generatePicture(),
                String.format("%s-%s", String.join("-", parserResult.getHeader()), subHeader)
        );
    }
}
