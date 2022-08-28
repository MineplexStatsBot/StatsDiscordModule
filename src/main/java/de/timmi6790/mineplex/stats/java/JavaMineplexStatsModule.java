package de.timmi6790.mineplex.stats.java;

import de.timmi6790.discord_framework.module.AbstractModule;
import de.timmi6790.discord_framework.module.modules.setting.SettingModule;
import de.timmi6790.discord_framework.module.modules.slashcommand.SlashCommandModule;
import de.timmi6790.mineplex.stats.common.BaseMineplexStatsModule;
import de.timmi6790.mineplex.stats.java.commands.group.GroupPlayerStatsCommand;
import de.timmi6790.mineplex.stats.java.commands.group.GroupsCommand;
import de.timmi6790.mineplex.stats.java.commands.leaderboard.JavaGamesCommand;
import de.timmi6790.mineplex.stats.java.commands.leaderboard.JavaLeaderboardCommand;
import de.timmi6790.mineplex.stats.java.commands.leaderboard.UnfilteredJavaLeaderboardCommand;
import de.timmi6790.mineplex.stats.java.commands.managment.JavaFilterCommand;
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
                SlashCommandModule.class,
                SettingModule.class
        );
    }

    @Override
    public boolean onInitialize() {
        this.statApicClient = this.getModuleOrThrow(BaseMineplexStatsModule.class)
                .getMpStatsApiClient()
                .getJavaClient();

        final SlashCommandModule commandModule = this.getModuleOrThrow(SlashCommandModule.class);
        commandModule.registerCommands(
                this,
                new JavaLeaderboardCommand(this.statApicClient, commandModule),
                new JavaPlayerCommand(this.statApicClient, commandModule),
                new JavaPlayerStatsRatioCommand(this.statApicClient, commandModule),
                new JavaGamesCommand(this.statApicClient, commandModule),
                new UnfilteredJavaLeaderboardCommand(this.statApicClient, commandModule),
                new UnfilteredJavaPlayerCommand(this.statApicClient, commandModule),
                new GroupsCommand(this.statApicClient, commandModule),
                new GroupPlayerStatsCommand(this.statApicClient, commandModule),
                new JavaFilterCommand(this.statApicClient, commandModule, this.getDiscord())
        );

        this.getModuleOrThrow(SettingModule.class).registerSetting(
                this,
                new JavaNameReplacementSetting()
        );


        return true;
    }
}
