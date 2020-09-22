package de.timmi6790.minecraft.mojang_api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.timmi6790.discord_framework.DiscordBot;
import de.timmi6790.minecraft.mojang_api.models.MojangUser;
import de.timmi6790.minecraft.mojang_api.models.NameHistory;
import kong.unirest.GetRequest;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;

import java.util.Optional;
import java.util.UUID;

public class MojangApi {
    private final Gson gson;

    public MojangApi() {
        this.gson = new GsonBuilder()
                .registerTypeAdapter(MojangUser.class, new MojangUser.MojangUserDeserializer())
                .registerTypeAdapter(NameHistory.class, new NameHistory.NameHistoryDeserializer())
                .create();
    }

    private <T> Optional<T> getCustomJson(final GetRequest getRequest, final Class<T> clazz) {
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

        return Optional.of(this.gson.fromJson(response.getBody(), clazz));
    }

    public Optional<MojangUser> getUser(final String playerName) {
        return this.getCustomJson(
                Unirest.get("https://api.mojang.com/users/profiles/minecraft/{player}")
                        .routeParam("player", playerName),
                MojangUser.class
        );
    }

    public Optional<NameHistory> getUserNames(final UUID uuid) {
        return this.getCustomJson(
                Unirest.get("https://api.mojang.com/user/profiles/{uuid}/names")
                        .routeParam("uuid", uuid.toString().replace("-", "")),
                NameHistory.class
        );
    }
}
