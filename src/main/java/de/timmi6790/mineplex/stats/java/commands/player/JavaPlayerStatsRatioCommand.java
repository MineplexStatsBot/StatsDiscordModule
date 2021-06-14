package de.timmi6790.mineplex.stats.java.commands.player;

import com.google.common.collect.Lists;
import com.googlecode.charts4j.GCharts;
import com.googlecode.charts4j.PieChart;
import com.googlecode.charts4j.Slice;
import de.timmi6790.discord_framework.module.modules.command.CommandParameters;
import de.timmi6790.discord_framework.module.modules.command.CommandResult;
import de.timmi6790.discord_framework.module.modules.command.exceptions.CommandReturnException;
import de.timmi6790.discord_framework.module.modules.command.property.properties.MinArgCommandProperty;
import de.timmi6790.mineplex.stats.common.commands.BaseStatsCommand;
import de.timmi6790.mineplex.stats.common.settings.DisclaimerMessagesSetting;
import de.timmi6790.mineplex.stats.common.utilities.ArgumentParsingUtilities;
import de.timmi6790.mineplex.stats.common.utilities.ErrorMessageUtilities;
import de.timmi6790.mineplex.stats.common.utilities.FormationUtilities;
import de.timmi6790.mineplex.stats.common.utilities.SetUtilities;
import de.timmi6790.mineplex.stats.java.utilities.JavaArgumentParsingUtilities;
import de.timmi6790.mpstats.api.client.common.BaseApiClient;
import de.timmi6790.mpstats.api.client.common.board.exceptions.InvalidBoardNameException;
import de.timmi6790.mpstats.api.client.common.board.models.Board;
import de.timmi6790.mpstats.api.client.common.filter.models.Reason;
import de.timmi6790.mpstats.api.client.common.leaderboard.exceptions.InvalidLeaderboardCombinationRestException;
import de.timmi6790.mpstats.api.client.common.player.models.PlayerEntry;
import de.timmi6790.mpstats.api.client.common.player.models.PlayerStats;
import de.timmi6790.mpstats.api.client.common.stat.exceptions.InvalidStatNameRestException;
import de.timmi6790.mpstats.api.client.common.stat.models.Stat;
import de.timmi6790.mpstats.api.client.java.JavaMpStatsApiClient;
import de.timmi6790.mpstats.api.client.java.player.models.JavaPlayer;
import lombok.Data;
import net.dv8tion.jda.api.utils.MarkdownUtil;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

// TODO: Reimplement old logic
//- The stat pls command now only includes data of the last x days. This will prevent  that your old stats are mixed into this.
//   All: 365 * 10 days
//   Yearly: Days since the start of the year
//   Monthly: Days since the start of the month
//   Weekly: Days since the start of the week
//   Daily: 1

public class JavaPlayerStatsRatioCommand extends BaseStatsCommand<JavaPlayer> {
    private static final String GLOBAL_GAME_NAME = "Global";

    private static final int STAT_POSITION = 1;
    private static final int BOARD_POSITION = 2;

    public JavaPlayerStatsRatioCommand(final BaseApiClient<JavaPlayer> apiClient) {
        super(
                apiClient,
                "playerstats",
                "Java",
                "Player stats as graph",
                "<player> <stat> [board]",
                "pls", "plsats", "plstat"
        );

        this.addProperties(
                new MinArgCommandProperty(2)
        );
    }

    private String generatePieUrl(final PieChart gCharts) {
        String url = gCharts.toURLString();
        // Series colours | RED, YELLOW_GREEN, GREEN, BLUE, PURPLE
        url += "&chco=FF0000,ADFF2F,00FF00,0000FF,a020f0";
        // Legend colour and size
        url += "&chdls=ffffff,15";
        // Legend order
        url += "&chdlp=r|a";

        return url;
    }

    private float calculatePercentage(final long value, final long totalValue) {
        if (totalValue == 0 || value == 0) {
            return 0;
        } else {
            return ((float) value / totalValue) * 100;
        }
    }

    private CalculatedPie parseSlices(final long totalValue, final List<PlayerEntry> entries) {
        ZonedDateTime biggestTime = LocalDateTime.MIN.atZone(ZoneId.systemDefault());
        final List<Slice> slices = Lists.newArrayListWithExpectedSize(entries.size());
        long foundSum = 0;
        for (final PlayerEntry entry : entries) {
            // We need to ignore the global game, because it represents 100%
            if (GLOBAL_GAME_NAME.equals(entry.getLeaderboard().getGame().getGameName())) {
                continue;
            }

            final double percentage = this.calculatePercentage(entry.getScore(), totalValue);
            foundSum += entry.getScore();
            if (entry.getSaveTime().isAfter(biggestTime)) {
                biggestTime = entry.getSaveTime();
            }

            slices.add(Slice.newSlice(
                    (int) Math.round(percentage * 100),
                    String.format(
                            "%s %s %s",
                            entry.getLeaderboard().getGame().getCleanName(),
                            FormationUtilities.getFormattedNumber(percentage) + "%",
                            FormationUtilities.getFormattedNumber(entry.getScore())
                    )
            ));
        }

        // Calculate unknown
        final long difference = totalValue - foundSum;
        if (difference > 0) {
            final float percentage = this.calculatePercentage(difference, totalValue);
            slices.add(Slice.newSlice(
                    Math.round(percentage * 100),
                    String.format(
                            "%s %s %s",
                            "Unknown",
                            FormationUtilities.getFormattedNumber(this.calculatePercentage(difference, totalValue)) + "%",
                            FormationUtilities.getFormattedNumber(difference)
                    )
            ));
        }

        // Sort after percentage
        slices.sort(Comparator.comparingLong(Slice::getPercentage));
        return new CalculatedPie(slices, biggestTime);
    }

    private long calculateTotalValue(final Collection<PlayerEntry> entries) {
        long totalValue = 0;

        // Calculate the sum of all values or take the global value if found
        for (final PlayerEntry entry : entries) {
            if (GLOBAL_GAME_NAME.equalsIgnoreCase(entry.getLeaderboard().getGame().getGameName())) {
                totalValue = entry.getScore();
                break;
            }

            totalValue += entry.getScore();
        }

        return totalValue;
    }

    private String generatePieChart(final PlayerStats<JavaPlayer> playerStats) {
        final long totalValue = this.calculateTotalValue(playerStats.getStats());
        final CalculatedPie calculatedPie = this.parseSlices(
                totalValue,
                new ArrayList<>(playerStats.getStats())
        );

        final PlayerEntry entry = SetUtilities.getFirstEntry(playerStats.getStats());
        final PieChart pieChart = GCharts.newPieChart(calculatedPie.getSlices());
        pieChart.setSize(750, 400);
        pieChart.setTitle(String.format(
                "%s %s %s %s %s",
                playerStats.getPlayer().getName(),
                entry.getLeaderboard().getStat().getCleanName(),
                entry.getLeaderboard().getBoard().getCleanName(),
                FormationUtilities.getFormattedNumber(totalValue),
                FormationUtilities.getFormattedTime(calculatedPie.getHighestTime())
        ));

        return this.generatePieUrl(pieChart);
    }

    private Optional<PlayerStats<JavaPlayer>> getPlayerStats(final CommandParameters commandParameters,
                                                             final UUID playerUUID,
                                                             final String stat,
                                                             final String board,
                                                             final ZonedDateTime zonedDateTime,
                                                             final Set<Reason> filterReasons) {
        try {
            return ((JavaMpStatsApiClient) this.getApiClient()).getPlayerClient().getPlayerStatStats(
                    playerUUID,
                    stat,
                    board,
                    false,
                    zonedDateTime,
                    filterReasons
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

    private void sendDisclaimerMessage(final CommandParameters commandParameters) {
        this.sendMessage(
                commandParameters,
                this.getEmbedBuilder(commandParameters)
                        .setTitle("Prototype Command")
                        .setDescription(
                                """
                                        %s%nEverything you see is based on leaderboards, that means that for your data to show, you need to fulfill one of the 2 conditions:
                                        You either need to have that stat on mineplex.com/players/YourName or to be on the leaderboard for the specific stat.
                                        This is also the reason you sometimes see %s. Unknown basically means that I know what your total value is, but I can't find all the sources of it.
                                        I hope I don't really need to mention that this command is not even close to be done.
                                        """,
                                MarkdownUtil.bold("The data you see up there is not 100% correct."),
                                MarkdownUtil.bold("Unknown")
                        )
        );
    }

    @Override
    protected CommandResult onStatsCommand(final CommandParameters commandParameters) {
        // Parse args
        final String playerName = JavaArgumentParsingUtilities.getJavaPlayerNameOrThrow(commandParameters, 0);
        final UUID playerUUID = JavaArgumentParsingUtilities.getPlayerUUIDOrThrow(commandParameters, playerName);
        final String stat = this.getArg(commandParameters, STAT_POSITION);
        final String board = this.getArgOrDefault(commandParameters, BOARD_POSITION, ArgumentParsingUtilities.getDefaultBoard());
        final ZonedDateTime zonedDateTime = ArgumentParsingUtilities.getDateTimeOrThrow(commandParameters, 3);
        final Set<Reason> filterReasons = ArgumentParsingUtilities.getFilterReasons(commandParameters);

        // Web request
        final Optional<PlayerStats<JavaPlayer>> playerStatsOpt = this.getPlayerStats(
                commandParameters,
                playerUUID,
                stat,
                board,
                zonedDateTime,
                filterReasons
        );
        if (playerStatsOpt.isEmpty()) {
            ErrorMessageUtilities.sendNotDataFoundMessage(commandParameters);
            return CommandResult.SUCCESS;
        }

        final PlayerStats<JavaPlayer> playerStats = playerStatsOpt.get();
        final String pieUrl = this.generatePieChart(playerStats);

        // Send to server
        commandParameters.getLowestMessageChannel()
                .sendMessage(pieUrl)
                .queue();

        if (commandParameters.getUserDb().getSettingOrDefault(DisclaimerMessagesSetting.class, true)) {
            this.sendDisclaimerMessage(commandParameters);
        }
        return CommandResult.SUCCESS;
    }

    @Data
    private static class CalculatedPie {
        private final List<Slice> slices;
        private final ZonedDateTime highestTime;
    }
}
