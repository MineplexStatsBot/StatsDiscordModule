package de.timmi6790.mineplex.stats.common.utilities;

import de.timmi6790.mpstats.api.client.common.stat.models.Stat;
import lombok.experimental.UtilityClass;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@UtilityClass
public class FormationUtilities {
    private static final DecimalFormat FORMAT_NUMBER = (DecimalFormat) NumberFormat.getInstance(Locale.US);

    static {
        final DecimalFormatSymbols numberSymbol = FORMAT_NUMBER.getDecimalFormatSymbols();
        numberSymbol.setGroupingSeparator(',');
        FORMAT_NUMBER.setDecimalFormatSymbols(numberSymbol);
    }

    public String getFormattedTime(final ZonedDateTime zonedDateTime) {
        final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss O");
        return zonedDateTime.withZoneSameInstant(ZoneOffset.UTC).format(dateFormat);
    }

    public String getFormattedNumber(final Number number) {
        return FORMAT_NUMBER.format(number);
    }

    public String getFormattedStat(final Stat stat) {
        return (stat.isAchievement() ? "Achievement" : "") + stat.getCleanName();
    }

    public String getFormattedScore(final Stat stat, final long score) {
        // TODO: Handle stat specific things for time
        if (score == -1) {
            return "Unknown";
        }

        return FormationUtilities.getFormattedNumber(score);
    }

    public String getFormattedPosition(final int position) {
        if (position == -1) {
            return "Unknown";
        }

        return FormationUtilities.getFormattedNumber(position);
    }
}
