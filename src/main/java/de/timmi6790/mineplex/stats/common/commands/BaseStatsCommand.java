package de.timmi6790.mineplex.stats.common.commands;

import de.timmi6790.discord_framework.DiscordBot;
import de.timmi6790.discord_framework.module.modules.reactions.button.ButtonReaction;
import de.timmi6790.discord_framework.module.modules.reactions.button.ButtonReactionModule;
import de.timmi6790.discord_framework.module.modules.reactions.button.actions.ButtonAction;
import de.timmi6790.discord_framework.module.modules.slashcommand.MainReplaceData;
import de.timmi6790.discord_framework.module.modules.slashcommand.SlashCommand;
import de.timmi6790.discord_framework.module.modules.slashcommand.SlashCommandModule;
import de.timmi6790.discord_framework.module.modules.slashcommand.exceptions.CommandReturnException;
import de.timmi6790.discord_framework.module.modules.slashcommand.option.Option;
import de.timmi6790.discord_framework.module.modules.slashcommand.option.options.StringOption;
import de.timmi6790.discord_framework.module.modules.slashcommand.parameters.SlashCommandParameters;
import de.timmi6790.discord_framework.module.modules.slashcommand.parameters.action.CommandRestAction;
import de.timmi6790.discord_framework.module.modules.slashcommand.parameters.options.DiscordOption;
import de.timmi6790.discord_framework.module.modules.slashcommand.result.BaseCommandResult;
import de.timmi6790.discord_framework.module.modules.slashcommand.result.CommandResult;
import de.timmi6790.discord_framework.module.modules.slashcommand.utilities.SlashMessageUtilities;
import de.timmi6790.mineplex.stats.common.utilities.ErrorMessageUtilities;
import de.timmi6790.mpstats.api.client.common.BaseApiClient;
import de.timmi6790.mpstats.api.client.common.player.models.Player;
import de.timmi6790.mpstats.api.client.exception.exceptions.ApiOfflineException;
import de.timmi6790.mpstats.api.client.exception.exceptions.InvalidApiKeyException;
import de.timmi6790.mpstats.api.client.exception.exceptions.RateLimitException;
import de.timmi6790.mpstats.api.client.exception.exceptions.UnknownApiException;
import io.sentry.Sentry;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class BaseStatsCommand<P extends Player> extends SlashCommand {
    public static final Option<String> GAME_OPTION_REQUIRED = new StringOption("game", "Game").setRequired(true);
    public static final Option<String> STAT_OPTION_REQUIRED = new StringOption("stat", "Game Stat").setRequired(true);
    public static final Option<String> BOARD_OPTION_REQUIRED = new StringOption("board", "Game Board").setRequired(true);

    public static final Option<String> JAVA_PLAYER_NAME_REQUIRED = new StringOption("player", "Player Name").setRequired(true);
    public static final Option<String> BEDROCK_PLAYER_NAME_REQUIRED = new StringOption("player", "Player Name").setRequired(true);

    public static final Option<String> GAME_OPTION = new StringOption("game", "Game");
    public static final Option<String> STAT_OPTION = new StringOption("stat", "Game Stat");
    public static final Option<String> BOARD_OPTION = new StringOption("board", "Game Board");
    public static final Option<String> DATE_OPTION = new StringOption("date", "Date");

    @Getter(AccessLevel.PROTECTED)
    private final BaseApiClient<P> apiClient;

    protected BaseStatsCommand(final BaseApiClient<P> apiClient,
                               @NonNull final String name,
                               final String description,
                               final SlashCommandModule commandModule) {
        super(commandModule, name, description);

        this.apiClient = apiClient;
    }

    protected abstract CommandResult onStatsCommand(SlashCommandParameters commandParameters);

    @Override
    protected final CommandResult onCommand(final SlashCommandParameters commandParameters) {
        try {
            return this.onStatsCommand(commandParameters);
        } catch (final ApiOfflineException exception) {
            ErrorMessageUtilities.sendApiOfflineMessage(commandParameters);
        } catch (final InvalidApiKeyException exception) {
            ErrorMessageUtilities.sendInvalidApiKeyMessage(commandParameters);
        } catch (final RateLimitException exception) {
            ErrorMessageUtilities.sendRateLimitMessage(commandParameters, exception);
        } catch (final UnknownApiException exception) {
            ErrorMessageUtilities.sendUnknownApiExceptionMessage(commandParameters, exception);
            Sentry.captureException(exception);
        }
        return BaseCommandResult.ERROR;
    }

    public <T> void throwArgumentCorrectionMessage(final SlashCommandParameters commandParameters,
                                                   final Option<?> option,
                                                   final Class<? extends SlashCommand> mainCommandClass,
                                                   final Map<String, DiscordOption> mainNewArgs,
                                                   final List<T> similarValues,
                                                   final Function<T, String> valueToString) {
        final MainReplaceData replaceData;
        if (mainCommandClass == null) {
            replaceData = null;
        } else {
            replaceData = new MainReplaceData(mainCommandClass, null, mainNewArgs);
        }

        this.throwArgumentCorrectionMessage(
                commandParameters,
                option,
                replaceData,
                similarValues,
                valueToString
        );
    }

    public <T> void throwArgumentCorrectionMessage(final SlashCommandParameters commandParameters,
                                                   final Option<?> option,
                                                   final MainReplaceData replaceData,
                                                   final List<T> similarValues,
                                                   final Function<T, String> valueToString) {
        this.sendArgumentCorrectionMessage(
                commandParameters,
                option,
                replaceData,
                similarValues,
                valueToString
        );

        throw new CommandReturnException(BaseCommandResult.INVALID_ARGS);
    }

    protected CommandResult sendPicture(final SlashCommandParameters commandParameters,
                                        final InputStream inputStream,
                                        final String pictureName) {
        return this.sendPicture(
                commandParameters,
                inputStream,
                pictureName,
                null,
                null
        );
    }

    protected CommandResult sendPicture(final SlashCommandParameters commandParameters,
                                        final InputStream inputStream,
                                        final String pictureName,
                                        final Map<Button, ButtonAction> buttonActions) {
        // Handle empty actions
        // We need to handle them because jda will throw an exception otherwise
        if (buttonActions.isEmpty()) {
            return this.sendPicture(
                    commandParameters,
                    inputStream,
                    pictureName
            );
        }

        return this.sendPicture(
                commandParameters,
                inputStream,
                pictureName,
                commandRestAction -> commandRestAction.setActionRows(buttonActions.keySet()),
                message ->
                        DiscordBot.getInstance().getModuleManager().getModuleOrThrow(ButtonReactionModule.class).addButtonReactionMessage(
                                message,
                                new ButtonReaction(
                                        buttonActions,
                                        commandParameters.getUserDb().getDiscordId()
                                )
                        )
        );
    }

    protected CommandResult sendPicture(final SlashCommandParameters commandParameters,
                                        @Nullable final InputStream inputStream,
                                        final String pictureName,
                                        @Nullable final Consumer<CommandRestAction> messageActionFunction,
                                        @Nullable final Consumer<Message> messageConsumer) {
        if (inputStream != null) {
            final String fullPictureName = pictureName + ".png";
            final CommandRestAction messageAction = commandParameters.createFileAction(inputStream, fullPictureName);
            if (messageActionFunction != null) {
                messageActionFunction.accept(messageAction);
            }
            messageAction.queue(messageConsumer);

            return BaseCommandResult.SUCCESSFUL;
        } else {
            SlashMessageUtilities.sendErrorMessage(commandParameters, "Error while sending picture.");
            return BaseCommandResult.ERROR;
        }
    }
}
