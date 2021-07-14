package de.timmi6790.mineplex.stats.bedrock.commands.player;

import com.google.common.collect.Lists;
import de.timmi6790.discord_framework.module.modules.command.CommandModule;
import de.timmi6790.discord_framework.module.modules.command.exceptions.CommandReturnException;
import de.timmi6790.discord_framework.module.modules.command.models.BaseCommandResult;
import de.timmi6790.discord_framework.module.modules.command.models.CommandParameters;
import de.timmi6790.discord_framework.module.modules.command.models.CommandResult;
import de.timmi6790.discord_framework.module.modules.command.property.properties.controll.MinArgProperty;
import de.timmi6790.discord_framework.module.modules.command.property.properties.info.AliasNamesProperty;
import de.timmi6790.discord_framework.module.modules.command.property.properties.info.CategoryProperty;
import de.timmi6790.discord_framework.module.modules.command.property.properties.info.DescriptionProperty;
import de.timmi6790.discord_framework.module.modules.command.property.properties.info.SyntaxProperty;
import de.timmi6790.mineplex.stats.bedrock.utilities.BedrockArgumentParsingUtilities;
import de.timmi6790.mineplex.stats.common.commands.BaseStatsCommand;
import de.timmi6790.mineplex.stats.common.generators.picture.PictureTable;
import de.timmi6790.mineplex.stats.common.models.ParserResult;
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

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

public class BedrockPlayerCommand extends BaseStatsCommand<BedrockPlayer> {
    public BedrockPlayerCommand(final BaseApiClient<BedrockPlayer> baseApiClient, final CommandModule commandModule) {
        this(
                baseApiClient,
                commandModule,
                "bedrockPlayer",
                "Check player stats",
                "bpl"
        );
    }

    public BedrockPlayerCommand(final BaseApiClient<BedrockPlayer> baseApiClient,
                                final CommandModule commandModule,
                                final String name,
                                final String description,
                                final String... aliasNames) {
        super(
                baseApiClient,
                name,
                commandModule
        );

        this.addProperties(
                new MinArgProperty(1),
                new CategoryProperty("Bedrock"),
                new DescriptionProperty(description),
                new SyntaxProperty("<player>"),
                new AliasNamesProperty(aliasNames)
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
            throw new CommandReturnException(BaseCommandResult.SUCCESSFUL);
        }
    }

    protected String[] getTableHeader(final PlayerStats<BedrockPlayer> playerStats) {
        return new String[]{
                playerStats.getPlayer().getName()
        };
    }

    protected ParserResult parsePlayerStats(final PlayerStats<BedrockPlayer> playerStats) {
        final List<String[]> parsed = Lists.newArrayListWithCapacity(playerStats.getGeneratedStats().size() + playerStats.getStats().size() + 1);
        parsed.add(new String[]{"Game", "Score", "Position"});

        final List<PlayerEntry> entries = new ArrayList<>(playerStats.getStats());
        entries.sort(Comparator.comparing(lb -> lb.getLeaderboard().getGame().getCleanName()));

        ZonedDateTime highestTime = LocalDateTime.MIN.atZone(ZoneId.systemDefault());
        for (final PlayerEntry entry : entries) {
            if (entry.getSaveTime().isAfter(highestTime)) {
                highestTime = entry.getSaveTime();
            }

            parsed.add(
                    new String[]{
                            entry.getLeaderboard().getGame().getCleanName(),
                            FormationUtilities.getFormattedScore(entry.getLeaderboard().getStat(), entry.getScore()),
                            FormationUtilities.getFormattedPosition(entry.getPosition())
                    }
            );
        }

        final String[] header = this.getTableHeader(playerStats);
        final String[][] leaderboard = parsed.toArray(new String[0][3]);
        return new ParserResult(
                leaderboard,
                header,
                highestTime
        );
    }

    protected Set<Reason> getFilterReasons(final CommandParameters commandParameters) {
        return ArgumentParsingUtilities.getFilterReasons(commandParameters);
    }

    @Override
    protected CommandResult onStatsCommand(final CommandParameters commandParameters) {
        final String playerName = BedrockArgumentParsingUtilities.getBedrockPlayerNameThrow(commandParameters, 0);
        final Set<Reason> filterReasons = this.getFilterReasons(commandParameters);

        final Optional<PlayerStats<BedrockPlayer>> playerStatsOpt = this.getPlayerStats(commandParameters, playerName, filterReasons);
        if (playerStatsOpt.isEmpty()) {
            ErrorMessageUtilities.sendNotDataFoundMessage(commandParameters);
            return BaseCommandResult.SUCCESSFUL;
        }

        final PlayerStats<BedrockPlayer> playerStats = playerStatsOpt.get();
        final ParserResult parserResult = this.parsePlayerStats(playerStats);

        final String formattedSaveTime = FormationUtilities.getFormattedTime(parserResult.getHighestTime());
        final String subHeader = "Bedrock - " + formattedSaveTime;

        return this.sendPicture(
                commandParameters,
                new PictureTable(
                        parserResult.getHeader(),
                        subHeader,
                        parserResult.getLeaderboard()
                ).generatePicture(),
                String.format("%s-%s", String.join("-", parserResult.getHeader()), subHeader)
        );
    }

}
