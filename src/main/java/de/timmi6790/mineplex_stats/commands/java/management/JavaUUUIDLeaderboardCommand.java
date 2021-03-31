package de.timmi6790.mineplex_stats.commands.java.management;

import de.timmi6790.discord_framework.module.modules.command.AbstractCommand;
import de.timmi6790.discord_framework.module.modules.command.CommandCause;
import de.timmi6790.discord_framework.module.modules.command.CommandParameters;
import de.timmi6790.discord_framework.module.modules.emote_reaction.emotereactions.AbstractEmoteReaction;
import de.timmi6790.discord_framework.module.modules.emote_reaction.emotereactions.CommandEmoteReaction;
import de.timmi6790.discord_framework.utilities.discord.DiscordEmotes;
import de.timmi6790.mineplex_stats.commands.java.AbstractJavaLeaderboardCommand;
import de.timmi6790.mineplex_stats.statsapi.models.java.JavaLeaderboard;
import de.timmi6790.mineplex_stats.statsapi.models.java.JavaStat;

import java.util.*;

public class JavaUUUIDLeaderboardCommand extends AbstractJavaLeaderboardCommand {
    public JavaUUUIDLeaderboardCommand() {
        super("uuidLeaderboard", "Java UUID Leaderboard", "ul");

        this.setLeaderboardRowDistance(5);

        this.setCategory("MineplexStats - Java - Management");
    }

    @Override
    protected String[][] parseLeaderboard(final JavaStat stat, final JavaLeaderboard leaderboardResponse) {
        final List<String[]> parsed = new ArrayList<>(leaderboardResponse.getLeaderboard().size() + 1);
        parsed.add(new String[]{"Emote", "UUID", "Player", "Score", "Position"});

        int index = 1;
        for (final JavaLeaderboard.Leaderboard data : leaderboardResponse.getLeaderboard()) {
            parsed.add(new String[]{
                    String.valueOf(index),
                    data.getUuid().toString(),
                    data.getName(),
                    this.getFormattedScore(stat, data.getScore()),
                    String.valueOf(data.getPosition())
            });
            index++;
        }

        return parsed.toArray(new String[0][0]);
    }

    @Override
    protected String[] getHeader(final JavaLeaderboard.Info leaderboardInfo) {
        return new String[]{leaderboardInfo.getGame(), leaderboardInfo.getStat(), leaderboardInfo.getBoard()};
    }

    @Override
    protected Map<String, AbstractEmoteReaction> getCustomEmotes(final CommandParameters commandParameters,
                                                                 final JavaLeaderboard javaLeaderboard,
                                                                 final int startPos,
                                                                 final int endPos) {
        final Map<String, AbstractEmoteReaction> emotes = new LinkedHashMap<>();

        final JavaLeaderboard.Info leaderboardInfo = javaLeaderboard.getInfo();
        final Optional<AbstractCommand> filterCommandOpt = this.getCommandModule().getCommand(JavaPlayerFilterCommand.class);
        if (filterCommandOpt.isPresent()) {
            final AbstractCommand filterCommand = filterCommandOpt.get();

            int emoteIndex = 1;
            for (final JavaLeaderboard.Leaderboard data : javaLeaderboard.getLeaderboard()) {
                final CommandParameters newParameters = CommandParameters.of(
                        commandParameters,
                        CommandCause.EMOTES,
                        data.getUuid().toString(),
                        leaderboardInfo.getGame(),
                        leaderboardInfo.getStat(),
                        leaderboardInfo.getBoard()
                );

                emotes.put(
                        DiscordEmotes.getNumberEmote(emoteIndex).getEmote(),
                        new CommandEmoteReaction(filterCommand, newParameters)
                );

                emoteIndex++;
            }
        }

        emotes.putAll(super.getCustomEmotes(commandParameters, javaLeaderboard, startPos, endPos));
        return emotes;
    }
}
