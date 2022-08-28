package de.timmi6790.minecraft.commands;

import de.timmi6790.api.mojang.MojangApiClient;
import de.timmi6790.api.mojang.models.NameEntry;
import de.timmi6790.api.mojang.models.PlayerInfo;
import de.timmi6790.discord_framework.module.modules.slashcommand.SlashCommand;
import de.timmi6790.discord_framework.module.modules.slashcommand.SlashCommandModule;
import de.timmi6790.discord_framework.module.modules.slashcommand.exceptions.CommandReturnException;
import de.timmi6790.discord_framework.module.modules.slashcommand.option.Option;
import de.timmi6790.discord_framework.module.modules.slashcommand.option.options.StringOption;
import de.timmi6790.discord_framework.module.modules.slashcommand.parameters.SlashCommandParameters;
import de.timmi6790.discord_framework.module.modules.slashcommand.property.properties.info.CategoryProperty;
import de.timmi6790.discord_framework.module.modules.slashcommand.property.properties.info.SyntaxProperty;
import de.timmi6790.discord_framework.module.modules.slashcommand.result.BaseCommandResult;
import de.timmi6790.discord_framework.module.modules.slashcommand.result.CommandResult;
import de.timmi6790.discord_framework.module.modules.slashcommand.utilities.SlashMessageUtilities;
import de.timmi6790.minecraft.utilities.JavaUtilities;
import net.dv8tion.jda.api.utils.MarkdownUtil;

import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;

public class NameHistoryCommand extends SlashCommand {
    private final Option<String> nameOption;

    public NameHistoryCommand(final SlashCommandModule commandModule) {
        super(commandModule, "names", "Shows the players past names");

        this.addProperties(
                new CategoryProperty("Minecraft"),
                new SyntaxProperty("<playerName>")
        );

        this.nameOption = new StringOption("name", "Player name").setRequired(true);

        this.addOptions(
                this.nameOption
        );
    }

    private String getPlayerName(final SlashCommandParameters parameters) {
        final Optional<String> playerNameOpt = parameters.getOption(this.nameOption);

        if (playerNameOpt.isPresent()) {
            final String playerName = playerNameOpt.get();
            if (!JavaUtilities.isValidName(playerNameOpt.get())) {
                SlashMessageUtilities.sendInvalidArgumentMessage(parameters, playerName, "Minecraft Name");
                throw new CommandReturnException();
            }

            return playerName;
        }


        throw new CommandReturnException();
    }

    @Override
    protected CommandResult onCommand(final SlashCommandParameters parameters) {
        final String playerName = this.getPlayerName(parameters);
        final PlayerInfo playerInfo = MojangApiClient.getInstance().getPlayerInfo(playerName).orElseThrow(() -> {
            SlashMessageUtilities.sendInvalidArgumentMessage(parameters, playerName, "Minecraft Name");
            return new CommandReturnException();
        });

        final List<NameEntry> nameHistory = MojangApiClient.getInstance().getPlayerNameHistory(playerInfo.getUuid()).orElseThrow(() -> {
            parameters.sendMessage(
                    parameters.getEmbedBuilder()
                            .setTitle("Error")
                            .setDescription("Something went wrong while fetching the name history.")
            );
            return new CommandReturnException();
        });

        final StringJoiner descriptionBuilder = new StringJoiner("\n\n");
        for (final NameEntry entry : nameHistory) {
            descriptionBuilder.add(MarkdownUtil.monospace(entry.getName()) + " - " + entry.getFormattedTime());
        }

        parameters.sendMessage(
                parameters.getEmbedBuilder()
                        .setTitle(String.format("%s - Names", playerInfo.getName()))
                        .setThumbnail("https://minotar.net/avatar/" + playerInfo.getUuid().toString().replace("-", ""))
                        .setDescription(descriptionBuilder.toString())
        );

        return BaseCommandResult.SUCCESSFUL;
    }
}
