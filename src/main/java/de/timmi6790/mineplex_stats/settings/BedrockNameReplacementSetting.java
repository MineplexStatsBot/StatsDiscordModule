package de.timmi6790.mineplex_stats.settings;

import de.timmi6790.minecraft.utilities.BedrockUtilities;

public class BedrockNameReplacementSetting extends NameReplacementSetting {
    public BedrockNameReplacementSetting() {
        super("Bedrock", BedrockUtilities::isValidName);
    }
}
