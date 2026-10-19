package sonar.fluxnetworks.client.gui.basic;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import org.lwjgl.glfw.GLFW;
import sonar.fluxnetworks.FluxNetworks;
import sonar.fluxnetworks.client.gui.EnumNavigationTab;
import sonar.fluxnetworks.client.gui.GuiDraw;
import sonar.fluxnetworks.client.gui.button.FluxEditBox;
import sonar.fluxnetworks.common.connection.FluxMenu;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Fixes the focus of popup host and dialog popup.
 * <p>
 * Also bridges the 1.21.9+ event-object input methods to the (x, y, button) style handlers used
 * throughout the Flux Networks GUI, so subclasses and popups keep their original logic.
 */
public abstract class GuiFocusable extends AbstractContainerScreen<FluxMenu> {

    public static final int TEXTURE_SIZE = GuiDraw.TEXTURE_SIZE;

    public static final Identifier BACKGROUND = Identifier.fromNamespaceAndPath(
            FluxNetworks.MODID, "textures/gui/gui_background.png");
    public static final Identifier FRAME = Identifier.fromNamespaceAndPath(
            FluxNetworks.MODID, "textures/gui/gui_frame.png");
    public static final Identifier ICON = Identifier.fromNamespaceAndPath(
            FluxNetworks.MODID, "textures/gui/gui_icon.png");

    private int mLastModifiers;
    private boolean mLastDoubleClick;

    public GuiFocusable(FluxMenu menu, @Nonnull Player player) {
        super(menu, player.getInventory(), CommonComponents.EMPTY);
    }

    /**
     * Un-focus other text elements
     */
    @Override
    public void setFocused(@Nullable GuiEventListener listener) {
        super.setFocused(listener);
        for (GuiEventListener child : children()) {
            if (child != listener && child instanceof FluxEditBox editBox) {
                if (editBox.isFocused()) {
                    editBox.setFocused(false);
                }
            }
        }
    }

    // ---- input bridge ----

    protected final MouseButtonEvent mouseEvent(double x, double y, int button) {
        return new MouseButtonEvent(x, y, new MouseButtonInfo(button, mLastModifiers));
    }

    public static boolean clickChild(@Nonnull GuiEventListener child, double x, double y, int button) {
        return child.mouseClicked(new MouseButtonEvent(x, y, new MouseButtonInfo(button, 0)), false);
    }

    @Override
    public final boolean mouseClicked(@Nonnull MouseButtonEvent event, boolean doubleClick) {
        mLastModifiers = event.modifiers();
        mLastDoubleClick = doubleClick;
        return mouseClicked(event.x(), event.y(), event.button());
    }

    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        return super.mouseClicked(mouseEvent(mouseX, mouseY, mouseButton), mLastDoubleClick);
    }

    @Override
    public final boolean mouseReleased(@Nonnull MouseButtonEvent event) {
        mLastModifiers = event.modifiers();
        return mouseReleased(event.x(), event.y(), event.button());
    }

    public boolean mouseReleased(double mouseX, double mouseY, int mouseButton) {
        return super.mouseReleased(mouseEvent(mouseX, mouseY, mouseButton));
    }

    @Override
    public final boolean mouseDragged(@Nonnull MouseButtonEvent event, double deltaX, double deltaY) {
        mLastModifiers = event.modifiers();
        return mouseDragged(event.x(), event.y(), event.button(), deltaX, deltaY);
    }

    public boolean mouseDragged(double mouseX, double mouseY, int mouseButton, double deltaX, double deltaY) {
        return super.mouseDragged(mouseEvent(mouseX, mouseY, mouseButton), deltaX, deltaY);
    }

    @Override
    public final boolean keyPressed(@Nonnull KeyEvent event) {
        return keyPressed(event.key(), event.scancode(), event.modifiers());
    }

    @Override
    public final boolean keyReleased(@Nonnull KeyEvent event) {
        return keyReleased(event.key(), event.scancode(), event.modifiers());
    }

    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        return super.keyReleased(new KeyEvent(keyCode, scanCode, modifiers));
    }

    @Override
    public final boolean charTyped(@Nonnull CharacterEvent event) {
        return charTyped((char) event.codepoint(), mLastModifiers);
    }

    public boolean charTyped(char c, int modifiers) {
        return super.charTyped(new CharacterEvent(c));
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        KeyEvent event = new KeyEvent(keyCode, scanCode, modifiers);
        mLastModifiers = modifiers;
        if (getFocused() != null) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE ||
                    keyCode == GLFW.GLFW_KEY_ENTER ||
                    keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                setFocused(null);
                return true;
            }
            if (getMinecraft().options.keyInventory.matches(event)) {
                // consume the event if there's a focus
                return true;
            }
        } else if (keyCode == GLFW.GLFW_KEY_ESCAPE || getMinecraft().options.keyInventory.matches(event)) {
            if (this instanceof GuiPopupCore<?> core) {
                core.mHost.closePopup();
                return true;
            }
            if (this instanceof GuiTabCore core) {
                if (core.getNavigationTab() == EnumNavigationTab.TAB_HOME) {
                    onClose();
                } else {
                    core.switchTab(EnumNavigationTab.TAB_HOME, true);
                }
            }
            return true;
        }
        boolean result = super.keyPressed(event);
        // consume the event if there's a focus
        return result || getFocused() != null;
    }

    @Override
    protected void containerTick() {
        super.containerTick();
    }

    protected float getDeltaTicks() {
        return getMinecraft().getDeltaTracker().getRealtimeDeltaTicks();
    }

    protected void blitBackgroundOrFrame(@Nonnull GuiGraphicsExtractor gr) {
        float cx = width / 2f;
        float cy = height / 2f + 5;
        GuiDraw.blitFull(gr, cx - 86, cy - 86, 172, 172);
    }

    public static void blitF(@Nonnull GuiGraphicsExtractor gr, float x, float y, float width, float height,
                             float uOffset, float vOffset, float uWidth, float vHeight) {
        GuiDraw.blitF(gr, x, y, width, height, uOffset, vOffset, uWidth, vHeight);
    }

    /**
     * The Modern UI integration is not available on 26.1; always use the classic design.
     */
    public static boolean useModernDesign() {
        return false;
    }
}
