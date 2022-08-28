package de.timmi6790.mineplex.stats.bedrock.commands.player;

import com.google.common.collect.Lists;
import de.timmi6790.discord_framework.module.modules.slashcommand.SlashCommandModule;
import de.timmi6790.discord_framework.module.modules.slashcommand.exceptions.CommandReturnException;
import de.timmi6790.discord_framework.module.modules.slashcommand.parameters.SlashCommandParameters;
import de.timmi6790.discord_framework.module.modules.slashcommand.property.properties.info.CategoryProperty;
import de.timmi6790.discord_framework.module.modules.slashcommand.property.properties.info.SyntaxProperty;
import de.timmi6790.discord_framework.module.modules.slashcommand.result.BaseCommandResult;
import de.timmi6790.discord_framework.module.modules.slashcommand.result.CommandResult;
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
    public BedrockPlayerCommand(final BaseApiClient<BedrockPlayer> baseApiClient, final SlashCommandModule commandModule) {
        this(
                baseApiClient,
                commandModule,
                "bedrockplayer",
                "Check player stats",
                "bpl"
        );
    }

    public BedrockPlayerCommand(final BaseApiClient<BedrockPlayer> baseApiClient,
                                final SlashCommandModule commandModule,
                                final String name,
                                final String description,
                                final String... aliasNames) {
        super(
                baseApiClient,
                name,
                description,
                commandModule
        );

        this.addProperties(
                new CategoryProperty("Bedrock"),
                new SyntaxProperty("<player>")
        );

        this.addOptions(
                BEDROCK_PLAYER_NAME_REQUIRED
        );
    }

    protected Optional<PlayerStats<BedrockPlayer>> getPlayerStats(final SlashCommandParameters commandParameters,
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

    protected Set<Reason> getFilterReasons(final SlashCommandParameters commandParameters) {
        return ArgumentParsingUtilities.getFilterReasons(commandParameters);
    }

    @Override
    protected CommandResult onStatsCommand(final SlashCommandParameters commandParameters) {
        final String playerName = BedrockArgumentParsingUtilities.getBedrockPlayerNameThrow(commandParameters, BEDROCK_PLAYER_NAME_REQUIRED);
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
