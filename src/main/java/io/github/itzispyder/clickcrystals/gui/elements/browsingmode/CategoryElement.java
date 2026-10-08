package io.github.itzispyder.clickcrystals.gui.elements.browsingmode;

import io.github.itzispyder.clickcrystals.events.listeners.UserInputListener;
import io.github.itzispyder.clickcrystals.gui.GuiElement;
import io.github.itzispyder.clickcrystals.gui.misc.Color;
import io.github.itzispyder.clickcrystals.gui.misc.Shades;
import io.github.itzispyder.clickcrystals.gui.misc.animators.Hover;
import io.github.itzispyder.clickcrystals.gui.screens.modulescreen.BrowsingScreen;
import io.github.itzispyder.clickcrystals.modules.Category;
import io.github.itzispyder.clickcrystals.util.minecraft.render.RenderUtils;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class CategoryElement extends GuiElement {

    private final Category category;
    private final Hover hover = new Hover(140);

    public CategoryElement(Category category, int x, int y) {
        super(x, y, 90, 10);
        super.setTooltip("Filter by category: " + category.name());
        this.category = category;
    }

    @Override
    public void onRender(GuiGraphicsExtractor context, int mouseX, int mouseY) {
        boolean selected = BrowsingScreen.currentCategory == category && mc.gui.screen() instanceof BrowsingScreen;
        // pills fade between plain, hovered and selected instead of popping
        double glow = hover.update(selected ? 1.0 : isHovered(mouseX, mouseY) ? 0.5 : 0.0);

        if (glow > 0.01) {
            RenderUtils.fillRoundHoriLine(context, x, y, width, height, Color.blend(0x00888888, Shades.GENERIC_LOW, glow));
        }
        if (selected) {
            RenderUtils.fillRoundShadow(context, x, y, width, height, height / 2, 3, 0x8000B7FF, 0x0000B7FF);
        }

        int slide = (int)Math.round(glow * 2);
        RenderUtils.drawTexture(context, category.texture(), 10 + x + 1 + slide, y + 1, 8, 8);
        RenderUtils.drawText(context, category.name(), 15 + x + height - 2 + slide, y + height / 3, 0.65F, false);
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (isHovered((int)mouseX, (int)mouseY)) {
            BrowsingScreen.currentCategory = category;
            UserInputListener.openModulesScreen();
        }
        super.mouseClicked(mouseX, mouseY, button);
    }

    public Category getCategory() {
        return category;
    }
}
