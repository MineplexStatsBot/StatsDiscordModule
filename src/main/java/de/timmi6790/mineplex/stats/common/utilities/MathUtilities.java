package de.timmi6790.mineplex.stats.common.utilities;

import lombok.experimental.UtilityClass;

@UtilityClass
public class MathUtilities {
    // https://stackoverflow.com/questions/8911356/whats-the-best-practice-to-round-a-float-to-2-decimals/45772416#45772416
    public static float round(final float value, final int scale) {
        int pow = 10;
        for (int i = 1; i < scale; i++) {
            pow *= 10;
        }
        final float tmp = value * pow;
        final float tmpSub = tmp - (int) tmp;

        return ((float) ((int) (
                value >= 0
                        ? (tmpSub >= 0.5f ? tmp + 1 : tmp)
                        : (tmpSub >= -0.5f ? tmp : tmp - 1)
        ))) / pow;
    }
}
