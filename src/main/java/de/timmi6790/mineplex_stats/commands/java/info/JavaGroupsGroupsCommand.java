package de.timmi6790.mineplex_stats.commands.java.info;

import de.timmi6790.discord_framework.module.modules.command.CommandParameters;
import de.timmi6790.discord_framework.module.modules.command.CommandResult;
import de.timmi6790.discord_framework.module.modules.command.property.properties.ExampleCommandsCommandProperty;
import de.timmi6790.mineplex_stats.commands.java.AbstractJavaStatsCommand;
import de.timmi6790.mineplex_stats.statsapi.models.java.JavaGroup;
import lombok.EqualsAndHashCode;
import org.apache.commons.collections4.list.TreeList;

import java.util.List;
import java.util.StringJoiner;

@EqualsAndHashCode(callSuper = true)
public class JavaGroupsGroupsCommand extends AbstractJavaStatsCommand {
    public JavaGroupsGroupsCommand() {
        super("groups", "Java Groups", "[group]");

        this.setCategory("MineplexStats - Java - Group");
        this.addProperties(
                new ExampleCommandsCommandProperty(
                        "MixedArcade"
                )
        );
    }

    @Override
    protected CommandResult onCommand(final CommandParameters commandParameters) {
        // Show all groups
        if (commandParameters.getArgs().length == 0) {
            return this.handleAllGroups(commandParameters);
        }

        // Group info
        final JavaGroup group = this.getJavaGroup(commandParameters, 0);
        return this.handleGroupInfo(commandParameters, group);
    }

    private CommandResult handleAllGroups(final CommandParameters commandParameters) {
        final List<String> sortedGroupNames = new TreeList<>();
        for (final JavaGroup group : this.getMineplexStatsModule().getJavaGroups()) {
            sortedGroupNames.add(group.getName());
        }

        this.sendTimedMessage(
                commandParameters,
                this.getEmbedBuilder(commandParameters)
                        .setTitle("Java Groups")
                        .addField("Groups", String.join("\n", sortedGroupNames))
                        .setFooterFormat(
                                "TIP: Run %s %s <group> to see more details",
                                this.getCommandModule().getMainCommand(),
                                this.getName()
                        ),
                150
        );
        return CommandResult.SUCCESS;
    }

    private CommandResult handleGroupInfo(final CommandParameters commandParameters, final JavaGroup group) {
        // Remove "Achievement" from all stat names, because MixedArcade is above the 1024 character limit
        final StringJoiner stats = new StringJoiner(", ");
        for (final String statName : group.getStatNames()) {
            stats.add(statName.replace(" ", "").replace("Achievement", ""));
        }

        this.sendTimedMessage(
                commandParameters,
                this.getEmbedBuilder(commandParameters)
                        .setTitle("Java Groups - " + group.getName())
                        .addField(
                                "Description",
                                group.getDescription(),
                                false,
                                !group.getDescription().isEmpty()
                        )
                        .addField(
                                "Alias names",
                                String.join(", ", group.getAliasNames()),
                                false,
                                group.getAliasNames().length > 0
                        )
                        .addField(
                                "Games",
                                String.join(", ", group.getGameNames())
                        )
                        .addField(
                                "Stats",
                                stats.toString().substring(0, Math.min(stats.length(), 1024))
                        ),
                150
        );
        return CommandResult.SUCCESS;
    }
}
