package de.timmi6790.mineplex_stats.utilities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BiggestLong {
    private long number = 0;

    public boolean tryNumber(final long number) {
        if (number > this.number) {
            this.number = number;
            return true;
        }
        return false;
    }

    public long get() {
        return this.number;
    }

    public long ifValueReplace(final long ifValue, final long replaceValue) {
        return this.getNumber() == ifValue ? replaceValue : this.getNumber();
    }
}
