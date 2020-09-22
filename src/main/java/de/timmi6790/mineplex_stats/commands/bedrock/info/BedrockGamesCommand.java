package de.timmi6790.mineplex_stats.commands.bedrock.info;

import de.timmi6790.discord_framework.datatypes.builders.MultiEmbedBuilder;
import de.timmi6790.discord_framework.modules.command.CommandParameters;
import de.timmi6790.discord_framework.modules.command.CommandResult;
import de.timmi6790.mineplex_stats.commands.bedrock.AbstractBedrockStatsCommand;
import de.timmi6790.mineplex_stats.statsapi.models.bedrock.BedrockGame;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class BedrockGamesCommand extends AbstractBedrockStatsCommand {
    public BedrockGamesCommand() {
        super("bgames", "Bedrock games", "", "bg");
    }

    @Override
    protected CommandResult onCommand(final CommandParameters commandParameters) {
        final MultiEmbedBuilder message = getEmbedBuilder(commandParameters)
                .setTitle("Bedrock Games");

        final Map<String, List<BedrockGame>> sortedMap = this.getModule().getBedrockGames().values()
                .stream()
                .collect(Collectors.groupingBy(k -> k.isRemoved() ? "Removed" : "Games", TreeMap::new, Collectors.toList()));

        for (final Map.Entry<String, List<BedrockGame>> entry : sortedMap.entrySet()) {
            message.addField(
                    entry.getKey(),
                    entry.getValue().stream()
                            .map(BedrockGame::getName)
                            .sorted(Comparator.naturalOrder())
                            .collect(Collectors.joining("\n")),
                    false
            );
        }

        sendTimedMessage(commandParameters, message, 150);
        return CommandResult.SUCCESS;
    }
}
