package de.timmi6790.mineplex_stats.settings;

import de.timmi6790.discord_framework.modules.setting.settings.BooleanSetting;

public class DisclaimerMessageSetting extends BooleanSetting {
    public DisclaimerMessageSetting() {
        super("DisclaimerMessage", "Your only way to escape my disclaimer messages.", true);
    }
}
