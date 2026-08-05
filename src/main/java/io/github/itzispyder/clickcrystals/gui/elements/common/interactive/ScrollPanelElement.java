package io.github.itzispyder.clickcrystals.gui.elements.common.interactive;

import io.github.itzispyder.clickcrystals.gui.GuiElement;
import io.github.itzispyder.clickcrystals.gui.GuiScreen;
import io.github.itzispyder.clickcrystals.gui.misc.Color;
import io.github.itzispyder.clickcrystals.gui.misc.Shades;
import io.github.itzispyder.clickcrystals.gui.misc.animators.Animations;
import io.github.itzispyder.clickcrystals.gui.misc.animators.Animator;
import io.github.itzispyder.clickcrystals.modules.Module;
import io.github.itzispyder.clickcrystals.modules.modules.clickcrystals.GuiBorders;
import io.github.itzispyder.clickcrystals.util.MathUtils;
import io.github.itzispyder.clickcrystals.util.minecraft.render.RenderUtils;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class ScrollPanelElement extends GuiElement {

    private final GuiScreen parentScreen;
    public static final int SCROLL_MULTIPLIER = 15;
    public static final int SCROLLBAR_WIDTH = 6;
    private static final int THUMB_WIDTH = 4;
    private static final int MIN_THUMB_LENGTH = 14;
    private int remainingUp, remainingDown, limitTop, limitBottom, scrollbarY, scrollbarHeight, prevDrag;
    private boolean scrolling;
    private boolean verticalStack;
    private int stackGap;

    private final Animator interpolation;
    private int interpolationLength;
    private final Animator thumbHighlight;
    private boolean thumbHot;

    public ScrollPanelElement(GuiScreen parentScreen, int x, int y, int width, int height, int gap) {
        this(parentScreen, x, y, width, height);
        this.verticalStack(gap);
    }

    public ScrollPanelElement(GuiScreen parentScreen, int x, int y, int width, int height) {
        super(x, y, width, height);
        super.setContainer(true);
        this.parentScreen = parentScreen;
        this.interpolation = new Animator(140, Animations.FADE_IN_AND_OUT);
        this.thumbHighlight = new Animator(120, Animations.FADE_IN_AND_OUT_SLIGHT);

        remainingUp = remainingDown = 0;
        limitTop = y;
        limitBottom = y + height;
        scrolling = false;

        // callbacks
        parentScreen.mouseClickListeners.add((mouseX, mouseY, button, click) -> {
            if (isHovered((int)mouseX, (int)mouseY) && click.isDown()) {
                scrolling = true;
                prevDrag = (int)mouseY;
            }
            else if (scrolling && click.isRelease()) {
                scrolling = false;
            }
        });
    }

    public boolean canScrollDown() {
        return remainingDown > 0;
    }

    public boolean canScrollUp() {
        return remainingUp > 0;
    }

    public boolean canScroll() {
        return canScrollUp() || canScrollDown();
    }

    public GuiScreen getParentScreen() {
        return parentScreen;
    }

    public boolean canScrollInDirection(int amount) {
        if (amount >= 0) {
            return canScrollUp();
        }
        else {
            return canScrollDown();
        }
    }

    @Override
    public void addChild(GuiElement child) {
        super.addChild(child);
        updateBounds(child);
        child.scrollOnPanel(this, 0);
    }

    public void updateBounds(GuiElement child) {
        if (child.y < limitTop) {
            limitTop = child.y;
        }
        if (child.y + child.height > limitBottom) {
            limitBottom = child.y + child.height;
        }
        remainingUp = y - limitTop;
        remainingDown = limitBottom - (y + height);
    }

    public void recalculatePositions() {
        remainingUp = remainingDown = 0;
        limitTop = y;
        limitBottom = y + height;

        for (GuiElement child : getChildren()) {
            updateBounds(child);
        }
    }

    // Stack children vertically by their layout height, so collapsed/animating elements close the gap.
    private void verticalStack(int gap) {
        this.verticalStack = true;
        this.stackGap = gap;
    }

    // Anchors on the first child (which already tracks the scroll offset) and re-flows the rest below it.
    private void restack() {
        if (!verticalStack || getChildren().isEmpty())
            return;

        GuiElement first = getChildren().get(0);
        int caret = first.y + first.getLayoutHeight() + stackGap;
        for (int i = 1; i < getChildren().size(); i++) {
            GuiElement child = getChildren().get(i);
            if (child.y != caret)
                child.move(0, caret - child.y);
            caret += child.getLayoutHeight() + stackGap;
        }
        recalculatePositions();
    }

    @Override
    public void onRender(GuiGraphicsExtractor context, int mouseX, int mouseY) {

    }

    @Override
    public void render(GuiGraphicsExtractor context, int mouseX, int mouseY) {
        restack();
        boolean bl = canRender();

        float interpolatedDelta = (float)(interpolationLength * interpolation.getAnimationReversed());
        context.enableScissor(x, y, x + width, y + height);
        context.pose().pushMatrix();
        context.pose().translate(0, -interpolatedDelta);

        if (bl)
            onRender(context, mouseX, mouseY);
        for (GuiElement child : this.getChildren())
            if (child.y + child.height > this.y || child.y < this.y + this.height)
                child.render(context, mouseX, mouseY);

        context.pose().popMatrix();
        context.disableScissor();

        if (Module.isEnabled(GuiBorders.class))
            RenderUtils.drawRect(context, x, y, width, height, 0xFFFFFFFF);
        if (bl)
            postRender(context, mouseX, mouseY);
    }

    @Override
    public void postRender(GuiGraphicsExtractor context, int mouseX, int mouseY) {
        if (!canScroll() || !canRender())
            return;

        double fullDoc = remainingUp + remainingDown + this.height;

        if (scrolling && mouseY != prevDrag) {
            double deltaY = mouseY - prevDrag;
            double multiplier = Math.abs(deltaY) / this.height * fullDoc;

            scrollWithMultiplier(deltaY > 0 ? -1 : 1, (int)multiplier);
            prevDrag = mouseY;
        }

        // a thumb the size of what is on screen, but never so short it cannot be grabbed
        int drawLength = Math.max(MIN_THUMB_LENGTH, (int)(this.height * (this.height / fullDoc)));
        int drawStart = (int)((this.height - drawLength) * (remainingUp / (double)(remainingUp + remainingDown)));

        scrollbarY = this.y + drawStart;
        scrollbarHeight = drawLength;

        boolean hot = scrolling || isHovered(mouseX, mouseY);
        if (hot != thumbHot) {
            thumbHot = hot;
            thumbHighlight.reset();
        }
        double highlight = hot ? thumbHighlight.getAnimation() : thumbHighlight.getAnimationReversed();

        int trackX = x + width - SCROLLBAR_WIDTH + (SCROLLBAR_WIDTH - THUMB_WIDTH) / 2;
        RenderUtils.fillRoundVertLine(context, trackX + 1, y, height, THUMB_WIDTH - 2, Shades.TRANS_DARK_GRAY);
        RenderUtils.fillRoundVertLine(context, trackX, scrollbarY, drawLength, THUMB_WIDTH, Color.blend(Shades.GENERIC_LOW, Shades.GENERIC, highlight));
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        for (int i = getChildren().size() - 1; i >= 0; i--) {
            if (MathUtils.oob(i, 0, getChildren().size() - 1))
                break;
            GuiElement child = getChildren().get(i);
            child.mouseClicked(mouseX, mouseY, button);
        }
    }

    /**
     * Redefinition of mouse hover to hovering over the scroll bar, not the entire element.
     * @param mouseX mouse x
     * @param mouseY mouse y
     * @return is mouse hovered
     */
    @Override
    public boolean isHovered(int mouseX, int mouseY) {
        return super.isHovered(mouseX, mouseY) && mouseX > (x + width - SCROLLBAR_WIDTH) && mouseY > scrollbarY && mouseY < scrollbarY + scrollbarHeight;
    }

    public void onScroll(double amount) {
        interpolationLength = scrollWithMultiplier(amount, SCROLL_MULTIPLIER);
        interpolation.reset();
    }

    private int scrollWithMultiplier(double amount, int multiplier) {
        int total = 0;
        for (int i = 0; i < multiplier; i++) {
            total += scrollWithoutMultiplier(amount);
        }
        return total;
    }

    private int scrollWithoutMultiplier(double amount) {
        int a = (int)amount;
        if (!canScrollInDirection(a))
            return 0;

        for (GuiElement child : getChildren())
            child.scrollOnPanel(this, a);

        remainingDown += a;
        remainingUp -= a;
        return a;
    }
}
