package de.timmi6790.mineplex_stats.commands.java.player;

import de.timmi6790.discord_framework.modules.command.CommandParameters;
import de.timmi6790.discord_framework.modules.command.CommandResult;
import de.timmi6790.discord_framework.modules.command.property.properties.ExampleCommandsCommandProperty;
import de.timmi6790.discord_framework.modules.command.property.properties.MinArgCommandProperty;
import de.timmi6790.mineplex_stats.commands.java.AbstractJavaStatsCommand;
import de.timmi6790.mineplex_stats.picture.PictureTable;
import de.timmi6790.mineplex_stats.statsapi.models.ResponseModel;
import de.timmi6790.mineplex_stats.statsapi.models.java.*;
import de.timmi6790.mineplex_stats.utilities.BiggestLong;
import lombok.Data;
import lombok.SneakyThrows;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class JavaPlayerGroupCommand extends AbstractJavaStatsCommand {
    private static final String[] LEADERBOARD_HEADER = new String[]{"Game", "Score", "Position"};

    public JavaPlayerGroupCommand() {
        super("gplayer", "Java player group stats", "<player> <group> <stat> [board] [date]", "gpl");

        this.setCategory("MineplexStats - Java - Group");

        this.addProperties(
                new MinArgCommandProperty(3),
                new ExampleCommandsCommandProperty(
                        "nwang888 MixedArcade wins",
                        "nwang888 MixedArcade wins yearly",
                        "nwang888 MixedArcade wins global 1/30/2020"
                )
        );
    }

    private LeaderboardData parseLeaderboard(final JavaGroupsPlayer groupStats,
                                             final JavaStat stat,
                                             final List<JavaGame> statSpecificGames) {
        final JavaGroupsPlayer.Info playerStatsInfo = groupStats.getInfo();

        final BiggestLong highestUnixTime = new BiggestLong();
        final List<String[]> leaderboard = new ArrayList<>(statSpecificGames.size() + 1);
        leaderboard.add(LEADERBOARD_HEADER);
        for (final JavaGame game : statSpecificGames) {
            final JavaGroupsPlayer.Stats playerStat = groupStats.getStats().get(game.getName());
            if (playerStat == null) {
                leaderboard.add(new String[]{
                        game.getName(),
                        UNKNOWN_SCORE,
                        UNKNOWN_POSITION
                });
            } else {
                highestUnixTime.tryNumber(playerStat.getUnix());
                leaderboard.add(new String[]{
                        game.getName(),
                        this.getFormattedScore(stat, playerStat.getScore()),
                        String.valueOf(playerStat.getPosition())
                });
            }
        }

        final String[] header = {
                playerStatsInfo.getName(),
                playerStatsInfo.getGroup(),
                playerStatsInfo.getStat(),
                playerStatsInfo.getBoard()
        };

        return new LeaderboardData(
                leaderboard.toArray(new String[0][0]),
                header,
                highestUnixTime.get()
        );
    }

    @SneakyThrows
    @Override
    protected CommandResult onCommand(final CommandParameters commandParameters) {
        // Parse input
        final UUID playerUUID = this.getPlayerUUIDFromNameThrow(commandParameters, 0);
        final JavaGroup javaGroup = this.getJavaGroup(commandParameters, 1);
        final JavaStat stat = this.getJavaStat(javaGroup, commandParameters, 2);

        // TODO: Fix me: Possible bug when not all game stats have the same boards
        final List<JavaGame> statSpecificGames = javaGroup.getGames(stat);
        final JavaBoard board = this.getBoard(statSpecificGames.get(0), stat, commandParameters, 3);
        final long unixTime = this.getUnixTimeThrow(commandParameters, 4);

        final ResponseModel responseModel = this.getMineplexStatsModule()
                .getMpStatsRestClient()
                .getPlayerGroup(playerUUID, javaGroup.getGroup(), stat.getName(), board.getName(), unixTime);
        this.checkApiResponseThrow(commandParameters, responseModel, "No stats available");

        // Parse data
        final JavaGroupsPlayer groupStats = (JavaGroupsPlayer) responseModel;
        final CompletableFuture<BufferedImage> skinFuture = this.getPlayerSkin(groupStats.getInfo().getUuid());
        final LeaderboardData leaderboardData = this.parseLeaderboard(groupStats, stat, statSpecificGames);
        final BufferedImage skin = this.awaitOrDefault(skinFuture, null);

        return this.sendPicture(
                commandParameters,
                new PictureTable(
                        leaderboardData.getHeader(),
                        this.getFormattedUnixTime(leaderboardData.getHighestUnixTime()),
                        leaderboardData.getLeaderboard(),
                        skin
                ).getPlayerPicture(),
                String.join("-", leaderboardData.getHeader()) + "-" + leaderboardData.getHighestUnixTime()
        );
    }

    @Data
    private static class LeaderboardData {
        private final String[][] leaderboard;
        private final String[] header;
        private final long highestUnixTime;
    }
}
