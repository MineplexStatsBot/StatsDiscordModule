package de.timmi6790.mineplex.stats.common.utilities;

import lombok.experimental.UtilityClass;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;
import java.util.Optional;

@UtilityClass
public class DateUtilities {
    public Optional<ZonedDateTime> parseZonedDateTime(final String input, final String[] dateFormats) {
        for (final String formatString : dateFormats) {
            // Maybe we need to cache them based on the usages
            final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(formatString);

            final TemporalAccessor temporalAccessor;
            try {
                temporalAccessor = formatter.parse(input);
            } catch (final DateTimeParseException ignore) {
                continue;
            }

            // We can't parse it to local date time directly if the input has no time,
            // that is the reason we need to fall back to local date
            LocalDateTime localDateTime;
            try {
                localDateTime = LocalDateTime.from(temporalAccessor);
            } catch (final DateTimeException ignore) {
                final LocalDate date = LocalDate.from(temporalAccessor);
                localDateTime = LocalDateTime.of(date, LocalTime.MIN);
            }

            return Optional.of(ZonedDateTime.of(localDateTime, ZoneId.of("UTC")));
        }
        return Optional.empty();
    }
}
