package de.timmi6790.mineplex.stats.java.commands.group;

import de.timmi6790.discord_framework.module.modules.slashcommand.SlashCommandModule;
import de.timmi6790.discord_framework.module.modules.slashcommand.option.Option;
import de.timmi6790.discord_framework.module.modules.slashcommand.option.options.StringOption;
import de.timmi6790.discord_framework.module.modules.slashcommand.parameters.SlashCommandParameters;
import de.timmi6790.discord_framework.module.modules.slashcommand.property.properties.info.CategoryProperty;
import de.timmi6790.discord_framework.module.modules.slashcommand.property.properties.info.SyntaxProperty;
import de.timmi6790.discord_framework.module.modules.slashcommand.result.BaseCommandResult;
import de.timmi6790.discord_framework.module.modules.slashcommand.result.CommandResult;
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
import java.util.Optional;

public class GroupsCommand extends BaseStatsCommand<JavaPlayer> {
    public static final Option<String> GROUP_OPTION = new StringOption("group", "Group");

    public GroupsCommand(final BaseApiClient<JavaPlayer> apiClient, final SlashCommandModule commandModule) {
        super(
                apiClient,
                "groups",
                "Groups",
                commandModule
        );

        this.addProperties(
                new CategoryProperty("Java"),
                new SyntaxProperty("[group]")
        );
    }

    protected CommandResult handleAllGroupsCommand(final SlashCommandParameters commandParameters) {
        final List<Group> groups = this.getApiClient().getGroupClient().getGroups();

        final List<String> groupNames = ListUtilities.toStringList(groups, Group::getGroupName);
        groupNames.sort(Comparator.naturalOrder());

        commandParameters.sendMessage(
                commandParameters.getEmbedBuilder()
                        .setTitle("Java Groups")
                        .setDescription(String.join("\n", groupNames))
                        .setFooterFormat(
                                "TIP: Run /%s <group> to see more details",
                                this.getName()
                        )
        );

        return BaseCommandResult.SUCCESSFUL;
    }

    protected CommandResult handleGroupCommand(final SlashCommandParameters commandParameters, final String groupName) {
        final Group group;
        try {
            group = this.getApiClient().getGroupClient().getGroup(groupName);
        } catch (final InvalidGroupNameRestException exception) {
            this.throwArgumentCorrectionMessage(
                    commandParameters,
                    GROUP_OPTION,
                    null,
                    exception.getSuggestedGroups(),
                    Group::getGroupName
            );
            return BaseCommandResult.INVALID_ARGS;
        }

        commandParameters.sendMessage(
                commandParameters.getEmbedBuilder()
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
                        )
        );

        return BaseCommandResult.SUCCESSFUL;
    }

    @Override
    protected CommandResult onStatsCommand(final SlashCommandParameters commandParameters) {
        final Optional<String> groupNameOpt = commandParameters.getOption(GROUP_OPTION);
        if (groupNameOpt.isPresent()) {
            return this.handleGroupCommand(commandParameters, groupNameOpt.get());
        }

        return this.handleAllGroupsCommand(commandParameters);
    }
}
