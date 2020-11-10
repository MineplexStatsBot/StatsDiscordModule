package de.timmi6790.mineplex_stats.commands.java;

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
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@EqualsAndHashCode(callSuper = true)
public abstract class AbstractJavaPlayerStatsCommand extends AbstractJavaStatsCommand {
    private final boolean filteredStats;

    protected AbstractJavaPlayerStatsCommand(final boolean filteredStats, final String name, final String description, final String... aliasNames) {
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
        final ResponseModel responseModel = this.getMineplexStatsModule()
                .getMpStatsRestClient()
                .getJavaPlayerStats(playerUUID, playerName, javaGame.getName(), board.getName(), unixTime, this.filteredStats);
        this.checkApiResponseThrow(commandParameters, responseModel, "No stats available");

        final JavaPlayerStats playerStats = (JavaPlayerStats) responseModel;
        final JavaPlayerStats.Info playerStatsInfo = playerStats.getInfo();

        final CompletableFuture<BufferedImage> skinFuture = this.getPlayerSkin(playerStatsInfo.getUuid());

        // Parse data into image generator
        final JavaGame game = this.getMineplexStatsModule().getJavaGame(playerStatsInfo.getGame()).orElseThrow(RuntimeException::new);
        final BiggestLong highestUnixTime = new BiggestLong(0);

        final List<String[]> leaderboard = new ArrayList<>(playerStats.getWebsiteStats().size() + game.getStats().size() + 1);
        leaderboard.add(new String[]{"Category", "Score", "Position"});

        // Website stats
        for (final JavaPlayerStats.WebsiteStat websiteStat : playerStats.getWebsiteStats().values()) {
            leaderboard.add(new String[]{websiteStat.getStat(), this.getFormattedNumber(websiteStat.getScore()), ""});
        }

        // Leaderboard stats
        for (final String statName : game.getStatNames()) {
            final Optional<JavaStat> statOpt = game.getStat(statName);
            if (!statOpt.isPresent()) {
                continue;
            }

            final JavaStat stat = statOpt.get();
            final JavaPlayerStats.Stat foundStat = playerStats.getStats().get(stat.getName());
            if (foundStat != null) {
                highestUnixTime.tryNumber(foundStat.getUnix());

                final String position = foundStat.getPosition() == -1 ? AbstractStatsCommand.UNKNOWN_POSITION : String.valueOf(foundStat.getPosition());
                leaderboard.add(new String[]{stat.getPrintName(), this.getFormattedScore(stat, foundStat.getScore()), position});
            } else {
                leaderboard.add(new String[]{stat.getPrintName(), AbstractStatsCommand.UNKNOWN_SCORE, AbstractStatsCommand.UNKNOWN_POSITION});
            }
        }

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
                new PictureTable(
                        header,
                        this.getFormattedUnixTime(highestTime),
                        leaderboard.toArray(new String[0][0]),
                        skin
                ).getPlayerPicture(),
                String.join("-", header) + "-" + highestUnixTime
        );
    }
}
