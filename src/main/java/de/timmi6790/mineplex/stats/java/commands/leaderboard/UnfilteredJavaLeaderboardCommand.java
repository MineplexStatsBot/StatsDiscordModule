package de.timmi6790.mineplex.stats.java.commands.leaderboard;

import de.timmi6790.discord_framework.module.modules.command.CommandParameters;
import de.timmi6790.mpstats.api.client.common.BaseApiClient;
import de.timmi6790.mpstats.api.client.common.filter.models.Reason;
import de.timmi6790.mpstats.api.client.java.player.models.JavaPlayer;

import java.util.EnumSet;
import java.util.Set;

public class UnfilteredJavaLeaderboardCommand extends JavaLeaderboardCommand {
    public UnfilteredJavaLeaderboardCommand(final BaseApiClient<JavaPlayer> baseApiClient) {
        super(baseApiClient, "unfilteredLeaderboard", "ulb");

        this.setDescription("Check the unfiltered leaderboard");
    }

    @Override
    protected Set<Reason> getFilterReasons(final CommandParameters commandParameters) {
        return EnumSet.noneOf(Reason.class);
    }
}
