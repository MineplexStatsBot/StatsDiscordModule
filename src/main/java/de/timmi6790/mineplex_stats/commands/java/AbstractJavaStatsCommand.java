package de.timmi6790.mineplex_stats.commands.java;

import de.timmi6790.discord_framework.modules.command.CommandParameters;
import de.timmi6790.discord_framework.modules.command.exceptions.CommandReturnException;
import de.timmi6790.discord_framework.utilities.DataUtilities;
import de.timmi6790.minecraft.mojang_api.MojangApi;
import de.timmi6790.minecraft.mojang_api.models.MojangUser;
import de.timmi6790.minecraft.utilities.JavaUtilities;
import de.timmi6790.mineplex_stats.commands.AbstractStatsCommand;
import de.timmi6790.mineplex_stats.commands.java.info.JavaGamesCommand;
import de.timmi6790.mineplex_stats.commands.java.info.JavaGroupsGroupsCommand;
import de.timmi6790.mineplex_stats.settings.JavaNameReplacementSetting;
import de.timmi6790.mineplex_stats.settings.NameReplacementSetting;
import de.timmi6790.mineplex_stats.statsapi.models.java.JavaBoard;
import de.timmi6790.mineplex_stats.statsapi.models.java.JavaGame;
import de.timmi6790.mineplex_stats.statsapi.models.java.JavaGroup;
import de.timmi6790.mineplex_stats.statsapi.models.java.JavaStat;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.utils.MarkdownUtil;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@EqualsAndHashCode(callSuper = true)
public abstract class AbstractJavaStatsCommand extends AbstractStatsCommand {
    private static final List<String> STATS_TIME = Arrays.asList("Ingame Time", "Hub Time", "Time Playing");

    protected AbstractJavaStatsCommand(final String name,
                                       final String description,
                                       final String syntax,
                                       final String... aliasNames) {
        super(name, "MineplexStats - Java", description, syntax, aliasNames);
    }

    protected String getFormattedScore(final JavaStat stat, final long score) {
        if (STATS_TIME.contains(stat.getName())) {
            return this.getFormattedTime(score);
        }

        return this.getFormattedNumber(score);
    }

    protected CompletableFuture<BufferedImage> getPlayerSkin(@NonNull final UUID uuid) {
        return JavaUtilities.getPlayerSkin(uuid);
    }

    @SneakyThrows
    protected <T> T awaitOrDefault(final CompletableFuture<T> completableFuture, final T defaultValue) {
        try {
            return completableFuture.get();
        } catch (final ExecutionException ignore) {
            return defaultValue;
        }
    }

    // Arg Parsing
    protected UUID getPlayerUUIDFromNameThrow(final CommandParameters commandParameters, final int argPos) {
        final String playerName = this.getPlayer(commandParameters, argPos);
        final Optional<MojangUser> mojangUser = MojangApi.getUser(playerName);
        if (mojangUser.isPresent()) {
            return mojangUser.get().getUuid();
        }

        throw new CommandReturnException(
                this.getEmbedBuilder(commandParameters)
                        .setTitle("Invalid User")
                        .appendDescription(
                                "The user %s does not exist.\n" +
                                        "Are you sure that you typed his name correctly?",
                                MarkdownUtil.monospace(playerName)
                        )
        );
    }

    protected JavaGame getGame(final CommandParameters commandParameters, final int argPos) {
        return this.getArgumentOrThrow(
                commandParameters,
                "game",
                argPos,
                gameName -> this.getMineplexStatsModule()
                        .getJavaGame(gameName),
                JavaGame::getName,
                () -> this.getMineplexStatsModule().getJavaGames(),
                () -> new String[0],
                JavaGamesCommand.class
        );
    }

    protected JavaStat getStat(final JavaGame game, final CommandParameters commandParameters, final int argPos) {
        return this.getArgumentOrThrow(
                commandParameters,
                "stat",
                argPos,
                game::getStat,
                JavaStat::getName,
                () -> game.getStats().values(),
                () -> new String[]{game.getName()},
                JavaGamesCommand.class
        );
    }

    protected JavaStat getStat(final CommandParameters commandParameters, final int argPos) {
        final String userInput = this.getArg(commandParameters, argPos);

        // Tries to locate the stat with the requested name and the highest board size.
        // We need the highest board size here to prevent an issue that it would later not find the board in the stat
        // This is a shitty fix for this issue and should be changed in the future
        JavaStat foundStat = null;
        for (final JavaGame game : this.getMineplexStatsModule().getJavaGames()) {
            final Optional<JavaStat> statOpt = game.getStat(userInput);
            if (statOpt.isPresent() &&
                    (foundStat == null || statOpt.get().getBoards().size() > foundStat.getBoards().size())) {
                foundStat = statOpt.get();
            }
        }
        if (foundStat != null) {
            return foundStat;
        }

        this.sendTimedMessage(
                commandParameters,
                this.getEmbedBuilder(commandParameters)
                        .setTitle("Invalid Stat")
                        .setDescription(MarkdownUtil.monospace(userInput) + " is not a valid stat. " +
                                "\nTODO: Add help emotes." +
                                "\n In the meantime just use any valid stat name, if that is not working scream at me."),
                90
        );

        throw new CommandReturnException();
    }

    protected JavaBoard getBoard(final JavaGame game, final CommandParameters commandParameters, final int argPos) {
        final String userInput = this.getArgOrDefault(commandParameters, argPos, "All");
        for (final JavaStat stat : game.getStats().values()) {
            final Optional<JavaBoard> javaBoardOpt = stat.getBoard(userInput);
            if (javaBoardOpt.isPresent()) {
                return javaBoardOpt.get();
            }
        }

        final List<String> similarBoards = DataUtilities.getSimilarityList(
                userInput,
                game.getStats()
                        .values()
                        .stream()
                        .flatMap(stat -> stat.getBoardNames().stream())
                        .collect(Collectors.toSet()),
                0.0,
                6
        );
        if (!similarBoards.isEmpty() && commandParameters.getUserDb().hasAutoCorrection()) {
            final String newBoard = similarBoards.get(0);
            final Optional<JavaBoard> similarBoard = game.getStats().values().stream()
                    .flatMap(stat -> stat.getBoards().values().stream())
                    .filter(board -> board.getName().equalsIgnoreCase(newBoard))
                    .findAny();

            if (similarBoard.isPresent()) {
                return similarBoard.get();
            }
        }

        this.sendHelpMessage(
                commandParameters,
                userInput,
                argPos,
                "board",
                JavaGamesCommand.class,
                new String[]{
                        game.getName(),
                        game.getStats().values().stream().findFirst().map(JavaStat::getName).orElse("")
                },
                similarBoards
        );
        throw new CommandReturnException();
    }

    protected JavaBoard getBoard(final JavaGame game,
                                 final JavaStat stat,
                                 final CommandParameters commandParameters,
                                 final int argPos) {
        return this.getArgumentDefaultOrThrow(
                commandParameters,
                "board",
                argPos,
                "All",
                stat::getBoard,
                JavaBoard::getName,
                () -> stat.getBoards().values(),
                () -> new String[]{game.getName(), stat.getName()},
                JavaGamesCommand.class
        );
    }

    protected JavaBoard getBoard(final JavaStat stat, final CommandParameters commandParameters, final int argPos) {
        final String userInput = this.getArgOrDefault(commandParameters, argPos, "All");
        final Optional<JavaBoard> board = stat.getBoard(userInput);
        if (board.isPresent()) {
            return board.get();
        }

        this.sendTimedMessage(
                commandParameters,
                this.getEmbedBuilder(commandParameters)
                        .setTitle("Invalid Board")
                        .setDescription(MarkdownUtil.monospace(userInput) + " is not a valid board. " +
                                "\nTODO: Add help emotes." +
                                "\nHow did you do this?! Just pick all, yearly, weekly, daily or monthly."),
                90
        );

        throw new CommandReturnException();
    }

    protected String getPlayer(final CommandParameters commandParameters, final int argPos) {
        String userInput = this.getArg(commandParameters, argPos);

        // Check for setting
        if (userInput.equalsIgnoreCase(NameReplacementSetting.getKeyword())) {
            final String settingName = commandParameters.getUserDb().getSettingOrDefault(JavaNameReplacementSetting.class, "");
            if (!settingName.isEmpty()) {
                userInput = settingName;
            }
        }

        if (JavaUtilities.isValidName(userInput)) {
            return userInput;
        }

        throw new CommandReturnException(
                this.getEmbedBuilder(commandParameters)
                        .setTitle("Invalid Name")
                        .setDescription(MarkdownUtil.monospace(userInput) + " is not a minecraft name.")
        );
    }

    protected JavaGroup getJavaGroup(final CommandParameters commandParameters, final int argPos) {
        return this.getArgumentOrThrow(
                commandParameters,
                "group",
                argPos,
                groupName -> this.getMineplexStatsModule().getJavaGroup(groupName),
                JavaGroup::getName,
                () -> this.getMineplexStatsModule().getJavaGroups(),
                () -> new String[]{},
                JavaGroupsGroupsCommand.class
        );
    }

    protected JavaStat getJavaStat(final JavaGroup group, final CommandParameters commandParameters, final int argPos) {
        final String userInput = this.getArg(commandParameters, argPos);
        for (final JavaGame game : group.getGames()) {
            final Optional<JavaStat> statOpt = game.getStat(userInput);
            if (statOpt.isPresent()) {
                return statOpt.get();
            }
        }

        final List<JavaStat> similarStats = this.getSimilarityList(
                JavaGame.getCleanStat(userInput),
                group.getStats(),
                JavaStat::getName
        );
        if (!similarStats.isEmpty() && commandParameters.getUserDb().hasAutoCorrection()) {
            return similarStats.get(0);
        }

        this.sendHelpMessage(
                commandParameters,
                userInput,
                argPos,
                "stat",
                JavaGroupsGroupsCommand.class,
                new String[]{group.getName()},
                this.listToStringList(similarStats, JavaStat::getName)
        );
        throw new CommandReturnException();
    }
}
