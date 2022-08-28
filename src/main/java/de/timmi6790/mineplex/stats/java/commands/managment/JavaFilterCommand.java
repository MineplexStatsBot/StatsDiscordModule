package de.timmi6790.mineplex.stats.java.commands.managment;

import de.timmi6790.discord_framework.module.modules.slashcommand.SlashCommandModule;
import de.timmi6790.mineplex.stats.common.commands.managment.FilterCommand;
import de.timmi6790.mpstats.api.client.common.BaseApiClient;
import de.timmi6790.mpstats.api.client.java.player.models.JavaPlayer;
import net.dv8tion.jda.api.sharding.ShardManager;

public class JavaFilterCommand extends FilterCommand<JavaPlayer> {
    public JavaFilterCommand(final BaseApiClient<JavaPlayer> apiClient,
                             final SlashCommandModule commandModule,
                             final ShardManager shardManager) {
        super(
                apiClient,
                "filter",
                commandModule,
                shardManager,
                "Java"
        );
    }

    @Override
    protected String getFormattedPlayer(final JavaPlayer player) {
        return String.format(
                "[%s[%s]](%s)",
                player.getName(),
                player.getUuid(),
                "https://de.namemc.com/profile/" + player.getUuid()
        );
    }

    @Override
    protected String getSchemaName() {
        return "Java";
    }
}
