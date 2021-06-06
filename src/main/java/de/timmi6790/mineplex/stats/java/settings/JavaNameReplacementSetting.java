package de.timmi6790.mineplex.stats.java.settings;

import de.timmi6790.minecraft.utilities.JavaUtilities;
import de.timmi6790.mineplex.stats.common.settings.NameReplacementSetting;

public class JavaNameReplacementSetting extends NameReplacementSetting {
    public JavaNameReplacementSetting() {
        super("Java", JavaUtilities::isValidName);
    }
}
