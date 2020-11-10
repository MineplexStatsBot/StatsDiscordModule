package de.timmi6790.mineplex_stats.commands.bedrock;

import de.timmi6790.discord_framework.modules.command.CommandParameters;
import de.timmi6790.discord_framework.modules.command.CommandResult;
import de.timmi6790.discord_framework.modules.command.property.properties.ExampleCommandsCommandProperty;
import de.timmi6790.discord_framework.modules.command.property.properties.MinArgCommandProperty;
import de.timmi6790.discord_framework.modules.emote_reaction.EmoteReactionMessage;
import de.timmi6790.discord_framework.modules.emote_reaction.emotereactions.AbstractEmoteReaction;
import de.timmi6790.mineplex_stats.picture.PictureTable;
import de.timmi6790.mineplex_stats.statsapi.models.ResponseModel;
import de.timmi6790.mineplex_stats.statsapi.models.bedrock.BedrockGame;
import de.timmi6790.mineplex_stats.statsapi.models.bedrock.BedrockLeaderboard;
import lombok.EqualsAndHashCode;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Setter
@EqualsAndHashCode(callSuper = true)
public abstract class AbstractBedrockLeaderboardCommand extends AbstractBedrockStatsCommand {
    private static final int ARG_POS_START_POS = 1;
    private static final int ARG_POS_END_POS = 2;

    private static final int LEADERBOARD_UPPER_LIMIT = 100;

    private int leaderboardRowDistance = 15;

    protected AbstractBedrockLeaderboardCommand(final String name, final String description, final String... aliasNames) {
        super(name, description, "<game> [start] [end] [date]", aliasNames);

        this.addProperties(
                new MinArgCommandProperty(1),
                new ExampleCommandsCommandProperty(
                        "CakeWars",
                        "CakeWars daily",
                        "CakeWars global 20 40",
                        "CakeWars global 20 40 1/30/2020"
                )
        );
    }

    protected abstract String[][] parseLeaderboard(List<BedrockLeaderboard.Leaderboard> leaderboardResponse);

    protected String[] getHeader(final BedrockLeaderboard.Info leaderboardInfo) {
        return new String[]{"Bedrock " + leaderboardInfo.getGame()};
    }

    protected Map<String, AbstractEmoteReaction> getEmotes(final CommandParameters commandParameters,
                                                           final BedrockLeaderboard leaderboard,
                                                           final int startPos,
                                                           final int endPos) {
        final int fastRowDistance = leaderboard.getInfo().getTotalLength() * 50 / 100;
        return this.getLeaderboardEmotes(
                commandParameters,
                fastRowDistance,
                startPos,
                endPos,
                leaderboard.getInfo().getTotalLength(),
                ARG_POS_START_POS,
                ARG_POS_END_POS
        );
    }

    @Override
    protected CommandResult onCommand(final CommandParameters commandParameters) {
        // Parse args
        final BedrockGame game = this.getGame(commandParameters, 0);
        final int startPos = this.getStartPositionThrow(commandParameters, ARG_POS_START_POS, LEADERBOARD_UPPER_LIMIT);
        final int endPos = this.getEndPositionThrow(startPos, commandParameters, ARG_POS_END_POS, LEADERBOARD_UPPER_LIMIT, this.leaderboardRowDistance);
        final long unixTime = this.getUnixTimeThrow(commandParameters, 3);

        final ResponseModel responseModel = this.getMineplexStatsModule().getMpStatsRestClient()
                .getBedrockLeaderboard(game.getName(), startPos, endPos, unixTime);
        this.checkApiResponseThrow(commandParameters, responseModel, "No stats available");

        final BedrockLeaderboard bedrockLeaderboard = (BedrockLeaderboard) responseModel;
        // Parse the data into the image maker format
        final String[][] leaderboard = this.parseLeaderboard(bedrockLeaderboard.getLeaderboard());

        final BedrockLeaderboard.Info leaderboardInfo = bedrockLeaderboard.getInfo();

        final String[] header = this.getHeader(leaderboardInfo);
        return this.sendPicture(
                this.getLeaderboardFixedCommandParameter(commandParameters, ARG_POS_END_POS, ARG_POS_START_POS),
                new PictureTable(header, this.getFormattedUnixTime(leaderboardInfo.getUnix()), leaderboard).getPlayerPicture(),
                String.format("%s-%s", String.join("-", header), leaderboardInfo.getUnix()),
                new EmoteReactionMessage(
                        this.getEmotes(commandParameters, bedrockLeaderboard, startPos, endPos),
                        commandParameters.getUser().getIdLong(),
                        commandParameters.getLowestMessageChannel().getIdLong()
                )
        );
    }
}
