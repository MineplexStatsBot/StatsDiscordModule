package de.timmi6790.mineplex_stats.commands.java;

import de.timmi6790.commons.builders.ListBuilder;
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

import java.util.Map;
import java.util.stream.Collectors;

@EqualsAndHashCode(callSuper = true)
@Setter
public abstract class AbstractJavaLeaderboardCommand extends AbstractJavaStatsCommand {
    private static final int ARG_POS_BOARD_POS = 2;
    private static final int ARG_POS_START_POS = 3;
    private static final int ARG_POS_END_POS = 4;

    private static final int LEADERBOARD_UPPER_LIMIT = 1_000;

    private boolean filteredStats = true;
    private int leaderboardRowDistance = 15;

    public AbstractJavaLeaderboardCommand(final String name, final String description, final String... aliasNames) {
        super(name, description, "<game> <stat> [board] [start] [end] [date]", aliasNames);

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

    protected Map<String, AbstractEmoteReaction> getCustomEmotes(final CommandParameters commandParameters, final JavaLeaderboard javaLeaderboard,
                                                                 final int startPos, final int endPos) {
        final int fastRowDistance = javaLeaderboard.getInfo().getTotalLength() * 10 / 100;

        return this.getLeaderboardEmotes(commandParameters, fastRowDistance, startPos, endPos,
                javaLeaderboard.getInfo().getTotalLength(), ARG_POS_START_POS, ARG_POS_END_POS);
    }

    protected String[] getHeader(final JavaLeaderboard.Info leaderboardInfo) {
        if (this.filteredStats) {
            return new String[]{leaderboardInfo.getGame(), leaderboardInfo.getStat(), leaderboardInfo.getBoard()};
        } else {
            return new String[]{leaderboardInfo.getGame(), leaderboardInfo.getStat(), leaderboardInfo.getBoard(), "UNFILTERED"};
        }
    }

    protected String[][] parseLeaderboard(final JavaStat stat, final JavaLeaderboard leaderboardResponse) {
        return ListBuilder.<String[]>ofArrayList(leaderboardResponse.getLeaderboard().size() + 1)
                .add(new String[]{"Player", "Score", "Position"})
                .addAll(leaderboardResponse.getLeaderboard()
                        .stream()
                        .map(data -> new String[]{data.getName(), this.getFormattedScore(stat, data.getScore()), String.valueOf(data.getPosition())})
                        .collect(Collectors.toList()))
                .build()
                .toArray(new String[0][3]);
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
        return this.sendPicture(
                this.getLeaderboardFixedCommandParameter(commandParameters, ARG_POS_END_POS, ARG_POS_START_POS),
                new PictureTable(header, this.getFormattedUnixTime(leaderboardInfo.getUnix()), leaderboard).getPlayerPicture(),
                String.format("%s-%s", String.join("-", header), leaderboardInfo.getUnix()),
                new EmoteReactionMessage(
                        this.getCustomEmotes(commandParameters, leaderboardResponse, startPos, endPos),
                        commandParameters.getUser().getIdLong(),
                        commandParameters.getLowestMessageChannel().getIdLong()
                )
        );
    }
}
