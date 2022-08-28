package de.timmi6790.mineplex.stats.bedrock.utilities;


import de.timmi6790.discord_framework.module.modules.slashcommand.exceptions.CommandReturnException;
import de.timmi6790.discord_framework.module.modules.slashcommand.option.Option;
import de.timmi6790.discord_framework.module.modules.slashcommand.parameters.SlashCommandParameters;
import de.timmi6790.minecraft.utilities.BedrockUtilities;
import de.timmi6790.mineplex.stats.bedrock.settings.BedrockNameReplacementSetting;
import de.timmi6790.mineplex.stats.common.utilities.ErrorMessageUtilities;
import lombok.experimental.UtilityClass;

@UtilityClass
public class BedrockArgumentParsingUtilities {
    public String getBedrockPlayerNameThrow(final SlashCommandParameters commandParameters, final Option<String> playerOption) {
        String playerName = commandParameters.getOptionOrThrow(playerOption);

        // Check if the setting is used or not
        if (playerName.equals(BedrockNameReplacementSetting.getKeyword())) {
            playerName = commandParameters.getUserDb().getSettingOrDefault(BedrockNameReplacementSetting.class, playerName);
        }

        if (BedrockUtilities.isValidName(playerName)) {
            return playerName;
        }

        ErrorMessageUtilities.sendInvalidPlayerNameMessage(commandParameters, playerName);
        throw new CommandReturnException();
    }
}
