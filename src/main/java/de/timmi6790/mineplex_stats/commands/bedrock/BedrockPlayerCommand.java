package de.timmi6790.mineplex_stats.commands.bedrock;

import de.timmi6790.discord_framework.modules.command.CommandParameters;
import de.timmi6790.discord_framework.modules.command.CommandResult;
import de.timmi6790.discord_framework.modules.command.property.properties.ExampleCommandsCommandProperty;
import de.timmi6790.discord_framework.modules.command.property.properties.MinArgCommandProperty;
import de.timmi6790.mineplex_stats.picture.PictureTable;
import de.timmi6790.mineplex_stats.statsapi.models.ResponseModel;
import de.timmi6790.mineplex_stats.statsapi.models.bedrock.BedrockPlayerStats;
import de.timmi6790.mineplex_stats.utilities.BiggestLong;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class BedrockPlayerCommand extends AbstractBedrockStatsCommand {
    private static final String[] LIST_HEADER = new String[]{"Game", "Score", "Position"};

    public BedrockPlayerCommand() {
        super("bplayer", "Bedrock player stats", "<player>", "bpl");

        this.addProperties(
                new MinArgCommandProperty(1),
                new ExampleCommandsCommandProperty(
                        "xottic7"
                )
        );
    }

    @Override
    protected CommandResult onCommand(final CommandParameters commandParameters) {
        // Arg parse
        final String player = this.getPlayer(commandParameters, 0);

        final ResponseModel responseModel = this.getMineplexStatsModule()
                .getMpStatsRestClient()
                .getBedrockPlayerStats(player);
        this.checkApiResponseThrow(commandParameters, responseModel, "No stats available");

        final BedrockPlayerStats bedrockStats = (BedrockPlayerStats) responseModel;
        final BedrockPlayerStats.Info playerStatsInfo = bedrockStats.getInfo();
        final Map<String, BedrockPlayerStats.Stats> playerStats = bedrockStats.getStats();

        // Parse board data
        final BiggestLong highestUnixTime = new BiggestLong();
        final List<String[]> leaderboard = new ArrayList<>(bedrockStats.getStats().size() + 1);
        leaderboard.add(LIST_HEADER);

        final List<String> sortedGames = new ArrayList<>(bedrockStats.getStats().keySet());
        sortedGames.sort(Comparator.naturalOrder());

        for (final String game : sortedGames) {
            final BedrockPlayerStats.Stats playerStat = playerStats.get(game);

            highestUnixTime.tryNumber(playerStat.getUnix());
            leaderboard.add(new String[]{
                    game,
                    this.getFormattedNumber(playerStat.getScore()),
                    String.valueOf(playerStat.getPosition())
            });
        }

        // Create and send leaderboard
        final String[] header = {playerStatsInfo.getName() + " Bedrock"};
        return this.sendPicture(
                commandParameters,
                new PictureTable(
                        header,
                        this.getFormattedUnixTime(highestUnixTime.get()),
                        leaderboard.toArray(new String[0][3])
                ).getPlayerPicture(),
                String.join("-", header) + "-" + highestUnixTime
        );
    }
}
