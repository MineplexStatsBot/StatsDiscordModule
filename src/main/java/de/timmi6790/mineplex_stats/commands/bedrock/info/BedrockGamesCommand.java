package de.timmi6790.mineplex_stats.commands.bedrock.info;

import de.timmi6790.discord_framework.modules.command.CommandParameters;
import de.timmi6790.discord_framework.modules.command.CommandResult;
import de.timmi6790.discord_framework.utilities.MultiEmbedBuilder;
import de.timmi6790.mineplex_stats.commands.bedrock.AbstractBedrockStatsCommand;
import de.timmi6790.mineplex_stats.statsapi.models.bedrock.BedrockGame;
import org.apache.commons.collections4.list.TreeList;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class BedrockGamesCommand extends AbstractBedrockStatsCommand {
    public BedrockGamesCommand() {
        super("bgames", "Bedrock games", "", "bg");
    }

    @Override
    protected CommandResult onCommand(final CommandParameters commandParameters) {
        final MultiEmbedBuilder message = this.getEmbedBuilder(commandParameters)
                .setTitle("Bedrock Games");


        final Map<String, List<String>> sortedGames = new TreeMap<>();
        for (final BedrockGame game : this.getMineplexStatsModule().getBedrockGames()) {
            final String key = game.isRemoved() ? "Removed" : "Games";
            sortedGames.computeIfAbsent(key, k -> new TreeList<>()).add(game.getName());
        }

        for (final Map.Entry<String, List<String>> entry : sortedGames.entrySet()) {
            final List<String> gameNames = entry.getValue();
            gameNames.sort(Comparator.naturalOrder());
            message.addField(
                    entry.getKey(),
                    String.join("\n", gameNames),
                    false
            );
        }

        this.sendTimedMessage(commandParameters, message, 150);
        return CommandResult.SUCCESS;
    }
}
