package sonar.fluxnetworks.client.gui.button;

import sonar.fluxnetworks.client.gui.GuiDraw;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import sonar.fluxnetworks.client.gui.basic.GuiButtonCore;
import sonar.fluxnetworks.client.gui.basic.GuiFocusable;

/**
 * Button may have two states, and have different icons and texts.
 */
public class EditButton extends GuiButtonCore {

    private final int mCheckU0;
    private final int mUncheckU0;
    private final String mCheckText;
    private final String mUncheckText;

    private boolean mChecked = false;

    public EditButton(GuiFocusable screen, int x, int y,
                      int checkU0, int uncheckU0, String checkText, String uncheckText) {
        this(screen, x, y, 10, 10, checkU0, uncheckU0, checkText, uncheckText);
    }

    public EditButton(GuiFocusable screen, int x, int y, int width, int height,
                      int checkU0, int uncheckU0, String checkText, String uncheckText) {
        super(screen, x, y, width, height);
        mCheckU0 = checkU0;
        mUncheckU0 = uncheckU0;
        mCheckText = checkText;
        mUncheckText = uncheckText;
    }

    @Override
    protected void drawButton(GuiGraphicsExtractor gr, int mouseX, int mouseY, float deltaTicks) {
        boolean hovered = isMouseHovered(mouseX, mouseY);
        if (mClickable) {
            if (hovered) {
                GuiDraw.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            } else {
                GuiDraw.setShaderColor(0.7f, 0.7f, 0.7f, 1.0f);
            }
        } else {
            GuiDraw.setShaderColor(0.35f, 0.35f, 0.35f, 1.0f);
        }
        GuiDraw.setShaderTexture(GuiFocusable.ICON);

        GuiDraw.blitF(gr, x, y, width, height, mChecked ? mCheckU0 : mUncheckU0, 256, 64, 64);

        if (hovered && mClickable) {
            GuiDraw.centered(gr, screen.getMinecraft().font, mChecked ? mCheckText : mUncheckText,
                    x + width / 2, y - 9, 0xFFFFFFFF);
        }
        GuiDraw.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    public void toggle() {
        mChecked = !mChecked;
    }

    public boolean isChecked() {
        return mChecked;
    }

    public void setChecked(boolean checked) {
        mChecked = checked;
    }
}
