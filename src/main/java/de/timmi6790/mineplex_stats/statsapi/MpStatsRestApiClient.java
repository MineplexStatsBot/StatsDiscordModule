package de.timmi6790.mineplex_stats.statsapi;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.timmi6790.mineplex_stats.statsapi.deserializer.JavaGamesModelDeserializer;
import de.timmi6790.mineplex_stats.statsapi.models.ResponseModel;
import de.timmi6790.mineplex_stats.statsapi.models.errors.ErrorModel;
import de.timmi6790.mineplex_stats.statsapi.models.java.JavaGamesModel;
import kong.unirest.Unirest;
import kong.unirest.UnirestInstance;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import java.util.UUID;

@Log4j2
public class MpStatsRestApiClient {
    private static final String ALIAS = "alias";
    private static final String PLAYER = "player";
    private static final String BOARD = "board";
    private static final String GAME = "game";
    private static final String STAT = "stat";
    private static final String DATE = "date";
    private static final String FILTERING = "filtering";

    private static final ErrorModel UNKNOWN_ERROR_RESPONSE_MODEL = new ErrorModel(-1, "Unknown Error");
    private static final ErrorModel TIMEOUT_ERROR_RESPONSE_MODEL = new ErrorModel(-1, "API Timeout Exception");

    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(JavaGamesModel.class, new JavaGamesModelDeserializer())
            .create();

    @Getter(value = AccessLevel.PRIVATE)
    private final boolean validCredentials;

    private final UnirestInstance unirest;

    public MpStatsRestApiClient(final String authName,
                                final String authPassword,
                                final String url,
                                final int timeout) {
        this.validCredentials = authName != null && authPassword != null;

        this.unirest = Unirest.spawnInstance();
        this.unirest.config()
                .defaultBaseUrl(url)
                .connectTimeout(timeout)
                .addDefaultHeader("User-Agent", "MpStatsRestApiClient-Java")
                .setDefaultBasicAuth(authName, authPassword);
    }

    public ResponseModel getJavaGames() {
        return UNKNOWN_ERROR_RESPONSE_MODEL;
    }

    public ResponseModel getJavaPlayerStats(final String player,
                                            final String game,
                                            final String board,
                                            final long unixTime,
                                            final boolean filtering) {
        return UNKNOWN_ERROR_RESPONSE_MODEL;

    }

    public ResponseModel getJavaPlayerStats(final UUID playerUUId,
                                            final String player,
                                            final String game,
                                            final String board,
                                            final long unixTime,
                                            final boolean filtering) {
        return UNKNOWN_ERROR_RESPONSE_MODEL;

    }

    public ResponseModel getJavaLeaderboard(final String game,
                                            final String stat,
                                            final String board,
                                            final int startPos,
                                            final int endPos,
                                            final long unixTime,
                                            final boolean filtering) {
        return UNKNOWN_ERROR_RESPONSE_MODEL;

    }

    public ResponseModel getGroups() {
        return UNKNOWN_ERROR_RESPONSE_MODEL;

    }

    public ResponseModel getPlayerGroup(final String player,
                                        final String group,
                                        final String stat,
                                        final String board,
                                        final long unixTime) {
        return UNKNOWN_ERROR_RESPONSE_MODEL;

    }

    public ResponseModel getPlayerGroup(final UUID playerUUID,
                                        final String group,
                                        final String stat,
                                        final String board,
                                        final long unixTime) {
        return UNKNOWN_ERROR_RESPONSE_MODEL;

    }

    public ResponseModel getPlayerStatsRatio(final String player,
                                             final String stat,
                                             final String board,
                                             final long unixTime) {
        return UNKNOWN_ERROR_RESPONSE_MODEL;

    }

    // Bedrock
    public ResponseModel getBedrockGames() {
        return UNKNOWN_ERROR_RESPONSE_MODEL;

    }

    public ResponseModel getBedrockLeaderboard(final String game,
                                               final int startPos,
                                               final int endPos,
                                               final long unixTime) {
        return UNKNOWN_ERROR_RESPONSE_MODEL;

    }

    public ResponseModel getBedrockPlayerStats(final String player) {
        return UNKNOWN_ERROR_RESPONSE_MODEL;

    }

    // Internal
    public void addJavaPlayerFilter(final UUID uuid,
                                    final String game,
                                    final String stat,
                                    final String board) {
        if (this.isValidCredentials()) {
            this.unirest.post("java/leaderboards/filter")
                    .queryString(GAME, game)
                    .queryString(STAT, stat)
                    .queryString(BOARD, board.toLowerCase())
                    .queryString("uuid", uuid.toString())
                    .asEmpty();
        }
    }

    public void addBedrockPlayerFilter(final String player, final String game) {
        if (this.isValidCredentials()) {
            this.unirest.post("bedrock/leaderboards/filter")
                    .queryString(GAME, game)
                    .queryString("name", player)
                    .asEmpty();
        }
    }

    public void addJavaBoardAlias(final String board, final String alias) {
        if (this.isValidCredentials()) {
            this.unirest.post("java/leaderboards/alias/board")
                    .queryString(BOARD, board.toLowerCase())
                    .queryString(ALIAS, alias)
                    .asEmpty();
        }
    }

    public void addJavaGameAlias(final String game, final String alias) {
        if (this.isValidCredentials()) {
            this.unirest.post("java/leaderboards/alias/game")
                    .queryString(GAME, game)
                    .queryString(ALIAS, alias)
                    .asEmpty();
        }
    }

    public void addJavaStatAlias(final String game, final String stat, final String alias) {
        if (this.isValidCredentials()) {
            this.unirest.post("java/leaderboards/alias/stat")
                    .queryString(GAME, game)
                    .queryString(STAT, stat)
                    .queryString(ALIAS, alias)
                    .asEmpty();
        }
    }
}
