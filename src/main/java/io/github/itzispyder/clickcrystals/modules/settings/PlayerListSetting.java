package io.github.itzispyder.clickcrystals.modules.settings;

import io.github.itzispyder.clickcrystals.gui.elements.browsingmode.module.PlayerListSettingElement;
import io.github.itzispyder.clickcrystals.util.StringUtils;
import io.github.itzispyder.clickcrystals.util.minecraft.PlayerUtils;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.world.entity.player.Player;

import java.util.List;

/**
 * A list of players by name. Names are kept as typed so an offline player can be listed,
 * but the GUI resolves what you type against the tab list and shows the heads it knows.
 */
public class PlayerListSetting extends AbstractListSetting {

    public PlayerListSetting(String name, String description, String def, String val) {
        super(name, description, def, val);
    }

    @Override
    public PlayerListSettingElement toGuiElement(int x, int y) {
        return new PlayerListSettingElement(this, x, y);
    }

    public boolean matches(Player player) {
        return player != null && contains(player.getGameProfile().name());
    }

    /**
     * Tab list entry for a name, or null when that player is not around.
     */
    public static PlayerInfo getInfo(String name) {
        if (PlayerUtils.invalid()) {
            return null;
        }
        return PlayerUtils.player().connection.getPlayerInfoIgnoreCase(name.trim());
    }

    public static List<String> getOnlineNames() {
        if (PlayerUtils.invalid()) {
            return List.of();
        }
        return PlayerUtils.player().connection.getOnlinePlayers().stream()
                .map(entry -> entry.getProfile().name())
                .toList();
    }

    /**
     * The online player the input was probably meant to be, or the input itself when
     * nobody matches, since listing someone who is not online yet is fair game.
     */
    public static String closest(String input) {
        String match = StringUtils.closest(getOnlineNames(), input.trim());
        return match != null ? match : input.trim();
    }

    public static Builder create() {
        return new Builder();
    }

    public static class Builder extends SettingBuilder<String, Builder, PlayerListSetting> {

        public Builder def(String... names) {
            return def(String.join(SEPARATOR, names));
        }

        @Override
        public PlayerListSetting buildSetting() {
            return new PlayerListSetting(name, description, def, getOrDef(val, def));
        }
    }
}
