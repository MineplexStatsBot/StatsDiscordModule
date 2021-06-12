package de.timmi6790.mineplex.stats.common.commands;

import de.timmi6790.discord_framework.module.modules.command.AbstractCommand;
import de.timmi6790.discord_framework.module.modules.command.CommandParameters;
import de.timmi6790.discord_framework.module.modules.command.CommandResult;
import de.timmi6790.discord_framework.module.modules.command.exceptions.CommandReturnException;
import de.timmi6790.discord_framework.module.modules.reactions.button.ButtonReaction;
import de.timmi6790.discord_framework.module.modules.reactions.button.ButtonReactionModule;
import de.timmi6790.discord_framework.module.modules.reactions.button.actions.ButtonAction;
import de.timmi6790.discord_framework.module.modules.reactions.button.actions.CommandButtonAction;
import de.timmi6790.discord_framework.utilities.commons.StringUtilities;
import de.timmi6790.discord_framework.utilities.discord.DiscordEmotes;
import de.timmi6790.mineplex.stats.common.utilities.ArrayUtilities;
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
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.interactions.components.ButtonStyle;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import net.dv8tion.jda.api.utils.MarkdownUtil;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class BaseStatsCommand<P extends Player> extends AbstractCommand {
    @Getter(AccessLevel.PROTECTED)
    private final ButtonReactionModule buttonReactionModule;
    @Getter(AccessLevel.PROTECTED)
    private final BaseApiClient<P> apiClient;

    protected BaseStatsCommand(final BaseApiClient<P> apiClient,
                               @NonNull final String name,
                               @NonNull final String category,
                               @NonNull final String description,
                               @NonNull final String syntax,
                               final String... aliasNames) {
        super(name, category, description, syntax, aliasNames);

        this.apiClient = apiClient;

        this.buttonReactionModule = this.getModuleOrThrow(ButtonReactionModule.class);
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
        return CommandResult.ERROR;
    }

    public <T> void throwArgumentCorrectionMessage(final CommandParameters commandParameters,
                                                   final String userArg,
                                                   final int argPos,
                                                   final String argName,
                                                   @Nullable final Class<? extends AbstractCommand> mainCommandClass,
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
        throw new CommandReturnException(CommandResult.INVALID_ARGS);
    }

    public <T> void sendArgumentCorrectionMessage(final CommandParameters commandParameters,
                                                  final String userArg,
                                                  final int argPos,
                                                  final String argName,
                                                  @Nullable final Class<? extends AbstractCommand> mainCommandClass,
                                                  final String[] mainNewArgs,
                                                  final List<T> similarValues,
                                                  final Function<T, String> valueToString) {
        final AbstractCommand mainCommand;
        if (mainCommandClass == null) {
            mainCommand = null;
        } else {
            mainCommand = this.getCommandModule().getCommand(mainCommandClass).orElse(null);

        }

        final Map<Button, ButtonAction> buttons = new LinkedHashMap<>();
        final StringBuilder description = new StringBuilder(
                String.format(
                        "%s is not a valid %s.%n",
                        MarkdownUtil.monospace(userArg),
                        argName
                )
        );

        // Only main command
        if (similarValues.isEmpty() && mainCommand != null) {
            description.append(String.format(
                    "Use the %s command or click the %s emote to see all %ss.",
                    MarkdownUtil.bold(
                            String.join(" ",
                                    this.getCommandModule().getMainCommand(),
                                    mainCommand.getName(),
                                    String.join(" ", mainNewArgs)
                            )
                    ),
                    DiscordEmotes.FOLDER.getEmote(),
                    argName
            ));
        } else {
            // Contains help values
            description.append("Is it possible that you wanted to write?\n\n");

            // We can only have 5 buttons per message
            final int allowedButtons = mainCommand != null ? 4 : 5;
            for (int index = 0; Math.min(allowedButtons, similarValues.size()) > index; index++) {
                final String similarValue = valueToString.apply(similarValues.get(index));
                final String emote = DiscordEmotes.getNumberEmote(index + 1).getEmote();

                description.append(String.format(
                        "%s %s%n",
                        emote,
                        similarValue
                ));

                buttons.put(
                        Button.of(ButtonStyle.SECONDARY, emote, "").withEmoji(Emoji.fromUnicode(emote)),
                        new CommandButtonAction(
                                this.getClass(),
                                CommandParameters.of(
                                        commandParameters,
                                        ArrayUtilities.modifyArrayAtPosition(
                                                commandParameters.getArgs(),
                                                similarValue,
                                                argPos
                                        )
                                )
                        )
                );
            }

            if (mainCommand != null) {
                description.append(String.format(
                        "%n%s %s",
                        DiscordEmotes.FOLDER.getEmote(),
                        MarkdownUtil.bold("All " + argName + "s")
                ));
            }
        }


        if (mainCommand != null) {
            final CommandParameters newCommandParameters = CommandParameters.of(commandParameters, mainNewArgs);
            final String everythingEmote = DiscordEmotes.FOLDER.getEmote();
            buttons.put(
                    Button.of(ButtonStyle.SECONDARY, everythingEmote, "")
                            .withEmoji(Emoji.fromUnicode(everythingEmote)),
                    new CommandButtonAction(
                            mainCommand.getClass(),
                            newCommandParameters
                    )
            );
        }

        // Send message
        commandParameters.getLowestMessageChannel()
                .sendMessage(
                        this.getEmbedBuilder(commandParameters)
                                .setTitle("Invalid " + StringUtilities.capitalize(argName))
                                .setDescription(description.toString())
                                .setFooter("↓ Click Me!")
                                .buildSingle()
                ).setActionRows(ActionRow.of(buttons.keySet()))
                .queue(message ->
                        this.buttonReactionModule.addButtonReactionMessage(
                                message,
                                new ButtonReaction(
                                        buttons,
                                        commandParameters.getUserDb().getDiscordId()
                                )
                        )
                );
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
                        this.buttonReactionModule.addButtonReactionMessage(
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

            return CommandResult.SUCCESS;
        } else {
            this.sendErrorMessage(commandParameters, "Error while sending picture.");
            return CommandResult.ERROR;
        }
    }
}
