package de.timmi6790.mineplex.stats.common.commands.leaderboard;

import de.timmi6790.discord_framework.utilities.MultiEmbedBuilder;
import de.timmi6790.mineplex.stats.common.commands.BaseStatsCommand;
import de.timmi6790.mpstats.api.client.common.BaseApiClient;
import de.timmi6790.mpstats.api.client.common.game.models.Game;
import de.timmi6790.mpstats.api.client.common.player.models.Player;
import lombok.NonNull;

import java.util.*;

public abstract class GamesCommand<P extends Player> extends BaseStatsCommand<P> {
    protected GamesCommand(final BaseApiClient<P> apiClient,
                           @NonNull final String name,
                           @NonNull final String category,
                           @NonNull final String description,
                           @NonNull final String syntax,
                           final String... aliasNames) {
        super(apiClient, name, category, description, syntax, aliasNames);
    }

    protected MultiEmbedBuilder parseGames(final List<Game> games, final MultiEmbedBuilder messageBuilder) {
        final Map<String, List<String>> sortedGames = new HashMap<>();
        for (final Game game : games) {
            sortedGames.computeIfAbsent(game.getCategoryName(), k -> new ArrayList<>()).add(game.getGameName());
        }

        final List<String> categories = new ArrayList<>(sortedGames.keySet());
        categories.sort(Comparator.naturalOrder());
        for (final String category : categories) {
            final List<String> gameNames = sortedGames.get(category);
            gameNames.sort(Comparator.naturalOrder());

            messageBuilder.addField(
                    category,
                    String.join(", ", gameNames)
            );
        }

        return messageBuilder;
    }
}
