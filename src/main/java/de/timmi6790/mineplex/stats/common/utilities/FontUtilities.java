package de.timmi6790.mineplex.stats.common.utilities;

import io.sentry.Sentry;
import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@UtilityClass
@Log4j2
public class FontUtilities {
    public Optional<Font> loadFontFromFile(final String path, final int fontFormat) {
        try {
            final ClassLoader classLoader = FontUtilities.class.getClassLoader();
            final URL url = classLoader.getResource(path);
            if (url == null) {
                return Optional.empty();
            }

            final InputStream fontStream = url.openStream();
            return Optional.ofNullable(Font.createFont(fontFormat, fontStream));
        } catch (final FontFormatException | IOException e) {
            Sentry.captureException(e);
            log.error("Can't load font from file: " + path, e);
            return Optional.empty();
        }
    }

    public Font getRandomFont() {
        final GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
        final Font[] allFonts = graphicsEnvironment.getAllFonts();
        final int arrayIndex = ThreadLocalRandom.current().nextInt(allFonts.length);
        return allFonts[arrayIndex];
    }
}
