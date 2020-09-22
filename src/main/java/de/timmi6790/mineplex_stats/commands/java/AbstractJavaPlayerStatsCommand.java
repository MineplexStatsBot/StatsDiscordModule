package de.timmi6790.mineplex_stats.commands.java;

import de.timmi6790.commons.builders.ListBuilder;
import de.timmi6790.discord_framework.modules.command.CommandParameters;
import de.timmi6790.discord_framework.modules.command.CommandResult;
import de.timmi6790.discord_framework.modules.command.properties.ExampleCommandsCommandProperty;
import de.timmi6790.discord_framework.modules.command.properties.MinArgCommandProperty;
import de.timmi6790.discord_framework.modules.command.properties.RequiredDiscordBotPermsCommandProperty;
import de.timmi6790.mineplex_stats.commands.AbstractStatsCommand;
import de.timmi6790.mineplex_stats.picture.PictureTable;
import de.timmi6790.mineplex_stats.statsapi.models.ResponseModel;
import de.timmi6790.mineplex_stats.statsapi.models.java.JavaBoard;
import de.timmi6790.mineplex_stats.statsapi.models.java.JavaGame;
import de.timmi6790.mineplex_stats.statsapi.models.java.JavaPlayerStats;
import de.timmi6790.mineplex_stats.statsapi.models.java.JavaStat;
import de.timmi6790.mineplex_stats.statsapi.utilities.BiggestLong;
import lombok.EqualsAndHashCode;
import net.dv8tion.jda.api.Permission;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@EqualsAndHashCode(callSuper = true)
public abstract class AbstractJavaPlayerStatsCommand extends AbstractJavaStatsCommand {
    private final boolean filteredStats;

    public AbstractJavaPlayerStatsCommand(final boolean filteredStats, final String name, final String description, final String syntax, final String... aliasNames) {
        super(name, description, syntax, aliasNames);

        this.filteredStats = filteredStats;

        this.addProperties(
                new MinArgCommandProperty(2),
                new RequiredDiscordBotPermsCommandProperty(Permission.MESSAGE_ATTACH_FILES),
                new ExampleCommandsCommandProperty(
                        "nwang888 Global",
                        "nwang888 Global yearly",
                        "nwang888 Global global 1/25/2020"
                )
        );
    }

    protected abstract String[] getHeader(JavaPlayerStats.Info playerStatsInfo);

    @Override
    protected CommandResult onCommand(final CommandParameters commandParameters) {
        // Parse args
        final String player = this.getPlayer(commandParameters, 0);
        final JavaGame javaGame = this.getGame(commandParameters, 1);
        final JavaBoard board = this.getBoard(javaGame, commandParameters, 2);
        final long unixTime = this.getUnixTime(commandParameters, 3);

        // Web Requests
        final ResponseModel responseModel = this.getStatsModule().getMpStatsRestClient().getJavaPlayerStats(player, javaGame.getName(), board.getName(), unixTime, this.filteredStats);
        this.checkApiResponse(commandParameters, responseModel, "No stats available");

        final JavaPlayerStats playerStats = (JavaPlayerStats) responseModel;
        final JavaPlayerStats.Info playerStatsInfo = playerStats.getInfo();

        final CompletableFuture<BufferedImage> skinFuture = this.getPlayerSkin(playerStatsInfo.getUuid());

        // Parse data into image generator
        final JavaGame game = this.getStatsModule().getJavaGame(playerStatsInfo.getGame()).orElseThrow(RuntimeException::new);
        final BiggestLong highestUnixTime = new BiggestLong(0);
        final String[][] leaderboard = new ListBuilder<String[]>(() -> new ArrayList<>(playerStats.getWebsiteStats().size() + game.getStats().size() + 1))
                .add(new String[]{"Category", "Score", "Position"})
                .addAll(playerStats.getWebsiteStats().values()
                        .stream()
                        .map(websiteStat -> new String[]{websiteStat.getStat(), this.getFormattedNumber(websiteStat.getScore()), ""}))
                .addAll(game.getStatNames()
                        .stream()
                        .map(game::getStat)
                        .filter(Optional::isPresent)
                        .map(statOptional -> {
                            final JavaStat gameStat = statOptional.get();
                            return Optional.ofNullable(playerStats.getStats().get(gameStat.getName()))
                                    .map(stat -> {
                                        highestUnixTime.tryNumber(stat.getUnix());

                                        final String position = stat.getPosition() == -1 ? AbstractStatsCommand.UNKNOWN_POSITION : String.valueOf(stat.getPosition());
                                        return new String[]{gameStat.getPrintName(), this.getFormattedScore(gameStat, stat.getScore()), position};
                                    })
                                    .orElse(new String[]{gameStat.getPrintName(), AbstractStatsCommand.UNKNOWN_SCORE, AbstractStatsCommand.UNKNOWN_POSITION});
                        }))
                .build()
                .toArray(new String[0][3]);

        BufferedImage skin;
        try {
            skin = skinFuture.get();
        } catch (final InterruptedException | ExecutionException e) {
            skin = null;
        }

        final String[] header = this.getHeader(playerStatsInfo);
        return this.sendPicture(
                commandParameters,
                new PictureTable(header, this.getFormattedUnixTime(highestUnixTime.get()), leaderboard, skin).getPlayerPicture(),
                String.join("-", header) + "-" + highestUnixTime
        );
    }
}
