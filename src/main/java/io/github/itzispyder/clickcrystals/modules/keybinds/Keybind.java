package io.github.itzispyder.clickcrystals.modules.keybinds;

import io.github.itzispyder.clickcrystals.Global;
import io.github.itzispyder.clickcrystals.util.StringUtils;
import io.github.itzispyder.clickcrystals.util.misc.ManualMap;
import com.mojang.blaze3d.platform.InputConstants;

import java.util.HashMap;
import java.util.Map;

public class Keybind implements Global {

    public static final int DEFAULT_SCANCODE = 42;
    public static final int NONE = -1;
    public static final Map<Integer, String> EXTRAS = ManualMap.fromItems(
            InputConstants.KEY_LSHIFT, "LS",
            InputConstants.KEY_RSHIFT, "RS",
            InputConstants.KEY_LALT, "LA",
            InputConstants.KEY_RALT, "RA",
            InputConstants.KEY_LCONTROL, "LC",
            InputConstants.KEY_RCONTROL, "RC",
            InputConstants.KEY_PAGEDOWN, "P⇧",
            InputConstants.KEY_PAGEUP, "P⇩",
            InputConstants.KEY_UP, "⇧",
            InputConstants.KEY_DOWN, "⇩",
            InputConstants.KEY_LEFT, "⇦",
            InputConstants.KEY_RIGHT, "⇨"
    );
    public static final Map<Integer, String> EXTENDED_NAMES = ManualMap.fromItems(
            InputConstants.KEY_LSHIFT, "left_shift",
            InputConstants.KEY_RSHIFT, "right_shift",
            InputConstants.KEY_LALT, "left_alt",
            InputConstants.KEY_RALT, "right_alt",
            InputConstants.KEY_LCONTROL, "left_control",
            InputConstants.KEY_RCONTROL, "right_control",
            InputConstants.KEY_PAGEDOWN, "page_down",
            InputConstants.KEY_PAGEUP, "page_up",
            InputConstants.KEY_UP, "up_arrow",
            InputConstants.KEY_DOWN, "down_arrow",
            InputConstants.KEY_LEFT, "left_arrow",
            InputConstants.KEY_RIGHT, "right_arrow",
            InputConstants.KEY_ESCAPE, "escape",
            InputConstants.KEY_BACKSPACE, "backspace",
            InputConstants.KEY_INSERT, "insert",
            InputConstants.KEY_DELETE, "delete",
            InputConstants.KEY_HOME, "home",
            InputConstants.KEY_END, "end",
            InputConstants.KEY_TAB, "tab",
            InputConstants.KEY_CAPSLOCK, "capslock",
            InputConstants.KEY_SPACE, "space",
            InputConstants.KEY_RETURN, "enter"
    );
    private static final Map<Character, Integer> CHAR_KEYS = new HashMap<>();

    static {
        String letters = "abcdefghijklmnopqrstuvwxyz";
        for (int i = 0; i < letters.length(); i++)
            CHAR_KEYS.put(letters.charAt(i), InputConstants.KEY_A + i);

        String digits = "1234567890";
        for (int i = 0; i < digits.length(); i++)
            CHAR_KEYS.put(digits.charAt(i), InputConstants.KEY_1 + i);

        CHAR_KEYS.put('-', InputConstants.KEY_MINUS);
        CHAR_KEYS.put('=', InputConstants.KEY_EQUALS);
        CHAR_KEYS.put('[', InputConstants.KEY_LBRACKET);
        CHAR_KEYS.put(']', InputConstants.KEY_RBRACKET);
        CHAR_KEYS.put('\\', InputConstants.KEY_BACKSLASH);
        CHAR_KEYS.put(';', InputConstants.KEY_SEMICOLON);
        CHAR_KEYS.put('\'', InputConstants.KEY_APOSTROPHE);
        CHAR_KEYS.put('`', InputConstants.KEY_GRAVE);
        CHAR_KEYS.put(',', InputConstants.KEY_COMMA);
        CHAR_KEYS.put('.', InputConstants.KEY_PERIOD);
        CHAR_KEYS.put('/', InputConstants.KEY_SLASH);
    }

    private static Character charOfKey(int key) {
        for (var entry : CHAR_KEYS.entrySet())
            if (entry.getValue() == key)
                return entry.getKey();
        return null;
    }

    private final String name, id;
    private int key, defaultKey;
    private KeyAction keyAction, changeAction;
    private BindCondition bindCondition;

    public Keybind(String id, int defaultKey, int key, KeyAction keyAction, KeyAction changeAction, BindCondition bindCondition) {
        this.id = id;
        this.name = StringUtils.capitalizeWords(id);
        this.key = key;
        this.defaultKey = defaultKey;
        this.keyAction = keyAction;
        this.changeAction = changeAction;
        this.bindCondition = bindCondition;
        system.addKeybind(this);
    }

    public void onPress() {
        if (bindCondition.meets(this, mc.gui.screen())) {
            keyAction.onKey(this);
        }
    }

    public boolean canPress(int keyCode, int scanCode) {
        boolean notNull = InputConstants.Type.KEYBOARD.getOrCreate(key) != InputConstants.UNKNOWN;
        boolean isExtra = EXTRAS.containsKey(key);
        boolean isKey = keyCode == key;
        return (notNull || isExtra) && isKey;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public int getKey() {
        return key;
    }

    public String getKeyName() {
        Character ch = charOfKey(key);
        if (ch != null)
            return String.valueOf(ch);

        return EXTRAS.getOrDefault(key, InputConstants.Type.KEYBOARD.getOrCreate(key).getDisplayName().getString());
    }

    public static String getExtendedKeyName(int key, int scancode) {
        Character ch = charOfKey(key);
        if (ch != null)
            return String.valueOf(ch);

        return EXTENDED_NAMES.getOrDefault(key, InputConstants.Type.KEYBOARD.getOrCreate(key).getDisplayName().getString());
    }

    public static int fromExtendedKeyName(String name) {
        for (var entry : EXTENDED_NAMES.entrySet())
            if (entry.getValue().equalsIgnoreCase(name))
                return entry.getKey();

        if (name.length() == 1) {
            Integer key = CHAR_KEYS.get(Character.toLowerCase(name.charAt(0)));
            return key != null ? key : NONE;
        }
        return NONE;
    }

    public void setKey(int key) {
        this.key = key;
        this.changeAction.onKey(this);
    }

    public int getDefaultKey() {
        return defaultKey;
    }

    public void setDefaultKey(int defaultKey) {
        this.defaultKey = defaultKey;
    }

    public KeyAction getKeyAction() {
        return keyAction;
    }

    public void setKeyAction(KeyAction keyAction) {
        this.keyAction = keyAction;
    }

    public BindCondition getBindCondition() {
        return bindCondition;
    }

    public void setBindCondition(BindCondition bindCondition) {
        this.bindCondition = bindCondition;
    }

    public KeyAction getChangeAction() {
        return changeAction;
    }

    public void setChangeAction(KeyAction changeAction) {
        this.changeAction = changeAction;
    }

    public static Builder create() {
        return new Builder();
    }

    public static class Builder {

        private String id;
        private int key, defaultKey;
        private KeyAction keyAction, changeAction;
        private BindCondition bindCondition;

        public Builder() {
            id = "unregistered-keybind";
            key = defaultKey = NONE;
            keyAction = changeAction = bind -> {};
            bindCondition = (bind, screen) -> true;
        }

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder key(int key) {
            this.key = key;
            return this;
        }

        public Builder defaultKey(int defaultKey) {
            this.defaultKey = defaultKey;
            return this;
        }

        public Builder onPress(KeyAction keyAction) {
            this.keyAction = keyAction;
            return this;
        }

        public Builder onChange(KeyAction changeAction) {
            this.changeAction = changeAction;
            return this;
        }

        public Builder condition(BindCondition bindCondition) {
            this.bindCondition = bindCondition;
            return this;
        }

        public Keybind build() {
            key = key == NONE ? defaultKey : key;
            return new Keybind(id, defaultKey, key, keyAction, changeAction, bindCondition);
        }
    }
}
