package de.timmi6790.mineplex_stats.commands.java;

import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import de.timmi6790.discord_framework.DiscordBot;
import de.timmi6790.discord_framework.modules.command.CommandModule;
import de.timmi6790.discord_framework.modules.command.CommandParameters;
import de.timmi6790.discord_framework.modules.command.exceptions.CommandReturnException;
import de.timmi6790.discord_framework.utilities.DataUtilities;
import de.timmi6790.mineplex_stats.commands.AbstractStatsCommand;
import de.timmi6790.mineplex_stats.commands.java.info.JavaGamesCommand;
import de.timmi6790.mineplex_stats.commands.java.info.JavaGroupsGroupsCommand;
import de.timmi6790.mineplex_stats.statsapi.models.java.JavaBoard;
import de.timmi6790.mineplex_stats.statsapi.models.java.JavaGame;
import de.timmi6790.mineplex_stats.statsapi.models.java.JavaGroup;
import de.timmi6790.mineplex_stats.statsapi.models.java.JavaStat;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import net.dv8tion.jda.api.utils.MarkdownUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@EqualsAndHashCode(callSuper = true)
public abstract class AbstractJavaStatsCommand extends AbstractStatsCommand {
    private static final Pattern NAME_PATTERN = Pattern.compile("^\\w{1,16}$");
    private static final List<String> STATS_TIME = new ArrayList<>(Arrays.asList("Ingame Time", "Hub Time", "Time Playing"));


    private static final AsyncLoadingCache<UUID, BufferedImage> SKIN_CACHE = Caffeine.newBuilder()
            .maximumSize(10_000)
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .expireAfterAccess(2, TimeUnit.MINUTES)
            .buildAsync(uuid -> {
                final HttpResponse<byte[]> response = Unirest.get("https://visage.surgeplay.com/frontfull/{uuid}.png")
                        .routeParam("uuid", uuid.toString().replace("-", ""))
                        .connectTimeout(10_000)
                        .asBytes();

                if (!response.isSuccess()) {
                    return null;
                }

                try (final InputStream in = new ByteArrayInputStream(response.getBody())) {
                    return ImageIO.read(in);
                } catch (final IOException e) {
                    DiscordBot.getLogger().error(e);
                    DiscordBot.getInstance().getSentry().sendException(e);
                    return null;
                }
            });

    public AbstractJavaStatsCommand(final String name, final String description, final String syntax, final String... aliasNames) {
        super(name, "MineplexStats - Java", description, syntax, aliasNames);
    }

    protected String getFormattedScore(final JavaStat stat, final long score) {
        if (STATS_TIME.contains(stat.getName())) {
            return this.getFormattedTime(score);
        }

        return this.getFormattedNumber(score);
    }

    protected CompletableFuture<BufferedImage> getPlayerSkin(@NonNull final UUID uuid) {
        return SKIN_CACHE.get(uuid);
    }

    // Arg Parsing
    protected JavaGame getGame(final CommandParameters commandParameters, final int argPos) {
        final String name = commandParameters.getArgs()[argPos];
        final Optional<JavaGame> game = this.getModule().getJavaGame(name);
        if (game.isPresent()) {
            return game.get();
        }

        final List<JavaGame> similarGames = this.getModule().getSimilarJavaGames(name, 0.6, 3);
        if (!similarGames.isEmpty() && commandParameters.getUserDb().hasAutoCorrection()) {
            return similarGames.get(0);
        }

        this.sendHelpMessage(
                commandParameters,
                name,
                argPos,
                "game",
                this.getModule().getModuleOrThrow(CommandModule.class)
                        .getCommand(JavaGamesCommand.class)
                        .orElse(null),
                new String[0],
                similarGames.stream()
                        .map(JavaGame::getName)
                        .collect(Collectors.toList())
        );
        throw new CommandReturnException();
    }

    protected JavaStat getStat(final JavaGame game, final CommandParameters commandParameters, final int argPos) {
        final String name = commandParameters.getArgs()[argPos];
        final Optional<JavaStat> stat = game.getStat(name);
        if (stat.isPresent()) {
            return stat.get();
        }

        final List<JavaStat> similarStats = game.getSimilarStats(JavaGame.getCleanStat(name), 0.6, 3);
        if (!similarStats.isEmpty() && commandParameters.getUserDb().hasAutoCorrection()) {
            return similarStats.get(0);
        }

        this.sendHelpMessage(
                commandParameters,
                name,
                argPos,
                "stat",
                this.getModule().getModuleOrThrow(CommandModule.class)
                        .getCommand(JavaGamesCommand.class)
                        .orElse(null),
                new String[]{game.getName()},
                similarStats.stream()
                        .map(JavaStat::getName)
                        .collect(Collectors.toList())
        );
        throw new CommandReturnException();
    }

    protected JavaStat getStat(final CommandParameters commandParameters, final int argPos) {
        final String name = commandParameters.getArgs()[argPos];
        final Optional<JavaStat> stat = this.getModule().getJavaGames().values()
                .stream()
                .map(game -> game.getStat(name))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .max(Comparator.comparingInt(javaStat -> javaStat.getBoards().size()));
        if (stat.isPresent()) {
            return stat.get();
        }

        sendTimedMessage(
                commandParameters,
                getEmbedBuilder(commandParameters)
                        .setTitle("Invalid Stat")
                        .setDescription(MarkdownUtil.monospace(name) + " is not a valid stat. " +
                                "\nTODO: Add help emotes." +
                                "\n In the meantime just use any valid stat name, if that is not working scream at me."),
                90
        );

        throw new CommandReturnException();
    }

    protected JavaBoard getBoard(final JavaGame game, final CommandParameters commandParameters, final int argPos) {
        final String name = (argPos >= commandParameters.getArgs().length || commandParameters.getArgs()[argPos] == null) ? "All" : commandParameters.getArgs()[argPos];

        for (final JavaStat stat : game.getStats().values()) {
            final Optional<JavaBoard> javaBoardOpt = stat.getBoard(name);
            if (javaBoardOpt.isPresent()) {
                return javaBoardOpt.get();
            }
        }

        final List<String> similarBoards = DataUtilities.getSimilarityList(
                name,
                game.getStats().values()
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
                name,
                argPos,
                "board",
                this.getModule().getModuleOrThrow(CommandModule.class)
                        .getCommand(JavaGamesCommand.class)
                        .orElse(null),
                new String[]{game.getName(), game.getStats().values().stream().findFirst().map(JavaStat::getName).orElse("")},
                similarBoards
        );
        throw new CommandReturnException();
    }

    protected JavaBoard getBoard(final JavaGame game, final JavaStat stat, final CommandParameters commandParameters, final int argPos) {
        final String name = argPos >= commandParameters.getArgs().length ? "All" : commandParameters.getArgs()[argPos];
        final Optional<JavaBoard> board = stat.getBoard(name);
        if (board.isPresent()) {
            return board.get();
        }

        final List<JavaBoard> similarBoards = stat.getSimilarBoard(name, 0.0, 6);
        if (!similarBoards.isEmpty() && commandParameters.getUserDb().hasAutoCorrection()) {
            return similarBoards.get(0);
        }

        this.sendHelpMessage(
                commandParameters,
                name,
                argPos,
                "board",
                this.getModule().getModuleOrThrow(CommandModule.class)
                        .getCommand(JavaGamesCommand.class)
                        .orElse(null),
                new String[]{game.getName(), stat.getName()},
                similarBoards.stream()
                        .map(JavaBoard::getName)
                        .collect(Collectors.toList())
        );
        throw new CommandReturnException();
    }

    protected JavaBoard getBoard(final JavaStat stat, final CommandParameters commandParameters, final int argPos) {
        final String name = argPos >= commandParameters.getArgs().length ? "All" : commandParameters.getArgs()[argPos];
        final Optional<JavaBoard> board = stat.getBoard(name);
        if (board.isPresent()) {
            return board.get();
        }

        sendTimedMessage(
                commandParameters,
                getEmbedBuilder(commandParameters)
                        .setTitle("Invalid Board")
                        .setDescription(MarkdownUtil.monospace(name) + " is not a valid board. " +
                                "\nTODO: Add help emotes." +
                                "\nHow did you do this?! Just pick all, yearly, weekly, daily or monthly."),
                90
        );

        throw new CommandReturnException();
    }

    protected String getPlayer(final CommandParameters commandParameters, final int argPos) {
        final String name = commandParameters.getArgs()[argPos];
        if (NAME_PATTERN.matcher(name).find()) {
            return name;
        }

        throw new CommandReturnException(
                getEmbedBuilder(commandParameters)
                        .setTitle("Invalid Name")
                        .setDescription(MarkdownUtil.monospace(name) + " is not a minecraft name.")
        );
    }

    public JavaGroup getJavaGroup(final CommandParameters commandParameters, final int argPos) {
        final String name = commandParameters.getArgs()[argPos];
        final Optional<JavaGroup> group = this.getModule().getJavaGroup(name);
        if (group.isPresent()) {
            return group.get();
        }

        final List<JavaGroup> similarGroup = this.getModule().getSimilarJavaGroups(name, 0.6, 3);
        if (!similarGroup.isEmpty() && commandParameters.getUserDb().hasAutoCorrection()) {
            return similarGroup.get(0);
        }

        this.sendHelpMessage(
                commandParameters,
                name,
                argPos,
                "group",
                this.getModule().getModuleOrThrow(CommandModule.class)
                        .getCommand(JavaGroupsGroupsCommand.class)
                        .orElse(null),
                new String[]{},
                similarGroup.stream()
                        .map(JavaGroup::getName)
                        .collect(Collectors.toList())
        );
        throw new CommandReturnException();
    }

    public JavaStat getJavaStat(final JavaGroup group, final CommandParameters commandParameters, final int argPos) {
        final String name = commandParameters.getArgs()[argPos];

        for (final JavaGame game : group.getGames()) {
            final Optional<JavaStat> statOpt = game.getStat(name);
            if (statOpt.isPresent()) {
                return statOpt.get();
            }
        }

        final List<JavaStat> similarStats = DataUtilities.getSimilarityList(
                JavaGame.getCleanStat(name),
                group.getStats(),
                JavaStat::getName,
                0.6,
                3
        );
        if (!similarStats.isEmpty() && commandParameters.getUserDb().hasAutoCorrection()) {
            return similarStats.get(0);
        }

        this.sendHelpMessage(
                commandParameters,
                name,
                argPos,
                "stat",
                this.getModule().getModuleOrThrow(CommandModule.class)
                        .getCommand(JavaGroupsGroupsCommand.class)
                        .orElse(null),
                new String[]{group.getName()},
                similarStats.stream()
                        .map(JavaStat::getName)
                        .collect(Collectors.toList())
        );
        throw new CommandReturnException();
    }
}
