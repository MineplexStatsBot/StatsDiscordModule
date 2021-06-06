package de.timmi6790.mineplex.stats.bedrock;

import de.timmi6790.discord_framework.module.AbstractModule;
import de.timmi6790.discord_framework.module.modules.command.CommandModule;
import de.timmi6790.discord_framework.module.modules.setting.SettingModule;
import de.timmi6790.mineplex.stats.bedrock.commands.leaderboard.BedrockGamesCommand;
import de.timmi6790.mineplex.stats.bedrock.commands.leaderboard.BedrockLeaderboardCommand;
import de.timmi6790.mineplex.stats.bedrock.commands.player.BedrockPlayerCommand;
import de.timmi6790.mineplex.stats.bedrock.settings.BedrockNameReplacementSetting;
import de.timmi6790.mineplex.stats.common.BaseMineplexStatsModule;
import de.timmi6790.mpstats.api.client.bedrock.BedrockMpStatsApiClient;
import lombok.Getter;

public class BedrockMineplexStatsModule extends AbstractModule {
    @Getter
    private BedrockMpStatsApiClient statApicClient;

    public BedrockMineplexStatsModule() {
        super("BedrockMineplexStats");

        this.addDependenciesAndLoadAfter(
                BaseMineplexStatsModule.class,
                CommandModule.class,
                SettingModule.class
        );
    }

    @Override
    public boolean onInitialize() {
        this.statApicClient = this.getModuleOrThrow(BaseMineplexStatsModule.class).getMpStatsApiClient().getBedrockClient();

        this.getModuleOrThrow(CommandModule.class).registerCommands(
                this,
                new BedrockLeaderboardCommand(this.statApicClient),
                new BedrockPlayerCommand(this.statApicClient),
                new BedrockGamesCommand(this.statApicClient)
        );

        this.getModuleOrThrow(SettingModule.class).registerSetting(
                this,
                new BedrockNameReplacementSetting()
        );

        return true;
    }
}
