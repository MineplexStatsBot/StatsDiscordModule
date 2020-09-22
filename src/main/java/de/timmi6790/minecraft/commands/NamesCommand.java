package de.timmi6790.minecraft.commands;

import de.timmi6790.discord_framework.modules.command.AbstractCommand;
import de.timmi6790.discord_framework.modules.command.CommandParameters;
import de.timmi6790.discord_framework.modules.command.CommandResult;
import de.timmi6790.discord_framework.modules.command.exceptions.CommandReturnException;
import de.timmi6790.discord_framework.modules.command.properties.MinArgCommandProperty;
import de.timmi6790.minecraft.MinecraftModule;
import de.timmi6790.minecraft.mojang_api.models.MojangUser;
import de.timmi6790.minecraft.mojang_api.models.NameHistory;
import net.dv8tion.jda.api.utils.MarkdownUtil;

import java.util.StringJoiner;
import java.util.regex.Pattern;

public class NamesCommand extends AbstractCommand<MinecraftModule> {
    private static final Pattern NAME_PATTERN = Pattern.compile("^\\w{1,16}$");

    public NamesCommand() {
        super("names", "Minecraft", "Shows the players past names", "<playerName>", "n");

        this.addProperties(
                new MinArgCommandProperty(1)
        );
    }

    @Override
    protected CommandResult onCommand(final CommandParameters commandParameters) {
        final String playerName = this.getPlayer(commandParameters, 0);
        final MojangUser user = this.getModule().getMojangApi().getUser(playerName)
                .orElseThrow(() -> {
                    throwInvalidArg(commandParameters, 0, "Minecraft Name");
                    return new CommandReturnException();
                });

        final NameHistory nameHistory = user.getNameHistory();
        final StringJoiner descriptionBuilder = new StringJoiner("\n\n");
        for (final NameHistory.NameHistoryData nameHistoryData : nameHistory.getHistory()) {
            descriptionBuilder.add(MarkdownUtil.monospace(nameHistoryData.getName()) + " - " + nameHistoryData.getFormattedTime());
        }

        sendTimedMessage(
                commandParameters,
                getEmbedBuilder(commandParameters)
                        .setTitle(String.format("%s - Names", user.getName()))
                        .setThumbnail(user.getHeadUrl())
                        .setDescription(descriptionBuilder.toString()),
                250
        );

        return CommandResult.SUCCESS;
    }

    private String getPlayer(final CommandParameters commandParameters, final int argPos) {
        final String name = commandParameters.getArgs()[argPos];
        if (NAME_PATTERN.matcher(name).find()) {
            return name;
        }

        throwInvalidArg(commandParameters, 0, "Minecraft Name");
        throw new CommandReturnException();
    }
}
