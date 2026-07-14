package io.github.itzispyder.clickcrystals.gui.elements.common.interactive;

import io.github.itzispyder.clickcrystals.gui.GuiElement;
import io.github.itzispyder.clickcrystals.gui.GuiScreen;
import io.github.itzispyder.clickcrystals.gui.misc.Color;
import io.github.itzispyder.clickcrystals.gui.misc.animators.Animations;
import io.github.itzispyder.clickcrystals.gui.misc.animators.Animator;
import io.github.itzispyder.clickcrystals.gui.misc.animators.PollingAnimator;
import io.github.itzispyder.clickcrystals.util.MathUtils;
import io.github.itzispyder.clickcrystals.util.minecraft.render.RenderUtils;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.Mth;

public class ColorWheelElement extends GuiElement {

    private final Slider sliderBrightness, sliderAlpha;
    private int curX, curY;
    private final Animator hoverAnimator;

    public ColorWheelElement(int x, int y, int r) {
        super(x, y, 2 * r, 2 * r);
        this.sliderBrightness = new Slider(x + width + 5, y + 10, width + 20, 5, 0xFF000000, 0xFFFFFFFF);
        this.sliderAlpha      = new Slider(x + width + 5, y + 30, width + 20, 5, 0x00FFFFFF, 0xFFFFFFFF);
        this.addChild(this.sliderBrightness);
        this.addChild(this.sliderAlpha);
        this.hoverAnimator = new PollingAnimator(200, () -> {
            return mc.gui.screen() instanceof GuiScreen screen
                    && (screen.selected == this || screen.selected == sliderBrightness || screen.selected == sliderAlpha);
        }, Animations.ELASTIC_BOUNCE);
    }

    @Override
    public void onRender(GuiGraphicsExtractor context, int mouseX, int mouseY) {
        // pre render tasks
        int r = width / 2;
        int cx = x + r;
        int cy = y + r;
        if (mc.gui.screen() instanceof GuiScreen screen && screen.selected == this) {
            curX = mouseX - cx;
            curY = mouseY - cy;
            clampCursor();
        }
        int currentArgb = getArgb();
        sliderBrightness.colorEnd = 0xFF << 24 | (currentArgb & 0xFFFFFF);
        // end pre render tasks

//        RenderUtils.fillRoundShadow(context, x, y, width, height, width / 2, 1, 0xFFFFFFFF, 0xFFFFFFFF);
        RenderUtils.fillVeryUselessColorWheel(context, x, y, width / 2);

        // eye drop preview
        this.renderEyeDropper(context, cx, cy, currentArgb);

        RenderUtils.fillCircle(context, curX + cx, curY + cy, 4, 0xFF000000);
        RenderUtils.fillCircle(context, curX + cx, curY + cy, 3, 0xFFFFFFFF);

        RenderUtils.drawText(context, "Brightness",   x + width + 5, y + 4, 0.6F, false);
        RenderUtils.drawText(context, "Transparency", x + width + 5, y + 24, 0.6F, false);
    }

    public void renderEyeDropper(GuiGraphicsExtractor context, int cx, int cy, int previewColor) {
        double anim = hoverAnimator.getAnimation();
        int eyedropHeight = (int) (45 * anim);
        int eyedropRadius = (int) (15 * anim);
        if (curY < 0) {
            context.pose().pushMatrix();
            context.pose().scaleAround(1, -1, curX + cx, curY + cy);
        }
        RenderUtils.fillEyeDrop(context, curX + cx, curY + cy, eyedropHeight, eyedropRadius, 0x80000000);
        RenderUtils.fillCircle(context, curX + cx, curY + cy - eyedropHeight + eyedropRadius, (int) (12 * anim), previewColor);
        if (curY < 0) {
            context.pose().popMatrix();
        }
    }

    public int getArgb() {
        int hsb = java.awt.Color.HSBtoRGB(getHsbH(), getHsbS(), getHsbB());
        int alpha = (int) (0xFF * getHsbA());
        return alpha << 24 | (hsb & 0xFFFFFF);
    }

    public void setArgb(int argb) {
        int a = argb >> 24 & 0xFF;
        int r = argb >> 16 & 0xFF;
        int g = argb >> 8 & 0xFF;
        int b = argb & 0xFF;
        float[] hsb = new float[3];

        java.awt.Color.RGBtoHSB(r, g, b, hsb);
        setHsbA(a / 255F);
        setHsbB(hsb[2]);

        float theta = hsb[0] * Mth.TWO_PI;
        float rad = hsb[1] * width / 2F;
        curX = (int) (rad * Mth.cos(theta));
        curY = (int) (rad * Mth.sin(theta));
    }

    public float getHsbH() {
        float dx = curX;
        float dy = curY;
        float theta = (float) Mth.atan2(dy, dx);
        return theta / Mth.TWO_PI;
    }

    public float getHsbS() {
        float r = width / 2F;
        float dx = curX;
        float dy = curY;
        float len = Mth.sqrt(dx * dx + dy * dy);
        return len / r;
    }

    public void setHsbH(float h) {
        float dx = curX;
        float dy = curY;
        float len = Mth.sqrt(dx * dx + dy * dy);
        float theta = Mth.TWO_PI * h;

        curX = (int) (len * Mth.cos(theta));
        curY = (int) (len * Mth.sin(theta));
    }

    public void setHsbS(float s) {
        float r = width / 2F;
        float dx = curX;
        float dy = curY;
        float len = Mth.sqrt(dx * dx + dy * dy);

        float scale = (r * s) / len;
        curX = (int) (dx * scale);
        curY = (int) (dy * scale);
    }

    public float getHsbB() {
        return sliderBrightness.getValue();
    }

    public void setHsbB(float b) {
        sliderBrightness.setValue(b);
    }

    public float getHsbA() {
        return sliderAlpha.getValue();
    }

    public void setHsbA(float a) {
        sliderAlpha.setValue(a);
    }

    private void clampCursor() {
        float r = width / 2F;
        float dx = curX;
        float dy = curY;
        float lenSq = dx * dx + dy * dy;
        if (lenSq <= r * r)
            return; // within bounds

        float len = Mth.sqrt(lenSq);
        float scale = r / len;
        curX = (int) (dx * scale);
        curY = (int) (dy * scale);
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (mc.gui.screen() instanceof GuiScreen screen && isHovered((int)mouseX, (int)mouseY)) {
            screen.selected = this;
        }
        super.mouseClicked(mouseX, mouseY, button);
    }

    public static class Slider extends GuiElement {

        private final Animator animator = new Animator(500, Animations.FADE_IN_AND_OUT);
        private int colorStart, colorEnd;
        private int fillEnd;

        public Slider(int x, int y, int w, int h, int colorStart, int colorEnd) {
            super(x, y, w, h);
            this.colorStart = colorStart;
            this.colorEnd = colorEnd;
            this.fillEnd = x + w;
        }

        @Override
        public void onRender(GuiGraphicsExtractor context, int mouseX, int mouseY) {
            // pre render tasks
            if (mc.gui.screen() instanceof GuiScreen screen && screen.selected == this) {
                fillEnd = mouseX;
                clampCursor();
            }
            // end pre render tasks

            int fillWidth = (int)((fillEnd - x) * animator.getAnimation());
            int colorFillEnd = Color.lerp(colorStart, colorEnd, fillWidth / (float) width);
            int colorFillCenter = Color.lerp(colorStart, colorFillEnd, 0.5F);

            RenderUtils.fillRoundRect(context, x, y, width, height, height / 2, 0xFF000000);
            RenderUtils.fillRoundRectGradient(context, x, y, fillWidth, height, height / 2, colorStart, colorFillEnd, colorFillEnd, colorStart, colorFillCenter);
        }

        public float getValue() {
            return (float) (fillEnd - x) / width;
        }

        public void setValue(float value) {
            fillEnd = (int) (x + value * width);
        }

        private void clampCursor() {
            fillEnd = MathUtils.clamp(fillEnd, x, x + width);
        }

        @Override
        public void mouseClicked(double mouseX, double mouseY, int button) {
            if (mc.gui.screen() instanceof GuiScreen screen && isHovered((int)mouseX, (int)mouseY)) {
                screen.selected = this;
            }
            super.mouseClicked(mouseX, mouseY, button);
        }
    }
}
