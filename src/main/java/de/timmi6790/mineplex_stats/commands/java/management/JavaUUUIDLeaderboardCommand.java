package de.timmi6790.mineplex_stats.commands.java.management;

import de.timmi6790.commons.builders.ListBuilder;
import de.timmi6790.discord_framework.modules.command.CommandCause;
import de.timmi6790.discord_framework.modules.command.CommandParameters;
import de.timmi6790.discord_framework.modules.emote_reaction.emotereactions.AbstractEmoteReaction;
import de.timmi6790.discord_framework.modules.emote_reaction.emotereactions.CommandEmoteReaction;
import de.timmi6790.discord_framework.utilities.discord.DiscordEmotes;
import de.timmi6790.mineplex_stats.commands.java.AbstractJavaLeaderboardCommand;
import de.timmi6790.mineplex_stats.statsapi.models.java.JavaLeaderboard;
import de.timmi6790.mineplex_stats.statsapi.models.java.JavaStat;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class JavaUUUIDLeaderboardCommand extends AbstractJavaLeaderboardCommand {
    public JavaUUUIDLeaderboardCommand() {
        super("uuidLeaderboard", "Java UUID Leaderboard", "ul");

        this.setLeaderboardRowDistance(5);

        this.setCategory("MineplexStats - Java - Management");
    }

    @Override
    protected String[][] parseLeaderboard(final JavaStat stat, final JavaLeaderboard leaderboardResponse) {
        final AtomicInteger index = new AtomicInteger(1);
        return ListBuilder.<String[]>ofArrayList(leaderboardResponse.getLeaderboard().size() + 1)
                .add(new String[]{"Emote", "UUID", "Player", "Score", "Position"})
                .addAll(leaderboardResponse.getLeaderboard()
                        .stream()
                        .map(data -> new String[]{String.valueOf(index.getAndIncrement()), data.getUuid().toString(), data.getName(),
                                this.getFormattedScore(stat, data.getScore()), String.valueOf(data.getPosition())}))
                .build()
                .toArray(new String[0][3]);
    }

    @Override
    protected String[] getHeader(final JavaLeaderboard.Info leaderboardInfo) {
        return new String[]{leaderboardInfo.getGame(), leaderboardInfo.getStat(), leaderboardInfo.getBoard()};
    }


    @Override
    protected Map<String, AbstractEmoteReaction> getCustomEmotes(final CommandParameters commandParameters, final JavaLeaderboard javaLeaderboard,
                                                                 final int startPos, final int endPos) {
        final Map<String, AbstractEmoteReaction> emotes = new LinkedHashMap<>();

        final JavaLeaderboard.Info leaderboardInfo = javaLeaderboard.getInfo();
        this.getCommandModule()
                .getCommand(JavaPlayerFilterCommand.class)
                .ifPresent(filterCommand -> {
                    final AtomicInteger emoteIndex = new AtomicInteger(1);
                    javaLeaderboard.getLeaderboard().forEach(data -> {
                        final CommandParameters newParameters = CommandParameters.of(
                                commandParameters,
                                CommandCause.EMOTES,
                                data.getUuid().toString(),
                                leaderboardInfo.getGame(),
                                leaderboardInfo.getStat(),
                                leaderboardInfo.getBoard()
                        );
                        emotes.put(DiscordEmotes.getNumberEmote(emoteIndex.getAndIncrement()).getEmote(), new CommandEmoteReaction(filterCommand, newParameters));
                    });
                });

        emotes.putAll(super.getCustomEmotes(commandParameters, javaLeaderboard, startPos, endPos));
        return emotes;
    }
}
