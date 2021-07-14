package de.timmi6790.mineplex.stats.java.commands.group;

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
import de.timmi6790.minecraft.utilities.JavaUtilities;
import de.timmi6790.mineplex.stats.common.commands.BaseStatsCommand;
import de.timmi6790.mineplex.stats.common.generators.picture.PictureTable;
import de.timmi6790.mineplex.stats.common.models.ParserResult;
import de.timmi6790.mineplex.stats.common.utilities.ArgumentParsingUtilities;
import de.timmi6790.mineplex.stats.common.utilities.ErrorMessageUtilities;
import de.timmi6790.mineplex.stats.common.utilities.FormationUtilities;
import de.timmi6790.mineplex.stats.common.utilities.SetUtilities;
import de.timmi6790.mineplex.stats.java.utilities.JavaArgumentParsingUtilities;
import de.timmi6790.mpstats.api.client.common.BaseApiClient;
import de.timmi6790.mpstats.api.client.common.board.exceptions.InvalidBoardNameException;
import de.timmi6790.mpstats.api.client.common.board.models.Board;
import de.timmi6790.mpstats.api.client.common.filter.models.Reason;
import de.timmi6790.mpstats.api.client.common.group.exceptions.InvalidGroupNameRestException;
import de.timmi6790.mpstats.api.client.common.group.models.Group;
import de.timmi6790.mpstats.api.client.common.group.models.GroupPlayerStats;
import de.timmi6790.mpstats.api.client.common.player.exceptions.InvalidPlayerNameRestException;
import de.timmi6790.mpstats.api.client.common.player.models.PlayerEntry;
import de.timmi6790.mpstats.api.client.common.stat.exceptions.InvalidStatNameRestException;
import de.timmi6790.mpstats.api.client.common.stat.models.Stat;
import de.timmi6790.mpstats.api.client.java.player.models.JavaPlayer;
import lombok.SneakyThrows;

import java.awt.image.BufferedImage;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class GroupPlayerStatsCommand extends BaseStatsCommand<JavaPlayer> {
    private static final int ROW_SOFT_LIMIT = 20;

    private static final int GROUP_POSITION = 1;
    private static final int STAT_POSITION = 2;
    private static final int BOARD_POSITION = 3;

    public GroupPlayerStatsCommand(final BaseApiClient<JavaPlayer> baseApiClient, final CommandModule commandModule) {
        super(
                baseApiClient,
                "groupPlayer",
                commandModule
        );

        this.addProperties(
                new MinArgProperty(3),
                new CategoryProperty("Java"),
                new DescriptionProperty("Check group player stats"),
                new SyntaxProperty("<player> <group> <stat> [board] [dateTime]"),
                new AliasNamesProperty("gpl")
        );
    }

    protected Optional<GroupPlayerStats<JavaPlayer>> getPlayerStats(final CommandParameters commandParameters,
                                                                    final String playerName,
                                                                    final String group,
                                                                    final String stat,
                                                                    final String board,
                                                                    final ZonedDateTime zonedDateTime,
                                                                    final Set<Reason> filterReasons) {
        try {
            return this.getApiClient().getGroupClient().getPlayerStats(
                    group,
                    playerName,
                    stat,
                    board,
                    zonedDateTime,
                    filterReasons,
                    false
            );
        } catch (final InvalidBoardNameException exception) {
            this.throwArgumentCorrectionMessage(
                    commandParameters,
                    board,
                    BOARD_POSITION,
                    "board",
                    null,
                    new String[0],
                    exception.getSuggestedBoards(),
                    Board::getBoardName
            );
        } catch (final InvalidStatNameRestException exception) {
            this.throwArgumentCorrectionMessage(
                    commandParameters,
                    stat,
                    STAT_POSITION,
                    "stat",
                    null,
                    new String[0],
                    exception.getSuggestedStats(),
                    Stat::getStatName
            );
        } catch (final InvalidPlayerNameRestException e) {
            ErrorMessageUtilities.sendInvalidPlayerNameMessage(commandParameters, playerName);
        } catch (final InvalidGroupNameRestException exception) {
            this.throwArgumentCorrectionMessage(
                    commandParameters,
                    group,
                    GROUP_POSITION,
                    "group",
                    null,
                    new String[0],
                    exception.getSuggestedGroups(),
                    Group::getGroupName
            );
        }
        throw new CommandReturnException();
    }

    protected Set<Reason> getFilterReasons(final CommandParameters commandParameters) {
        return ArgumentParsingUtilities.getFilterReasons(commandParameters);
    }

    protected String[] getTableHeader(final GroupPlayerStats<JavaPlayer> groupPlayerStats) {
        final PlayerEntry foundEntry = SetUtilities.getFirstEntry(groupPlayerStats.getStats());
        if (foundEntry == null) {
            return new String[]{
                    groupPlayerStats.getPlayer().getName(),
                    groupPlayerStats.getGroup().getCleanName(),
                    "Unknown"
            };
        } else {
            return new String[]{
                    groupPlayerStats.getPlayer().getName(),
                    groupPlayerStats.getGroup().getCleanName(),
                    foundEntry.getLeaderboard().getStat().getCleanName(),
                    foundEntry.getLeaderboard().getBoard().getBoardName()
            };
        }
    }

    protected ParserResult parsePlayerStats(final GroupPlayerStats<JavaPlayer> groupPlayerStats) {
        final List<String[]> parsed = Lists.newArrayListWithCapacity(groupPlayerStats.getStats().size() + 1);
        parsed.add(new String[]{"Game", "Score", "Position"});

        final List<PlayerEntry> statEntries = new ArrayList<>(groupPlayerStats.getStats());
        statEntries.sort(Comparator.comparing(entry -> entry.getLeaderboard().getGame().getCleanName()));

        final boolean aboveLimit = statEntries.size() > ROW_SOFT_LIMIT;
        ZonedDateTime highestTime = LocalDateTime.MIN.atZone(ZoneId.systemDefault());
        for (final PlayerEntry entry : statEntries) {
            // Don't show empty rows if we are above the limit
            if (aboveLimit && entry.getScore() == -1) {
                continue;
            }

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

        final String[] tableHeader = this.getTableHeader(groupPlayerStats);
        final String[][] leaderboard = parsed.toArray(new String[0][3]);
        return new ParserResult(
                leaderboard,
                tableHeader,
                highestTime
        );
    }

    @SneakyThrows
    @Override
    protected CommandResult onStatsCommand(final CommandParameters commandParameters) {
        final String playerName = JavaArgumentParsingUtilities.getJavaPlayerNameOrThrow(commandParameters, 0);
        final UUID playerUUID = JavaArgumentParsingUtilities.getPlayerUUIDOrThrow(commandParameters, playerName);
        final String groupName = commandParameters.getArg(GROUP_POSITION);
        final String stat = commandParameters.getArg(STAT_POSITION);
        final String board = commandParameters.getArgOrDefault(BOARD_POSITION, ArgumentParsingUtilities.getDefaultBoard());
        final ZonedDateTime zonedDateTime = ArgumentParsingUtilities.getDateTimeOrThrow(commandParameters, 4);
        final Set<Reason> filterReasons = this.getFilterReasons(commandParameters);

        final CompletableFuture<BufferedImage> skinFuture = JavaUtilities.getPlayerSkin(playerUUID);
        final Optional<GroupPlayerStats<JavaPlayer>> playerStatsOpt = this.getPlayerStats(
                commandParameters,
                playerName,
                groupName,
                stat,
                board,
                zonedDateTime,
                filterReasons
        );
        if (playerStatsOpt.isEmpty()) {
            ErrorMessageUtilities.sendNotDataFoundMessage(commandParameters);
            return BaseCommandResult.SUCCESSFUL;
        }

        final GroupPlayerStats<JavaPlayer> groupPlayerStats = playerStatsOpt.get();
        final ParserResult parserResult = this.parsePlayerStats(groupPlayerStats);

        final String formattedSaveTime = FormationUtilities.getFormattedTime(parserResult.getHighestTime());
        final String subHeader = "Java - " + formattedSaveTime;

        BufferedImage skin;
        try {
            skin = skinFuture.get(10, TimeUnit.SECONDS);
        } catch (final ExecutionException | TimeoutException e) {
            skin = null;
        }

        return this.sendPicture(
                commandParameters,
                new PictureTable(
                        parserResult.getHeader(),
                        subHeader,
                        parserResult.getLeaderboard(),
                        skin
                ).generatePicture(),
                String.format("%s-%s", String.join("-", parserResult.getHeader()), subHeader)
        );
    }
}
