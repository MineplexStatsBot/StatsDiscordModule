package de.timmi6790.mineplex.stats.common.commands;

import de.timmi6790.discord_framework.module.modules.command.Command;
import de.timmi6790.discord_framework.module.modules.command.CommandModule;
import de.timmi6790.discord_framework.module.modules.command.exceptions.CommandReturnException;
import de.timmi6790.discord_framework.module.modules.command.models.BaseCommandResult;
import de.timmi6790.discord_framework.module.modules.command.models.CommandParameters;
import de.timmi6790.discord_framework.module.modules.command.models.CommandResult;
import de.timmi6790.discord_framework.module.modules.command.utilities.MessageUtilities;
import de.timmi6790.discord_framework.module.modules.reactions.button.ButtonReaction;
import de.timmi6790.discord_framework.module.modules.reactions.button.actions.ButtonAction;
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
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class BaseStatsCommand<P extends Player> extends Command {
    @Getter(AccessLevel.PROTECTED)
    private final BaseApiClient<P> apiClient;

    protected BaseStatsCommand(final BaseApiClient<P> apiClient,
                               @NonNull final String name,
                               final CommandModule commandModule) {
        super(name, commandModule);

        this.apiClient = apiClient;
    }

    protected abstract CommandResult onStatsCommand(CommandParameters commandParameters);

    @Override
    protected final CommandResult onCommand(final CommandParameters commandParameters) {
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

    public <T> void throwArgumentCorrectionMessage(final CommandParameters commandParameters,
                                                   final String userArg,
                                                   final int argPos,
                                                   final String argName,
                                                   @Nullable final Class<? extends Command> mainCommandClass,
                                                   final String[] mainNewArgs,
                                                   final List<T> similarValues,
                                                   final Function<T, String> valueToString) {
        this.sendArgumentCorrectionMessage(
                commandParameters,
                userArg,
                argPos,
                argName,
                mainCommandClass,
                mainNewArgs,
                similarValues,
                valueToString
        );
        throw new CommandReturnException(BaseCommandResult.INVALID_ARGS);
    }

    protected CommandResult sendPicture(final CommandParameters commandParameters,
                                        @Nullable final InputStream inputStream,
                                        final String pictureName) {
        return this.sendPicture(
                commandParameters,
                inputStream,
                pictureName,
                null,
                null
        );
    }

    protected CommandResult sendPicture(final CommandParameters commandParameters,
                                        @Nullable final InputStream inputStream,
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
                messageAction -> messageAction.setActionRows(ActionRow.of(buttonActions.keySet())),
                message ->
                        this.getButtonReactionModule().addButtonReactionMessage(
                                message,
                                new ButtonReaction(
                                        buttonActions,
                                        commandParameters.getUserDb().getDiscordId()
                                )
                        )
        );
    }

    protected CommandResult sendPicture(final CommandParameters commandParameters,
                                        @Nullable final InputStream inputStream,
                                        final String pictureName,
                                        @Nullable final Function<MessageAction, MessageAction> messageActionFunction,
                                        @Nullable final Consumer<Message> messageConsumer) {
        if (inputStream != null) {
            final MessageChannel channel = commandParameters.getLowestMessageChannel();
            final String fullPictureName = pictureName + ".png";

            if (messageActionFunction == null) {
                channel.sendFile(inputStream, fullPictureName)
                        .queue(messageConsumer);
            } else {
                // We need to add an invisible message for the buttons to work
                final MessageAction messageAction = channel.sendMessage("** **")
                        .addFile(inputStream, fullPictureName);

                messageActionFunction.apply(messageAction)
                        .queue(messageConsumer);
            }

            return BaseCommandResult.SUCCESSFUL;
        } else {
            MessageUtilities.sendErrorMessage(commandParameters, "Error while sending picture.");
            return BaseCommandResult.ERROR;
        }
    }
}
