package de.timmi6790.minecraft.commands;

import de.timmi6790.api.mojang.MojangApiClient;
import de.timmi6790.api.mojang.models.NameEntry;
import de.timmi6790.api.mojang.models.PlayerInfo;
import de.timmi6790.discord_framework.module.modules.command.AbstractCommand;
import de.timmi6790.discord_framework.module.modules.command.CommandParameters;
import de.timmi6790.discord_framework.module.modules.command.CommandResult;
import de.timmi6790.discord_framework.module.modules.command.exceptions.CommandReturnException;
import de.timmi6790.discord_framework.module.modules.command.property.properties.MinArgCommandProperty;
import de.timmi6790.minecraft.utilities.JavaUtilities;
import net.dv8tion.jda.api.utils.MarkdownUtil;

import java.util.List;
import java.util.StringJoiner;

public class NamesCommand extends AbstractCommand {
    public NamesCommand() {
        super("names", "Minecraft", "Shows the players past names", "<playerName>", "n");

        this.addProperties(
                new MinArgCommandProperty(1)
        );
    }

    private String getPlayerName(final CommandParameters commandParameters, final int argPos) {
        final String name = commandParameters.getArgs()[argPos];
        if (JavaUtilities.isValidName(name)) {
            return name;
        }

        this.throwInvalidArg(commandParameters, 0, "Minecraft Name");
        throw new CommandReturnException();
    }

    @Override
    protected CommandResult onCommand(final CommandParameters commandParameters) {
        final String playerName = this.getPlayerName(commandParameters, 0);
        final PlayerInfo playerInfo = MojangApiClient.getInstance().getPlayerInfo(playerName).orElseThrow(() -> {
            this.throwInvalidArg(commandParameters, 0, "Minecraft Name");
            return new CommandReturnException();
        });

        final List<NameEntry> nameHistory = MojangApiClient.getInstance().getPlayerNameHistory(playerInfo.getUuid()).orElseThrow(() -> {
            this.sendTimedMessage(
                    commandParameters,
                    this.getEmbedBuilder(commandParameters)
                            .setTitle("Error")
                            .setDescription("Something went wrong while fetching the name history.")
            );
            return new CommandReturnException();
        });

        final StringJoiner descriptionBuilder = new StringJoiner("\n\n");
        for (final NameEntry entry : nameHistory) {
            descriptionBuilder.add(MarkdownUtil.monospace(entry.getName()) + " - " + entry.getFormattedTime());
        }

        this.sendTimedMessage(
                commandParameters,
                this.getEmbedBuilder(commandParameters)
                        .setTitle(String.format("%s - Names", playerInfo.getName()))
                        .setThumbnail("https://minotar.net/avatar/" + playerInfo.getUuid().toString().replace("-", ""))
                        .setDescription(descriptionBuilder.toString()),
                250
        );

        return CommandResult.SUCCESS;
    }
}
