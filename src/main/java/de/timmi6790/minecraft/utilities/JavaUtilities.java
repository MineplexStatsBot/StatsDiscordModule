package de.timmi6790.minecraft.utilities;

import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import de.timmi6790.discord_framework.DiscordBot;
import io.sentry.Sentry;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

@UtilityClass
public class JavaUtilities {
    private final Pattern NAME_PATTERN = Pattern.compile("^\\w{1,16}$");

    private static final AsyncLoadingCache<UUID, BufferedImage> SKIN_CACHE = Caffeine.newBuilder()
            .maximumSize(10_000)
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .expireAfterAccess(2, TimeUnit.MINUTES)
            .buildAsync(uuid -> {
                final HttpResponse<byte[]> response;
                try {
                    response = Unirest.get("https://visage.surgeplay.com/frontfull/{uuid}.png")
                            .routeParam("uuid", uuid.toString().replace("-", ""))
                            .connectTimeout((int) TimeUnit.SECONDS.toMillis(2))
                            .asBytes();
                } catch (Exception e) {
                    return null;
                }

                if (!response.isSuccess()) {
                    return null;
                }

                try (final InputStream in = new ByteArrayInputStream(response.getBody())) {
                    return ImageIO.read(in);
                } catch (final IOException e) {
                    DiscordBot.getLogger().error(e);
                    Sentry.captureException(e);
                    return null;
                }
            });


    public boolean isValidName(@NonNull final String playerName) {
        return NAME_PATTERN.matcher(playerName).find();
    }

    public CompletableFuture<BufferedImage> getPlayerSkin(@NonNull final UUID uuid) {
        return SKIN_CACHE.get(uuid);
    }
}
