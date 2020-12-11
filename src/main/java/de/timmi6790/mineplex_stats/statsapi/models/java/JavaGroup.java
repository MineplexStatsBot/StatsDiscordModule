package de.timmi6790.mineplex_stats.statsapi.models.java;

import de.timmi6790.discord_framework.DiscordBot;
import de.timmi6790.mineplex_stats.MineplexStatsModule;
import de.timmi6790.mineplex_stats.utilities.StatsComparator;
import lombok.Data;
import lombok.Getter;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Data
public class JavaGroup {
    private final String group;
    private final String description;
    private final String[] aliasNames;
    private final List<String> games;
    private List<JavaStat> groupStats;

    @Getter(lazy = true)
    private static final MineplexStatsModule module = DiscordBot.getInstance().getModuleManager().getModuleOrThrow(MineplexStatsModule.class);

    public JavaGroup(final String group, final String description, final String[] aliasNames,
                     final List<String> games, final List<JavaStat> groupStats) {
        this.group = group;
        this.description = description;
        this.aliasNames = aliasNames.clone();
        this.games = games;
        this.groupStats = groupStats;
    }

    public String[] getAliasNames() {
        return this.aliasNames.clone();
    }

    public String getName() {
        return this.group;
    }

    public List<JavaGame> getGames() {
        final List<JavaGame> parsedGames = new ArrayList<>();
        for (final String gameName : this.games) {
            getModule().getJavaGame(gameName).ifPresent(parsedGames::add);
        }

        return parsedGames;
    }

    public List<JavaGame> getGames(final JavaStat stat) {
        final List<JavaGame> foundGames = new ArrayList<>();
        for (final JavaGame game : this.getGames()) {
            game.getStat(stat.getName()).ifPresent(f -> foundGames.add(game));
        }

        return foundGames;
    }

    public List<String> getGameNames() {
        return this.games;
    }

    public List<JavaStat> getStats() {
        if (this.groupStats == null) {
            this.groupStats = this.getGames()
                    .stream()
                    .flatMap(game -> game.getStats().values().stream())
                    .sorted(new StatsComparator())
                    .distinct()
                    .collect(Collectors.toList());
        }

        return this.groupStats;
    }

    public Set<String> getStatNames() {
        return this.getStats()
                .stream()
                .map(JavaStat::getPrintName)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
