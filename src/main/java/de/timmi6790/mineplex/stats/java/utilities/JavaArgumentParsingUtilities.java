package de.timmi6790.mineplex.stats.java.utilities;

import de.timmi6790.api.mojang.MojangApiClient;
import de.timmi6790.discord_framework.module.modules.slashcommand.exceptions.CommandReturnException;
import de.timmi6790.discord_framework.module.modules.slashcommand.option.Option;
import de.timmi6790.discord_framework.module.modules.slashcommand.parameters.SlashCommandParameters;
import de.timmi6790.minecraft.utilities.JavaUtilities;
import de.timmi6790.mineplex.stats.common.utilities.InvalidArgUtilities;
import de.timmi6790.mineplex.stats.java.settings.JavaNameReplacementSetting;
import lombok.experimental.UtilityClass;
import net.dv8tion.jda.api.utils.MarkdownUtil;

import java.util.Optional;
import java.util.UUID;

@UtilityClass
public class JavaArgumentParsingUtilities {
    public String getJavaPlayerNameOrThrow(final SlashCommandParameters commandParameters, final Option<String> playerOption) {
        String playerName = commandParameters.getOptionOrThrow(playerOption);

        // Check if the setting is used or not
        if (playerName.equals(JavaNameReplacementSetting.getKeyword())) {
            playerName = commandParameters.getUserDb().getSettingOrDefault(JavaNameReplacementSetting.class, playerName);
        }

        if (JavaUtilities.isValidName(playerName)) {
            return playerName;
        }

        InvalidArgUtilities.throwInvalidArg(
                commandParameters,
                playerName,
                playerOption
        );
        throw new CommandReturnException();
    }

    public UUID getPlayerUUIDOrThrow(final SlashCommandParameters commandParameters, final String playerName) {
        final Optional<UUID> playerUUIDOpt = MojangApiClient.getInstance().getPlayerUUID(playerName);
        if (playerUUIDOpt.isPresent()) {
            return playerUUIDOpt.get();
        }

        commandParameters.sendMessage(
                commandParameters.getEmbedBuilder()
                        .setTitle("Invalid Player Name")
                        .setDescription(
                                "Are you sure that %s is the name of a current player?\n" +
                                        "The Mojang api can not find a uuid for the given player name.\n" +
                                        "This can have two reasons, either your input is incorrect or the api is down.",
                                MarkdownUtil.monospace(playerName)
                        )
        );
        throw new CommandReturnException();
    }
}
