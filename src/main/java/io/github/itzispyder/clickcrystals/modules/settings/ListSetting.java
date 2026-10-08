package io.github.itzispyder.clickcrystals.modules.settings;

import io.github.itzispyder.clickcrystals.gui.elements.browsingmode.module.ListSettingElement;

/**
 * A list of plain text entries, edited in place from the module GUI.
 */
public class ListSetting extends AbstractListSetting {

    public ListSetting(String name, String description, String def, String val) {
        super(name, description, def, val);
    }

    @Override
    public ListSettingElement toGuiElement(int x, int y) {
        return new ListSettingElement(this, x, y);
    }

    public static Builder create() {
        return new Builder();
    }

    public static class Builder extends SettingBuilder<String, Builder, ListSetting> {

        public Builder def(String... entries) {
            return def(String.join(SEPARATOR, entries));
        }

        @Override
        public ListSetting buildSetting() {
            return new ListSetting(name, description, def, getOrDef(val, def));
        }
    }
}
