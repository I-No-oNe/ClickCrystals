package io.github.itzispyder.clickcrystals.modules.settings;

import io.github.itzispyder.clickcrystals.modules.ModuleSetting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Base of the settings that hold an ordered list of text entries.
 * <p>
 * The value is kept as a comma separated string so it saves through the regular string
 * path in {@link io.github.itzispyder.clickcrystals.modules.ModuleFile} and stays readable
 * for anyone editing the config by hand.
 */
public abstract class AbstractListSetting extends ModuleSetting<String> {

    public static final String SEPARATOR = ",";

    public AbstractListSetting(String name, String description, String def, String val) {
        super(name, description, def, val);
    }

    public List<String> getEntries() {
        return split(getVal());
    }

    public List<String> getDefEntries() {
        return split(getDef());
    }

    public void setEntries(List<String> entries) {
        setVal(String.join(SEPARATOR, entries));
    }

    public void addEntry(String entry) {
        entry = entry.trim();
        if (entry.isEmpty() || contains(entry)) {
            return;
        }
        List<String> entries = getEntries();
        entries.add(entry);
        setEntries(entries);
    }

    public void removeEntry(String entry) {
        List<String> entries = getEntries();
        if (entries.removeIf(e -> e.equalsIgnoreCase(entry))) {
            setEntries(entries);
        }
    }

    public boolean contains(String entry) {
        return getEntries().stream().anyMatch(e -> e.equalsIgnoreCase(entry.trim()));
    }

    public boolean isEmpty() {
        return getEntries().isEmpty();
    }

    public static List<String> split(String val) {
        if (val == null || val.isBlank()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(Arrays.stream(val.split(SEPARATOR))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList());
    }
}
