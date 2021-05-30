package de.timmi6790.mineplex_stats.commands.java.management;

import de.timmi6790.discord_framework.module.modules.command.CommandParameters;
import de.timmi6790.discord_framework.module.modules.command.CommandResult;
import de.timmi6790.discord_framework.module.modules.command.property.properties.MinArgCommandProperty;
import de.timmi6790.discord_framework.utilities.commons.EnumUtilities;
import de.timmi6790.mineplex_stats.commands.java.AbstractJavaStatsCommand;
import net.dv8tion.jda.api.utils.MarkdownUtil;

public class JavaBoardAliasCommand extends AbstractJavaStatsCommand {
    public JavaBoardAliasCommand() {
        super("aliasBoard", "Board Alias", "<board> <alias>", "ab");

        this.setCategory("MineplexStats - Java - Management");
        this.addProperties(
                new MinArgCommandProperty(2)
        );
    }

    @Override
    protected CommandResult onCommand(final CommandParameters commandParameters) {
        final JavaBoards board = this.getFromEnumIgnoreCaseThrow(commandParameters, 0, JavaBoards.values());
        final String newAlias = this.getArg(commandParameters, 1);

        this.getMineplexStatsModule()
                .getMpStatsRestClient()
                .addJavaBoardAlias(EnumUtilities.getPrettyName(board), newAlias);
        this.getMineplexStatsModule().loadJavaGames();

        this.sendTimedMessage(
                commandParameters,
                this.getEmbedBuilder(commandParameters)
                        .setTitle("Added Board Alias")
                        .setDescription("Added new board alias " + MarkdownUtil.monospace(newAlias)),
                90
        );

        // Log
        this.getMineplexStatsModule().sendAliasNotification(
                commandParameters,
                "Java",
                EnumUtilities.getPrettyName(board),
                newAlias
        );

        return CommandResult.SUCCESS;
    }

    private enum JavaBoards {
        ALL,
        DAILY,
        WEEKLY,
        MONTHLY,
        YEARLY
    }
}
