package io.github.itzispyder.clickcrystals.gui.elements.browsingmode.module;

import io.github.itzispyder.clickcrystals.gui.GuiScreen;
import io.github.itzispyder.clickcrystals.gui.elements.common.Typeable;
import io.github.itzispyder.clickcrystals.gui.misc.Color;
import io.github.itzispyder.clickcrystals.gui.misc.Shades;
import io.github.itzispyder.clickcrystals.gui.misc.animators.Hover;
import io.github.itzispyder.clickcrystals.modules.settings.AbstractListSetting;
import io.github.itzispyder.clickcrystals.util.minecraft.render.RenderUtils;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Rows of text you edit in place. The last row is always an empty one, so filling it in
 * opens the next, and clearing a row drops it when you move on.
 */
public class ListSettingElement extends SettingElement<AbstractListSetting> implements Typeable {

    protected static final int ROW_HEIGHT = 14;
    protected static final int ROW_GAP = 3;
    protected static final int ROW_TOP_GAP = 4;
    protected static final int ICON_SIZE = 10;
    protected static final int REMOVE_WIDTH = 12;
    protected static final float TEXT_SCALE = 0.7F;
    private static final long GLOW_TIME = 110;

    private final AbstractListSetting setting;
    private final List<String> rows;
    private final List<Hover> glow;
    private int editing;
    private int detailsHeight;

    public ListSettingElement(AbstractListSetting setting, int x, int y) {
        super(setting, x, y);
        this.setting = setting;
        this.rows = new ArrayList<>();
        this.glow = new ArrayList<>();
        this.editing = -1;
        this.detailsHeight = height;
        createResetButton();
    }

    // rows follow the setting until a row is being typed in, then the local copy leads
    protected List<String> rows() {
        if (!isEditing()) {
            rows.clear();
            rows.addAll(setting.getEntries());
            rows.add("");
        }
        return rows;
    }

    protected boolean isEditing() {
        return editing >= 0 && mc.gui.screen() instanceof GuiScreen screen && screen.selected == this;
    }

    @Override
    protected int getSlotHeight() {
        return detailsHeight + ROW_TOP_GAP + rows().size() * (ROW_HEIGHT + ROW_GAP) + 4;
    }

    @Override
    public void onRender(GuiGraphicsExtractor context, int mouseX, int mouseY) {
        // the section separator belongs under the rows, not under the description
        boolean underline = shouldUnderline;
        shouldUnderline = false;
        this.renderSettingDetails(context);
        shouldUnderline = underline;
        this.detailsHeight = height;

        List<String> rows = rows();
        int caret = y + detailsHeight + ROW_TOP_GAP;
        for (int i = 0; i < rows.size(); i++) {
            renderRow(context, rows.get(i), i, caret, mouseX, mouseY);
            caret += ROW_HEIGHT + ROW_GAP;
        }
        caret += 1;

        this.setHeight(caret - y);

        if (shouldUnderline) {
            RenderUtils.drawHorLine(context, x + 5, caret, width - 10, Shades.DARK_GRAY);
        }
    }

    protected void renderRow(GuiGraphicsExtractor context, String text, int index, int rowY, int mouseX, int mouseY) {
        boolean active = isEditing() && editing == index;
        boolean hovered = isOverRow(mouseX, mouseY, rowY);
        boolean last = index == rows().size() - 1;

        int color = rowColor(index, active, hovered);
        double lift = glowAt(index);
        // the row leans towards the mouse as it lights up
        int rowX = rowX() + (int)Math.round(lift * 2);

        RenderUtils.fillRoundRect(context, rowX, rowY, rowWidth(), ROW_HEIGHT, 3, color);
        if (lift > 0.01) {
            // accent underline grows in from the middle as the row takes the keystrokes
            int lineWidth = (int)((rowWidth() - 6) * lift);
            RenderUtils.fillRoundHoriLine(context, rowX + rowWidth() / 2 - lineWidth / 2, rowY + ROW_HEIGHT - 1, lineWidth, 1, Color.blend(Shades.TRANS_GENERIC, Shades.GENERIC, lift));
        }

        int textX = rowX + 5 + renderRowIcon(context, text, rowX + 5, rowY);
        int textY = rowY + (ROW_HEIGHT - 6) / 2;
        int textW = rowX + rowWidth() - REMOVE_WIDTH - textX - 2;

        if (text.isEmpty() && !active) {
            RenderUtils.drawText(context, "§8" + (last ? placeholder() : "empty"), textX, textY, TEXT_SCALE, false);
        }
        else {
            RenderUtils.drawText(context, trimToWidth(text, textW) + (active && caretVisible() ? "§f§l|" : ""), textX, textY, TEXT_SCALE, false);
        }

        if (!text.isEmpty() && (hovered || active)) {
            boolean overRemove = isOverRemove(mouseX, mouseY, rowY);
            RenderUtils.drawText(context, overRemove ? "§cx" : "§7x", rowX + rowWidth() - REMOVE_WIDTH + 4, textY, TEXT_SCALE, false);
        }
    }

    // blinks the way every other text field does
    private boolean caretVisible() {
        return System.currentTimeMillis() % 1000 < 600;
    }

    protected double glowAt(int index) {
        return index < glow.size() ? glow.get(index).value() : 0.0;
    }

    // rows ease into their highlight instead of snapping between two flat colors
    protected int rowColor(int index, boolean active, boolean hovered) {
        while (glow.size() <= index) {
            glow.add(new Hover(GLOW_TIME));
        }

        double eased = glow.get(index).update(active ? 1.0 : hovered ? 0.45 : 0.0);
        return Color.blend(Shades.TRANS_DARK_GRAY, Shades.TRANS_GENERIC_LOW, eased);
    }

    /**
     * Draws whatever sits in front of a row's text.
     *
     * @return the width it took up
     */
    protected int renderRowIcon(GuiGraphicsExtractor context, String text, int iconX, int rowY) {
        return 0;
    }

    protected String placeholder() {
        return "add an entry...";
    }

    /**
     * Last chance to clean up a row once it is done being typed in.
     */
    protected String normalize(String text) {
        return text.trim();
    }

    // ---- editing ----

    protected void startEditing(int index) {
        if (mc.gui.screen() instanceof GuiScreen screen) {
            rows();
            screen.selected = this;
            this.editing = index;
        }
    }

    protected void stopEditing() {
        if (!isEditing()) {
            return;
        }
        rows.set(editing, normalize(rows.get(editing)));
        this.editing = -1;
        save();
    }

    protected void removeRow(int index) {
        rows();
        rows.remove(index);
        if (index < glow.size()) {
            glow.remove(index);
        }
        if (editing > index) {
            editing--;
        }
        else if (editing == index) {
            editing = -1;
        }
        save();
    }

    private void save() {
        setting.setEntries(rows.stream().map(String::trim).filter(row -> !row.isEmpty()).toList());
    }

    @Override
    public void onInput(Function<String, String> factory) {
        if (!isEditing()) {
            return;
        }
        rows.set(editing, factory.apply(rows.get(editing)).replace(AbstractListSetting.SEPARATOR, ""));

        // filling the last row opens the next one
        if (editing == rows.size() - 1 && !rows.get(editing).isBlank()) {
            rows.add("");
        }
        save();
    }

    @Override
    public boolean onKey(int key, int scan) {
        if (!isEditing()) {
            return Typeable.super.onKey(key, scan);
        }
        if (key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER || key == GLFW.GLFW_KEY_TAB) {
            int next = editing + 1;
            stopEditing();
            // enter walks onto the next row, so a list can be typed out in one go
            if (next < rows().size()) {
                startEditing(next);
            }
            return true;
        }
        if (key == GLFW.GLFW_KEY_ESCAPE) {
            stopEditing();
        }
        return Typeable.super.onKey(key, scan);
    }

    @Override
    public void revertSettingValue() {
        this.editing = -1;
        this.setting.setVal(setting.getDef());
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (setting.isVisible()) {
            int caret = y + detailsHeight + ROW_TOP_GAP;
            for (int i = 0; i < rows().size(); i++) {
                if (isOverRow((int)mouseX, (int)mouseY, caret)) {
                    if (isOverRemove((int)mouseX, (int)mouseY, caret) && !rows().get(i).isEmpty()) {
                        removeRow(i);
                    }
                    else {
                        stopEditing();
                        startEditing(i);
                    }
                    return;
                }
                caret += ROW_HEIGHT + ROW_GAP;
            }
            stopEditing();
        }
        super.mouseClicked(mouseX, mouseY, button);
    }

    // ---- geometry ----

    protected boolean isOverRow(int mouseX, int mouseY, int rowY) {
        return rendering && setting.isVisible()
                && mouseX >= rowX() && mouseX <= rowX() + rowWidth()
                && mouseY >= rowY && mouseY <= rowY + ROW_HEIGHT;
    }

    protected boolean isOverRemove(int mouseX, int mouseY, int rowY) {
        return isOverRow(mouseX, mouseY, rowY) && mouseX >= rowX() + rowWidth() - REMOVE_WIDTH;
    }

    protected String trimToWidth(String text, int maxWidth) {
        while (!text.isEmpty() && mc.font.width(text) * TEXT_SCALE > maxWidth) {
            text = text.substring(1);
        }
        return text;
    }

    protected int rowX() {
        return x + 5;
    }

    protected int rowWidth() {
        return width - 15;
    }

    protected int textX() {
        return rowX() + 5;
    }

    public AbstractListSetting getSetting() {
        return setting;
    }
}
