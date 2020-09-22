package de.timmi6790.mineplex_stats.commands.bedrock.management;

import de.timmi6790.commons.builders.ListBuilder;
import de.timmi6790.discord_framework.modules.command.CommandModule;
import de.timmi6790.discord_framework.modules.command.CommandParameters;
import de.timmi6790.discord_framework.modules.emote_reaction.emotereactions.AbstractEmoteReaction;
import de.timmi6790.discord_framework.modules.emote_reaction.emotereactions.CommandEmoteReaction;
import de.timmi6790.discord_framework.utilities.discord.DiscordEmotes;
import de.timmi6790.mineplex_stats.commands.bedrock.AbstractBedrockLeaderboardCommand;
import de.timmi6790.mineplex_stats.statsapi.models.bedrock.BedrockLeaderboard;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class BedrockFilterLeaderboardCommand extends AbstractBedrockLeaderboardCommand {
    public BedrockFilterLeaderboardCommand() {
        super("bfleaderboard", "Bedrock Filter Leaderboard", "<game> [start] [end] [date]", "bfl", "bflb");

        this.setLeaderboardRowDistance(5);
    }

    @Override
    protected String[][] parseLeaderboard(final List<BedrockLeaderboard.Leaderboard> leaderboardResponse) {
        final AtomicInteger index = new AtomicInteger(1);
        return ListBuilder.<String[]>ofArrayList(leaderboardResponse.size() + 1)
                .add(new String[]{"Emote", "Player", "Score", "Position"})
                .addAll(leaderboardResponse
                        .stream()
                        .map(data -> new String[]{String.valueOf(index.getAndIncrement()), data.getName(), String.valueOf(data.getScore()), String.valueOf(data.getPosition())}))
                .build()
                .toArray(new String[0][3]);
    }

    @Override
    protected Map<String, AbstractEmoteReaction> getEmotes(final CommandParameters commandParameters, final BedrockLeaderboard leaderboard, final int startPos, final int endPos) {
        final Map<String, AbstractEmoteReaction> emotes = new LinkedHashMap<>();

        this.getModule().getModuleOrThrow(CommandModule.class).getCommand(BedrockPlayerFilterCommand.class).ifPresent(filterCommand -> {
            final AtomicInteger emoteIndex = new AtomicInteger(1);
            for (final BedrockLeaderboard.Leaderboard data : leaderboard.getLeaderboard()) {
                final CommandParameters newParameters = new CommandParameters(commandParameters, leaderboard.getInfo().getGame(), data.getName());
                emotes.put(DiscordEmotes.getNumberEmote(emoteIndex.getAndIncrement()).getEmote(), new CommandEmoteReaction(filterCommand, newParameters));
            }
        });

        emotes.putAll(super.getEmotes(commandParameters, leaderboard, startPos, endPos));
        return emotes;
    }
}

