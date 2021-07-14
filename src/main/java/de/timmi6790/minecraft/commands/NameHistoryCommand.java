package de.timmi6790.minecraft.commands;

import de.timmi6790.api.mojang.MojangApiClient;
import de.timmi6790.api.mojang.models.NameEntry;
import de.timmi6790.api.mojang.models.PlayerInfo;
import de.timmi6790.discord_framework.module.modules.command.Command;
import de.timmi6790.discord_framework.module.modules.command.CommandModule;
import de.timmi6790.discord_framework.module.modules.command.exceptions.CommandReturnException;
import de.timmi6790.discord_framework.module.modules.command.models.BaseCommandResult;
import de.timmi6790.discord_framework.module.modules.command.models.CommandParameters;
import de.timmi6790.discord_framework.module.modules.command.models.CommandResult;
import de.timmi6790.discord_framework.module.modules.command.property.properties.controll.MinArgProperty;
import de.timmi6790.discord_framework.module.modules.command.property.properties.info.AliasNamesProperty;
import de.timmi6790.discord_framework.module.modules.command.property.properties.info.CategoryProperty;
import de.timmi6790.discord_framework.module.modules.command.property.properties.info.DescriptionProperty;
import de.timmi6790.discord_framework.module.modules.command.property.properties.info.SyntaxProperty;
import de.timmi6790.discord_framework.module.modules.command.utilities.MessageUtilities;
import de.timmi6790.minecraft.utilities.JavaUtilities;
import net.dv8tion.jda.api.utils.MarkdownUtil;

import java.util.List;
import java.util.StringJoiner;

public class NameHistoryCommand extends Command {
    public NameHistoryCommand(CommandModule commandModule) {
        super("names", commandModule);
        
        this.addProperties(
                new MinArgProperty(1),
                new CategoryProperty("Minecraft"),
                new DescriptionProperty("Shows the players past names"),
                new SyntaxProperty("<playerName>"),
                new AliasNamesProperty("n")
        );
    }

    private String getPlayerName(final CommandParameters commandParameters, final int argPos) {
        final String name = commandParameters.getArgs()[argPos];
        if (JavaUtilities.isValidName(name)) {
            return name;
        }

        MessageUtilities.sendInvalidArgumentMessage(commandParameters, name, "Minecraft Name");
        throw new CommandReturnException();
    }

    @Override
    protected CommandResult onCommand(final CommandParameters commandParameters) {
        final String playerName = this.getPlayerName(commandParameters, 0);
        final PlayerInfo playerInfo = MojangApiClient.getInstance().getPlayerInfo(playerName).orElseThrow(() -> {
            MessageUtilities.sendInvalidArgumentMessage(commandParameters, playerName, "Minecraft Name");
            return new CommandReturnException();
        });

        final List<NameEntry> nameHistory = MojangApiClient.getInstance().getPlayerNameHistory(playerInfo.getUuid()).orElseThrow(() -> {
            commandParameters.sendMessage(
                    commandParameters.getEmbedBuilder()
                            .setTitle("Error")
                            .setDescription("Something went wrong while fetching the name history.")
            );
            return new CommandReturnException();
        });

        final StringJoiner descriptionBuilder = new StringJoiner("\n\n");
        for (final NameEntry entry : nameHistory) {
            descriptionBuilder.add(MarkdownUtil.monospace(entry.getName()) + " - " + entry.getFormattedTime());
        }

        commandParameters.sendMessage(
                commandParameters.getEmbedBuilder()
                        .setTitle(String.format("%s - Names", playerInfo.getName()))
                        .setThumbnail("https://minotar.net/avatar/" + playerInfo.getUuid().toString().replace("-", ""))
                        .setDescription(descriptionBuilder.toString())
        );

        return BaseCommandResult.SUCCESSFUL;
    }
}
