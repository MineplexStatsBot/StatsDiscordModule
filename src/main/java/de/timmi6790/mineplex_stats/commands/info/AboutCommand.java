package de.timmi6790.mineplex_stats.commands.info;

import de.timmi6790.discord_framework.DiscordBot;
import de.timmi6790.discord_framework.modules.command.AbstractCommand;
import de.timmi6790.discord_framework.modules.command.CommandParameters;
import de.timmi6790.discord_framework.modules.command.CommandResult;
import net.dv8tion.jda.api.utils.MarkdownUtil;

public class AboutCommand extends AbstractCommand {
    public AboutCommand() {
        super("about", "Info", "About the bot", "");
    }

    @Override
    protected CommandResult onCommand(final CommandParameters commandParameters) {
        final String mainCommand = this.getCommandModule().getMainCommand();
        sendTimedMessage(
                commandParameters,
                getEmbedBuilder(commandParameters)
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
                        .addField("Version", DiscordBot.BOT_VERSION, false),
                150
        );
        return CommandResult.SUCCESS;
    }
}
