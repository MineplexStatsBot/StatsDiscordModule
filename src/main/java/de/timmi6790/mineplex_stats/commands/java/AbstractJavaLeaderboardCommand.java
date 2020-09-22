package de.timmi6790.mineplex_stats.commands.java;

import de.timmi6790.discord_framework.modules.command.CommandParameters;
import de.timmi6790.discord_framework.modules.command.CommandResult;
import de.timmi6790.discord_framework.modules.command.properties.ExampleCommandsCommandProperty;
import de.timmi6790.discord_framework.modules.command.properties.MinArgCommandProperty;
import de.timmi6790.discord_framework.modules.emote_reaction.EmoteReactionMessage;
import de.timmi6790.discord_framework.modules.emote_reaction.emotereactions.AbstractEmoteReaction;
import de.timmi6790.mineplex_stats.picture.PictureTable;
import de.timmi6790.mineplex_stats.statsapi.models.ResponseModel;
import de.timmi6790.mineplex_stats.statsapi.models.java.JavaBoard;
import de.timmi6790.mineplex_stats.statsapi.models.java.JavaGame;
import de.timmi6790.mineplex_stats.statsapi.models.java.JavaLeaderboard;
import de.timmi6790.mineplex_stats.statsapi.models.java.JavaStat;
import lombok.EqualsAndHashCode;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Setter
public abstract class AbstractJavaLeaderboardCommand extends AbstractJavaStatsCommand {
    private static final int ARG_POS_BOARD_POS = 2;
    private static final int ARG_POS_START_POS = 3;
    private static final int ARG_POS_END_POS = 4;

    private static final int LEADERBOARD_UPPER_LIMIT = 1_000;

    private final boolean filteredStats;
    private int leaderboardRowDistance = 15;

    public AbstractJavaLeaderboardCommand(final boolean filteredStats, final String name, final String description, final String syntax, final String... aliasNames) {
        super(name, description, syntax, aliasNames);

        this.filteredStats = filteredStats;

        this.addProperties(
                new MinArgCommandProperty(2),
                new ExampleCommandsCommandProperty(
                        "Global ExpEarned",
                        "Global ExpEarned daily",
                        "Global ExpEarned global 20 40",
                        "Global ExpEarned global 20 40 1/30/2020"
                )
        );
    }

    protected abstract String[][] parseLeaderboard(JavaStat stat, JavaLeaderboard leaderboardResponse);

    protected abstract String[] getHeader(JavaLeaderboard.Info leaderboardInfo);

    protected Map<String, AbstractEmoteReaction> getCustomEmotes(final CommandParameters commandParameters, final JavaLeaderboard javaLeaderboard) {
        return new HashMap<>();
    }

    @Override
    protected CommandResult onCommand(final CommandParameters commandParameters) {
        // Parse args
        final JavaGame game = this.getGame(commandParameters, 0);
        final JavaStat stat = this.getStat(game, commandParameters, 1);
        final JavaBoard board = this.getBoard(game, commandParameters, ARG_POS_BOARD_POS);
        final int startPos = this.getStartPositionThrow(commandParameters, ARG_POS_START_POS, LEADERBOARD_UPPER_LIMIT);
        final int endPos = this.getEndPositionThrow(startPos, commandParameters, ARG_POS_END_POS, LEADERBOARD_UPPER_LIMIT, this.leaderboardRowDistance);
        final long unixTime = this.getUnixTimeThrow(commandParameters, 5);

        final ResponseModel responseModel = this.getModule().getMpStatsRestClient().getJavaLeaderboard(game.getName(), stat.getName(), board.getName(), startPos, endPos, unixTime, this.filteredStats);
        this.checkApiResponseThrow(commandParameters, responseModel, "No stats available");

        // Parse data to image generator
        final JavaLeaderboard leaderboardResponse = (JavaLeaderboard) responseModel;
        final String[][] leaderboard = this.parseLeaderboard(stat, leaderboardResponse);

        final JavaLeaderboard.Info leaderboardInfo = leaderboardResponse.getInfo();
        final String[] header = this.getHeader(leaderboardInfo);

        // Emote Reaction
        final int rowDistance = endPos - startPos;
        final int fastRowDistance = leaderboardInfo.getTotalLength() * 10 / 100;

        // Create a new args array if the old array has no positions
        final CommandParameters newCommandParameters;
        if (Math.max(ARG_POS_END_POS, ARG_POS_START_POS) + 1 > commandParameters.getArgs().length) {
            final String[] newArgs = new String[Math.max(ARG_POS_END_POS, ARG_POS_START_POS) + 1];
            System.arraycopy(commandParameters.getArgs(), 0, newArgs, 0, commandParameters.getArgs().length);
            newCommandParameters = new CommandParameters(commandParameters, newArgs);
        } else {
            newCommandParameters = commandParameters;
        }

        final Map<String, AbstractEmoteReaction> emotes = this.getLeaderboardEmotes(commandParameters, rowDistance, fastRowDistance, startPos, endPos,
                leaderboardInfo.getTotalLength(), ARG_POS_START_POS, ARG_POS_END_POS);
        emotes.putAll(this.getCustomEmotes(commandParameters, leaderboardResponse));

        return this.sendPicture(
                newCommandParameters,
                new PictureTable(header, this.getFormattedUnixTime(leaderboardInfo.getUnix()), leaderboard).getPlayerPicture(),
                String.format("%s-%s", String.join("-", header), leaderboardInfo.getUnix()),
                new EmoteReactionMessage(
                        emotes,
                        commandParameters.getUser().getIdLong(),
                        commandParameters.getLowestMessageChannel().getIdLong()
                )
        );
    }
}
