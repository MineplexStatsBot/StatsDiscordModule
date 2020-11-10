package de.timmi6790.mineplex_stats.commands.java.management;

import de.timmi6790.discord_framework.modules.command.CommandParameters;
import de.timmi6790.discord_framework.modules.command.CommandResult;
import de.timmi6790.discord_framework.modules.command.property.properties.MinArgCommandProperty;
import de.timmi6790.mineplex_stats.commands.java.AbstractJavaStatsCommand;
import de.timmi6790.mineplex_stats.statsapi.models.java.JavaGame;
import net.dv8tion.jda.api.utils.MarkdownUtil;

public class JavaGameAliasCommand extends AbstractJavaStatsCommand {
    public JavaGameAliasCommand() {
        super("aliasGame", "Game Alias", "<game> <alias>", "ag");

        this.setCategory("MineplexStats - Java - Management");
        this.addProperties(
                new MinArgCommandProperty(2)
        );
    }

    @Override
    protected CommandResult onCommand(final CommandParameters commandParameters) {
        final JavaGame game = this.getGame(commandParameters, 0);
        final String newAlias = commandParameters.getArgs()[1];

        this.getMineplexStatsModule()
                .getMpStatsRestClient()
                .addJavaGameAlias(game.getName(), newAlias);
        this.getMineplexStatsModule().loadJavaGames();
        
        this.sendTimedMessage(
                commandParameters,
                this.getEmbedBuilder(commandParameters)
                        .setTitle("Added Game Alias")
                        .setDescription("Added new game alias " + MarkdownUtil.monospace(newAlias)),
                90
        );

        return CommandResult.SUCCESS;
    }
}
