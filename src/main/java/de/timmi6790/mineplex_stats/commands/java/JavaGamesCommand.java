package de.timmi6790.mineplex_stats.commands.java;

import de.timmi6790.discord_framework.datatypes.builders.MultiEmbedBuilder;
import de.timmi6790.discord_framework.modules.command.CommandModule;
import de.timmi6790.discord_framework.modules.command.CommandParameters;
import de.timmi6790.discord_framework.modules.command.CommandResult;
import de.timmi6790.discord_framework.modules.command.properties.ExampleCommandsCommandProperty;
import de.timmi6790.mineplex_stats.statsapi.models.java.JavaGame;
import de.timmi6790.mineplex_stats.statsapi.models.java.JavaStat;
import lombok.EqualsAndHashCode;

import java.util.Comparator;
import java.util.TreeMap;
import java.util.stream.Collectors;

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
            final MultiEmbedBuilder message = getEmbedBuilder(commandParameters)
                    .setTitle("Java Games")
                    .setFooter("TIP: Run " + this.getModule().getModuleOrThrow(CommandModule.class).getMainCommand() + " games <game> to see more details");

            this.getStatsModule().getJavaGames().values().stream()
                    .collect(Collectors.groupingBy(JavaGame::getCategory, TreeMap::new, Collectors.toList()))
                    .forEach((key, value) ->
                            message.addField(
                                    key,
                                    value.stream()
                                            .map(JavaGame::getName)
                                            .sorted(Comparator.naturalOrder())
                                            .collect(Collectors.joining(", ")),
                                    false
                            ));

            sendTimedMessage(commandParameters, message, 150);
            return CommandResult.SUCCESS;
        }

        // Game info
        final JavaGame game = this.getGame(commandParameters, 0);
        if (commandParameters.getArgs().length == 1) {
            final String stats = game.getStatNames()
                    .stream()
                    .map(stat -> stat.replace(" ", ""))
                    .collect(Collectors.joining(", "));
            sendTimedMessage(
                    commandParameters,
                    getEmbedBuilder(commandParameters)
                            .setTitle("Java Games - " + game.getName())
                            .addField("Wiki", "[" + game.getName() + "](" + game.getWikiUrl() + ")", false, !game.getWikiUrl().isEmpty())
                            .addField("Description", game.getDescription(), false, !game.getDescription().isEmpty())
                            .addField("Alias names", String.join(", ", game.getAliasNames()), false, game.getAliasNames().length > 0)
                            .addField("Stats (You don't need to type Achievement in front of it)", stats, false)
                            .setFooter("TIP: Run " + this.getModule().getModuleOrThrow(CommandModule.class).getMainCommand() + " games " + game.getName() + " <stat> to see more details"),
                    90
            );

            return CommandResult.SUCCESS;
        }

        // Stat info
        final JavaStat stat = this.getStat(game, commandParameters, 1);
        sendTimedMessage(
                commandParameters,
                getEmbedBuilder(commandParameters)
                        .setTitle("Java Games - " + game.getName() + " - " + stat.getPrintName())
                        .addField("Description", stat.getDescription(), false, !stat.getDescription().isEmpty())
                        .addField("Alias names", String.join(", ", stat.getAliasNames()), false, stat.getAliasNames().length > 0)
                        .addField("Boards", String.join(", ", stat.getBoardNames()), false),
                150
        );
        return CommandResult.SUCCESS;
    }
}
