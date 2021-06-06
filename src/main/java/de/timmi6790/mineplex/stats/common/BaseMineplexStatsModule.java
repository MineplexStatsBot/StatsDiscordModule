package de.timmi6790.mineplex.stats.common;

import de.timmi6790.discord_framework.module.AbstractModule;
import de.timmi6790.discord_framework.module.modules.command.CommandModule;
import de.timmi6790.discord_framework.module.modules.config.ConfigModule;
import de.timmi6790.discord_framework.module.modules.reactions.button.ButtonReactionModule;
import de.timmi6790.discord_framework.module.modules.setting.SettingModule;
import de.timmi6790.mineplex.stats.common.settings.DisclaimerMessagesSetting;
import de.timmi6790.mineplex.stats.common.settings.FilterReasonSetting;
import de.timmi6790.mpstats.api.client.MpStatsApiClient;
import lombok.Getter;

public class BaseMineplexStatsModule extends AbstractModule {
    @Getter
    private MpStatsApiClient mpStatsApiClient;

    public BaseMineplexStatsModule() {
        super("BaseMineplexStats");

        this.addDependenciesAndLoadAfter(
                CommandModule.class,
                ButtonReactionModule.class,
                ConfigModule.class,
                SettingModule.class
        );
    }

    @Override
    public boolean onInitialize() {
        final Config config = this.getModuleOrThrow(ConfigModule.class).registerAndGetConfig(this, new Config());

        this.mpStatsApiClient = new MpStatsApiClient(
                config.getApi().getUrl(),
                config.getApi().getKey()
        );

        this.getModuleOrThrow(SettingModule.class).registerSettings(
                this,
                new FilterReasonSetting(),
                new DisclaimerMessagesSetting()
        );

        return true;
    }
}
