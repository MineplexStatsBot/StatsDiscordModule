package de.timmi6790.mineplex_stats.commands.bedrock;

import de.timmi6790.discord_framework.modules.command.CommandParameters;
import de.timmi6790.discord_framework.modules.command.CommandResult;
import de.timmi6790.discord_framework.modules.emote_reaction.EmoteReactionMessage;
import de.timmi6790.discord_framework.modules.emote_reaction.emotereactions.AbstractEmoteReaction;
import de.timmi6790.mineplex_stats.picture.PictureTable;
import de.timmi6790.mineplex_stats.statsapi.models.ResponseModel;
import de.timmi6790.mineplex_stats.statsapi.models.bedrock.BedrockGame;
import de.timmi6790.mineplex_stats.statsapi.models.bedrock.BedrockLeaderboard;
import lombok.EqualsAndHashCode;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Setter
@EqualsAndHashCode(callSuper = true)
public abstract class AbstractBedrockLeaderboardCommand extends AbstractBedrockStatsCommand {
    private static final int ARG_POS_START_POS = 1;
    private static final int ARG_POS_END_POS = 2;

    private static final int LEADERBOARD_UPPER_LIMIT = 100;

    private int leaderboardRowDistance = 15;

    public AbstractBedrockLeaderboardCommand(final String name, final String description, final String syntax, final String... aliasNames) {
        super(name, description, syntax, aliasNames);
    }

    protected abstract String[][] parseLeaderboard(List<BedrockLeaderboard.Leaderboard> leaderboardResponse);

    protected abstract String[] getHeader(BedrockLeaderboard.Info leaderboardInfo);

    protected Map<String, AbstractEmoteReaction> getCustomEmotes(final CommandParameters commandParameters, final BedrockLeaderboard leaderboard) {
        return new HashMap<>();
    }

    @Override
    protected CommandResult onCommand(final CommandParameters commandParameters) {
        // Parse args
        final BedrockGame game = this.getGame(commandParameters, 0);
        final int startPos = this.getStartPositionThrow(commandParameters, ARG_POS_START_POS, LEADERBOARD_UPPER_LIMIT);
        final int endPos = this.getEndPositionThrow(startPos, commandParameters, ARG_POS_END_POS, LEADERBOARD_UPPER_LIMIT, this.leaderboardRowDistance);
        final long unixTime = this.getUnixTimeThrow(commandParameters, 3);

        final ResponseModel responseModel = this.getModule().getMpStatsRestClient().getBedrockLeaderboard(game.getName(), startPos, endPos, unixTime);
        this.checkApiResponseThrow(commandParameters, responseModel, "No stats available");

        final BedrockLeaderboard bedrockLeaderboard = (BedrockLeaderboard) responseModel;
        // Parse the data into the image maker format
        final String[][] leaderboard = this.parseLeaderboard(bedrockLeaderboard.getLeaderboard());

        final BedrockLeaderboard.Info leaderboardInfo = bedrockLeaderboard.getInfo();

        final String[] header = this.getHeader(leaderboardInfo);

        // Emotes
        final int rowDistance = endPos - startPos;
        final int fastRowDistance = leaderboardInfo.getTotalLength() * 50 / 100;

        final CommandParameters newCommandParameters;
        if (Math.max(ARG_POS_END_POS, ARG_POS_START_POS) + 1 > commandParameters.getArgs().length) {
            final String[] newArgs = new String[Math.max(ARG_POS_END_POS, ARG_POS_START_POS) + 1];
            System.arraycopy(commandParameters.getArgs(), 0, newArgs, 0, commandParameters.getArgs().length);
            newCommandParameters = new CommandParameters(commandParameters, newArgs);
        } else {
            newCommandParameters = commandParameters;
        }

        final Map<String, AbstractEmoteReaction> emotes = this.getCustomEmotes(commandParameters, bedrockLeaderboard);
        emotes.putAll(this.getLeaderboardEmotes(commandParameters, rowDistance, fastRowDistance, startPos, endPos, leaderboardInfo.getTotalLength(),
                ARG_POS_START_POS, ARG_POS_END_POS));

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
