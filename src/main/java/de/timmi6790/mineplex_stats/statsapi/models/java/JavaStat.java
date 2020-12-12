package de.timmi6790.mineplex_stats.statsapi.models.java;

import lombok.Data;
import org.apache.commons.collections4.map.CaseInsensitiveMap;

import java.util.*;

@Data
public class JavaStat {
    private final String name;
    private final String[] aliasNames;
    private final boolean achievement;
    private final String description;

    private final Map<String, JavaBoard> boards = new CaseInsensitiveMap<>();
    private final Map<String, String> boardAlias = new CaseInsensitiveMap<>();

    public JavaStat(final String name,
                    final String[] aliasNames,
                    final boolean achievement,
                    final String description,
                    final Map<String, JavaBoard> boards) {
        this.name = name;
        this.aliasNames = aliasNames.clone();
        this.achievement = achievement;
        this.description = description;

        for (final JavaBoard javaBoard : boards.values()) {
            boards.put(javaBoard.getName(), javaBoard);
            for (final String aliasName : javaBoard.getAliasNames()) {
                this.boardAlias.put(aliasName, javaBoard.getName());
            }
        }
    }

    public String[] getAliasNames() {
        return this.aliasNames.clone();
    }

    public String getPrintName() {
        if (this.achievement) {
            return "Achievement " + this.name;
        }

        return this.name;
    }

    public List<String> getBoardNames() {
        return new ArrayList<>(this.boards.keySet());
    }

    public List<String> getBoardNamesSorted() {
        final List<String> javaBoardNames = this.getBoardNames();
        javaBoardNames.sort(Comparator.naturalOrder());
        return javaBoardNames;
    }

    public Optional<JavaBoard> getBoard(String name) {
        name = this.boardAlias.getOrDefault(name.toLowerCase(), name.toLowerCase());
        return Optional.ofNullable(this.boards.get(name));
    }
}