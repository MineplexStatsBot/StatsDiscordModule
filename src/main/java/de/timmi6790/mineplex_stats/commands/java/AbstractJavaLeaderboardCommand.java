package de.timmi6790.mineplex_stats.commands.java;

import de.timmi6790.discord_framework.modules.command.CommandParameters;
import de.timmi6790.discord_framework.modules.command.CommandResult;
import de.timmi6790.discord_framework.modules.command.property.properties.ExampleCommandsCommandProperty;
import de.timmi6790.discord_framework.modules.command.property.properties.MinArgCommandProperty;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Setter
public abstract class AbstractJavaLeaderboardCommand extends AbstractJavaStatsCommand {
    private static final int ARG_POS_BOARD = 2;
    private static final int ARG_POS_START = 3;
    private static final int ARG_POS_END = 4;

    private static final int LEADERBOARD_UPPER_LIMIT = 1_000;

    private boolean filteredStats = true;
    private int leaderboardRowDistance = 15;

    protected AbstractJavaLeaderboardCommand(final String name,
                                             final String description,
                                             final String... aliasNames) {
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

    protected Map<String, AbstractEmoteReaction> getCustomEmotes(final CommandParameters commandParameters,
                                                                 final JavaLeaderboard javaLeaderboard,
                                                                 final int startPos,
                                                                 final int endPos) {
        final int fastRowDistance = javaLeaderboard.getInfo().getTotalLength() * 10 / 100;
        return this.getLeaderboardEmotes(
                commandParameters,
                fastRowDistance,
                startPos,
                endPos,
                javaLeaderboard.getInfo().getTotalLength(),
                ARG_POS_START,
                ARG_POS_END
        );
    }

    protected String[] getHeader(final JavaLeaderboard.Info leaderboardInfo) {
        if (this.filteredStats) {
            return new String[]{
                    leaderboardInfo.getGame(),
                    leaderboardInfo.getStat(),
                    leaderboardInfo.getBoard()
            };
        } else {
            return new String[]{
                    leaderboardInfo.getGame(),
                    leaderboardInfo.getStat(),
                    leaderboardInfo.getBoard(),
                    "UNFILTERED"
            };
        }
    }

    protected String[][] parseLeaderboard(final JavaStat stat, final JavaLeaderboard leaderboardResponse) {
        final List<String[]> parsed = new ArrayList<>(leaderboardResponse.getLeaderboard().size() + 1);
        parsed.add(new String[]{"Player", "Score", "Position"});

        for (final JavaLeaderboard.Leaderboard row : leaderboardResponse.getLeaderboard()) {
            parsed.add(new String[]{
                    row.getName(),
                    this.getFormattedScore(stat, row.getScore()),
                    String.valueOf(row.getPosition())
            });
        }

        return parsed.toArray(new String[0][3]);
    }

    @Override
    protected CommandResult onCommand(final CommandParameters commandParameters) {
        // Parse args
        final JavaGame game = this.getGame(commandParameters, 0);
        final JavaStat stat = this.getStat(game, commandParameters, 1);
        final JavaBoard board = this.getBoard(game, commandParameters, ARG_POS_BOARD);
        final int startPos = this.getStartPositionThrow(commandParameters, ARG_POS_START, LEADERBOARD_UPPER_LIMIT);
        final int endPos = this.getEndPositionThrow(startPos, commandParameters, ARG_POS_END, LEADERBOARD_UPPER_LIMIT, this.leaderboardRowDistance);
        final long unixTime = this.getUnixTimeThrow(commandParameters, 5);

        final ResponseModel responseModel = this.getMineplexStatsModule()
                .getMpStatsRestClient()
                .getJavaLeaderboard(game.getName(), stat.getName(), board.getName(), startPos, endPos, unixTime, this.filteredStats);
        this.checkApiResponseThrow(commandParameters, responseModel, "No stats available");

        // Parse data to image generator
        final JavaLeaderboard leaderboardResponse = (JavaLeaderboard) responseModel;
        final String[][] leaderboard = this.parseLeaderboard(stat, leaderboardResponse);

        final JavaLeaderboard.Info leaderboardInfo = leaderboardResponse.getInfo();

        final String[] header = this.getHeader(leaderboardInfo);
        return this.sendPicture(
                this.getLeaderboardFixedCommandParameter(commandParameters, ARG_POS_END, ARG_POS_START),
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
