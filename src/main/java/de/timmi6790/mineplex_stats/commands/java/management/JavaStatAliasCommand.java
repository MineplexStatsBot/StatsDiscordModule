package de.timmi6790.mineplex_stats.commands.java.management;

import de.timmi6790.discord_framework.module.modules.command.CommandParameters;
import de.timmi6790.discord_framework.module.modules.command.CommandResult;
import de.timmi6790.discord_framework.module.modules.command.property.properties.MinArgCommandProperty;
import de.timmi6790.mineplex_stats.commands.java.AbstractJavaStatsCommand;
import de.timmi6790.mineplex_stats.statsapi.models.java.JavaGame;
import de.timmi6790.mineplex_stats.statsapi.models.java.JavaStat;
import net.dv8tion.jda.api.utils.MarkdownUtil;

public class JavaStatAliasCommand extends AbstractJavaStatsCommand {
    public JavaStatAliasCommand() {
        super("aliasStat", "Stat Alias", "<game> <stat> <alias>", "as");

        this.setCategory("MineplexStats - Java - Management");
        this.addProperties(
                new MinArgCommandProperty(3)
        );
    }

    @Override
    protected CommandResult onCommand(final CommandParameters commandParameters) {
        final JavaGame game = this.getGame(commandParameters, 0);
        final JavaStat stat = this.getStat(game, commandParameters, 1);
        final String newAlias = this.getArg(commandParameters, 2);

        this.getMineplexStatsModule()
                .getMpStatsRestClient()
                .addJavaStatAlias(game.getName(), stat.getName(), newAlias);
        this.getMineplexStatsModule().loadJavaGames();

        this.sendTimedMessage(
                commandParameters,
                this.getEmbedBuilder(commandParameters)
                        .setTitle("Added Stat Alias")
                        .setDescription(
                                "Added new stat alias %s for %s.",
                                MarkdownUtil.monospace(newAlias),
                                MarkdownUtil.bold(game.getName() + " " + stat.getPrintName())
                        ),
                90
        );

        // Log
        this.getMineplexStatsModule().sendAliasNotification(
                commandParameters,
                "Java",
                String.join("-", game.getName(), stat.getName()),
                newAlias
        );

        return CommandResult.SUCCESS;
    }
}
