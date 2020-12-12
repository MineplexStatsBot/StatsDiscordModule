package de.timmi6790.mineplex_stats.commands.java.player;

import com.googlecode.charts4j.GCharts;
import com.googlecode.charts4j.PieChart;
import com.googlecode.charts4j.Slice;
import de.timmi6790.discord_framework.modules.command.CommandParameters;
import de.timmi6790.discord_framework.modules.command.CommandResult;
import de.timmi6790.discord_framework.modules.command.property.properties.ExampleCommandsCommandProperty;
import de.timmi6790.discord_framework.modules.command.property.properties.MinArgCommandProperty;
import de.timmi6790.mineplex_stats.commands.java.AbstractJavaStatsCommand;
import de.timmi6790.mineplex_stats.settings.DisclaimerMessagesSetting;
import de.timmi6790.mineplex_stats.statsapi.models.ResponseModel;
import de.timmi6790.mineplex_stats.statsapi.models.java.JavaBoard;
import de.timmi6790.mineplex_stats.statsapi.models.java.JavaRatioPlayer;
import de.timmi6790.mineplex_stats.statsapi.models.java.JavaStat;
import de.timmi6790.mineplex_stats.utilities.BiggestLong;
import lombok.Data;
import net.dv8tion.jda.api.utils.MarkdownUtil;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class JavaPlayerStatsRatioCommand extends AbstractJavaStatsCommand {
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#0.00");

    static {
        DECIMAL_FORMAT.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.US));
    }

    public JavaPlayerStatsRatioCommand() {
        super("playerstats", "Player stats as graph", "<player> <stat> [board]", "pls", "plsats", "plstat");

        this.setCategory("PROTOTYPE - MineplexStats - Java");

        this.addProperties(
                new MinArgCommandProperty(2),
                new ExampleCommandsCommandProperty(
                        "nwang888 wins",
                        "nwang888 wins yearly"
                )
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

    private PieSlicesData parseSlices(final long totalValue, final List<JavaRatioPlayer.Stat> stats) {
        stats.sort(Comparator.comparingLong(JavaRatioPlayer.Stat::getScore));

        final BiggestLong highestUnixTime = new BiggestLong();
        final List<Slice> slices = new ArrayList<>();
        for (final JavaRatioPlayer.Stat stat : stats) {
            final double percentage;
            if (totalValue == 0 || stat.getScore() == 0) {
                percentage = 0;
            } else {
                percentage = ((double) stat.getScore() / (double) totalValue) * 100;
            }

            highestUnixTime.tryNumber(stat.getUnix());
            slices.add(Slice.newSlice(
                    stat.getScore(),
                    String.format("%s %s %s",
                            stat.getGame(),
                            DECIMAL_FORMAT.format(percentage) + "%",
                            this.getFormattedNumber(stat.getScore())
                    )
            ));
        }

        return new PieSlicesData(slices, highestUnixTime.get());
    }

    private String generatePieChart(final JavaRatioPlayer javaRatioPlayer) {
        final JavaRatioPlayer.Info info = javaRatioPlayer.getInfo();
        final long totalValue = info.getTotalNumber();

        // Add to pie chart
        final PieSlicesData pieSlicesData = this.parseSlices(
                totalValue,
                new ArrayList<>(javaRatioPlayer.getStats().values())
        );

        final PieChart pieChart = GCharts.newPieChart(pieSlicesData.getSlices());
        pieChart.setSize(750, 400);
        pieChart.setTitle(String.format(
                "%s %s %s %s %s",
                info.getName(),
                info.getStat(),
                info.getBoard(),
                this.getFormattedNumber(totalValue),
                this.getFormattedUnixTime(pieSlicesData.getHighestUnixTime())
        ));

        return this.generatePieUrl(pieChart);
    }

    @Override
    protected CommandResult onCommand(final CommandParameters commandParameters) {
        // Parse args
        final String player = this.getPlayer(commandParameters, 0);
        // TODO: Add a parser
        final JavaStat stat = this.getStat(commandParameters, 1);
        final JavaBoard board = this.getBoard(stat, commandParameters, 2);
        final long unixTime = Instant.now().getEpochSecond();// this.getUnixTime(commandParameters, 3);

        // Web request
        final ResponseModel responseModel = this.getMineplexStatsModule()
                .getMpStatsRestClient()
                .getPlayerStatsRatio(player, stat.getPrintName(), board.getName(), unixTime);
        this.checkApiResponseThrow(commandParameters, responseModel, "No stats available");

        final String pieUrl = this.generatePieChart((JavaRatioPlayer) responseModel);

        // Send to server
        commandParameters.getLowestMessageChannel()
                .sendMessage(pieUrl)
                .queue();


        if (commandParameters.getUserDb().getSettingOrDefault(DisclaimerMessagesSetting.class, true)) {
            this.sendMessage(
                    commandParameters,
                    this.getEmbedBuilder(commandParameters)
                            .setTitle("Prototype Command")
                            .setDescription(
                                    "%s%n" +
                                            "Everything you see is based on leaderboards, " +
                                            "that means that for your data to show, you need to fulfill one of the 2 conditions: " +
                                            "You either need to have that stat on mineplex.com/players/YourName or to be on the leaderboard for the specific stat." +
                                            "\n\nThis is also the reason you sometimes see %s. " +
                                            "Unknown basically means that I know what your total value is, but I can't find all the sources of it." +
                                            "\n\nI hope I don't really need to mention that this command is not even close to be done.",
                                    MarkdownUtil.bold("The data you see up there is not 100% correct."),
                                    MarkdownUtil.bold("Unknown")
                            )
            );
        }
        return CommandResult.SUCCESS;
    }

    @Data
    private static class PieSlicesData {
        private final List<Slice> slices;
        private final long highestUnixTime;
    }
}
