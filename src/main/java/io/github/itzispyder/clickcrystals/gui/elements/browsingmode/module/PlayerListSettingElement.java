package io.github.itzispyder.clickcrystals.gui.elements.browsingmode.module;

import io.github.itzispyder.clickcrystals.gui.misc.Shades;
import io.github.itzispyder.clickcrystals.modules.settings.AbstractListSetting;
import io.github.itzispyder.clickcrystals.modules.settings.PlayerListSetting;
import io.github.itzispyder.clickcrystals.util.minecraft.render.RenderUtils;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.PlayerFaceExtractor;
import net.minecraft.client.multiplayer.PlayerInfo;

public class PlayerListSettingElement extends ListSettingElement {

    private static final int HEAD_SIZE = 9;

    private final PlayerListSetting setting;

    public PlayerListSettingElement(PlayerListSetting setting, int x, int y) {
        super(setting, x, y);
        this.setting = setting;
    }

    @Override
    protected void renderEntry(GuiGraphicsExtractor context, String entry, int rowY, int mouseX, int mouseY) {
        RenderUtils.fillRoundHoriLine(context, rowX(), rowY, rowWidth(), ROW_HEIGHT, Shades.GRAY);

        PlayerInfo info = PlayerListSetting.getInfo(entry);
        if (info != null) {
            PlayerFaceExtractor.extractRenderState(context, info.getSkin(), textX(), rowY + (ROW_HEIGHT - HEAD_SIZE) / 2, HEAD_SIZE);
        }
        // offline players stay in the list, just greyed out
        RenderUtils.drawText(context, (info != null ? "" : "§7") + entry, textX() + HEAD_SIZE + 3, rowY + ROW_HEIGHT / 3, 0.7F, false);
        renderRemoveButton(context, rowY, mouseX, mouseY);
    }

    // fix up the casing of names that are online, they have to match exactly to be useful
    @Override
    protected void commit() {
        for (String entry : AbstractListSetting.split(getInput())) {
            setting.addEntry(PlayerListSetting.closest(entry));
        }
        clearInput();
    }
}
