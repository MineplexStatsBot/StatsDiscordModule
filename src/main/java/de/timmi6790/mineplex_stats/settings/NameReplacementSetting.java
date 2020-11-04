package de.timmi6790.mineplex_stats.settings;

import de.timmi6790.discord_framework.modules.command.CommandParameters;
import de.timmi6790.discord_framework.modules.setting.settings.StringSetting;
import lombok.Getter;
import org.ocpsoft.prettytime.shade.edu.emory.mathcs.backport.java.util.Arrays;

import java.util.List;
import java.util.Optional;

public class NameReplacementSetting extends StringSetting {
    @Getter
    private static final String KEYWORD = "me";

    protected NameReplacementSetting(final String name) {
        super("mineplex_stats.settings." + name.toLowerCase() + "_name_replacement",
                name + "NameReplacement",
                "Use `me` inside your " + name.toLowerCase() + " related commands to replace it with this value.",
                "");
    }

    @Override
    public String toDatabaseValue(final String s) {
        return s;
    }

    @Override
    public String fromDatabaseValue(final String s) {
        return s;
    }

    @Override
    protected Optional<String> parseNewValue(final CommandParameters commandParameters, final String s) {
        return Optional.of(s);
    }

    @Override
    protected List<String> possibleValues(final CommandParameters commandParameters, final String s) {
        return Arrays.asList(new String[]{"Timmi6790"});
    }
}
