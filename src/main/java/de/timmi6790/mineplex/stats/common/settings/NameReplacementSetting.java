package de.timmi6790.mineplex.stats.common.settings;

import de.timmi6790.discord_framework.module.modules.command.models.CommandParameters;
import de.timmi6790.discord_framework.module.modules.setting.settings.StringSetting;
import net.dv8tion.jda.api.utils.MarkdownUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class NameReplacementSetting extends StringSetting {
    private static final String KEYWORD = "me";

    private final String name;
    private final Predicate<String> namePredicate;

    public static String getKeyword() {
        return KEYWORD;
    }

    protected NameReplacementSetting(final String name, final Predicate<String> namePredicate) {
        super(name + "NameReplacement",
                String.format(
                        "Use %s inside your %s related commands to replace it with this value.",
                        MarkdownUtil.monospace(getKeyword()),
                        name.toLowerCase()
                ),
                ""
        );

        this.name = name;
        this.namePredicate = namePredicate;
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
        if (this.namePredicate.test(s)) {
            return Optional.of(s);
        }
        return Optional.empty();
    }

    @Override
    protected List<String> possibleValues(final CommandParameters commandParameters, final String s) {
        return new ArrayList<>();
    }

    @Override
    protected void sendInvalidInputMessage(final CommandParameters commandParameters,
                                           final String userInput,
                                           final Iterable<String> possibleValues) {
        commandParameters.sendMessage(
                commandParameters.getEmbedBuilder()
                        .setTitleFormat("Invalid %s name", this.name.toLowerCase())
                        .setDescription(
                                "%s is not a valid minecraft %s name.",
                                MarkdownUtil.monospace(userInput),
                                this.name.toLowerCase()
                        )
        );
    }
}