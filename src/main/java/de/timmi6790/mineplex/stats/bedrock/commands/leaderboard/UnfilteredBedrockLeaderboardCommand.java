package de.timmi6790.mineplex.stats.bedrock.commands.leaderboard;

import de.timmi6790.discord_framework.module.modules.command.CommandModule;
import de.timmi6790.discord_framework.module.modules.command.models.CommandParameters;
import de.timmi6790.discord_framework.module.modules.command.property.properties.info.DescriptionProperty;
import de.timmi6790.mpstats.api.client.bedrock.player.models.BedrockPlayer;
import de.timmi6790.mpstats.api.client.common.BaseApiClient;
import de.timmi6790.mpstats.api.client.common.filter.models.Reason;

import java.util.EnumSet;
import java.util.Set;

public class UnfilteredBedrockLeaderboardCommand extends BedrockLeaderboardCommand {
    public UnfilteredBedrockLeaderboardCommand(final BaseApiClient<BedrockPlayer> baseApiClient,
                                               final CommandModule commandModule) {
        super(
                baseApiClient,
                commandModule,
                "unfilteredBedrockLeaderboard",
                "ublb"
        );

        this.addProperties(
                new DescriptionProperty("Check the unfiltered leaderboard")
        );
    }

    @Override
    protected Set<Reason> getFilterReasons(final CommandParameters commandParameters) {
        return EnumSet.noneOf(Reason.class);
    }
}
