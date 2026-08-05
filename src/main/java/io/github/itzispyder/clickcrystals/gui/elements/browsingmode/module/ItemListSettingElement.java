package io.github.itzispyder.clickcrystals.gui.elements.browsingmode.module;

import io.github.itzispyder.clickcrystals.gui.misc.Shades;
import io.github.itzispyder.clickcrystals.modules.settings.ItemListSetting;
import io.github.itzispyder.clickcrystals.util.minecraft.render.RenderUtils;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;

public class ItemListSettingElement extends ListSettingElement {

    private static final int ICON_SIZE = 10;

    public ItemListSettingElement(ItemListSetting setting, int x, int y) {
        super(setting, x, y);
    }

    @Override
    protected void renderEntry(GuiGraphicsExtractor context, String entry, int rowY, int mouseX, int mouseY) {
        boolean hoveringRemove = isOverRemove(mouseX, mouseY, rowY);
        RenderUtils.fillRoundHoriLine(context, rowX(), rowY, rowWidth(), ROW_HEIGHT, Shades.GRAY);

        ItemStack icon = ItemListSetting.getIcon(entry);
        if (!icon.isEmpty()) {
            RenderUtils.drawItem(context, icon, textX(), rowY + (ROW_HEIGHT - ICON_SIZE) / 2, ICON_SIZE);
        }
        RenderUtils.drawText(context, entry, textX() + ICON_SIZE + 3, rowY + ROW_HEIGHT / 3, 0.7F, false);
        RenderUtils.drawText(context, hoveringRemove ? "§cx" : "§7x", rowX() + rowWidth() - REMOVE_WIDTH + 4, rowY + ROW_HEIGHT / 3, 0.7F, false);
    }
}
