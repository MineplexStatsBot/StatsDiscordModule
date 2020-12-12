package de.timmi6790.mineplex_stats.commands.bedrock.management;

import de.timmi6790.discord_framework.modules.command.CommandParameters;
import de.timmi6790.discord_framework.modules.command.CommandResult;
import de.timmi6790.discord_framework.modules.command.property.properties.MinArgCommandProperty;
import de.timmi6790.discord_framework.modules.command.property.properties.RequiredDiscordBotPermsCommandProperty;
import de.timmi6790.discord_framework.modules.emote_reaction.emotereactions.AbstractEmoteReaction;
import de.timmi6790.discord_framework.modules.emote_reaction.emotereactions.EmptyEmoteReaction;
import de.timmi6790.discord_framework.utilities.MultiEmbedBuilder;
import de.timmi6790.discord_framework.utilities.discord.DiscordEmotes;
import de.timmi6790.mineplex_stats.commands.bedrock.AbstractBedrockStatsCommand;
import de.timmi6790.mineplex_stats.statsapi.models.bedrock.BedrockGame;
import net.dv8tion.jda.api.Permission;

import java.util.LinkedHashMap;
import java.util.Map;

public class BedrockPlayerFilterCommand extends AbstractBedrockStatsCommand {
    public BedrockPlayerFilterCommand() {
        super("bfilter", "Filter Bedrock Players", "<game> <player>");

        this.addProperties(
                new MinArgCommandProperty(2),
                new RequiredDiscordBotPermsCommandProperty(Permission.MESSAGE_ADD_REACTION)
        );
    }

    @Override
    protected CommandResult onCommand(final CommandParameters commandParameters) {
        final BedrockGame game = this.getGame(commandParameters, 0);
        final String player = this.getPlayer(commandParameters, 1);

        final MultiEmbedBuilder embedBuilder = this.getEmbedBuilder(commandParameters)
                .addField("Player", player, false)
                .addField("Game", game.getName(), false);

        // Emote reactions
        final Map<String, AbstractEmoteReaction> emotes = new LinkedHashMap<>();
        emotes.put(DiscordEmotes.CHECK_MARK.getEmote(), () -> {
            this.getMineplexStatsModule().getMpStatsRestClient().addBedrockPlayerFilter(player, game.getName());

            this.sendTimedMessage(
                    commandParameters,
                    embedBuilder.setTitle("Successfully Filtered"),
                    90
            );

            // Log
            this.getMineplexStatsModule().sendFilterNotification(
                    commandParameters,
                    "Bedrock",
                    player,
                    game.getName()
            );
        });
        emotes.put(DiscordEmotes.RED_CROSS_MARK.getEmote(), new EmptyEmoteReaction());

        // Send
        sendEmoteMessage(
                commandParameters,
                embedBuilder.setTitle("Filter Confirm")
                        .setDescription("Are you sure that you want to filter this person?"),
                emotes
        );

        return CommandResult.SUCCESS;
    }
}
