package de.timmi6790.mineplex_stats.settings;

import de.timmi6790.discord_framework.modules.command.CommandParameters;
import de.timmi6790.discord_framework.modules.setting.settings.StringSetting;
import de.timmi6790.discord_framework.utilities.discord.DiscordMessagesUtilities;
import lombok.EqualsAndHashCode;
import net.dv8tion.jda.api.utils.MarkdownUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

@EqualsAndHashCode(callSuper = true)
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
                        MarkdownUtil.monospace("me"),
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
                                           final List<String> possibleValues) {
        DiscordMessagesUtilities.sendMessageTimed(
                commandParameters.getLowestMessageChannel(),
                DiscordMessagesUtilities.getEmbedBuilder(commandParameters)
                        .setTitleFormat("Invalid %s name", this.name.toLowerCase())
                        .setDescription(
                                "%s is not a valid minecraft %s name.",
                                MarkdownUtil.monospace(userInput),
                                this.name.toLowerCase()
                        ),
                400
        );
    }
}
