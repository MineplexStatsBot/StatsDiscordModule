package de.timmi6790.mineplex_stats.statsapi;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.timmi6790.commons.builders.MapBuilder;
import de.timmi6790.discord_framework.DiscordBot;
import de.timmi6790.mineplex_stats.statsapi.models.ResponseModel;
import de.timmi6790.mineplex_stats.statsapi.models.bedrock.BedrockGames;
import de.timmi6790.mineplex_stats.statsapi.models.bedrock.BedrockLeaderboard;
import de.timmi6790.mineplex_stats.statsapi.models.bedrock.BedrockPlayerStats;
import de.timmi6790.mineplex_stats.statsapi.models.errors.ErrorModel;
import de.timmi6790.mineplex_stats.statsapi.models.java.*;
import de.timmi6790.mineplex_stats.statsapi.utilities.JavaGamesModelDeserializer;
import kong.unirest.*;
import kong.unirest.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MpStatsRestApiClient {
    private static final String ALIAS = "alias";
    private static final String PLAYER = "player";
    private static final String BOARD = "board";
    private static final String GAME = "game";
    private static final String STAT = "stat";
    private static final String DATE = "date";
    private static final String FILTERING = "filtering";

    private static final String BASE_URL = "https://mpstats.timmi6790.de/";//  "http://127.0.0.1:8000/"

    private static final ErrorModel UNKNOWN_ERROR_RESPONSE_MODEL = new ErrorModel(-1, "Unknown Error");
    private static final ErrorModel TIMEOUT_ERROR_RESPONSE_MODEL = new ErrorModel(-1, "API Timeout Exception");

    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(JavaGamesModel.class, new JavaGamesModelDeserializer())
            .create();

    private final String authName;
    private final String authPassword;

    private final UnirestInstance unirest;

    public MpStatsRestApiClient(final String authName, final String authPassword) {
        this.authName = authName;
        this.authPassword = authPassword;

        this.unirest = Unirest.spawnInstance();
        this.unirest.config()
                .defaultBaseUrl(BASE_URL)
                .connectTimeout(6_000)
                .addDefaultHeader("User-Agent", "MpStatsRestApiClient-Java")
                .setDefaultBasicAuth(this.authName, this.authPassword);
    }

    private ResponseModel makeRequest(final String url, final Map<String, Object> params, final Class<? extends ResponseModel> wantedClazz) {
        try {
            final HttpResponse<JsonNode> response = this.unirest.get(url)
                    .queryString(params)
                    .asJson();

            if (!response.isSuccess()) {
                return UNKNOWN_ERROR_RESPONSE_MODEL;
            }

            final JSONObject jsonObject = response.getBody().getObject();
            if (!jsonObject.getBoolean("success")) {
                return this.gson.fromJson(jsonObject.toString(), ErrorModel.class);
            }
            return this.gson.fromJson(jsonObject.toString(), wantedClazz);

        } catch (final UnirestException e) {
            DiscordBot.getLogger().error(e);
            return TIMEOUT_ERROR_RESPONSE_MODEL;
        } catch (final Exception e) {
            DiscordBot.getLogger().error(e);
            return UNKNOWN_ERROR_RESPONSE_MODEL;
        }
    }

    public ResponseModel getJavaGames() {
        return this.makeRequest("java/leaderboards/games", new HashMap<>(0), JavaGamesModel.class);
    }

    public ResponseModel getJavaPlayerStats(final String player, final String game, final String board, final long unixTime, final boolean filtering) {
        return this.makeRequest(
                "java/leaderboards/player",
                MapBuilder.<String, Object>ofHashMap(5)
                        .put(PLAYER, player)
                        .put(GAME, game)
                        .put(BOARD, board.toLowerCase())
                        .put(DATE, unixTime)
                        .put(FILTERING, filtering)
                        .build(),
                JavaPlayerStats.class
        );
    }

    public ResponseModel getJavaPlayerStats(final UUID playerUUId, final String player, final String game, final String board, final long unixTime, final boolean filtering) {
        return this.makeRequest(
                "java/leaderboards/playerUUID",
                MapBuilder.<String, Object>ofHashMap(5)
                        .put("uuid", playerUUId.toString())
                        .put(PLAYER, player)
                        .put(GAME, game)
                        .put(BOARD, board.toLowerCase())
                        .put(DATE, unixTime)
                        .put(FILTERING, filtering)
                        .build(),
                JavaPlayerStats.class
        );
    }

    public ResponseModel getJavaLeaderboard(final String game, final String stat, final String board, final int startPos, final int endPos,
                                            final long unixTime, final boolean filtering) {
        return this.makeRequest(
                "java/leaderboards/leaderboard",
                MapBuilder.<String, Object>ofHashMap(7)
                        .put(GAME, game)
                        .put(STAT, stat)
                        .put(BOARD, board.toLowerCase())
                        .put("startPosition", startPos)
                        .put("endPosition", endPos)
                        .put(DATE, unixTime)
                        .put(FILTERING, filtering)
                        .build(),
                JavaLeaderboard.class
        );
    }

    public ResponseModel getGroups() {
        return this.makeRequest("java/leaderboards/group/groups", new HashMap<>(0), JavaGroupsGroups.class);
    }

    public ResponseModel getPlayerGroup(final String player, final String group, final String stat, final String board, final long unixTime) {
        return this.makeRequest(
                "java/leaderboards/group/player",
                MapBuilder.<String, Object>ofHashMap(5)
                        .put(PLAYER, player)
                        .put("group", group)
                        .put(STAT, stat)
                        .put(BOARD, board.toLowerCase())
                        .put(DATE, unixTime)
                        .build(),
                JavaGroupsPlayer.class
        );
    }

    public ResponseModel getPlayerGroup(final UUID playerUUID, final String group, final String stat, final String board, final long unixTime) {
        return this.makeRequest(
                "java/leaderboards/group/playerUUID",
                MapBuilder.<String, Object>ofHashMap(5)
                        .put("uuid", playerUUID.toString())
                        .put("group", group)
                        .put(STAT, stat)
                        .put(BOARD, board.toLowerCase())
                        .put(DATE, unixTime)
                        .build(),
                JavaGroupsPlayer.class
        );
    }

    public ResponseModel getPlayerStatsRatio(final String player, final String stat, final String board, final long unixTime) {
        return this.makeRequest(
                "java/leaderboards/ratio/player",
                MapBuilder.<String, Object>ofHashMap(4)
                        .put(PLAYER, player)
                        .put(STAT, stat)
                        .put(BOARD, board.toLowerCase())
                        .put(DATE, unixTime)
                        .build(),
                JavaRatioPlayer.class
        );
    }

    // Bedrock
    public ResponseModel getBedrockGames() {
        return this.makeRequest("bedrock/leaderboards/games", new HashMap<>(0), BedrockGames.class);
    }

    public ResponseModel getBedrockLeaderboard(final String game, final int startPos, final int endPos, final long unixTime) {
        return this.makeRequest(
                "bedrock/leaderboards/leaderboard",
                MapBuilder.<String, Object>ofHashMap(4)
                        .put(GAME, game)
                        .put("startPosition", startPos)
                        .put("endPosition", endPos)
                        .put(DATE, unixTime)
                        .build(),
                BedrockLeaderboard.class
        );
    }

    public ResponseModel getBedrockPlayerStats(final String player) {
        return this.makeRequest(
                "bedrock/leaderboards/player",
                MapBuilder.<String, Object>ofHashMap(1)
                        .put("name", player)
                        .build(),
                BedrockPlayerStats.class
        );
    }

    // Internal
    public void addJavaPlayerFilter(final UUID uuid, final String game, final String stat, final String board) {
        if (this.authName == null || this.authPassword == null) {
            return;
        }

        this.unirest.post("java/leaderboards/filter")
                .queryString(GAME, game)
                .queryString(STAT, stat)
                .queryString(BOARD, board.toLowerCase())
                .queryString("uuid", uuid.toString())
                .asEmpty();
    }

    public void addBedrockPlayerFilter(final String player, final String game) {
        if (this.authName == null || this.authPassword == null) {
            return;
        }

        this.unirest.post("bedrock/leaderboards/filter")
                .queryString(GAME, game)
                .queryString("name", player)
                .asEmpty();
    }

    public void addJavaBoardAlias(final String board, final String alias) {
        if (this.authName == null || this.authPassword == null) {
            return;
        }

        this.unirest.post("java/leaderboards/alias/board")
                .queryString(BOARD, board.toLowerCase())
                .queryString(ALIAS, alias)
                .asEmpty();
    }

    public void addJavaGameAlias(final String game, final String alias) {
        if (this.authName == null || this.authPassword == null) {
            return;
        }

        this.unirest.post("java/leaderboards/alias/game")
                .queryString(GAME, game)
                .queryString(ALIAS, alias)
                .asEmpty();
    }

    public void addJavaStatAlias(final String game, final String stat, final String alias) {
        if (this.authName == null || this.authPassword == null) {
            return;
        }

        this.unirest.post("java/leaderboards/alias/stat")
                .queryString(GAME, game)
                .queryString(STAT, stat)
                .queryString(ALIAS, alias)
                .asEmpty();
    }
}
