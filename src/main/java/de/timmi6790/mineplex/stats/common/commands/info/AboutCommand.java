package de.timmi6790.mineplex.stats.common.commands.info;

import de.timmi6790.discord_framework.DiscordBot;
import de.timmi6790.discord_framework.module.modules.command.Command;
import de.timmi6790.discord_framework.module.modules.command.CommandModule;
import de.timmi6790.discord_framework.module.modules.command.models.BaseCommandResult;
import de.timmi6790.discord_framework.module.modules.command.models.CommandParameters;
import de.timmi6790.discord_framework.module.modules.command.models.CommandResult;
import de.timmi6790.discord_framework.module.modules.command.property.properties.info.CategoryProperty;
import de.timmi6790.discord_framework.module.modules.command.property.properties.info.DescriptionProperty;
import net.dv8tion.jda.api.utils.MarkdownUtil;

public class AboutCommand extends Command {
    public AboutCommand(final CommandModule commandModule) {
        super("about", commandModule);

        this.addProperties(
                new CategoryProperty("Info"),
                new DescriptionProperty("About the bot")
        );
    }

    @Override
    protected CommandResult onCommand(final CommandParameters commandParameters) {
        final String mainCommand = this.getCommandModule().getMainCommand();
        commandParameters.sendMessage(
                commandParameters.getEmbedBuilder()
                        .setTitle("About")
                        .setDescription("This bot is themed around a minecraft server called [Mineplex](https://www.mineplex.com/home/).\n"
                                + "You can use it to show the game leaderboards or player stats.")
                        .addField("Data Deletion",
                                "You can always delete all your saved data with the "
                                        + MarkdownUtil.monospace(mainCommand + "deleteMyAccount") + " command. " +
                                        "Abuse of this command could result in a ban, be warned."
                        )
                        .addField(
                                "Data Request",
                                "You can request your personal data once per day with the " +
                                        MarkdownUtil.monospace(mainCommand + "giveMeMyData") + " command."
                        )
                        .addField(
                                "Support Server",
                                "If you want to talk about the bot, here is your best way to do it [INVITE LINK](https://discord.gg/xmchPsY).\n" +
                                        "It doesn't matter if it is about a bug or an idea to make better."
                        )
                        .addField("SourceCode", "[StatsBotDiscord Github](https://github.com/Timmi6790/DiscordBotFramework)", false)
                        .addField("Version", DiscordBot.BOT_VERSION, false)
        );
        return BaseCommandResult.SUCCESSFUL;
    }
}
