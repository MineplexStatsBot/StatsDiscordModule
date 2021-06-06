package de.timmi6790.mineplex.stats.common.utilities;

import de.timmi6790.discord_framework.module.modules.command.CommandParameters;
import de.timmi6790.mpstats.api.client.common.leaderboard.exceptions.InvalidLeaderboardCombinationRestException;
import de.timmi6790.mpstats.api.client.common.leaderboard.models.Leaderboard;
import de.timmi6790.mpstats.api.client.exception.exceptions.RateLimitException;
import de.timmi6790.mpstats.api.client.exception.exceptions.UnknownApiException;
import lombok.experimental.UtilityClass;
import net.dv8tion.jda.api.utils.MarkdownUtil;

import java.util.StringJoiner;

import static de.timmi6790.discord_framework.utilities.discord.DiscordMessagesUtilities.getEmbedBuilder;
import static de.timmi6790.discord_framework.utilities.discord.DiscordMessagesUtilities.sendMessageTimed;

@UtilityClass
public class ErrorMessageUtilities {
    public void sendApiOfflineMessage(final CommandParameters commandParameters) {
        sendMessageTimed(
                commandParameters.getLowestMessageChannel(),
                getEmbedBuilder(commandParameters)
                        .setTitle("Api Offline")
                        .setDescription("It seems that the api is currently unavailable. Please stay calm and don't panic!\n" +
                                "(Btw, if the api is down for more than 15 minutes you could also contact me on discord)")
                        .setImage("https://media1.tenor.com/images/bed96e4b6a47f7141e608029bbca3446/tenor.gif?itemid=4655431"),
                300
        );
    }

    public void sendInvalidApiKeyMessage(final CommandParameters commandParameters) {
        sendMessageTimed(
                commandParameters.getLowestMessageChannel(),
                getEmbedBuilder(commandParameters)
                        .setTitle("Invalid Api Key")
                        .setDescription("The used api key is either invalid or has not enough perms for the executed action.")
                        .setImage("https://media1.tenor.com/images/5984738f08363bbbd7376ac0f313cce9/tenor.gif?itemid=4354576"),
                300
        );
    }

    public void sendRateLimitMessage(final CommandParameters commandParameters, final RateLimitException rateLimitException) {
        sendMessageTimed(
                commandParameters.getLowestMessageChannel(),
                getEmbedBuilder(commandParameters)
                        .setTitle("Reached Api Rate Limit")
                        .setDescription("It seems that we are getting rate limited by the api.\n" +
                                "The rate limit ends at " + FormationUtilities.getFormattedTime(rateLimitException.getRetryAt()))
                        .setImage("https://media1.tenor.com/images/969c97eb52d9668d5421d4383daa407c/tenor.gif?itemid=21441690"),
                300
        );
    }

    public void sendUnknownApiExceptionMessage(final CommandParameters commandParameters, final UnknownApiException exception) {
        sendMessageTimed(
                commandParameters.getLowestMessageChannel(),
                getEmbedBuilder(commandParameters)
                        .setTitle("Api Exception")
                        .setDescription("Something went horrible wrong.")
                        .addField("Error", exception.getMessage()),
                300
        );
    }

    public void sendInvalidPlayerNameMessage(final CommandParameters commandParameters, final String playerName) {
        sendMessageTimed(
                commandParameters.getLowestMessageChannel(),
                getEmbedBuilder(commandParameters)
                        .setTitle("Invalid Player Name")
                        .setDescription(
                                "The provided player name %s is invalid.",
                                MarkdownUtil.monospace(playerName)
                        ),
                300
        );
    }

    public void sendNotDataFoundMessage(final CommandParameters commandParameters) {
        sendMessageTimed(
                commandParameters.getLowestMessageChannel(),
                getEmbedBuilder(commandParameters)
                        .setTitle("No Data Found")
                        .setDescription("No data was found for the given input"),
                300
        );
    }

    public void sendInvalidLeaderboardCombinationMessage(final CommandParameters commandParameters,
                                                         final InvalidLeaderboardCombinationRestException exception) {
        final StringJoiner possibleCombinations = new StringJoiner("\n");
        for (final Leaderboard leaderboard : exception.getSuggestedLeaderboards()) {
            possibleCombinations.add(
                    leaderboard.getGame().getGameName() + "-" + leaderboard.getStat().getStatName() + "-" + leaderboard.getBoard().getBoardName()
            );
        }
        sendMessageTimed(
                commandParameters.getLowestMessageChannel(),
                getEmbedBuilder(commandParameters)
                        .setTitle("Invalid Leaderboard Combination")
                        .setDescription(
                                "The used combination is not a valid leaderboard. " +
                                        "WIP: Add autocompletion buttons"
                        ).addField("Possible Combinations", possibleCombinations.toString()),
                300
        );
    }
}
