package de.timmi6790.mineplex.stats.bedrock;

import de.timmi6790.discord_framework.module.AbstractModule;
import de.timmi6790.discord_framework.module.modules.setting.SettingModule;
import de.timmi6790.discord_framework.module.modules.slashcommand.SlashCommandModule;
import de.timmi6790.mineplex.stats.bedrock.commands.leaderboard.BedrockGamesCommand;
import de.timmi6790.mineplex.stats.bedrock.commands.leaderboard.BedrockLeaderboardCommand;
import de.timmi6790.mineplex.stats.bedrock.commands.leaderboard.UnfilteredBedrockLeaderboardCommand;
import de.timmi6790.mineplex.stats.bedrock.commands.player.BedrockPlayerCommand;
import de.timmi6790.mineplex.stats.bedrock.commands.player.UnfilteredBedrockPlayerCommand;
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
                SlashCommandModule.class,
                SettingModule.class
        );
    }

    @Override
    public boolean onInitialize() {
        this.statApicClient = this.getModuleOrThrow(BaseMineplexStatsModule.class).getMpStatsApiClient().getBedrockClient();

        final SlashCommandModule commandModule = this.getModuleOrThrow(SlashCommandModule.class);
        commandModule.registerCommands(
                this,
                new BedrockLeaderboardCommand(this.statApicClient, commandModule),
                new BedrockPlayerCommand(this.statApicClient, commandModule),
                new BedrockGamesCommand(this.statApicClient, commandModule),
                new UnfilteredBedrockLeaderboardCommand(this.statApicClient, commandModule),
                new UnfilteredBedrockPlayerCommand(this.statApicClient, commandModule)
        );

        this.getModuleOrThrow(SettingModule.class).registerSetting(
                this,
                new BedrockNameReplacementSetting()
        );

        return true;
    }
}
