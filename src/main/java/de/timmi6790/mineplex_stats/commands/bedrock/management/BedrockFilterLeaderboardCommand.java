package de.timmi6790.mineplex_stats.commands.bedrock.management;

import de.timmi6790.discord_framework.modules.command.CommandParameters;
import de.timmi6790.discord_framework.modules.emote_reaction.emotereactions.AbstractEmoteReaction;
import de.timmi6790.discord_framework.modules.emote_reaction.emotereactions.CommandEmoteReaction;
import de.timmi6790.discord_framework.utilities.discord.DiscordEmotes;
import de.timmi6790.mineplex_stats.commands.bedrock.AbstractBedrockLeaderboardCommand;
import de.timmi6790.mineplex_stats.statsapi.models.bedrock.BedrockLeaderboard;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BedrockFilterLeaderboardCommand extends AbstractBedrockLeaderboardCommand {
    private static final String[] LIST_HEADER = new String[]{"Emote", "Player", "Score", "Position"};

    public BedrockFilterLeaderboardCommand() {
        super("bfleaderboard", "Bedrock Filter Leaderboard", "bfl", "bflb");

        this.setLeaderboardRowDistance(5);
    }

    @Override
    protected String[][] parseLeaderboard(final List<BedrockLeaderboard.Leaderboard> leaderboardResponse) {
        final List<String[]> parsed = new ArrayList<>(leaderboardResponse.size() + 1);
        parsed.add(LIST_HEADER);

        int emoteIndex = 1;
        for (final BedrockLeaderboard.Leaderboard data : leaderboardResponse) {
            parsed.add(new String[]{
                    String.valueOf(emoteIndex),
                    data.getName(),
                    String.valueOf(data.getScore()),
                    String.valueOf(data.getPosition())
            });
            emoteIndex++;
        }

        return parsed.toArray(new String[0][0]);
    }

    @Override
    protected Map<String, AbstractEmoteReaction> getEmotes(final CommandParameters commandParameters,
                                                           final BedrockLeaderboard leaderboard,
                                                           final int startPos,
                                                           final int endPos) {
        final Map<String, AbstractEmoteReaction> emotes = new LinkedHashMap<>();

        getCommandModule()
                .getCommand(BedrockPlayerFilterCommand.class)
                .ifPresent(filterCommand -> {
                    int emoteIndex = 1;
                    for (final BedrockLeaderboard.Leaderboard data : leaderboard.getLeaderboard()) {
                        final CommandParameters newParameters = CommandParameters.of(
                                commandParameters,
                                leaderboard.getInfo().getGame(),
                                data.getName()
                        );
                        emotes.put(
                                DiscordEmotes.getNumberEmote(emoteIndex++).getEmote(),
                                new CommandEmoteReaction(filterCommand, newParameters)
                        );
                    }
                });

        emotes.putAll(super.getEmotes(commandParameters, leaderboard, startPos, endPos));
        return emotes;
    }
}

