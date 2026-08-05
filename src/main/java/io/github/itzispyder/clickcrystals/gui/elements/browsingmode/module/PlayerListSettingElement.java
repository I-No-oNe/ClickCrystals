package io.github.itzispyder.clickcrystals.gui.elements.browsingmode.module;

import io.github.itzispyder.clickcrystals.modules.settings.PlayerListSetting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.PlayerFaceExtractor;
import net.minecraft.client.multiplayer.PlayerInfo;

public class PlayerListSettingElement extends ListSettingElement {

    public PlayerListSettingElement(PlayerListSetting setting, int x, int y) {
        super(setting, x, y);
    }

    @Override
    protected int renderRowIcon(GuiGraphicsExtractor context, String text, int iconX, int rowY) {
        PlayerInfo info = PlayerListSetting.getInfo(text);
        if (info == null) {
            return 0;
        }
        PlayerFaceExtractor.extractRenderState(context, info.getSkin(), iconX, rowY + (ROW_HEIGHT - ICON_SIZE) / 2, ICON_SIZE);
        return ICON_SIZE + 3;
    }

    @Override
    protected String placeholder() {
        return "add a player...";
    }

    // names have to match exactly to be worth anything, so borrow the casing from the tab list
    @Override
    protected String normalize(String text) {
        return PlayerListSetting.closest(text);
    }
}
