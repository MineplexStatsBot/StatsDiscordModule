package de.timmi6790.mineplex.stats.java.commands.leaderboard;


import de.timmi6790.discord_framework.module.modules.slashcommand.SlashCommandModule;
import de.timmi6790.discord_framework.module.modules.slashcommand.parameters.SlashCommandParameters;
import de.timmi6790.mineplex.stats.common.commands.leaderboard.LeaderboardCommand;
import de.timmi6790.mineplex.stats.common.utilities.ArgumentParsingUtilities;
import de.timmi6790.mpstats.api.client.common.BaseApiClient;
import de.timmi6790.mpstats.api.client.java.player.models.JavaPlayer;

public class JavaLeaderboardCommand extends LeaderboardCommand<JavaPlayer> {
    public JavaLeaderboardCommand(final BaseApiClient<JavaPlayer> baseApiClient, final SlashCommandModule commandModule) {
        this(
                baseApiClient,
                "leaderboard",
                commandModule,
                "lb"
        );
    }

    public JavaLeaderboardCommand(final BaseApiClient<JavaPlayer> baseApiClient,
                                  final String name,
                                  final SlashCommandModule commandModule,
                                  final String... aliasNames) {
        super(
                baseApiClient,
                commandModule,
                "Java",
                name,
                "Check the leaderboard",
                "Java",
                "<game> <stat> [board]",
                aliasNames
        );

        this.addOptions(
                STAT_OPTION_REQUIRED,
                BOARD_OPTION
        );
    }


    @Override
    protected String getStat(final SlashCommandParameters commandParameters) {
        return commandParameters.getOptionOrThrow(STAT_OPTION_REQUIRED);
    }

    @Override
    protected String getBoard(final SlashCommandParameters commandParameters) {
        return commandParameters.getOption(BOARD_OPTION).orElse(ArgumentParsingUtilities.getDefaultBoard());
    }
}
