package io.github.itzispyder.clickcrystals.gui.elements.browsingmode.module;

import io.github.itzispyder.clickcrystals.gui.GuiScreen;
import io.github.itzispyder.clickcrystals.gui.elements.common.Typeable;
import io.github.itzispyder.clickcrystals.gui.misc.Shades;
import io.github.itzispyder.clickcrystals.modules.settings.AbstractListSetting;
import io.github.itzispyder.clickcrystals.util.minecraft.render.RenderUtils;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.function.Function;

public class ListSettingElement extends SettingElement<AbstractListSetting> implements Typeable {

    protected static final int ROW_HEIGHT = 13;
    protected static final int ROW_GAP = 2;
    protected static final int REMOVE_WIDTH = 12;

    private final AbstractListSetting setting;
    private String input;
    private int detailsHeight;

    public ListSettingElement(AbstractListSetting setting, int x, int y) {
        super(setting, x, y);
        this.setting = setting;
        this.input = "";
        this.detailsHeight = height;
        createResetButton();
    }

    @Override
    protected int getSlotHeight() {
        return detailsHeight + (setting.getEntries().size() + 1) * (ROW_HEIGHT + ROW_GAP) + 5;
    }

    @Override
    public void onRender(GuiGraphicsExtractor context, int mouseX, int mouseY) {
        // the section separator belongs under the rows, not under the description
        boolean underline = shouldUnderline;
        shouldUnderline = false;
        this.renderSettingDetails(context);
        shouldUnderline = underline;
        this.detailsHeight = height;

        int caret = y + detailsHeight;
        List<String> entries = setting.getEntries();
        for (String entry : entries) {
            renderEntry(context, entry, caret, mouseX, mouseY);
            caret += ROW_HEIGHT + ROW_GAP;
        }
        renderInput(context, caret);
        caret += ROW_HEIGHT + 5;

        this.setHeight(caret - y);

        if (shouldUnderline) {
            RenderUtils.drawHorLine(context, x + 5, caret - 3, width - 10, Shades.DARK_GRAY);
        }
    }

    protected void renderEntry(GuiGraphicsExtractor context, String entry, int rowY, int mouseX, int mouseY) {
        RenderUtils.fillRoundHoriLine(context, rowX(), rowY, rowWidth(), ROW_HEIGHT, Shades.GRAY);
        RenderUtils.drawText(context, entry, textX(), rowY + ROW_HEIGHT / 3, 0.7F, false);
        renderRemoveButton(context, rowY, mouseX, mouseY);
    }

    protected void renderRemoveButton(GuiGraphicsExtractor context, int rowY, int mouseX, int mouseY) {
        String x = isOverRemove(mouseX, mouseY, rowY) ? "§cx" : "§7x";
        RenderUtils.drawText(context, x, rowX() + rowWidth() - REMOVE_WIDTH + 4, rowY + ROW_HEIGHT / 3, 0.7F, false);
    }

    protected void renderInput(GuiGraphicsExtractor context, int rowY) {
        boolean selected = mc.gui.screen() instanceof GuiScreen screen && screen.selected == this;
        RenderUtils.fillRoundHoriLine(context, rowX(), rowY, rowWidth(), ROW_HEIGHT, selected ? Shades.LIGHT_GRAY : Shades.DARK_GRAY);

        String text = input;
        while (!text.isEmpty() && mc.font.width(text) * 0.7F > rowWidth() - 10) {
            text = text.substring(1);
        }
        if (selected) {
            text = text + "§8§l︳";
        }
        else if (text.isEmpty()) {
            text = "§8+ add entry";
        }
        RenderUtils.drawText(context, text, textX(), rowY + ROW_HEIGHT / 3, 0.7F, false);
    }

    @Override
    public void revertSettingValue() {
        this.input = "";
        this.setting.setVal(setting.getDef());
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (setting.isVisible()) {
            int caret = y + detailsHeight;
            for (String entry : setting.getEntries()) {
                if (isOverRemove((int)mouseX, (int)mouseY, caret)) {
                    setting.removeEntry(entry);
                    return;
                }
                caret += ROW_HEIGHT + ROW_GAP;
            }
            if (isOverRow((int)mouseX, (int)mouseY, caret) && mc.gui.screen() instanceof GuiScreen screen) {
                screen.selected = this;
                return;
            }
        }
        super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean onKey(int key, int scan) {
        if (key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER) {
            commit();
            return true;
        }
        return Typeable.super.onKey(key, scan);
    }

    @Override
    public void onInput(Function<String, String> factory) {
        this.input = factory.apply(input);
        // typing the separator is how you finish an entry, same as writing a comma by hand
        if (input.contains(AbstractListSetting.SEPARATOR)) {
            commit();
        }
    }

    protected void commit() {
        for (String entry : AbstractListSetting.split(input)) {
            setting.addEntry(entry);
        }
        clearInput();
    }

    protected void clearInput() {
        this.input = "";
    }

    protected boolean isOverRow(int mouseX, int mouseY, int rowY) {
        return rendering && setting.isVisible()
                && mouseX >= rowX() && mouseX <= rowX() + rowWidth()
                && mouseY >= rowY && mouseY <= rowY + ROW_HEIGHT;
    }

    protected boolean isOverRemove(int mouseX, int mouseY, int rowY) {
        return isOverRow(mouseX, mouseY, rowY) && mouseX >= rowX() + rowWidth() - REMOVE_WIDTH;
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

    public String getInput() {
        return input;
    }

    public AbstractListSetting getSetting() {
        return setting;
    }
}
