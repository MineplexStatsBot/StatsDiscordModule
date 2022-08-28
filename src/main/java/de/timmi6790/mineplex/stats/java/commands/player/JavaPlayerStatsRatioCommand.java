package de.timmi6790.mineplex.stats.java.commands.player;

import com.google.common.collect.Lists;
import com.googlecode.charts4j.GCharts;
import com.googlecode.charts4j.PieChart;
import com.googlecode.charts4j.Slice;
import de.timmi6790.discord_framework.module.modules.slashcommand.SlashCommandModule;
import de.timmi6790.discord_framework.module.modules.slashcommand.exceptions.CommandReturnException;
import de.timmi6790.discord_framework.module.modules.slashcommand.parameters.SlashCommandParameters;
import de.timmi6790.discord_framework.module.modules.slashcommand.property.properties.info.CategoryProperty;
import de.timmi6790.discord_framework.module.modules.slashcommand.property.properties.info.SyntaxProperty;
import de.timmi6790.discord_framework.module.modules.slashcommand.result.BaseCommandResult;
import de.timmi6790.discord_framework.module.modules.slashcommand.result.CommandResult;
import de.timmi6790.mineplex.stats.common.commands.BaseStatsCommand;
import de.timmi6790.mineplex.stats.common.settings.DisclaimerMessagesSetting;
import de.timmi6790.mineplex.stats.common.utilities.*;
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

    public JavaPlayerStatsRatioCommand(final BaseApiClient<JavaPlayer> apiClient, final SlashCommandModule commandModule) {
        super(
                apiClient,
                "playerstats",
                "Player stats as graph",
                commandModule
        );

        this.addProperties(
                new CategoryProperty("Java"),
                new SyntaxProperty("<player> <stat> [board]")
        );

        this.addOptions(
                JAVA_PLAYER_NAME_REQUIRED,
                STAT_OPTION_REQUIRED,
                BOARD_OPTION,
                DATE_OPTION
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
            return MathUtilities.round((((float) value / totalValue) * 100), 2);
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

    private Optional<PlayerStats<JavaPlayer>> getPlayerStats(final SlashCommandParameters commandParameters,
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
                    STAT_OPTION,
                    null,
                    exception.getSuggestedStats(),
                    Stat::getStatName
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

    private void sendDisclaimerMessage(final SlashCommandParameters commandParameters) {
        commandParameters.sendMessage(
                commandParameters.getEmbedBuilder()
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
    protected CommandResult onStatsCommand(final SlashCommandParameters commandParameters) {
        // Parse args
        final String playerName = JavaArgumentParsingUtilities.getJavaPlayerNameOrThrow(commandParameters, JAVA_PLAYER_NAME_REQUIRED);
        final UUID playerUUID = JavaArgumentParsingUtilities.getPlayerUUIDOrThrow(commandParameters, playerName);
        final String stat = commandParameters.getOptionOrThrow(STAT_OPTION_REQUIRED);
        final String board = commandParameters.getOption(BOARD_OPTION).orElseGet(ArgumentParsingUtilities::getDefaultBoard);
        final ZonedDateTime zonedDateTime = ArgumentParsingUtilities.getDateTimeOrThrow(commandParameters, DATE_OPTION);
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
            return BaseCommandResult.SUCCESSFUL;
        }

        final PlayerStats<JavaPlayer> playerStats = playerStatsOpt.get();
        final String pieUrl = this.generatePieChart(playerStats);

        // Send to server
        if (pieUrl.length() <= 2000) {
            commandParameters
                    .sendMessage(pieUrl);
        } else {
            commandParameters
                    .sendMessage("Currently not working for the input. This should only affect nwang exp");
        }

        if (commandParameters.getUserDb().getSettingOrDefault(DisclaimerMessagesSetting.class, true)) {
            this.sendDisclaimerMessage(commandParameters);
        }
        return BaseCommandResult.SUCCESSFUL;
    }

    @Data
    private static class CalculatedPie {
        private final List<Slice> slices;
        private final ZonedDateTime highestTime;
    }
}
