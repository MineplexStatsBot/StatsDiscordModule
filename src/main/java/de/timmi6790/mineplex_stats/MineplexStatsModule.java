package de.timmi6790.mineplex_stats;

import de.timmi6790.discord_framework.module.AbstractModule;
import de.timmi6790.discord_framework.module.modules.command.CommandModule;
import de.timmi6790.discord_framework.module.modules.command.CommandParameters;
import de.timmi6790.discord_framework.module.modules.config.ConfigModule;
import de.timmi6790.discord_framework.module.modules.setting.SettingModule;
import de.timmi6790.discord_framework.utilities.MultiEmbedBuilder;
import de.timmi6790.discord_framework.utilities.discord.DiscordMessagesUtilities;
import de.timmi6790.minecraft.MinecraftModule;
import de.timmi6790.mineplex_stats.commands.bedrock.BedrockLeaderboardCommand;
import de.timmi6790.mineplex_stats.commands.bedrock.BedrockPlayerCommand;
import de.timmi6790.mineplex_stats.commands.bedrock.info.BedrockGamesCommand;
import de.timmi6790.mineplex_stats.commands.bedrock.management.BedrockFilterLeaderboardCommand;
import de.timmi6790.mineplex_stats.commands.bedrock.management.BedrockPlayerFilterCommand;
import de.timmi6790.mineplex_stats.commands.debug.ReloadDataCommand;
import de.timmi6790.mineplex_stats.commands.info.AboutCommand;
import de.timmi6790.mineplex_stats.commands.java.JavaLeaderboardCommand;
import de.timmi6790.mineplex_stats.commands.java.info.JavaGamesCommand;
import de.timmi6790.mineplex_stats.commands.java.info.JavaGroupsGroupsCommand;
import de.timmi6790.mineplex_stats.commands.java.management.*;
import de.timmi6790.mineplex_stats.commands.java.player.JavaPlayerGroupCommand;
import de.timmi6790.mineplex_stats.commands.java.player.JavaPlayerStatsCommand;
import de.timmi6790.mineplex_stats.commands.java.player.JavaPlayerStatsRatioCommand;
import de.timmi6790.mineplex_stats.commands.java.unfiltered.JavaUnfilteredLeaderboardCommand;
import de.timmi6790.mineplex_stats.commands.java.unfiltered.JavaUnfilteredPlayerStatsCommand;
import de.timmi6790.mineplex_stats.settings.BedrockNameReplacementSetting;
import de.timmi6790.mineplex_stats.settings.DisclaimerMessagesSetting;
import de.timmi6790.mineplex_stats.settings.JavaNameReplacementSetting;
import de.timmi6790.mineplex_stats.statsapi.MpStatsRestApiClient;
import de.timmi6790.mineplex_stats.statsapi.models.ResponseModel;
import de.timmi6790.mineplex_stats.statsapi.models.bedrock.BedrockGame;
import de.timmi6790.mineplex_stats.statsapi.models.bedrock.BedrockGames;
import de.timmi6790.mineplex_stats.statsapi.models.java.JavaGame;
import de.timmi6790.mineplex_stats.statsapi.models.java.JavaGamesModel;
import de.timmi6790.mineplex_stats.statsapi.models.java.JavaGroup;
import de.timmi6790.mineplex_stats.statsapi.models.java.JavaGroupsGroups;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.utils.MarkdownUtil;
import org.apache.commons.collections4.map.CaseInsensitiveMap;

import java.time.Instant;
import java.util.*;

@EqualsAndHashCode(callSuper = true)
public class MineplexStatsModule extends AbstractModule {
    private final Map<String, JavaGame> javaGames = new CaseInsensitiveMap<>();
    private final Map<String, String> javaGamesAlias = new CaseInsensitiveMap<>();
    private final Map<String, JavaGroup> javaGroups = new CaseInsensitiveMap<>();
    private final Map<String, String> javaGroupsAlias = new CaseInsensitiveMap<>();
    private final Map<String, BedrockGame> bedrockGames = new CaseInsensitiveMap<>();

    @Getter
    private MpStatsRestApiClient mpStatsRestClient;
    private Config statsConfig;

    public MineplexStatsModule() {
        super("MineplexStats");

        this.addDependenciesAndLoadAfter(
                ConfigModule.class,
                CommandModule.class,
                MinecraftModule.class,
                SettingModule.class
        );
    }

    @Override
    public boolean onInitialize() {
        this.statsConfig = this.getModuleOrThrow(ConfigModule.class)
                .registerAndGetConfig(this, new Config());
        this.mpStatsRestClient = new MpStatsRestApiClient(
                this.statsConfig.getApiName(),
                this.statsConfig.getApiPassword(),
                this.statsConfig.getApiUrl(),
                this.statsConfig.getApiTimeout()
        );

        // I should maybe handle the api downtime better
        this.loadJavaGames();
        this.loadJavaGroups();

        this.loadBedrockGames();

        this.getModuleOrThrow(SettingModule.class).registerSettings(
                this,
                new JavaNameReplacementSetting(),
                new BedrockNameReplacementSetting(),
                new DisclaimerMessagesSetting()
        );

        this.getModuleOrThrow(CommandModule.class).registerCommands(
                this,
                new JavaGamesCommand(),
                new JavaPlayerStatsCommand(),
                new JavaPlayerGroupCommand(),
                new JavaGroupsGroupsCommand(),
                new JavaLeaderboardCommand(),
                new JavaPlayerStatsRatioCommand(),

                new BedrockGamesCommand(),
                new BedrockPlayerCommand(),
                new BedrockLeaderboardCommand(),

                new ReloadDataCommand(),

                new JavaUUUIDLeaderboardCommand(),
                new JavaPlayerFilterCommand(),
                new JavaGameAliasCommand(),
                new JavaBoardAliasCommand(),
                new JavaStatAliasCommand(),

                new BedrockPlayerFilterCommand(),
                new BedrockFilterLeaderboardCommand(),

                new JavaUnfilteredLeaderboardCommand(),
                new JavaUnfilteredPlayerStatsCommand(),

                new AboutCommand()
        );

        return true;
    }

    public void loadJavaGames() {
        final ResponseModel responseModel = this.mpStatsRestClient.getJavaGames();
        if (!(responseModel instanceof JavaGamesModel)) {
            return;
        }

        synchronized (this.javaGames) {
            synchronized (this.javaGamesAlias) {
                this.javaGames.clear();
                this.javaGamesAlias.clear();

                for (final JavaGame game : ((JavaGamesModel) responseModel).getGames().values()) {
                    this.javaGames.put(game.getName(), game);
                    for (final String aliasName : game.getAliasNames()) {
                        this.javaGamesAlias.put(aliasName, game.getName());
                    }
                }
            }
        }
    }

    public void loadJavaGroups() {
        final ResponseModel responseModel = this.mpStatsRestClient.getGroups();
        if (!(responseModel instanceof JavaGroupsGroups)) {
            return;
        }

        synchronized (this.javaGroups) {
            synchronized (this.javaGroupsAlias) {
                this.javaGroups.clear();
                this.javaGroupsAlias.clear();

                for (final JavaGroup javaGroup : ((JavaGroupsGroups) responseModel).getGroups().values()) {
                    Arrays.sort(javaGroup.getAliasNames());
                    javaGroup.getGameNames().sort(Comparator.naturalOrder());

                    this.javaGroups.put(javaGroup.getName(), javaGroup);
                    for (final String aliasName : javaGroup.getAliasNames()) {
                        this.javaGroupsAlias.put(aliasName, javaGroup.getName());
                    }
                }
            }
        }
    }

    public void loadBedrockGames() {
        final ResponseModel responseModel = this.mpStatsRestClient.getBedrockGames();
        if (!(responseModel instanceof BedrockGames)) {
            return;
        }

        synchronized (this.bedrockGames) {
            this.bedrockGames.clear();
            for (final BedrockGame game : ((BedrockGames) responseModel).getGames()) {
                this.bedrockGames.put(game.getName(), game);
            }
        }
    }

    // Data
    public Optional<JavaGame> getJavaGame(String name) {
        name = this.javaGamesAlias.getOrDefault(name, name);
        return Optional.ofNullable(this.javaGames.get(name));
    }

    public List<JavaGame> getJavaGames() {
        return new ArrayList<>(this.javaGames.values());
    }

    public Optional<JavaGroup> getJavaGroup(String name) {
        name = this.javaGroupsAlias.getOrDefault(name, name);
        return Optional.ofNullable(this.javaGroups.get(name));
    }

    public List<JavaGroup> getJavaGroups() {
        return new ArrayList<>(this.javaGroups.values());
    }

    public Optional<BedrockGame> getBedrockGame(final String name) {
        return Optional.ofNullable(this.bedrockGames.get(name));
    }

    public List<BedrockGame> getBedrockGames() {
        return new ArrayList<>(this.bedrockGames.values());
    }

    // Notifications
    private void sendNotification(final CommandParameters commandParameters,
                                  final long channelId,
                                  final String format,
                                  final Object... arguments) {
        final TextChannel logChannel = this.getDiscord().getTextChannelById(channelId);
        if (logChannel != null) {
            final MultiEmbedBuilder multiEmbedBuilder = DiscordMessagesUtilities.getEmbedBuilder(commandParameters)
                    .appendDescription(format, arguments)
                    .setTimestamp(Instant.now());

            if (logChannel.isNews()) {
                for (final MessageEmbed embedMessage : multiEmbedBuilder.build()) {
                    logChannel
                            .sendMessage(embedMessage)
                            .flatMap(Message::crosspost)
                            .queue();
                }

            } else {
                DiscordMessagesUtilities.sendMessage(
                        logChannel,
                        multiEmbedBuilder
                );
            }
        }
    }

    public void sendFilterNotification(final CommandParameters commandParameters,
                                       final String category,
                                       final String filteredPlayer,
                                       final String leaderboard) {
        this.sendNotification(
                commandParameters,
                this.statsConfig.getNotificationChannel().getFilterChannel(),
                "[%s]%s added a new filter entry for %s in %s",
                category,
                commandParameters.getUser().getAsMention(),
                filteredPlayer,
                MarkdownUtil.monospace(leaderboard)
        );
    }

    public void sendAliasNotification(final CommandParameters commandParameters,
                                      final String category,
                                      final String leaderboard,
                                      final String newAliasName) {
        this.sendNotification(
                commandParameters,
                this.statsConfig.getNotificationChannel().getAliasNameChannel(),
                "[%s]%s added a new alias entry %s for %s",
                category,
                commandParameters.getUser().getAsMention(),
                MarkdownUtil.monospace(newAliasName),
                MarkdownUtil.monospace(leaderboard)
        );
    }
}
