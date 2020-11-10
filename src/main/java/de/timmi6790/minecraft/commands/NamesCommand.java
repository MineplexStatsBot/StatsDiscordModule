package de.timmi6790.minecraft.commands;

import de.timmi6790.discord_framework.modules.command.AbstractCommand;
import de.timmi6790.discord_framework.modules.command.CommandParameters;
import de.timmi6790.discord_framework.modules.command.CommandResult;
import de.timmi6790.discord_framework.modules.command.exceptions.CommandReturnException;
import de.timmi6790.discord_framework.modules.command.property.properties.MinArgCommandProperty;
import de.timmi6790.minecraft.mojang_api.MojangApi;
import de.timmi6790.minecraft.mojang_api.models.MojangUser;
import de.timmi6790.minecraft.mojang_api.models.NameHistory;
import de.timmi6790.minecraft.utilities.JavaUtilities;
import net.dv8tion.jda.api.utils.MarkdownUtil;

import java.util.StringJoiner;

public class NamesCommand extends AbstractCommand {
    public NamesCommand() {
        super("names", "Minecraft", "Shows the players past names", "<playerName>", "n");

        this.addProperties(
                new MinArgCommandProperty(1)
        );
    }

    private String getPlayer(final CommandParameters commandParameters, final int argPos) {
        final String name = commandParameters.getArgs()[argPos];
        if (JavaUtilities.isValidName(name)) {
            return name;
        }

        this.throwInvalidArg(commandParameters, 0, "Minecraft Name");
        throw new CommandReturnException();
    }

    @Override
    protected CommandResult onCommand(final CommandParameters commandParameters) {
        final String playerName = this.getPlayer(commandParameters, 0);
        final MojangUser user = MojangApi.getUser(playerName)
                .orElseThrow(() -> {
                    this.throwInvalidArg(commandParameters, 0, "Minecraft Name");
                    return new CommandReturnException();
                });

        final StringJoiner descriptionBuilder = new StringJoiner("\n\n");
        for (final NameHistory.NameHistoryData nameHistoryData : user.getNameHistory().getHistory()) {
            descriptionBuilder.add(MarkdownUtil.monospace(nameHistoryData.getName()) + " - " + nameHistoryData.getFormattedTime());
        }

        this.sendTimedMessage(
                commandParameters,
                this.getEmbedBuilder(commandParameters)
                        .setTitle(String.format("%s - Names", user.getName()))
                        .setThumbnail(user.getHeadUrl())
                        .setDescription(descriptionBuilder.toString()),
                250
        );

        return CommandResult.SUCCESS;
    }
}
