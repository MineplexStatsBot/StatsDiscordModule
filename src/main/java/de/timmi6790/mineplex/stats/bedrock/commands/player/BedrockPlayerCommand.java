package de.timmi6790.mineplex.stats.bedrock.commands.player;

import com.google.common.collect.Lists;
import de.timmi6790.discord_framework.module.modules.command.CommandParameters;
import de.timmi6790.discord_framework.module.modules.command.CommandResult;
import de.timmi6790.discord_framework.module.modules.command.exceptions.CommandReturnException;
import de.timmi6790.discord_framework.module.modules.command.property.properties.MinArgCommandProperty;
import de.timmi6790.mineplex.stats.bedrock.utilities.BedrockArgumentParsingUtilities;
import de.timmi6790.mineplex.stats.common.commands.BaseStatsCommand;
import de.timmi6790.mineplex.stats.common.generators.picture.PictureTable;
import de.timmi6790.mineplex.stats.common.utilities.ArgumentParsingUtilities;
import de.timmi6790.mineplex.stats.common.utilities.ErrorMessageUtilities;
import de.timmi6790.mineplex.stats.common.utilities.FormationUtilities;
import de.timmi6790.mpstats.api.client.bedrock.BedrockMpStatsApiClient;
import de.timmi6790.mpstats.api.client.bedrock.player.models.BedrockPlayer;
import de.timmi6790.mpstats.api.client.common.BaseApiClient;
import de.timmi6790.mpstats.api.client.common.filter.models.Reason;
import de.timmi6790.mpstats.api.client.common.player.exceptions.InvalidPlayerNameRestException;
import de.timmi6790.mpstats.api.client.common.player.models.PlayerEntry;
import de.timmi6790.mpstats.api.client.common.player.models.PlayerStats;

import java.time.ZonedDateTime;
import java.util.*;

public class BedrockPlayerCommand extends BaseStatsCommand<BedrockPlayer> {
    public BedrockPlayerCommand(final BaseApiClient<BedrockPlayer> baseApiClient) {
        super(baseApiClient, "bedrockPlayer", "Bedrock", "Check player stats", "<player>", "bpl");

        this.addProperties(
                new MinArgCommandProperty(1)
        );
    }

    protected Optional<PlayerStats<BedrockPlayer>> getPlayerStats(final CommandParameters commandParameters,
                                                                  final String playerName,
                                                                  final Set<Reason> filterReasons) {
        try {
            return ((BedrockMpStatsApiClient) this.getApiClient()).getPlayerClient().getPlayerStats(
                    playerName,
                    false,
                    filterReasons
            );
        } catch (final InvalidPlayerNameRestException exception) {
            ErrorMessageUtilities.sendInvalidPlayerNameMessage(commandParameters, playerName);
            throw new CommandReturnException(CommandResult.SUCCESS);
        }
    }

    protected String[] getTableHeader(final PlayerStats<BedrockPlayer> playerStats) {
        return new String[]{
                playerStats.getPlayer().getName()
        };
    }

    protected String[][] parsePlayerStats(final PlayerStats<BedrockPlayer> playerStats) {
        final List<String[]> parsed = Lists.newArrayListWithCapacity(playerStats.getGeneratedStats().size() + playerStats.getStats().size() + 1);
        parsed.add(new String[]{"Game", "Score", "Position"});

        final List<PlayerEntry> entries = new ArrayList<>(playerStats.getStats());
        entries.sort(Comparator.comparing(lb -> lb.getLeaderboard().getGame().getCleanName()));
        for (final PlayerEntry entry : entries) {
            parsed.add(
                    new String[]{
                            entry.getLeaderboard().getGame().getCleanName(),
                            FormationUtilities.getFormattedScore(entry.getLeaderboard().getStat(), entry.getScore()),
                            FormationUtilities.getFormattedPosition(entry.getPosition())
                    }
            );
        }

        return parsed.toArray(new String[0][3]);
    }

    @Override
    protected CommandResult onStatsCommand(final CommandParameters commandParameters) {
        final String playerName = BedrockArgumentParsingUtilities.getBedrockPlayerNameThrow(commandParameters, 0);
        final Set<Reason> filterReasons = ArgumentParsingUtilities.getFilterReasons(commandParameters);

        final Optional<PlayerStats<BedrockPlayer>> playerStatsOpt = this.getPlayerStats(commandParameters, playerName, filterReasons);
        if (playerStatsOpt.isEmpty()) {
            ErrorMessageUtilities.sendNotDataFoundMessage(commandParameters);
            return CommandResult.SUCCESS;
        }

        final PlayerStats<BedrockPlayer> playerStats = playerStatsOpt.get();

        final String[][] parsedLeaderboard = this.parsePlayerStats(playerStats);
        // Check if only the first row exists
        if (parsedLeaderboard.length <= 1) {
            ErrorMessageUtilities.sendNotDataFoundMessage(commandParameters);
            return CommandResult.SUCCESS;
        }

        final String[] header = this.getTableHeader(playerStats);

        // TODO: Implement the actual time
        final String formattedSaveTime = FormationUtilities.getFormattedTime(ZonedDateTime.now());
        final String subHeader = "Bedrock - " + formattedSaveTime;

        return this.sendPicture(
                commandParameters,
                new PictureTable(
                        header,
                        subHeader,
                        parsedLeaderboard
                ).generatePicture(),
                String.format("%s-%s", String.join("-", header), subHeader)
        );
    }
}
