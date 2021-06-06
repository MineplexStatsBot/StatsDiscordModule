package de.timmi6790.mineplex.stats.common.utilities;

import lombok.experimental.UtilityClass;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;

@UtilityClass
public class DateUtilities {
    // TODO: FIX ME
    public Optional<ZonedDateTime> parseZonedDateTime(final String input, final String[] dateFormats) {
        for (final String formatString : dateFormats) {
            final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(formatString);

            try {
                // TODO: Parse at UTC time
                System.out.println(input + " " + formatter);

                return Optional.of(ZonedDateTime.parse(input, formatter));
            } catch (final DateTimeParseException ignore) {
                ignore.printStackTrace();
            }
        }
        return Optional.empty();
    }

    public static void main(final String[] args) {
        final String[] ALLOWED_DATE_FORMATS = new String[]{
                "MM.dd.yyyy",
                "MM.dd.yyyy HH",
                "MM.dd.yyyy HH:mm",
                "MM.dd.yyyy HH:mm:ss"
        };

        System.out.println(parseZonedDateTime("06.03.2021", ALLOWED_DATE_FORMATS));
    }
}
