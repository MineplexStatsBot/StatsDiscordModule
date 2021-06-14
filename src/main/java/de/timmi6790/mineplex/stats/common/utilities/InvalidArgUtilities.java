package de.timmi6790.mineplex.stats.common.utilities;

import de.timmi6790.discord_framework.module.modules.command.CommandParameters;
import de.timmi6790.discord_framework.module.modules.command.CommandResult;
import de.timmi6790.discord_framework.module.modules.command.exceptions.CommandReturnException;
import de.timmi6790.discord_framework.utilities.discord.DiscordMessagesUtilities;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import net.dv8tion.jda.api.utils.MarkdownUtil;

@UtilityClass
public class InvalidArgUtilities {
    public void throwInvalidArg(@NonNull final CommandParameters commandParameters,
                                final int argPos,
                                @NonNull final String argName) {
        DiscordMessagesUtilities.sendMessageTimed(
                commandParameters.getLowestMessageChannel(),
                DiscordMessagesUtilities.getEmbedBuilder(commandParameters)
                        .setTitle("Invalid " + argName)
                        .setDescription(
                                "%s is not a valid %s.",
                                MarkdownUtil.monospace(commandParameters.getArgs()[argPos]),
                                MarkdownUtil.bold(argName.toLowerCase())
                        ),
                300
        );

        throw new CommandReturnException(CommandResult.INVALID_ARGS);
    }
}
