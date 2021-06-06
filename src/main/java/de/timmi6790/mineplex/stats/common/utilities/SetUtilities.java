package de.timmi6790.mineplex.stats.common.utilities;

import lombok.experimental.UtilityClass;

import java.util.Set;

@UtilityClass
public class SetUtilities {
    public <T> T getFirstEntry(final Set<T> values) {
        for (final T value : values) {
            return value;
        }

        throw new IllegalArgumentException("Not possible with an empty set");
    }
}
