package de.timmi6790.mineplex_stats.commands.bedrock;

import de.timmi6790.discord_framework.module.modules.command.CommandParameters;
import de.timmi6790.discord_framework.module.modules.command.exceptions.CommandReturnException;
import de.timmi6790.minecraft.utilities.BedrockUtilities;
import de.timmi6790.mineplex_stats.commands.AbstractStatsCommand;
import de.timmi6790.mineplex_stats.commands.bedrock.info.BedrockGamesCommand;
import de.timmi6790.mineplex_stats.settings.BedrockNameReplacementSetting;
import de.timmi6790.mineplex_stats.settings.NameReplacementSetting;
import de.timmi6790.mineplex_stats.statsapi.models.bedrock.BedrockGame;
import net.dv8tion.jda.api.utils.MarkdownUtil;

import java.util.Arrays;

public abstract class AbstractBedrockStatsCommand extends AbstractStatsCommand {
    protected AbstractBedrockStatsCommand(final String name,
                                          final String description,
                                          final String syntax,
                                          final String... aliasNames) {
        super(name, "MineplexStats - Bedrock", description, syntax, aliasNames);
    }

    protected BedrockGame getGame(final CommandParameters commandParameters, final int argPos) {
        return this.getArgumentOrThrow(
                commandParameters,
                "game",
                argPos,
                gameName -> this.getMineplexStatsModule().getBedrockGame(gameName),
                BedrockGame::getName,
                () -> this.getMineplexStatsModule().getBedrockGames(),
                () -> new String[0],
                BedrockGamesCommand.class
        );
    }

    protected String getPlayer(final CommandParameters commandParameters, final int startPos) {
        String name = String.join(
                " ",
                Arrays.copyOfRange(
                        commandParameters.getArgs(),
                        startPos,
                        commandParameters.getArgs().length
                )
        );

        // Check for setting
        if (name.equalsIgnoreCase(NameReplacementSetting.getKeyword())) {
            final String settingName = commandParameters.getUserDb().getSettingOrDefault(BedrockNameReplacementSetting.class, "");
            if (!settingName.isEmpty()) {
                name = settingName;
            }
        }

        if (BedrockUtilities.isValidName(name)) {
            return name;
        }

        throw new CommandReturnException(
                this.getEmbedBuilder(commandParameters)
                        .setTitle("Invalid Name")
                        .setDescription(MarkdownUtil.monospace(name) + " is not a minecraft name.")
        );
    }
}
