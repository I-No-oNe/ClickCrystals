package io.github.itzispyder.clickcrystals.gui.elements.browsingmode.module;

import io.github.itzispyder.clickcrystals.gui.misc.Shades;
import io.github.itzispyder.clickcrystals.modules.settings.AbstractListSetting;
import io.github.itzispyder.clickcrystals.modules.settings.ItemListSetting;
import io.github.itzispyder.clickcrystals.util.minecraft.render.RenderUtils;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public class ItemListSettingElement extends ListSettingElement {

    private static final int ICON_SIZE = 10;

    private final ItemListSetting setting;

    public ItemListSettingElement(ItemListSetting setting, int x, int y) {
        super(setting, x, y);
        this.setting = setting;
    }

    @Override
    protected void renderEntry(GuiGraphicsExtractor context, String entry, int rowY, int mouseX, int mouseY) {
        RenderUtils.fillRoundHoriLine(context, rowX(), rowY, rowWidth(), ROW_HEIGHT, Shades.GRAY);

        Item item = ItemListSetting.parse(entry);
        if (item != Items.AIR) {
            RenderUtils.drawItem(context, item.getDefaultInstance(), textX(), rowY + (ROW_HEIGHT - ICON_SIZE) / 2, ICON_SIZE);
        }
        RenderUtils.drawText(context, entry, textX() + ICON_SIZE + 3, rowY + ROW_HEIGHT / 3, 0.7F, false);
        renderRemoveButton(context, rowY, mouseX, mouseY);
    }

    // typed text names an item loosely, "dia sword" is meant to be a diamond sword
    @Override
    protected void commit() {
        for (String entry : AbstractListSetting.split(getInput())) {
            Item item = ItemListSetting.closest(entry);
            if (item != Items.AIR) {
                setting.addItem(item);
            }
        }
        clearInput();
    }
}
