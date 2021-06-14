package de.timmi6790.mineplex.stats.common;

import lombok.Data;

@Data
public class Config {
    private final NotificationChannel notificationChannel = new NotificationChannel();
    private final ApiConfig api = new ApiConfig();

    @Data
    public static class NotificationChannel {
        private long filterChannel = 0;
        private long aliasNameChannel = 0;
    }

    @Data
    public static class ApiConfig {
        private String url = "https://mpstats.timmi6790.de";
        private String key = "";
    }
}