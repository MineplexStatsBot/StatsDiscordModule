package de.timmi6790.mineplex_stats.commands.java.info;

import de.timmi6790.discord_framework.modules.command.CommandParameters;
import de.timmi6790.discord_framework.modules.command.CommandResult;
import de.timmi6790.discord_framework.modules.command.property.properties.ExampleCommandsCommandProperty;
import de.timmi6790.discord_framework.utilities.MultiEmbedBuilder;
import de.timmi6790.mineplex_stats.commands.java.AbstractJavaStatsCommand;
import de.timmi6790.mineplex_stats.statsapi.models.java.JavaGame;
import de.timmi6790.mineplex_stats.statsapi.models.java.JavaStat;
import lombok.EqualsAndHashCode;
import org.apache.commons.collections4.list.TreeList;

import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.TreeMap;

@EqualsAndHashCode(callSuper = true)
public class JavaGamesCommand extends AbstractJavaStatsCommand {
    public JavaGamesCommand() {
        super("games", "Java Games", "[game] [stat]", "g");

        this.addProperties(
                new ExampleCommandsCommandProperty(
                        "Global",
                        "Global ExpEarned"
                )
        );
    }

    @Override
    protected CommandResult onCommand(final CommandParameters commandParameters) {
        // Show all games
        if (commandParameters.getArgs().length == 0) {
            return this.handleAllGames(commandParameters);
        }

        // Game info
        final JavaGame game = this.getGame(commandParameters, 0);
        if (commandParameters.getArgs().length == 1) {
            return this.handleGameInfo(commandParameters, game);
        }

        // Stat info
        final JavaStat stat = this.getStat(game, commandParameters, 1);
        return this.handleStatInfo(commandParameters, game, stat);
    }

    private CommandResult handleAllGames(final CommandParameters commandParameters) {
        final MultiEmbedBuilder message = this.getEmbedBuilder(commandParameters)
                .setTitle("Java Games")
                .setFooterFormat(
                        "TIP: Run %s %s <game> to see more details",
                        getCommandModule().getMainCommand(),
                        this.getName()
                );

        // Sort java games after category
        final Map<String, List<String>> sortedLeaderboard = new TreeMap<>();
        for (final JavaGame game : this.getMineplexStatsModule().getJavaGames()) {
            sortedLeaderboard.computeIfAbsent(game.getCategory(), k -> new TreeList<>()).add(game.getName());
        }

        for (final Map.Entry<String, List<String>> entry : sortedLeaderboard.entrySet()) {
            message.addField(
                    entry.getKey(),
                    String.join(", ", entry.getValue())
            );
        }

        this.sendTimedMessage(commandParameters, message, 150);
        return CommandResult.SUCCESS;
    }

    private CommandResult handleGameInfo(final CommandParameters commandParameters, final JavaGame game) {
        final StringJoiner stats = new StringJoiner(", ");
        for (final String stat : game.getStatNames()) {
            stats.add(stat.replace(" ", ""));
        }

        this.sendTimedMessage(
                commandParameters,
                this.getEmbedBuilder(commandParameters)
                        .setTitle("Java Games - " + game.getName())
                        .addField(
                                "Wiki",
                                "[" + game.getName() + "](" + game.getWikiUrl() + ")",
                                false,
                                !game.getWikiUrl().isEmpty()
                        )
                        .addField(
                                "Description",
                                game.getDescription(),
                                false,
                                !game.getDescription().isEmpty()
                        )
                        .addField(
                                "Alias names",
                                String.join(", ", game.getAliasNames()),
                                false,
                                game.getAliasNames().length > 0
                        )
                        .addField(
                                "Stats (You don't need to type Achievement in front of it)",
                                stats.toString()
                        )
                        .setFooterFormat(
                                "TIP: Run %s %s %s <stat> to see more details",
                                getCommandModule().getMainCommand(),
                                this.getName(),
                                game.getName()
                        ),
                90
        );

        return CommandResult.SUCCESS;
    }

    private CommandResult handleStatInfo(final CommandParameters commandParameters,
                                         final JavaGame game,
                                         final JavaStat stat) {
        this.sendTimedMessage(
                commandParameters,
                this.getEmbedBuilder(commandParameters)
                        .setTitleFormat(
                                "Java Games - %s - %s",
                                game.getName(),
                                stat.getPrintName()
                        )
                        .addField(
                                "Description",
                                stat.getDescription(),
                                false,
                                !stat.getDescription().isEmpty()
                        )
                        .addField(
                                "Alias names",
                                String.join(", ", stat.getAliasNames()),
                                false,
                                stat.getAliasNames().length > 0
                        )
                        .addField(
                                "Boards",
                                String.join(", ", stat.getBoardNamesSorted()),
                                false
                        ),
                150
        );
        return CommandResult.SUCCESS;
    }
}
