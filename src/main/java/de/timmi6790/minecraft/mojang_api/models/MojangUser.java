package de.timmi6790.minecraft.mojang_api.models;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import de.timmi6790.minecraft.mojang_api.MojangApi;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

import java.lang.reflect.Type;
import java.util.UUID;
import java.util.regex.Pattern;

@Data
@AllArgsConstructor
public class MojangUser {
    private static final Pattern FULL_UUID_PATTERN = Pattern.compile("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})");
    private static final String PLAYER_HEARD_URL = "https://minotar.net/avatar/%s";

    @NonNull
    private final String name;
    @NonNull
    private final UUID uuid;

    public NameHistory getNameHistory() {
        return MojangApi.getUserNames(this.uuid).orElseThrow(RuntimeException::new);
    }

    public String getHeadUrl() {
        return String.format(PLAYER_HEARD_URL, this.uuid.toString().replace("-", ""));
    }

    public static class MojangUserDeserializer implements JsonDeserializer<MojangUser> {
        @Override
        public MojangUser deserialize(final JsonElement jsonElement,
                                      final Type type,
                                      final JsonDeserializationContext jsonDeserializationContext) {
            final String uuidShort = jsonElement.getAsJsonObject().get("id").getAsString();
            final String uuid = FULL_UUID_PATTERN.matcher(uuidShort).replaceAll("$1-$2-$3-$4-$5");
            return new MojangUser(
                    jsonElement.getAsJsonObject().get("name").getAsString(),
                    UUID.fromString(uuid)
            );
        }
    }
}
