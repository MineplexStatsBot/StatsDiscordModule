package de.timmi6790.mineplex.stats.bedrock.settings;

import de.timmi6790.minecraft.utilities.BedrockUtilities;
import de.timmi6790.mineplex.stats.common.settings.NameReplacementSetting;

public class BedrockNameReplacementSetting extends NameReplacementSetting {
    public BedrockNameReplacementSetting() {
        super("Bedrock", BedrockUtilities::isValidName);
    }
}
