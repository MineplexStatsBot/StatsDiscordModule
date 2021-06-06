package de.timmi6790.mineplex.stats.common.settings;

import de.timmi6790.discord_framework.module.modules.command.CommandParameters;
import de.timmi6790.discord_framework.module.modules.setting.AbstractSetting;
import de.timmi6790.mpstats.api.client.common.filter.models.Reason;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

// TODO: Finish me
public class FilterReasonSetting extends AbstractSetting<Set<Reason>> {
    private static final Set<Reason> DEFAULT_REASONS = EnumSet.of(Reason.GLITCHED, Reason.GIVEN);

    public static Set<Reason> getDefaultReasons() {
        return DEFAULT_REASONS;
    }

    public FilterReasonSetting() {
        super(
                "FilterReasons",
                "Filter reasons that should be excluded",
                DEFAULT_REASONS
        );
    }

    @Override
    public String toDatabaseValue(final Set<Reason> value) {
        return null;
    }

    @Override
    public Set<Reason> fromDatabaseValue(final String value) {
        return null;
    }

    @Override
    protected Optional<Set<Reason>> parseNewValue(final CommandParameters commandParameters, final String userInput) {
        return Optional.empty();
    }

    @Override
    protected List<Set<Reason>> possibleValues(final CommandParameters commandParameters, final String userInput) {
        return null;
    }
}
