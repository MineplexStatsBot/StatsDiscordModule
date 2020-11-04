package de.timmi6790.mineplex_stats.commands.java;

import de.timmi6790.commons.builders.ListBuilder;
import de.timmi6790.discord_framework.modules.command.CommandParameters;
import de.timmi6790.discord_framework.modules.command.CommandResult;
import de.timmi6790.discord_framework.modules.command.property.properties.ExampleCommandsCommandProperty;
import de.timmi6790.discord_framework.modules.command.property.properties.MinArgCommandProperty;
import de.timmi6790.discord_framework.modules.command.property.properties.RequiredDiscordBotPermsCommandProperty;
import de.timmi6790.mineplex_stats.commands.AbstractStatsCommand;
import de.timmi6790.mineplex_stats.picture.PictureTable;
import de.timmi6790.mineplex_stats.statsapi.models.ResponseModel;
import de.timmi6790.mineplex_stats.statsapi.models.java.JavaBoard;
import de.timmi6790.mineplex_stats.statsapi.models.java.JavaGame;
import de.timmi6790.mineplex_stats.statsapi.models.java.JavaPlayerStats;
import de.timmi6790.mineplex_stats.statsapi.models.java.JavaStat;
import de.timmi6790.mineplex_stats.statsapi.utilities.BiggestLong;
import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.Permission;

import java.awt.image.BufferedImage;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@EqualsAndHashCode(callSuper = true)
public abstract class AbstractJavaPlayerStatsCommand extends AbstractJavaStatsCommand {
    private final boolean filteredStats;

    public AbstractJavaPlayerStatsCommand(final boolean filteredStats, final String name, final String description, final String... aliasNames) {
        super(name, description, "<player> <game> [board] [date]", aliasNames);

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

    protected String[] getHeader(final JavaPlayerStats.Info playerStatsInfo) {
        if (this.filteredStats) {
            return new String[]{playerStatsInfo.getName(), playerStatsInfo.getGame(), playerStatsInfo.getBoard()};
        } else {
            return new String[]{playerStatsInfo.getName(), playerStatsInfo.getGame(), playerStatsInfo.getBoard(), "UNFILTERED"};
        }
    }

    @SneakyThrows
    @Override
    protected CommandResult onCommand(final CommandParameters commandParameters) {
        // Parse args
        final String playerName = this.getPlayer(commandParameters, 0);
        final UUID playerUUID = this.getPlayerUUIDFromName(commandParameters, 0);
        final JavaGame javaGame = this.getGame(commandParameters, 1);
        final JavaBoard board = this.getBoard(javaGame, commandParameters, 2);
        final long unixTime = this.getUnixTimeThrow(commandParameters, 3);

        // Web Requests
        final ResponseModel responseModel = this.getMineplexStatsModule().getMpStatsRestClient().getJavaPlayerStats(playerUUID, playerName, javaGame.getName(),
                board.getName(), unixTime, this.filteredStats);
        this.checkApiResponseThrow(commandParameters, responseModel, "No stats available");

        final JavaPlayerStats playerStats = (JavaPlayerStats) responseModel;
        final JavaPlayerStats.Info playerStatsInfo = playerStats.getInfo();

        final CompletableFuture<BufferedImage> skinFuture = this.getPlayerSkin(playerStatsInfo.getUuid());

        // Parse data into image generator
        final JavaGame game = this.getMineplexStatsModule().getJavaGame(playerStatsInfo.getGame()).orElseThrow(RuntimeException::new);
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
        } catch (final ExecutionException e) {
            skin = null;
        }

        // This will resolve the issue when we only have website stats and the highest time is 0
        final long highestTime = highestUnixTime.get() == 0 ? Instant.now().getEpochSecond() : highestUnixTime.get();
        final String[] header = this.getHeader(playerStatsInfo);
        return this.sendPicture(
                commandParameters,
                new PictureTable(header, this.getFormattedUnixTime(highestTime), leaderboard, skin).getPlayerPicture(),
                String.join("-", header) + "-" + highestUnixTime
        );
    }
}
