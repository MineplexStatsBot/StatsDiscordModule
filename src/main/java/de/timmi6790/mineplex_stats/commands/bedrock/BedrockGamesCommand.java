package de.timmi6790.mineplex_stats.commands.bedrock;

import de.timmi6790.discord_framework.datatypes.builders.MultiEmbedBuilder;
import de.timmi6790.discord_framework.modules.command.CommandParameters;
import de.timmi6790.discord_framework.modules.command.CommandResult;
import de.timmi6790.mineplex_stats.statsapi.models.bedrock.BedrockGame;

import java.util.Comparator;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class BedrockGamesCommand extends AbstractBedrockStatsCommand {
    public BedrockGamesCommand() {
        super("bgames", "Bedrock games", "", "bg");
    }

    @Override
    protected CommandResult onCommand(final CommandParameters commandParameters) {
        final MultiEmbedBuilder message = this.getEmbedBuilder(commandParameters)
                .setTitle("Bedrock Games");

        this.getStatsModule().getBedrockGames().values()
                .stream()
                .collect(Collectors.groupingBy(BedrockGame::isRemoved, TreeMap::new, Collectors.toList()))
                .forEach((key, value) ->
                        message.addField(
                                key ? "Removed" : "Games",
                                value.stream()
                                        .map(BedrockGame::getName)
                                        .sorted(Comparator.naturalOrder())
                                        .collect(Collectors.joining("\n ")),
                                false
                        ));

        this.sendTimedMessage(commandParameters, message, 150);
        return CommandResult.SUCCESS;
    }
}
