package io.github.itzispyder.clickcrystals.gui.screens.scripts;

import io.github.itzispyder.clickcrystals.Global;
import io.github.itzispyder.clickcrystals.gui.misc.Shades;
import io.github.itzispyder.clickcrystals.util.minecraft.render.RenderUtils;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class EditorTabBar implements Global {

    private static final int HEIGHT = 12;
    private static final int TAB_PADDING = 6;
    private static final int CLOSE_W = 8;
    private static final int COLOR_ACTIVE   = 0xFF2A2A3A;
    private static final int COLOR_INACTIVE = 0xFF1A1A28;
    private static final int COLOR_HOVER    = 0xFF353550;
    private static final int COLOR_CLOSE    = 0xFFBB4444;

    private final List<EditorTab> tabs = new ArrayList<>();
    private int activeIndex = 0;

    public boolean open(File file) {
        for (int i = 0; i < tabs.size(); i++) {
            if (tabs.get(i).filepath.equals(file.getPath())) {
                activeIndex = i;
                return false; // already open, just switched
            }
        }
        tabs.add(new EditorTab(file));
        activeIndex = tabs.size() - 1;
        return true; // new tab
    }

    public boolean close(int index) {
        if (tabs.size() <= 1) return false; // don't close last tab
        tabs.remove(index);
        activeIndex = Math.min(activeIndex, tabs.size() - 1);
        return true;
    }

    public EditorTab getActive() {
        return tabs.isEmpty() ? null : tabs.get(activeIndex);
    }

    public int getActiveIndex() {
        return activeIndex;
    }

    public int size() {
        return tabs.size();
    }

    public static int getHeight() {
        return HEIGHT;
    }

    public void render(GuiGraphicsExtractor context, int mouseX, int mouseY, int x, int y, int width) {
        int caret = x;
        for (int i = 0; i < tabs.size(); i++) {
            EditorTab tab = tabs.get(i);
            int tabW = tabWidth(tab);

            boolean hovered = mouseX >= caret && mouseX < caret + tabW && mouseY >= y && mouseY < y + HEIGHT;
            boolean active = i == activeIndex;

            int bg = active ? COLOR_ACTIVE : (hovered ? COLOR_HOVER : COLOR_INACTIVE);
            RenderUtils.fillRect(context, caret, y, tabW, HEIGHT, bg);

            if (active) {
                RenderUtils.fillRect(context, caret, y, tabW, 1, Shades.GENERIC);
            }

            RenderUtils.drawText(context, tab.filename, caret + TAB_PADDING, y + 2, 0.7F, false);

            // close button
            int closeX = caret + tabW - CLOSE_W - 2;
            boolean closeHovered = mouseX >= closeX && mouseX < closeX + CLOSE_W && mouseY >= y && mouseY < y + HEIGHT;
            int closeColor = closeHovered ? COLOR_CLOSE : Shades.LIGHT_GRAY;
            RenderUtils.drawText(context, "§" + (closeHovered ? "c" : "7") + "×", closeX, y + 2, 0.7F, false);

            caret += tabW + 1;
        }
    }

    // Returns true if click was consumed. onSwitch/onClose callbacks handled externally.
    public HandleResult handleClick(int mouseX, int mouseY, int button, int x, int y) {
        int caret = x;
        for (int i = 0; i < tabs.size(); i++) {
            EditorTab tab = tabs.get(i);
            int tabW = tabWidth(tab);

            if (mouseX >= caret && mouseX < caret + tabW && mouseY >= y && mouseY < y + HEIGHT) {
                int closeX = caret + tabW - CLOSE_W - 2;
                if (mouseX >= closeX && mouseX < closeX + CLOSE_W) {
                    return HandleResult.close(i);
                }
                if (button == 0) {
                    return HandleResult.switchTab(i);
                }
            }
            caret += tabW + 1;
        }
        return HandleResult.none();
    }

    private int tabWidth(EditorTab tab) {
        return mc.font.width(tab.filename) + TAB_PADDING * 2 + CLOSE_W + 4;
    }

    public record HandleResult(Type type, int index) {
        public enum Type { NONE, SWITCH, CLOSE }
        public static HandleResult none()              { return new HandleResult(Type.NONE, -1); }
        public static HandleResult switchTab(int idx)  { return new HandleResult(Type.SWITCH, idx); }
        public static HandleResult close(int idx)      { return new HandleResult(Type.CLOSE, idx); }
        public boolean isNone()   { return type == Type.NONE; }
        public boolean isSwitch() { return type == Type.SWITCH; }
        public boolean isClose()  { return type == Type.CLOSE; }
    }
}
