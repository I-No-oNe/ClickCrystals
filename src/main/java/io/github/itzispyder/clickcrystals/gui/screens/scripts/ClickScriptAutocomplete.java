package io.github.itzispyder.clickcrystals.gui.screens.scripts;

import io.github.itzispyder.clickcrystals.Global;
import io.github.itzispyder.clickcrystals.scripting.ClickScript;
import io.github.itzispyder.clickcrystals.scripting.components.Conditionals;
import io.github.itzispyder.clickcrystals.scripting.syntax.InputType;
import io.github.itzispyder.clickcrystals.scripting.syntax.TargetType;
import io.github.itzispyder.clickcrystals.scripting.syntax.client.ConfigCmd;
import io.github.itzispyder.clickcrystals.scripting.syntax.client.DefineCmd;
import io.github.itzispyder.clickcrystals.scripting.syntax.client.ModuleCmd;
import io.github.itzispyder.clickcrystals.scripting.syntax.logic.OnEventCmd;
import io.github.itzispyder.clickcrystals.util.minecraft.render.RenderUtils;
import io.github.itzispyder.clickcrystals.util.misc.Dimensions;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ClickScriptAutocomplete implements Global {

    private static final int MAX_SUGGESTIONS = 8;
    private static final int ROW_H           = 9;
    private static final int PADDING         = 3;
    private static final int POPUP_W         = 120;
    private static final int COLOR_BG        = 0xF0141420;
    private static final int COLOR_SELECTED  = 0xFF2244AA;
    private static final int COLOR_BORDER    = 0xFF3355CC;
    private static final int COLOR_TEXT      = 0xFFAAAAAA;
    private static final int COLOR_HIGHLIGHT = 0xFFFFFFFF;

    private static final List<String> COMMANDS;
    private static final List<String> EVENT_TYPES;
    private static final List<String> CONDITIONALS;
    private static final List<String> MODULE_ACTIONS;
    private static final List<String> CONFIG_TYPES;
    private static final List<String> DEFINE_TYPES;
    private static final List<String> INPUT_TYPES;
    private static final List<String> TARGET_TYPES;
    private static final List<String> DIMENSIONS;

    static {
        COMMANDS       = sorted(Arrays.asList(ClickScript.collectNames()));
        EVENT_TYPES    = enumValues(OnEventCmd.EventType.class);
        CONDITIONALS   = sorted(new ArrayList<>(Conditionals.registeredNames()));
        MODULE_ACTIONS = enumValues(ModuleCmd.Action.class);
        CONFIG_TYPES   = enumValues(ConfigCmd.Type.class);
        DEFINE_TYPES   = enumValues(DefineCmd.Type.class);
        INPUT_TYPES    = enumValues(InputType.class);
        TARGET_TYPES   = enumValues(TargetType.class);
        DIMENSIONS     = enumValues(Dimensions.class);
    }

    private final List<String> suggestions = new ArrayList<>();
    private int selectedIndex = 0;
    private boolean visible = false;

    private static <E extends Enum<E>> List<String> enumValues(Class<E> cls) {
        return sorted(Arrays.stream(cls.getEnumConstants())
                .map(e -> e.name().toLowerCase())
                .toList());
    }

    private static List<String> sorted(List<String> list) {
        return list.stream().sorted().toList();
    }

    public void update(String line, int cursorCol) {
        suggestions.clear();
        visible = false;

        if (line.startsWith("//")) return;

        String beforeCursor = cursorCol <= line.length() ? line.substring(0, cursorCol) : line;
        String[] tokens = beforeCursor.stripLeading().split(" +", -1);
        int tokenIdx = tokens.length - 1;
        String prefix = tokenIdx >= 0 ? tokens[tokenIdx] : "";

        if (prefix.isEmpty() && tokenIdx > 0) return;

        List<String> pool = resolvePool(tokens, tokenIdx);
        for (String kw : pool) {
            if (kw.startsWith(prefix) && !kw.equals(prefix)) {
                suggestions.add(kw);
                if (suggestions.size() >= MAX_SUGGESTIONS) break;
            }
        }

        visible = !suggestions.isEmpty();
        selectedIndex = 0;
    }

    private List<String> resolvePool(String[] tokens, int tokenIdx) {
        if (tokenIdx == 0) return COMMANDS;
        String cmd = tokens[0].toLowerCase();
        return switch (cmd) {
            case "on"                                     -> tokenIdx == 1 ? EVENT_TYPES    : List.of();
            case "if", "if_not"                          -> tokenIdx == 1 ? CONDITIONALS   : List.of();
            case "module"                                -> tokenIdx == 1 ? MODULE_ACTIONS  : List.of();
            case "config"                                -> tokenIdx == 1 ? CONFIG_TYPES    : List.of();
            case "define", "def"                        -> tokenIdx == 1 ? DEFINE_TYPES    : List.of();
            case "input", "hold_input", "toggle_input"  -> tokenIdx == 1 ? INPUT_TYPES     : List.of();
            case "interact"                              -> tokenIdx == 1 ? TARGET_TYPES    : List.of();
            case "dimension"                             -> tokenIdx == 1 ? DIMENSIONS      : List.of();
            default                                      -> List.of();
        };
    }

    public boolean onKey(int key) {
        if (!visible) return false;
        return switch (key) {
            case GLFW.GLFW_KEY_UP -> {
                selectedIndex = (selectedIndex - 1 + suggestions.size()) % suggestions.size();
                yield true;
            }
            case GLFW.GLFW_KEY_DOWN -> {
                selectedIndex = (selectedIndex + 1) % suggestions.size();
                yield true;
            }
            case GLFW.GLFW_KEY_ESCAPE -> {
                visible = false;
                yield true;
            }
            default -> false;
        };
    }

    public boolean isInsertKey(int key) {
        return visible && (key == GLFW.GLFW_KEY_TAB || key == GLFW.GLFW_KEY_ENTER);
    }

    public String getSelected() {
        if (!visible || suggestions.isEmpty()) return null;
        return suggestions.get(selectedIndex);
    }

    public boolean isVisible() {
        return visible;
    }

    public void hide() {
        visible = false;
    }

    public void render(GuiGraphicsExtractor context, int cursorPixelX, int cursorPixelY, int scissorX, int scissorW) {
        if (!visible || suggestions.isEmpty()) return;

        int popupH = suggestions.size() * ROW_H + PADDING * 2;
        int px = Math.max(scissorX, Math.min(cursorPixelX, scissorX + scissorW - POPUP_W));
        int py = cursorPixelY + ROW_H + 1;

        RenderUtils.fillRect(context, px - 1, py - 1, POPUP_W + 2, popupH + 2, COLOR_BORDER);
        RenderUtils.fillRect(context, px, py, POPUP_W, popupH, COLOR_BG);

        for (int i = 0; i < suggestions.size(); i++) {
            int rowY = py + PADDING + i * ROW_H;
            if (i == selectedIndex) {
                RenderUtils.fillRect(context, px, rowY - 1, POPUP_W, ROW_H + 1, COLOR_SELECTED);
            }
            int color = i == selectedIndex ? COLOR_HIGHLIGHT : COLOR_TEXT;
            RenderUtils.drawDefaultScaledText(context, Component.literal(suggestions.get(i)), px + PADDING, rowY, 0.8F, false, color);
        }
    }
}
