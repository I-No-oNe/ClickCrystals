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
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ClickScriptAutocomplete implements Global {

    private static final int MAX_SUGGESTIONS = 8;
    private static final int ROW_H = 10;
    private static final int PADDING = 4;
    private static final int POPUP_W = 120;
    private static final int COLOR_BG = 0xF0141420;
    private static final int COLOR_BORDER = 0xFF00B7FF;
    private static final int COLOR_SELECTED = 0x4000B7FF;
    private static final int COLOR_TEXT = 0xFFAAAAAA;
    private static final int COLOR_HIGHLIGHT = 0xFFFFFFFF;

    private static final List<String> COMMANDS, EVENT_TYPES, CONDITIONALS, MODULE_ACTIONS,
                                      CONFIG_TYPES, DEFINE_TYPES, INPUT_TYPES, TARGET_TYPES,
                                      DIMENSIONS, AS_TYPES, SNAP_TYPES;
    private static final Set<String> COMMAND_SET;

    static {
        COMMANDS       = sorted(Arrays.asList(ClickScript.collectNames()));
        COMMAND_SET    = new HashSet<>(COMMANDS);
        EVENT_TYPES    = enumValues(OnEventCmd.EventType.class);
        CONDITIONALS   = sorted(new ArrayList<>(Conditionals.registeredNames()));
        MODULE_ACTIONS = enumValues(ModuleCmd.Action.class);
        CONFIG_TYPES   = enumValues(ConfigCmd.Type.class);
        DEFINE_TYPES   = enumValues(DefineCmd.Type.class);
        INPUT_TYPES    = enumValues(InputType.class);
        TARGET_TYPES   = enumValues(TargetType.class);
        DIMENSIONS     = enumValues(Dimensions.class);
        AS_TYPES       = sorted(List.of("any_entity", "client", "nearest_entity", "target_entity"));
        SNAP_TYPES     = sorted(List.of("any_block", "any_entity", "nearest_block", "nearest_entity", "polar", "position", "target_entity"));
    }

    private final List<String> suggestions = new ArrayList<>();
    private int selectedIndex = 0;
    private boolean visible = false;

    private static <E extends Enum<E>> List<String> enumValues(Class<E> cls) {
        return sorted(Arrays.stream(cls.getEnumConstants()).map(e -> e.name().toLowerCase()).toList());
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

        // Only skip blank lines, not mid-line spaces (e.g. "on " should still suggest).
        if (prefix.isEmpty() && tokenIdx == 0) return;

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
        if (cmd.equals("on"))                              return tokenIdx == 1 ? EVENT_TYPES : List.of();
        if (cmd.equals("if") || cmd.equals("if_not"))     return resolveIfPool(tokens, tokenIdx);
        if (cmd.equals("while") || cmd.equals("while_not")) return resolveWhilePool(tokens, tokenIdx);
        return firstArgPool(cmd, tokenIdx);
    }

    // "if"/"if_not": if <conditional> [cond_args...] <inline_command> [cmd_args...]
    // Scans forward to find the inline command, then hands off to firstArgPool.
    private List<String> resolveIfPool(String[] tokens, int tokenIdx) {
        if (tokenIdx == 1) return CONDITIONALS;

        for (int i = 2; i < tokenIdx; i++) {
            if (COMMAND_SET.contains(tokens[i].toLowerCase())) {
                return firstArgPool(tokens[i].toLowerCase(), tokenIdx - i);
            }
        }

        // Still in conditional args or about to type the inline command.
        return COMMANDS;
    }

    // "while"/"while_not": while <num>? <conditional> {}
    // The repeat count is optional, so the conditional may be at token 1 or 2.
    private List<String> resolveWhilePool(String[] tokens, int tokenIdx) {
        if (tokenIdx == 1) return CONDITIONALS;
        if (tokenIdx == 2 && isNumber(tokens[1])) return CONDITIONALS;
        return List.of();
    }

    private static boolean isNumber(String s) {
        try { Double.parseDouble(s); return true; } catch (NumberFormatException e) { return false; }
    }

    // Returns the first-argument suggestion pool for a command, or nothing for any other position.
    private static List<String> firstArgPool(String cmd, int idx) {
        if (idx != 1) return List.of();
        return switch (cmd) {
            case "module"                               -> MODULE_ACTIONS;
            case "config"                               -> CONFIG_TYPES;
            case "define", "def"                       -> DEFINE_TYPES;
            case "input", "hold_input", "toggle_input" -> INPUT_TYPES;
            case "interact"                            -> TARGET_TYPES;
            case "dimension"                           -> DIMENSIONS;
            case "as"                                  -> AS_TYPES;
            case "snap_to", "turn_to"                 -> SNAP_TYPES;
            case "while", "while_not"                  -> CONDITIONALS;
            default                                    -> List.of();
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
        int px = Mth.clamp(cursorPixelX, scissorX, scissorX + scissorW - POPUP_W);
        int py = cursorPixelY + ROW_H + 1;

        RenderUtils.fillRoundRect(context, px, py, POPUP_W, popupH, 3, COLOR_BG);
        RenderUtils.fillRoundShadow(context, px, py, POPUP_W, popupH, 3, 1, COLOR_BORDER, COLOR_BORDER);

        for (int i = 0; i < suggestions.size(); i++) {
            int rowY = py + PADDING + i * ROW_H;
            if (i == selectedIndex) {
                RenderUtils.fillRect(context, px + 1, rowY - 1, POPUP_W - 2, ROW_H, COLOR_SELECTED);
            }
            int color = i == selectedIndex ? COLOR_HIGHLIGHT : COLOR_TEXT;
            RenderUtils.drawDefaultScaledText(context, Component.literal(suggestions.get(i)), px + PADDING, rowY, 0.8F, false, color);
        }
    }
}