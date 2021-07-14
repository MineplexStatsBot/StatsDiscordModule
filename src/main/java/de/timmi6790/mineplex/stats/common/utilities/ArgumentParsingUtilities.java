package de.timmi6790.mineplex.stats.common.utilities;

import de.timmi6790.discord_framework.module.modules.command.exceptions.CommandReturnException;
import de.timmi6790.discord_framework.module.modules.command.models.CommandParameters;
import de.timmi6790.mineplex.stats.common.BaseMineplexStatsModule;
import de.timmi6790.mineplex.stats.common.settings.FilterReasonSetting;
import de.timmi6790.mpstats.api.client.common.filter.models.Reason;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import net.dv8tion.jda.api.utils.MarkdownUtil;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;

@UtilityClass
public class ArgumentParsingUtilities {
    @Getter
    private final String defaultBoard = "All";
    @Getter
    private final String[] allowedDateFormats = new String[]{
            "MM.dd.yyyy",
            "MM.dd.yyyy HH",
            "MM.dd.yyyy HH:mm",
            "MM.dd.yyyy HH:mm:ss"
    };

    public ZonedDateTime getDateTimeOrThrow(final CommandParameters commandParameters, final int startArgPos) {
        if (startArgPos >= commandParameters.getArgs().length) {
            return ZonedDateTime.now();
        }

        final String[] dateArgs = Arrays.copyOfRange(commandParameters.getArgs(), startArgPos, commandParameters.getArgs().length);
        final String dateInput = String.join(" ", dateArgs)
                .replace("/", ".")
                .replace("-", ".");

        final Optional<ZonedDateTime> zonedDateTimeOpt = DateUtilities.parseZonedDateTime(dateInput, allowedDateFormats);
        if (zonedDateTimeOpt.isPresent()) {
            return BaseMineplexStatsModule.getMineplexTimeZone(zonedDateTimeOpt.get());
        }

        // Send error message
        final StringJoiner validDateFormats = new StringJoiner("\n");
        for (final String format : allowedDateFormats) {
            validDateFormats.add(format);
        }


        commandParameters.sendMessage(
                commandParameters.getEmbedBuilder()
                        .setTitle("Invalid Date")
                        .setDescription(MarkdownUtil.monospace(dateInput) + " is not a valid date time.")
                        .addField("Valid Formats", validDateFormats.toString(), false)
        );
        throw new CommandReturnException();
    }

    public Set<Reason> getFilterReasons(final CommandParameters commandParameters) {
        return commandParameters.getUserDb().getSettingOrDefault(
                FilterReasonSetting.class,
                FilterReasonSetting.getDefaultReasons()
        );
    }
}
