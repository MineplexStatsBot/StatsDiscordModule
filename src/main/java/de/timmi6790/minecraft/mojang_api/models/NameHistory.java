package de.timmi6790.minecraft.mojang_api.models;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

@Data
@AllArgsConstructor
public class NameHistory {
    private final List<NameHistoryData> history;

    @Data
    @AllArgsConstructor
    public static class NameHistoryData {
        private final String name;
        private final long changedAt;

        public String getFormattedTime() {
            if (this.changedAt == -1) {
                return "Original";
            }

            final SimpleDateFormat formatDate = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss z");
            formatDate.setTimeZone(TimeZone.getTimeZone("UTC"));
            return formatDate.format(this.changedAt);
        }
    }

    public static class NameHistoryDeserializer implements JsonDeserializer<NameHistory> {
        @Override
        public NameHistory deserialize(final JsonElement jsonElement,
                                       final Type type,
                                       final JsonDeserializationContext jsonDeserializationContext) {
            final List<NameHistoryData> nameHistory = new ArrayList<>();
            for (final JsonElement object : jsonElement.getAsJsonArray()) {
                long playerNameChangeDateTime = -1;
                if (object.getAsJsonObject().has("changedToAt")) {
                    playerNameChangeDateTime = object.getAsJsonObject().get("changedToAt").getAsLong();
                }

                nameHistory.add(
                        new NameHistoryData(
                                object.getAsJsonObject().get("name").getAsString(),
                                playerNameChangeDateTime
                        )
                );
            }

            return new NameHistory(
                    nameHistory
            );
        }
    }
}
