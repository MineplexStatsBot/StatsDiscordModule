package de.timmi6790.mineplex.stats.common.utilities;

import de.timmi6790.discord_framework.module.modules.slashcommand.exceptions.CommandReturnException;
import de.timmi6790.discord_framework.module.modules.slashcommand.option.Option;
import de.timmi6790.discord_framework.module.modules.slashcommand.parameters.SlashCommandParameters;
import de.timmi6790.discord_framework.module.modules.slashcommand.result.BaseCommandResult;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import net.dv8tion.jda.api.utils.MarkdownUtil;

@UtilityClass
public class InvalidArgUtilities {
    public void throwInvalidArg(@NonNull final SlashCommandParameters commandParameters,
                                final String value,
                                final Option<?> option) {
        commandParameters.sendMessage(
                commandParameters.getEmbedBuilder()
                        .setTitle("Invalid " + option.getName())
                        .setDescription(
                                "%s is not a valid %s.",
                                MarkdownUtil.monospace(value),
                                MarkdownUtil.bold(option.getName().toLowerCase())
                        )
        );

        throw new CommandReturnException(BaseCommandResult.INVALID_ARGS);
    }
}
