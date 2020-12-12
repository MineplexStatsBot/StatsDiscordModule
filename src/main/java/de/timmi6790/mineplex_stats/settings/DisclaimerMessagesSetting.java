package de.timmi6790.mineplex_stats.settings;

import de.timmi6790.discord_framework.modules.setting.settings.BooleanSetting;

public class DisclaimerMessagesSetting extends BooleanSetting {
    public DisclaimerMessagesSetting() {
        super("DisclaimerMessages", "Your only way to escape my disclaimer messages.", true);
    }
}
