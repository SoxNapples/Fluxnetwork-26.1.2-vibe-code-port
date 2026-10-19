package sonar.fluxnetworks.client.gui.button;

import sonar.fluxnetworks.client.gui.GuiDraw;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import sonar.fluxnetworks.client.gui.EnumNavigationTab;
import sonar.fluxnetworks.client.gui.basic.GuiButtonCore;
import sonar.fluxnetworks.client.gui.basic.GuiFocusable;

public class NavigationButton extends GuiButtonCore {

    private final EnumNavigationTab mTab;
    private boolean mSelected = false;

    public NavigationButton(GuiFocusable screen, int x, int y, EnumNavigationTab tab) {
        super(screen, x, y, 16, 16);
        mTab = tab;
    }

    @Override
    protected void drawButton(GuiGraphicsExtractor gr, int mouseX, int mouseY, float deltaTicks) {
        boolean hovered = isMouseHovered(mouseX, mouseY);
        if (mClickable) {
            if (mSelected || hovered) {
                GuiDraw.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            } else {
                GuiDraw.setShaderColor(0.7f, 0.7f, 0.7f, 1.0f);
            }
        } else {
            GuiDraw.setShaderColor(0.5f, 0.5f, 0.5f, 1.0f);
        }
        GuiDraw.setShaderTexture(GuiFocusable.ICON);

        GuiDraw.blitF(gr, x, y, width, height, 64 * mTab.ordinal(), 192, 64, 64);

        if (hovered && mClickable) {
            GuiDraw.centered(gr, screen.getMinecraft().font, mTab.getTranslatedName(),
                    x + width / 2, y - 10, 0xFFFFFFFF);
        }
        GuiDraw.setShaderColor(1.0F, 1.0F, 1.0F, 1.0f);
    }

    public EnumNavigationTab getTab() {
        return mTab;
    }

    public boolean isSelected() {
        return mSelected;
    }

    public void setSelected(boolean selected) {
        mSelected = selected;
    }
}
