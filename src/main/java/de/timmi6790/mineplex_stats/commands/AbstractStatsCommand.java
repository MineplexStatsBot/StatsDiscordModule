package de.timmi6790.mineplex_stats.commands;

import de.timmi6790.discord_framework.modules.command.AbstractCommand;
import de.timmi6790.discord_framework.modules.command.CommandParameters;
import de.timmi6790.discord_framework.modules.command.CommandResult;
import de.timmi6790.discord_framework.modules.command.exceptions.CommandReturnException;
import de.timmi6790.discord_framework.modules.emote_reaction.EmoteReactionMessage;
import de.timmi6790.discord_framework.modules.emote_reaction.EmoteReactionModule;
import de.timmi6790.discord_framework.modules.emote_reaction.emotereactions.AbstractEmoteReaction;
import de.timmi6790.discord_framework.modules.emote_reaction.emotereactions.CommandEmoteReaction;
import de.timmi6790.discord_framework.utilities.discord.DiscordEmotes;
import de.timmi6790.mineplex_stats.MineplexStatsModule;
import de.timmi6790.mineplex_stats.statsapi.models.ResponseModel;
import de.timmi6790.mineplex_stats.statsapi.models.errors.ErrorModel;
import net.dv8tion.jda.api.utils.MarkdownUtil;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.ocpsoft.prettytime.nlp.PrettyTimeParser;

import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;

public abstract class AbstractStatsCommand extends AbstractCommand<MineplexStatsModule> {
    protected static final String UNKNOWN_POSITION = ">1000";
    protected static final String UNKNOWN_SCORE = "Unknown";

    private static final DecimalFormat FORMAT_NUMBER = (DecimalFormat) NumberFormat.getInstance(Locale.US);

    private static final DecimalFormat FORMAT_DECIMAL_POINT = new DecimalFormat(".##");

    static {
        final DecimalFormatSymbols dateSymbol = FORMAT_DECIMAL_POINT.getDecimalFormatSymbols();
        dateSymbol.setDecimalSeparator('.');
        FORMAT_DECIMAL_POINT.setDecimalFormatSymbols(dateSymbol);

        final DecimalFormatSymbols numberSymbol = FORMAT_NUMBER.getDecimalFormatSymbols();
        numberSymbol.setGroupingSeparator(',');
        FORMAT_NUMBER.setDecimalFormatSymbols(numberSymbol);
    }

    public AbstractStatsCommand(final String name, final String category, final String description, final String syntax, final String... aliasNames) {
        super(name, category, description, syntax, aliasNames);
    }

    private boolean isInt(final String userInput) {
        try {
            Integer.parseInt(userInput);
            return true;
        } catch (final NumberFormatException e) {
            return false;
        }
    }

    protected String getFormattedTime(long time) {
        final long days = TimeUnit.SECONDS.toDays(time);
        time -= TimeUnit.DAYS.toSeconds(days);

        final long hours = TimeUnit.SECONDS.toHours(time);
        time -= TimeUnit.HOURS.toSeconds(hours);

        if (days != 0) {
            if (hours == 0) {
                return days + (days > 1 ? " days" : " day");
            }

            return days + FORMAT_DECIMAL_POINT.format(hours / 24D) + " days";
        }

        final long minutes = TimeUnit.SECONDS.toMinutes(time);
        time -= TimeUnit.MINUTES.toSeconds(minutes);
        if (hours != 0) {
            if (minutes == 0) {
                return hours + (hours > 1 ? " hours" : " hour");
            }

            return hours + FORMAT_DECIMAL_POINT.format(minutes / 60D) + " hours";
        }

        final long seconds = TimeUnit.SECONDS.toSeconds(time);
        if (minutes != 0) {
            if (seconds == 0) {
                return minutes + (minutes > 1 ? " minutes" : " minute");
            }

            return minutes + FORMAT_DECIMAL_POINT.format(seconds / 60D) + " minutes";
        }

        return seconds + (seconds > 1 ? " seconds" : " second");
    }

    public String getFormattedNumber(final long number) {
        return FORMAT_NUMBER.format(number);
    }

    protected String getFormattedUnixTime(final long unix) {
        final SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss z");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        return dateFormat.format(Date.from(Instant.ofEpochSecond(unix)));
    }

    public void checkApiResponseThrow(final CommandParameters commandParameters, final ResponseModel response, final String arguments) {
        if (response instanceof ErrorModel) {
            // No stats found
            if (((ErrorModel) response).getErrorCode() == 1) {
                throw new CommandReturnException(
                        getEmbedBuilder(commandParameters)
                                .setTitle("No stats found")
                                .setDescription("There are no collected stats.\n WIP")
                                .addField("Arguments", arguments, false),
                        CommandResult.SUCCESS
                );
            }

            throw new CommandReturnException(
                    getEmbedBuilder(commandParameters)
                            .setTitle("Error")
                            .setDescription("Something went wrong while requesting your data.")
                            .addField("Api Response", ((ErrorModel) response).getErrorMessage(), false)
                            .setImage("https://media1.tenor.com/images/981ee5030a18a779e899b2c307e65f7a/tenor.gif?itemid=13159552"),
                    CommandResult.ERROR
            );
        }
    }

    protected int getStartPositionThrow(final CommandParameters commandParameters, final int argPos, final int upperLimit) {
        final String name = argPos >= commandParameters.getArgs().length ? "1" : commandParameters.getArgs()[argPos];
        if (!this.isInt(name)) {
            throw new CommandReturnException(
                    getEmbedBuilder(commandParameters)
                            .setTitle("Invalid start position")
                            .setDescription(MarkdownUtil.monospace(name) + " is not a valid start position for the leaderboard.\n" +
                                    "Use a number between " + MarkdownUtil.bold("1") + " and " + MarkdownUtil.bold(String.valueOf(upperLimit)))
            );
        }

        return Math.min(Math.max(1, Integer.parseInt(name)), upperLimit);
    }

    protected int getEndPositionThrow(final int startPos, final CommandParameters commandParameters, final int argPos, final int upperLimit, final int maxDistance) {
        if (commandParameters.getArgs().length > argPos && !this.isInt(commandParameters.getArgs()[argPos])) {
            throw new CommandReturnException(
                    getEmbedBuilder(commandParameters)
                            .setTitle("Invalid end position")
                            .setDescription(MarkdownUtil.monospace(commandParameters.getArgs()[argPos]) + " is not a valid end position for the leaderboard.\n" +
                                    "Use a number between " + MarkdownUtil.bold("1") + " and " + MarkdownUtil.bold(String.valueOf(upperLimit)))
            );
        }

        int endPos = argPos >= commandParameters.getArgs().length ? upperLimit : Integer.parseInt(commandParameters.getArgs()[argPos]);
        if (startPos > endPos || endPos - startPos > maxDistance) {
            endPos = startPos + maxDistance;
        }

        return Math.min(Math.max(1, endPos), upperLimit);
    }

    protected long getUnixTimeThrow(final CommandParameters commandParameters, final int startArgPos) {
        if (startArgPos >= commandParameters.getArgs().length) {
            return Instant.now().getEpochSecond();
        }

        final String[] dateArgs = new String[commandParameters.getArgs().length - startArgPos];
        System.arraycopy(commandParameters.getArgs(), startArgPos, dateArgs, 0, dateArgs.length);
        final String name = String.join(" ", dateArgs).replace(".", "/");

        // TODO: Better date parsing :/
        final List<Date> dates = new PrettyTimeParser().parse(name);
        if (!dates.isEmpty()) {
            return TimeUnit.MILLISECONDS.toSeconds(dates.get(0).getTime());
        }

        throw new CommandReturnException(
                getEmbedBuilder(commandParameters)
                        .setTitle("Invalid Date")
                        .setDescription(MarkdownUtil.monospace(name) + " is not a valid date.")
        );
    }

    protected UUID getUUIDThrow(final CommandParameters commandParameters, final int argPos) {
        try {
            return UUID.fromString(commandParameters.getArgs()[argPos]);
        } catch (final IllegalArgumentException ignore) {
            throw new CommandReturnException(
                    getEmbedBuilder(commandParameters)
                            .setTitle("Invalid UUID")
                            .setDescription(MarkdownUtil.monospace(commandParameters.getArgs()[argPos]) + " is not a valid UUID")
            );
        }
    }

    private CommandParameters getLeaderboardNewCommandParameters(final CommandParameters commandParameters, final int argPosStart, final int argPosEnd, final int newStart,
                                                                 final int rowDistance) {
        final String[] newArgs = Arrays.copyOf(commandParameters.getArgs(), Math.max(commandParameters.getArgs().length, Math.max(argPosEnd, argPosStart) + 1));
        newArgs[argPosStart] = String.valueOf(newStart);
        newArgs[argPosEnd] = String.valueOf(newStart + rowDistance);

        return new CommandParameters(commandParameters, newArgs);
    }

    protected Map<String, AbstractEmoteReaction> getLeaderboardEmotes(final CommandParameters commandParameters, final int rowDistance, final int fastRowDistance,
                                                                      final int startPos, final int endPos, final int totalLength, final int argPosStart, final int argPosEnd) {
        final Map<String, AbstractEmoteReaction> emotes = new LinkedHashMap<>(4);

        // Far Left Arrow
        if (startPos - rowDistance > 2) {
            final int newStart = Math.max(1, (startPos - fastRowDistance));
            emotes.put(DiscordEmotes.FAR_LEFT_ARROW.getEmote(), new CommandEmoteReaction(this, this.getLeaderboardNewCommandParameters(commandParameters, argPosStart,
                    argPosEnd, newStart, rowDistance)));
        }

        // Left Arrow
        if (startPos > 1) {
            final int newStart = Math.max(1, (startPos - rowDistance - 1));
            emotes.put(DiscordEmotes.LEFT_ARROW.getEmote(), new CommandEmoteReaction(this, this.getLeaderboardNewCommandParameters(commandParameters, argPosStart,
                    argPosEnd, newStart, rowDistance)));
        }

        // Right Arrow
        if (totalLength > endPos) {
            final int newStart = Math.min(totalLength, (endPos + rowDistance + 1)) - rowDistance;
            emotes.put(DiscordEmotes.RIGHT_ARROW.getEmote(), new CommandEmoteReaction(this, this.getLeaderboardNewCommandParameters(commandParameters, argPosStart,
                    argPosEnd, newStart, rowDistance)));
        }

        // Far Right Arrow
        if (totalLength - rowDistance - 1 > endPos) {
            final int newStart = Math.min(totalLength, (endPos + fastRowDistance)) - rowDistance;
            emotes.put(DiscordEmotes.FAR_RIGHT_ARROW.getEmote(), new CommandEmoteReaction(this, this.getLeaderboardNewCommandParameters(commandParameters, argPosStart,
                    argPosEnd, newStart, rowDistance)));
        }

        return emotes;
    }

    protected CommandResult sendPicture(final CommandParameters commandParameters, @Nullable final InputStream inputStream, final String pictureName) {
        return this.sendPicture(commandParameters, inputStream, pictureName, null);
    }

    protected CommandResult sendPicture(final CommandParameters commandParameters, @Nullable final InputStream inputStream, final String pictureName,
                                        final @Nullable EmoteReactionMessage emoteReactionMessage) {
        if (inputStream != null) {
            commandParameters.getLowestMessageChannel()
                    .sendFile(inputStream, pictureName + ".png")
                    .queue(message -> {
                        if (emoteReactionMessage != null) {
                            this.getModule().getModuleOrThrow(EmoteReactionModule.class).addEmoteReactionMessage(message, emoteReactionMessage);
                        }
                    });
            return CommandResult.SUCCESS;
        } else {
            this.sendErrorMessage(commandParameters, "Error while creating picture.");
            return CommandResult.ERROR;
        }
    }
}
