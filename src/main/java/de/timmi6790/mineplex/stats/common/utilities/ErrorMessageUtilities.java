package de.timmi6790.mineplex.stats.common.utilities;

import de.timmi6790.discord_framework.module.modules.slashcommand.parameters.SlashCommandParameters;
import de.timmi6790.mpstats.api.client.common.leaderboard.exceptions.InvalidLeaderboardCombinationRestException;
import de.timmi6790.mpstats.api.client.common.leaderboard.models.Leaderboard;
import de.timmi6790.mpstats.api.client.exception.exceptions.RateLimitException;
import de.timmi6790.mpstats.api.client.exception.exceptions.UnknownApiException;
import lombok.experimental.UtilityClass;
import net.dv8tion.jda.api.utils.MarkdownUtil;

import java.util.StringJoiner;

@UtilityClass
public class ErrorMessageUtilities {
    public void sendApiOfflineMessage(final SlashCommandParameters commandParameters) {
        commandParameters.sendMessage(
                commandParameters.getEmbedBuilder()
                        .setTitle("Api Offline")
                        .setDescription("It seems that the api is currently unavailable. Please stay calm and don't panic!\n" +
                                "(Btw, if the api is down for more than 15 minutes you could also contact me on discord)")
                        .setImage("https://media1.tenor.com/images/bed96e4b6a47f7141e608029bbca3446/tenor.gif?itemid=4655431")
        );
    }

    public void sendInvalidApiKeyMessage(final SlashCommandParameters commandParameters) {
        commandParameters.sendMessage(
                commandParameters.getEmbedBuilder()
                        .setTitle("Invalid Api Key")
                        .setDescription("The used api key is either invalid or has not enough perms for the executed action.")
                        .setImage("https://media1.tenor.com/images/5984738f08363bbbd7376ac0f313cce9/tenor.gif?itemid=4354576")
        );
    }

    public void sendRateLimitMessage(final SlashCommandParameters commandParameters, final RateLimitException rateLimitException) {
        commandParameters.sendMessage(
                commandParameters.getEmbedBuilder()
                        .setTitle("Reached Api Rate Limit")
                        .setDescription("It seems that we are getting rate limited by the api.\n" +
                                "The rate limit ends at " + FormationUtilities.getFormattedTime(rateLimitException.getRetryAt()))
                        .setImage("https://media1.tenor.com/images/969c97eb52d9668d5421d4383daa407c/tenor.gif?itemid=21441690")
        );
    }

    public void sendUnknownApiExceptionMessage(final SlashCommandParameters commandParameters, final UnknownApiException exception) {
        commandParameters.sendMessage(
                commandParameters.getEmbedBuilder()
                        .setTitle("Api Exception")
                        .setDescription("Something went horrible wrong.")
                        .addField("Error", exception.getMessage())
        );
    }

    public void sendInvalidPlayerNameMessage(final SlashCommandParameters commandParameters, final String playerName) {
        commandParameters.sendMessage(
                commandParameters.getEmbedBuilder()
                        .setTitle("Invalid Player Name")
                        .setDescription(
                                "The provided player name %s is invalid.",
                                MarkdownUtil.monospace(playerName)
                        )
        );
    }

    public void sendNotDataFoundMessage(final SlashCommandParameters commandParameters) {
        commandParameters.sendMessage(
                commandParameters.getEmbedBuilder()
                        .setTitle("No Data Found")
                        .setDescription("No data was found for the given input")
        );
    }

    public void sendInvalidLeaderboardCombinationMessage(final SlashCommandParameters commandParameters,
                                                         final InvalidLeaderboardCombinationRestException exception) {
        // TODO: Add better support for the wrong combination
        final StringJoiner possibleCombinations = new StringJoiner("\n");
        for (final Leaderboard leaderboard : exception.getSuggestedLeaderboards()) {
            possibleCombinations.add(
                    leaderboard.getGame().getGameName() + "-" + leaderboard.getStat().getStatName() + "-" + leaderboard.getBoard().getBoardName()
            );
        }
        commandParameters.sendMessage(
                commandParameters.getEmbedBuilder()
                        .setTitle("Invalid Leaderboard Combination")
                        .setDescription(
                                "The used combination is not a valid leaderboard. " +
                                        "WIP: Add autocompletion buttons"
                        ).addField("Possible Combinations", possibleCombinations.toString())
        );
    }
}
