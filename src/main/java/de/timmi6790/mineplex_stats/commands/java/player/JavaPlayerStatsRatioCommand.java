package de.timmi6790.mineplex_stats.commands.java.player;

import com.googlecode.charts4j.GCharts;
import com.googlecode.charts4j.PieChart;
import com.googlecode.charts4j.Slice;
import de.timmi6790.discord_framework.modules.command.CommandParameters;
import de.timmi6790.discord_framework.modules.command.CommandResult;
import de.timmi6790.discord_framework.modules.command.properties.ExampleCommandsCommandProperty;
import de.timmi6790.discord_framework.modules.command.properties.MinArgCommandProperty;
import de.timmi6790.mineplex_stats.commands.java.AbstractJavaStatsCommand;
import de.timmi6790.mineplex_stats.statsapi.models.ResponseModel;
import de.timmi6790.mineplex_stats.statsapi.models.java.JavaBoard;
import de.timmi6790.mineplex_stats.statsapi.models.java.JavaRatioPlayer;
import de.timmi6790.mineplex_stats.statsapi.models.java.JavaStat;
import net.dv8tion.jda.api.utils.MarkdownUtil;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class JavaPlayerStatsRatioCommand extends AbstractJavaStatsCommand {
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

    private static String getPieUrl(final PieChart gCharts) {
        gCharts.setSize(750, 400);

        String url = gCharts.toURLString();
        // Series colours | RED, YELLOW_GREEN, GREEN, BLUE, PURPLE
        url += "&chco=FF0000,ADFF2F,00FF00,0000FF,a020f0";
        // Legend colour and size
        url += "&chdls=ffffff,15";
        // Legend order
        url += "&chdlp=r|a";

        return url;
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
        final ResponseModel responseModel = this.getModule().getMpStatsRestClient().getPlayerStatsRatio(player, stat.getPrintName(), board.getName(), unixTime);
        this.checkApiResponseThrow(commandParameters, responseModel, "No stats available");

        final JavaRatioPlayer javaRatioPlayer = (JavaRatioPlayer) responseModel;

        // Add to pie chart
        final List<Slice> slices = new ArrayList<>();

        final long totalValue = javaRatioPlayer.getStats().values().stream().mapToLong(JavaRatioPlayer.Stat::getScore).sum();
        final DecimalFormat decimalFormat = new DecimalFormat("#0.00");
        decimalFormat.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.US));
        javaRatioPlayer.getStats().values()
                .stream()
                .sorted(Comparator.comparingLong(JavaRatioPlayer.Stat::getScore))
                .forEach(value -> slices.add(
                        Slice.newSlice(
                                value.getScore(),
                                String.format("%s %s %s",
                                        value.getGame(),
                                        decimalFormat.format(Math.min(value.getScore(), totalValue) == 0 ? 0D : ((double) value.getScore() / (double) totalValue) * 100) + "%",
                                        this.getFormattedNumber(value.getScore())
                                )
                        )
                ));

        final PieChart gCharts = GCharts.newPieChart(slices);
        gCharts.setTitle(javaRatioPlayer.getInfo().getName() + " " + stat.getPrintName() + " " + board.getName() + " " +
                this.getFormattedNumber(javaRatioPlayer.getInfo().getTotalNumber()) + " " + this.getFormattedUnixTime(unixTime));

        // Send to server
        commandParameters.getLowestMessageChannel()
                .sendMessage(getPieUrl(gCharts))
                .queue();
        sendMessage(
                commandParameters,
                getEmbedBuilder(commandParameters)
                        .setTitle("Prototype Command")
                        .setDescription(
                                MarkdownUtil.bold("The data you see up there is not 100% correct.\n") +
                                        "Everything you see is based on leaderboards, " +
                                        "that means that for your data to show, you need to fulfill one of the 2 conditions: " +
                                        "You either need to have that stat on mineplex.com/players/YourName or to be on the leaderboard for the specific stat." +
                                        "\n\nThis is also the reason you sometimes see " + MarkdownUtil.bold("Unknown") + ". " +
                                        "Unknown basically means that I know what your total value is, but I can't find all the sources of it." +
                                        "\n\nI hope I don't really need to mention that this command is not even close to be done."
                        )
        );
        return CommandResult.SUCCESS;
    }
}
