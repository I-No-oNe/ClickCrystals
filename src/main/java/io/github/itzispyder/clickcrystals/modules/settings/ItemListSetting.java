package io.github.itzispyder.clickcrystals.modules.settings;

import io.github.itzispyder.clickcrystals.gui.elements.browsingmode.module.ItemListSettingElement;
import io.github.itzispyder.clickcrystals.util.StringUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Arrays;
import java.util.function.BooleanSupplier;
import java.util.List;
import java.util.Set;

/**
 * A list of items. Entries are item ids, so a list means the exact items it names and
 * nothing else. Typed text is resolved to the closest item before it is added, so
 * "dia sword" lands on minecraft:diamond_sword.
 */
public class ItemListSetting extends AbstractListSetting {

    // parsing every entry is not free and lists are read every tick, so keep the last result
    private String parsedFrom;
    private List<Item> parsed = List.of();
    private BooleanSupplier literal = () -> false;

    public ItemListSetting(String name, String description, String def, String val) {
        super(name, description, def, val);
    }

    @Override
    public ItemListSettingElement toGuiElement(int x, int y) {
        return new ItemListSettingElement(this, x, y);
    }

    /**
     * Whether entries are kept as typed. Modules that read the list as loose words rather
     * than item names turn this on so the GUI stops resolving what you type to an item.
     */
    public boolean isLiteral() {
        return literal.getAsBoolean();
    }

    /**
     * The items this list names, skipping entries that name nothing.
     */
    public List<Item> getItems() {
        String val = getVal();
        if (!val.equals(parsedFrom)) {
            parsedFrom = val;
            parsed = getRawItems().stream().map(ItemListSetting::parse).filter(item -> item != Items.AIR).toList();
        }
        return parsed;
    }

    /**
     * The entries as they are written, including any that name no item.
     */
    public List<String> getRawItems() {
        return getEntries();
    }

    public boolean matches(ItemStack stack) {
        return stack != null && !stack.isEmpty() && matches(stack.getItem());
    }

    public boolean matches(Item item) {
        return item != null && item != Items.AIR && getItems().contains(item);
    }

    public void addItem(Item item) {
        addEntry(idOf(item));
    }

    public void removeItem(Item item) {
        removeEntry(idOf(item));
    }

    /**
     * The item an entry names, or {@link Items#AIR} when it names nothing.
     */
    public static Item parse(String entry) {
        Identifier id = Identifier.tryParse(entry.trim().toLowerCase());
        return id == null ? Items.AIR : BuiltInRegistries.ITEM.getValue(id);
    }

    public static String idOf(Item item) {
        Identifier id = BuiltInRegistries.ITEM.getKey(item);
        return id.getNamespace().equals(Identifier.DEFAULT_NAMESPACE) ? id.getPath() : id.toString();
    }

    /**
     * Best item for whatever the user typed, or {@link Items#AIR} if nothing comes close.
     */
    public static Item closest(String input) {
        String query = input.trim().toLowerCase().replace(' ', '_');
        if (query.isEmpty()) {
            return Items.AIR;
        }

        Item exact = parse(query);
        if (exact != Items.AIR) {
            return exact;
        }

        Set<Identifier> ids = BuiltInRegistries.ITEM.keySet();
        String path = StringUtils.closest(ids.stream().map(Identifier::getPath).toList(), query);
        return ids.stream()
                .filter(id -> id.getPath().equals(path))
                .findFirst()
                .map(BuiltInRegistries.ITEM::getValue)
                .orElse(Items.AIR);
    }

    public static Builder create() {
        return new Builder();
    }

    public static class Builder extends SettingBuilder<String, Builder, ItemListSetting> {

        private BooleanSupplier literal = () -> false;

        public Builder literalWhen(BooleanSupplier literal) {
            this.literal = literal;
            return this;
        }

        public Builder def(Item... items) {
            return def(String.join(SEPARATOR, Arrays.stream(items).map(ItemListSetting::idOf).toList()));
        }

        public Builder def(String... entries) {
            return def(String.join(SEPARATOR, entries));
        }

        @Override
        public ItemListSetting buildSetting() {
            ItemListSetting setting = new ItemListSetting(name, description, def, getOrDef(val, def));
            setting.literal = literal;
            return setting;
        }
    }
}
