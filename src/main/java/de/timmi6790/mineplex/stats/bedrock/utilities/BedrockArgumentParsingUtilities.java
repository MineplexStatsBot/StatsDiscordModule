package de.timmi6790.mineplex.stats.bedrock.utilities;

import de.timmi6790.discord_framework.module.modules.command.CommandParameters;
import de.timmi6790.discord_framework.module.modules.command.exceptions.CommandReturnException;
import de.timmi6790.minecraft.utilities.BedrockUtilities;
import de.timmi6790.mineplex.stats.bedrock.settings.BedrockNameReplacementSetting;
import de.timmi6790.mineplex.stats.common.utilities.ErrorMessageUtilities;
import lombok.experimental.UtilityClass;

import java.util.Arrays;

@UtilityClass
public class BedrockArgumentParsingUtilities {
    public String getBedrockPlayerNameThrow(final CommandParameters commandParameters, final int startPosition) {
        final String[] nameParts = Arrays.copyOfRange(
                commandParameters.getArgs(),
                startPosition,
                commandParameters.getArgs().length
        );
        String playerName = String.join(" ", nameParts);

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
