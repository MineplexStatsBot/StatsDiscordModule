package de.timmi6790.mineplex_stats.picture;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Log4j2
public abstract class AbstractPicture {
    public static final Color COLOUR_DISCORD_DARK_MODE = new Color(54, 57, 63);

    protected static final Graphics2D GD_TEST = new BufferedImage(1, 1, BufferedImage.TYPE_4BYTE_ABGR).createGraphics();

    protected int getTextWidth(final String text, final Font font) {
        return GD_TEST.getFontMetrics(font).stringWidth(text);
    }

    protected Graphics2D getDiscordGraphics(final BufferedImage image) {
        final Graphics2D gd = image.createGraphics();

        // Render hints
        final RenderingHints renderingHints = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        renderingHints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        renderingHints.put(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        renderingHints.put(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        renderingHints.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        renderingHints.put(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
        renderingHints.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        gd.setRenderingHints(renderingHints);

        // Background
        gd.setPaint(COLOUR_DISCORD_DARK_MODE);
        gd.fillRect(0, 0, image.getWidth(), image.getHeight());
        gd.setPaint(Color.WHITE);

        return gd;
    }

    protected InputStream convertToInputStream(final BufferedImage image) {
        try (final ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", os);
            return new ByteArrayInputStream(os.toByteArray());
        } catch (final IOException e) {
            log.error("", e);
        }

        return null;
    }
}
