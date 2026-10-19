package sonar.fluxnetworks.client.gui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.FormattedCharSequence;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Compatibility layer for the 26.1 GUI extraction API.
 * <p>
 * The original GUI code relied on global render state ({@code RenderSystem.setShaderColor} and
 * {@code setShaderTexture}) and immediate-mode vertex buffers. Since 1.21.6 GUI drawing is deferred and
 * every draw call carries its own texture and color, so this class keeps the "current" texture and color
 * and applies them to each blit.
 */
public final class GuiDraw {

    public static final int TEXTURE_SIZE = 512;

    private static Identifier sTexture = null;
    private static int sColor = -1;

    private GuiDraw() {
    }

    public static void setShaderColor(float r, float g, float b, float a) {
        sColor = ARGB.colorFromFloat(a, r, g, b);
    }

    public static void resetColor() {
        sColor = -1;
    }

    public static int getColor() {
        return sColor;
    }

    public static void setShaderTexture(Identifier texture) {
        sTexture = texture;
    }

    /**
     * Ensures text colors written as 0xRRGGBB (no alpha) are drawn opaque, matching pre-1.21.6 behavior.
     */
    public static int opaqueIfNoAlpha(int color) {
        return (color & 0xFC000000) == 0 ? color | 0xFF000000 : color;
    }

    /**
     * Blit a region of the current 512x512 texture to a float-precision rectangle.
     */
    public static void blitF(@Nonnull GuiGraphicsExtractor gr, float x, float y, float width, float height,
                             float uOffset, float vOffset, float uWidth, float vHeight) {
        blitF(gr, sTexture, x, y, width, height, uOffset, vOffset, uWidth, vHeight, TEXTURE_SIZE, TEXTURE_SIZE, sColor);
    }

    public static void blitF(@Nonnull GuiGraphicsExtractor gr, @Nullable Identifier texture,
                             float x, float y, float width, float height,
                             float uOffset, float vOffset, float uWidth, float vHeight,
                             int textureWidth, int textureHeight, int color) {
        if (texture == null || width <= 0 || height <= 0) {
            return;
        }
        // Draw a 1x1 quad under a transform; positions in the extractor API are integers.
        gr.pose().pushMatrix();
        gr.pose().translate(x, y);
        gr.pose().scale(width, height);
        gr.blit(RenderPipelines.GUI_TEXTURED, texture, 0, 0, uOffset, vOffset, 1, 1,
                Math.round(uWidth), Math.round(vHeight), textureWidth, textureHeight, color);
        gr.pose().popMatrix();
    }

    /**
     * Blit the whole current texture stretched over a rectangle.
     */
    public static void blitFull(@Nonnull GuiGraphicsExtractor gr, float x, float y, float width, float height) {
        blitF(gr, sTexture, x, y, width, height, 0, 0, 1, 1, 1, 1, sColor);
    }

    // ---- text helpers (old GuiGraphics#drawString defaulted to a drop shadow) ----

    public static void text(@Nonnull GuiGraphicsExtractor gr, Font font, @Nullable String text, int x, int y, int color) {
        gr.text(font, text, x, y, opaqueIfNoAlpha(color), true);
    }

    public static void text(@Nonnull GuiGraphicsExtractor gr, Font font, @Nullable String text, int x, int y, int color, boolean shadow) {
        gr.text(font, text, x, y, opaqueIfNoAlpha(color), shadow);
    }

    public static void text(@Nonnull GuiGraphicsExtractor gr, Font font, @Nullable String text, float x, float y, int color, boolean shadow) {
        drawAt(gr, x, y, () -> gr.text(font, text, 0, 0, opaqueIfNoAlpha(color), shadow));
    }

    public static void text(@Nonnull GuiGraphicsExtractor gr, Font font, Component text, int x, int y, int color) {
        gr.text(font, text, x, y, opaqueIfNoAlpha(color), true);
    }

    public static void text(@Nonnull GuiGraphicsExtractor gr, Font font, Component text, int x, int y, int color, boolean shadow) {
        gr.text(font, text, x, y, opaqueIfNoAlpha(color), shadow);
    }

    public static void text(@Nonnull GuiGraphicsExtractor gr, Font font, Component text, float x, float y, int color, boolean shadow) {
        drawAt(gr, x, y, () -> gr.text(font, text, 0, 0, opaqueIfNoAlpha(color), shadow));
    }

    public static void text(@Nonnull GuiGraphicsExtractor gr, Font font, FormattedCharSequence text, int x, int y, int color) {
        gr.text(font, text, x, y, opaqueIfNoAlpha(color), true);
    }

    public static void text(@Nonnull GuiGraphicsExtractor gr, Font font, FormattedCharSequence text, int x, int y, int color, boolean shadow) {
        gr.text(font, text, x, y, opaqueIfNoAlpha(color), shadow);
    }

    public static void centered(@Nonnull GuiGraphicsExtractor gr, Font font, String text, int x, int y, int color) {
        gr.centeredText(font, text, x, y, opaqueIfNoAlpha(color));
    }

    public static void centered(@Nonnull GuiGraphicsExtractor gr, Font font, Component text, int x, int y, int color) {
        gr.centeredText(font, text, x, y, opaqueIfNoAlpha(color));
    }

    public static void centered(@Nonnull GuiGraphicsExtractor gr, Font font, FormattedCharSequence text, int x, int y, int color) {
        gr.centeredText(font, text, x, y, opaqueIfNoAlpha(color));
    }

    private static void drawAt(GuiGraphicsExtractor gr, float x, float y, Runnable r) {
        gr.pose().pushMatrix();
        gr.pose().translate(x, y);
        r.run();
        gr.pose().popMatrix();
    }
}
