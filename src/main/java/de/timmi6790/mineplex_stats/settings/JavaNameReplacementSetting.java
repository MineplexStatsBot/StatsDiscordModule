package de.timmi6790.mineplex_stats.settings;

import de.timmi6790.minecraft.utilities.JavaUtilities;

public class JavaNameReplacementSetting extends NameReplacementSetting {
    public JavaNameReplacementSetting() {
        super("Java", JavaUtilities::isValidName);
    }
}
