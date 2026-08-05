package io.github.itzispyder.clickcrystals.modules.settings;

import io.github.itzispyder.clickcrystals.gui.elements.browsingmode.module.ItemListSettingElement;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.HashMap;
import java.util.Map;

/**
 * A list whose entries name items, matched the same loose way the rest of the client
 * matches them: an entry hits when it appears anywhere in the item's translation key.
 */
public class ItemListSetting extends AbstractListSetting {

    private static final Map<String, ItemStack> ICONS = new HashMap<>();

    public ItemListSetting(String name, String description, String def, String val) {
        super(name, description, def, val);
    }

    @Override
    public ItemListSettingElement toGuiElement(int x, int y) {
        return new ItemListSettingElement(this, x, y);
    }

    public boolean matches(ItemStack item) {
        return item != null && !item.isEmpty() && matches(item.getItem());
    }

    public boolean matches(Item item) {
        if (item == null) {
            return false;
        }
        String id = item.getDescriptionId().toLowerCase();
        return getEntries().stream().anyMatch(entry -> id.contains(entry.toLowerCase()));
    }

    /**
     * Best guess icon for an entry, so the list can show what it is pointing at.
     * Exact ids win, otherwise the first item whose id contains the entry.
     */
    public static ItemStack getIcon(String entry) {
        return ICONS.computeIfAbsent(entry.toLowerCase(), key -> {
            Item exact = BuiltInRegistries.ITEM.getValue(Identifier.withDefaultNamespace(key));
            if (exact != Items.AIR) {
                return exact.getDefaultInstance();
            }
            return BuiltInRegistries.ITEM.stream()
                    .filter(item -> item != Items.AIR && item.getDescriptionId().toLowerCase().contains(key))
                    .findFirst()
                    .map(Item::getDefaultInstance)
                    .orElse(ItemStack.EMPTY);
        });
    }

    public static Builder create() {
        return new Builder();
    }

    public static class Builder extends SettingBuilder<String, Builder, ItemListSetting> {

        public Builder def(String... entries) {
            return def(String.join(SEPARATOR, entries));
        }

        @Override
        public ItemListSetting buildSetting() {
            return new ItemListSetting(name, description, def, getOrDef(val, def));
        }
    }
}
