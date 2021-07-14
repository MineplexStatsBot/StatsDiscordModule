package de.timmi6790.mineplex.stats.java.commands.player;

import com.google.common.collect.Lists;
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
import de.timmi6790.minecraft.utilities.JavaUtilities;
import de.timmi6790.mineplex.stats.common.commands.BaseStatsCommand;
import de.timmi6790.mineplex.stats.common.generators.picture.PictureTable;
import de.timmi6790.mineplex.stats.common.models.ParserResult;
import de.timmi6790.mineplex.stats.common.utilities.ArgumentParsingUtilities;
import de.timmi6790.mineplex.stats.common.utilities.ErrorMessageUtilities;
import de.timmi6790.mineplex.stats.common.utilities.FormationUtilities;
import de.timmi6790.mineplex.stats.common.utilities.SetUtilities;
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

    public JavaPlayerCommand(final BaseApiClient<JavaPlayer> baseApiClient, final CommandModule commandModule) {
        this(
                baseApiClient,
                commandModule,
                "player",
                "Check player stats",
                "pl"
        );
    }

    public JavaPlayerCommand(final BaseApiClient<JavaPlayer> baseApiClient,
                             final CommandModule commandModule,
                             final String name,
                             final String description,
                             final String... aliasNames) {
        super(
                baseApiClient,
                name,
                commandModule
        );

        this.addProperties(
                new MinArgProperty(2),
                new CategoryProperty("Java"),
                new DescriptionProperty(description),
                new SyntaxProperty("<player> <game> [board] [dateTime]"),
                new AliasNamesProperty(aliasNames)
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
        final PlayerEntry foundEntry = SetUtilities.getFirstEntry(playerStats.getStats());
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
        final String game = commandParameters.getArg(GAME_POSITION);
        final String board = commandParameters.getArgOrDefault(BOARD_POSITION, ArgumentParsingUtilities.getDefaultBoard());
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
            return BaseCommandResult.SUCCESSFUL;
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
