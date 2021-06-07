package de.timmi6790.mineplex.stats.common.utilities;

import de.timmi6790.mpstats.api.client.common.stat.models.Stat;
import de.timmi6790.mpstats.api.client.common.stat.models.StatType;
import lombok.experimental.UtilityClass;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

@UtilityClass
public class FormationUtilities {
    private static final DecimalFormat FORMAT_NUMBER = (DecimalFormat) NumberFormat.getInstance(Locale.US);
    private static final DecimalFormat FORMAT_DECIMAL_POINT = new DecimalFormat(".##");

    static {
        final DecimalFormatSymbols numberSymbol = FORMAT_NUMBER.getDecimalFormatSymbols();
        numberSymbol.setGroupingSeparator(',');
        FORMAT_NUMBER.setDecimalFormatSymbols(numberSymbol);

        final DecimalFormatSymbols dateSymbol = FORMAT_DECIMAL_POINT.getDecimalFormatSymbols();
        dateSymbol.setDecimalSeparator('.');
        FORMAT_DECIMAL_POINT.setDecimalFormatSymbols(dateSymbol);

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

    protected String getFormattedTime(long time) {
        final long days = TimeUnit.SECONDS.toDays(time);
        time -= TimeUnit.DAYS.toSeconds(days);

        final long hours = TimeUnit.SECONDS.toHours(time);
        time -= TimeUnit.HOURS.toSeconds(hours);

        if (days != 0) {
            if (hours == 0) {
                return days + (days > 1 ? " days" : " day");
            }

            return days + FORMAT_DECIMAL_POINT.format(hours / 24D) + " days";
        }

        final long minutes = TimeUnit.SECONDS.toMinutes(time);
        time -= TimeUnit.MINUTES.toSeconds(minutes);
        if (hours != 0) {
            if (minutes == 0) {
                return hours + (hours > 1 ? " hours" : " hour");
            }

            return hours + FORMAT_DECIMAL_POINT.format(minutes / 60D) + " hours";
        }

        final long seconds = TimeUnit.SECONDS.toSeconds(time);
        if (minutes != 0) {
            if (seconds == 0) {
                return minutes + (minutes > 1 ? " minutes" : " minute");
            }

            return minutes + FORMAT_DECIMAL_POINT.format(seconds / 60D) + " minutes";
        }

        return seconds + (seconds > 1 ? " seconds" : " second");
    }

    public String getFormattedScore(final Stat stat, final long score) {
        if (score == -1) {
            return "Unknown";
        }

        if (stat.getType() == StatType.TIME_IN_SECONDS) {
            return getFormattedTime(score);
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
