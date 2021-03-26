package de.timmi6790.minecraft.mojang_api;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.timmi6790.discord_framework.DiscordBot;
import de.timmi6790.minecraft.mojang_api.models.MojangUser;
import de.timmi6790.minecraft.mojang_api.models.NameHistory;
import kong.unirest.GetRequest;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import lombok.experimental.UtilityClass;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@UtilityClass
public class MojangApi {
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(MojangUser.class, new MojangUser.MojangUserDeserializer())
            .registerTypeAdapter(NameHistory.class, new NameHistory.NameHistoryDeserializer())
            .create();

    private final LoadingCache<String, Optional<MojangUser>> playerCache = Caffeine.newBuilder()
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .expireAfterAccess(5, TimeUnit.MINUTES)
            .build(playerName ->
                    parseJsonResponse(
                            Unirest.get("https://api.mojang.com/users/profiles/minecraft/{player}")
                                    .routeParam("player", playerName),
                            MojangUser.class
                    )
            );

    private <T> Optional<T> parseJsonResponse(final GetRequest getRequest, final Class<T> clazz) {
        final HttpResponse<String> response;
        try {
            response = getRequest.asString();
        } catch (final Exception e) {
            DiscordBot.getLogger().error(e);
            return Optional.empty();
        }

        if (!response.isSuccess() || response.getBody().isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(gson.fromJson(response.getBody(), clazz));
    }

    public Optional<MojangUser> getUser(final String playerName) {
        return playerCache.get(playerName);
    }

    public Optional<NameHistory> getUserNames(final UUID uuid) {
        return parseJsonResponse(
                Unirest.get("https://api.mojang.com/user/profiles/{uuid}/names")
                        .routeParam("uuid", uuid.toString().replace("-", "")),
                NameHistory.class
        );
    }
}
