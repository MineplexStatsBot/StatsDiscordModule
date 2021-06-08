package de.timmi6790.mineplex.stats.java;

import de.timmi6790.discord_framework.module.AbstractModule;
import de.timmi6790.discord_framework.module.modules.command.CommandModule;
import de.timmi6790.discord_framework.module.modules.setting.SettingModule;
import de.timmi6790.mineplex.stats.common.BaseMineplexStatsModule;
import de.timmi6790.mineplex.stats.java.commands.leaderboard.JavaGamesCommand;
import de.timmi6790.mineplex.stats.java.commands.leaderboard.JavaLeaderboardCommand;
import de.timmi6790.mineplex.stats.java.commands.leaderboard.UnfilteredJavaLeaderboardCommand;
import de.timmi6790.mineplex.stats.java.commands.player.JavaPlayerCommand;
import de.timmi6790.mineplex.stats.java.commands.player.JavaPlayerStatsRatioCommand;
import de.timmi6790.mineplex.stats.java.commands.player.UnfilteredJavaPlayerCommand;
import de.timmi6790.mineplex.stats.java.settings.JavaNameReplacementSetting;
import de.timmi6790.mpstats.api.client.java.JavaMpStatsApiClient;
import lombok.Getter;

public class JavaMineplexStatsModule extends AbstractModule {
    @Getter
    private JavaMpStatsApiClient statApicClient;

    public JavaMineplexStatsModule() {
        super("JavaMineplexStats");

        this.addDependenciesAndLoadAfter(
                BaseMineplexStatsModule.class,
                CommandModule.class,
                SettingModule.class
        );
    }

    @Override
    public boolean onInitialize() {
        this.statApicClient = this.getModuleOrThrow(BaseMineplexStatsModule.class).getMpStatsApiClient().getJavaClient();

        this.getModuleOrThrow(CommandModule.class).registerCommands(
                this,
                new JavaLeaderboardCommand(this.statApicClient),
                new JavaPlayerCommand(this.statApicClient),
                new JavaPlayerStatsRatioCommand(this.statApicClient),
                new JavaGamesCommand(this.statApicClient),
                new UnfilteredJavaLeaderboardCommand(this.statApicClient),
                new UnfilteredJavaPlayerCommand(this.statApicClient)
        );

        this.getModuleOrThrow(SettingModule.class).registerSetting(
                this,
                new JavaNameReplacementSetting()
        );


        return true;
    }
}
