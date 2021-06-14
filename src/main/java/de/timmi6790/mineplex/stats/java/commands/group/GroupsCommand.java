package de.timmi6790.mineplex.stats.java.commands.group;

import de.timmi6790.discord_framework.module.modules.command.CommandParameters;
import de.timmi6790.discord_framework.module.modules.command.CommandResult;
import de.timmi6790.discord_framework.utilities.commons.ListUtilities;
import de.timmi6790.mineplex.stats.common.commands.BaseStatsCommand;
import de.timmi6790.mpstats.api.client.common.BaseApiClient;
import de.timmi6790.mpstats.api.client.common.game.models.Game;
import de.timmi6790.mpstats.api.client.common.group.exceptions.InvalidGroupNameRestException;
import de.timmi6790.mpstats.api.client.common.group.models.Group;
import de.timmi6790.mpstats.api.client.java.player.models.JavaPlayer;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class GroupsCommand extends BaseStatsCommand<JavaPlayer> {
    private static final int GROUP_POSITION = 0;

    public GroupsCommand(final BaseApiClient<JavaPlayer> apiClient) {
        super(
                apiClient,
                "groups",
                "Java",
                "Groups",
                "[group]"
        );
    }

    protected CommandResult handleAllGroupsCommand(final CommandParameters commandParameters) {
        final List<Group> groups = this.getApiClient().getGroupClient().getGroups();

        final List<String> groupNames = ListUtilities.toStringList(groups, Group::getGroupName);
        groupNames.sort(Comparator.naturalOrder());

        this.sendTimedMessage(
                commandParameters,
                this.getEmbedBuilder(commandParameters)
                        .setTitle("Java Groups")
                        .setDescription(String.join("\n", groupNames))
                        .setFooterFormat(
                                "TIP: Run %s %s <group> to see more details",
                                this.getCommandModule().getMainCommand(),
                                this.getName()
                        )
        );

        return CommandResult.SUCCESS;
    }

    protected CommandResult handleGroupCommand(final CommandParameters commandParameters, final String groupName) {
        final Group group;
        try {
            group = this.getApiClient().getGroupClient().getGroup(groupName);
        } catch (final InvalidGroupNameRestException exception) {
            this.throwArgumentCorrectionMessage(
                    commandParameters,
                    groupName,
                    GROUP_POSITION,
                    "group",
                    null,
                    new String[0],
                    exception.getSuggestedGroups(),
                    Group::getGroupName
            );
            return CommandResult.INVALID_ARGS;
        }

        this.sendTimedMessage(
                commandParameters,
                this.getEmbedBuilder(commandParameters)
                        .setTitle("Java Groups - " + group.getCleanName())
                        .addField(
                                "Description",
                                Objects.requireNonNullElse(group.getDescription(), ""),
                                false,
                                group.getDescription() != null
                        )
                        .addField(
                                "Alias names",
                                String.join(", ", group.getAliasNames()),
                                false,
                                !group.getAliasNames().isEmpty()
                        )
                        .addField(
                                "Games",
                                String.join(", ", ListUtilities.toStringList(group.getGames(), Game::getGameName))
                        ),
                600
        );

        return CommandResult.SUCCESS;
    }

    @Override
    protected CommandResult onStatsCommand(final CommandParameters commandParameters) {
        if (commandParameters.getArgs().length == 0) {
            return this.handleAllGroupsCommand(commandParameters);
        }

        final String groupName = this.getArg(commandParameters, GROUP_POSITION);
        return this.handleGroupCommand(commandParameters, groupName);
    }
}
