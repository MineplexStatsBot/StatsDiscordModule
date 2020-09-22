package de.timmi6790.mineplex_stats.statsapi.models.java;

import lombok.Data;
import lombok.NonNull;

@Data
public class JavaBoard {
    private final String name;
    private final String[] aliasNames;

    public JavaBoard(@NonNull final String name, @NonNull final String[] aliasNames) {
        this.name = name;
        this.aliasNames = aliasNames.clone();
    }

    public String[] getAliasNames() {
        return this.aliasNames.clone();
    }
}
