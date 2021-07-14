package de.timmi6790.mineplex.stats.common.utilities;

import de.timmi6790.discord_framework.module.modules.command.exceptions.CommandReturnException;
import de.timmi6790.discord_framework.module.modules.command.models.BaseCommandResult;
import de.timmi6790.discord_framework.module.modules.command.models.CommandParameters;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import net.dv8tion.jda.api.utils.MarkdownUtil;

@UtilityClass
public class InvalidArgUtilities {
    public void throwInvalidArg(@NonNull final CommandParameters commandParameters,
                                final int argPos,
                                @NonNull final String argName) {
        commandParameters.sendMessage(
                commandParameters.getEmbedBuilder()
                        .setTitle("Invalid " + argName)
                        .setDescription(
                                "%s is not a valid %s.",
                                MarkdownUtil.monospace(commandParameters.getArgs()[argPos]),
                                MarkdownUtil.bold(argName.toLowerCase())
                        )
        );

        throw new CommandReturnException(BaseCommandResult.INVALID_ARGS);
    }
}
