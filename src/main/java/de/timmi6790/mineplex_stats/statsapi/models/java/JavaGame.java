package de.timmi6790.mineplex_stats.statsapi.models.java;

import de.timmi6790.mineplex_stats.utilities.StatsComparator;
import lombok.Data;

import java.util.*;
import java.util.regex.Pattern;

@Data
public class JavaGame {
    private static final Pattern ILLEGAL_STAT_CHARACTERS = Pattern.compile(
            "([ !<,\\.?`'])|(Achievement)",
            Pattern.CASE_INSENSITIVE
    );

    private final String name;
    private final String[] aliasNames;
    private final String category;
    private final String wikiUrl;
    private final String description;

    private final Map<String, JavaStat> stats;
    private final Map<String, String> statAlias = new HashMap<>();
    private final List<String> sortedStatsNames = new ArrayList<>();

    public JavaGame(final String name,
                    final String[] aliasNames,
                    final String category,
                    final String wikiUrl,
                    final String description,
                    final Map<String, JavaStat> stats) {
        this.name = name;
        this.aliasNames = aliasNames.clone();
        this.category = category;
        this.wikiUrl = wikiUrl;
        this.description = description;
        this.stats = stats;

        stats.values().forEach(stat -> {
            final String cleanStat = getCleanStat(stat.getName()).toLowerCase();
            for (final String alias : stat.getAliasNames()) {
                this.statAlias.put(alias.toLowerCase(), cleanStat);
            }
        });
    }

    public static String getCleanStat(final String name) {
        return ILLEGAL_STAT_CHARACTERS.matcher(name).replaceAll("");
    }

    public String[] getAliasNames() {
        return this.aliasNames.clone();
    }

    public Optional<JavaStat> getStat(String name) {
        name = JavaGame.getCleanStat(name);
        name = this.statAlias.getOrDefault(name.toLowerCase(), name.toLowerCase());
        return Optional.ofNullable(this.stats.get(name));
    }

    public List<String> getStatNames() {
        if (this.sortedStatsNames.isEmpty()) {
            final List<JavaStat> sortedStats = new ArrayList<>(this.stats.values());
            sortedStats.sort(new StatsComparator());
            for (final JavaStat stat : sortedStats) {
                this.sortedStatsNames.add(stat.getName());
            }
        }

        return this.sortedStatsNames;
    }
}