package io.github.itzispyder.clickcrystals.gui.elements.browsingmode.module;

import io.github.itzispyder.clickcrystals.modules.settings.ItemListSetting;
import io.github.itzispyder.clickcrystals.util.minecraft.render.RenderUtils;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public class ItemListSettingElement extends ListSettingElement {

    private final ItemListSetting setting;

    public ItemListSettingElement(ItemListSetting setting, int x, int y) {
        super(setting, x, y);
        this.setting = setting;
    }

    // shows what the row will end up being while it is still half typed
    @Override
    protected int renderRowIcon(GuiGraphicsExtractor context, String text, int iconX, int rowY) {
        if (text.isBlank()) {
            return 0;
        }
        Item item = ItemListSetting.closest(text);
        boolean drawn = RenderUtils.drawItemSafely(context, item, iconX, rowY + (ROW_HEIGHT - ICON_SIZE) / 2, ICON_SIZE);
        return drawn ? ICON_SIZE + 3 : 0;
    }

    @Override
    protected String placeholder() {
        return "add an item...";
    }

    // typed text names an item loosely, "dia sword" is meant to be a diamond sword
    @Override
    protected String normalize(String text) {
        if (setting.isLiteral()) {
            return text.trim().toLowerCase().replace(' ', '_');
        }
        Item item = ItemListSetting.closest(text);
        return item == Items.AIR ? "" : ItemListSetting.idOf(item);
    }
}
