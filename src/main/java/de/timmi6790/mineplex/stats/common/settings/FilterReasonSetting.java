package de.timmi6790.mineplex.stats.common.settings;

import de.timmi6790.discord_framework.module.modules.command.CommandParameters;
import de.timmi6790.discord_framework.module.modules.setting.AbstractSetting;
import de.timmi6790.discord_framework.utilities.commons.EnumUtilities;
import de.timmi6790.mpstats.api.client.common.filter.models.Reason;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.utils.MarkdownUtil;

import java.util.*;

@Log4j2
public class FilterReasonSetting extends AbstractSetting<Set<Reason>> {
    private static final String REPOSITORY_DIVIDER = "/";
    private static final Set<Reason> DEFAULT_REASONS = EnumSet.of(Reason.GLITCHED, Reason.GIVEN);

    public static Set<Reason> getDefaultReasons() {
        return DEFAULT_REASONS;
    }

    public FilterReasonSetting() {
        super(
                "FilterReasons",
                "Filter reasons that should be excluded." +
                        "\nPossible values: " + String.join(", ", EnumUtilities.getPrettyNames(Reason.values())) +
                        "\nDivide new input with " + MarkdownUtil.bold(","),
                DEFAULT_REASONS
        );
    }

    @Override
    public String toDatabaseValue(final Set<Reason> value) {
        // There is no reason to try to save disk space here for the limited usage
        // We can't use , or ; as the divider, this is currently not working with the main framework. I should really fix this
        final StringJoiner joinedReasons = new StringJoiner(REPOSITORY_DIVIDER);
        for (final Reason reason : value) {
            joinedReasons.add(reason.toString());
        }

        return joinedReasons.toString();
    }

    @Override
    public Set<Reason> fromDatabaseValue(final String value) {
        final Set<Reason> found = EnumSet.noneOf(Reason.class);
        final String[] parts = value.split(REPOSITORY_DIVIDER);
        for (final String part : parts) {
            try {
                final Reason reason = Reason.valueOf(part);
                found.add(reason);
            } catch (final IllegalArgumentException ignore) {
                log.warn("Invalid Reason found: " + part);
            }
        }

        return found;
    }

    @Override
    protected Optional<Set<Reason>> parseNewValue(final CommandParameters commandParameters, final String userInput) {
        final Set<Reason> found = EnumSet.noneOf(Reason.class);
        final String[] parts = userInput.split(",");
        for (final String part : parts) {
            EnumUtilities.getIgnoreCase(part.trim(), Reason.values()).ifPresent(found::add);
        }

        if (found.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(found);
    }

    @Override
    protected List<Set<Reason>> possibleValues(final CommandParameters commandParameters, final String userInput) {
        return List.of(EnumSet.allOf(Reason.class));
    }
}
