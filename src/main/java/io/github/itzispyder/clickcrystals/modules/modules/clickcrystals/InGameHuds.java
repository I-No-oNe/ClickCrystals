package io.github.itzispyder.clickcrystals.modules.modules.clickcrystals;

import io.github.itzispyder.clickcrystals.events.EventHandler;
import io.github.itzispyder.clickcrystals.events.Listener;
import io.github.itzispyder.clickcrystals.events.events.client.PlayerAttackEntityEvent;
import io.github.itzispyder.clickcrystals.gui.hud.Hud;
import io.github.itzispyder.clickcrystals.gui.hud.positionable.TargetPositionableHud;
import io.github.itzispyder.clickcrystals.gui.misc.Color;
import io.github.itzispyder.clickcrystals.modules.Categories;
import io.github.itzispyder.clickcrystals.modules.Module;
import io.github.itzispyder.clickcrystals.modules.ModuleSetting;
import io.github.itzispyder.clickcrystals.modules.settings.BooleanSetting;
import io.github.itzispyder.clickcrystals.modules.settings.DoubleSetting;
import io.github.itzispyder.clickcrystals.modules.settings.EnumSetting;
import io.github.itzispyder.clickcrystals.modules.settings.SettingSection;
import net.minecraft.world.entity.player.Player;

public class InGameHuds extends Module implements Listener {

    private final SettingSection scGeneral = getGeneralSection();
    private final SettingSection scHudVisibility = createSettingSection("hud-visibility");
    private final SettingSection scHudPosition = createSettingSection("position-hud-settings");
    private final SettingSection scHudTarget = createSettingSection("target-hud-settings");
    private final SettingSection scHudClock = createSettingSection("clock-hud-settings");
    public final ModuleSetting<Color> color = scGeneral.add(createColorSetting()
            .name("color")
            .description("Color of the huds")
            .def(Hud.DEFAULT_COLOR)
            .build()
    );
    public final ModuleSetting<Boolean> hudArmor = scHudVisibility.add(BooleanSetting.create()
            .name("render-armor-hud")
            .description("Renders the armor hud.")
            .def(true)
            .build()
    );
    public final ModuleSetting<Boolean> hudIcon = scHudVisibility.add(BooleanSetting.create()
            .name("render-icon-hud")
            .description("Renders the icon hud.")
            .def(false)
            .build()
    );
    public final ModuleSetting<Boolean> hudPing = scHudVisibility.add(BooleanSetting.create()
            .name("render-ping-hud")
            .description("Renders the ping hud.")
            .def(true)
            .build()
    );
    public final ModuleSetting<Boolean> hudFps = scHudVisibility.add(BooleanSetting.create()
            .name("render-fps-hud")
            .description("Renders the fps hud.")
            .def(true)
            .build()
    );
    public final ModuleSetting<Boolean> hudCps = scHudVisibility.add(BooleanSetting.create()
            .name("render-cps-hud")
            .description("Renders the clicks per second hud.")
            .def(true)
            .build()
    );
    public final ModuleSetting<Boolean> hudClock = scHudVisibility.add(BooleanSetting.create()
            .name("render-clock-hud")
            .description("Renders the clock hud.")
            .def(true)
            .build()
    );

    public final ModuleSetting<Boolean> hudTarget = scHudVisibility.add(BooleanSetting.create()
            .name("render-target-hud")
            .description("Renders the target hud.")
            .def(true)
            .build()
    );
    public final ModuleSetting<Boolean> hudPos = scHudVisibility.add(BooleanSetting.create()
            .name("render-position-hud")
            .description("Renders the position hud.")
            .def(false)
            .build()
    );
    public final ModuleSetting<Boolean> hudBiome = scHudVisibility.add(BooleanSetting.create()
            .name("render-biome-hud")
            .description("Renders the biome hud.")
            .def(false)
            .build()
    );
    public final ModuleSetting<Boolean> hudDirection = scHudVisibility.add(BooleanSetting.create()
            .name("render-direction-hud")
            .description("Renders the direction hud.")
            .def(false)
            .build()
    );
    public final ModuleSetting<Boolean> hudCrosshair = scHudVisibility.add(BooleanSetting.create()
            .name("render-crosshair-hud")
            .description("Renders the crosshair target hud.")
            .def(false)
            .build()
    );
    public final ModuleSetting<Boolean> hudRotation = scHudVisibility.add(BooleanSetting.create()
            .name("render-rotation-hud")
            .description("Renders the rotation hud.")
            .def(false)
            .build()
    );
    public final ModuleSetting<Boolean> hudResource = scHudVisibility.add(BooleanSetting.create()
            .name("render-resource-hud")
            .description("Renders the resource hud.")
            .def(false)
            .build()
    );
    public final ModuleSetting<Double> hudTargetStayTime = scHudTarget.add(DoubleSetting.create()
            .min(5.0)
            .max(600.0) // 10 minutes (600 seconds)
            .decimalPlaces(1)
            .name("target-hud-stay-time")
            .description("Tracking time of current target.")
            .visibleWhen(hudTarget::getVal)
            .def(10.0)
            .build()
    );
    public final ModuleSetting<Boolean> hudTargetDisableWhenNoCombat = scHudTarget.add(createBoolSetting()
            .name("target-hud-no-combat-disable")
            .description("Disables rendering of the target hud when not in combat.")
            .visibleWhen(hudTarget::getVal)
            .def(false)
            .build()
    );
    public final ModuleSetting<Boolean> showNetherCoords = scHudPosition.add(createBoolSetting()
            .name("show-nether-coords-in-overworld")
            .description("Show nether coords when in overworld")
            .visibleWhen(hudPos::getVal)
            .def(false)
            .build()
        );
    public final ModuleSetting<ClockDisplay> hudClockHourDisplay = scHudClock.add(EnumSetting.create(ClockDisplay.class)
            .name("clock-hud-hour-display")
            .description("Clock hour display.")
            .visibleWhen(hudClock::getVal)
            .def(ClockDisplay.HOUR_12)
            .build()
    );

    public InGameHuds() {
        super("in-game-huds", Categories.CLIENT,"Manager of all custom ClickCrystals in-game huds");
    }

    @Override
    protected void onEnable() {
        system.addListener(this);
    }

    @Override
    protected void onDisable() {
        system.removeListener(this);
    }

    @EventHandler
    private void onAttack(PlayerAttackEntityEvent e) {
        if (e.getEntity() instanceof Player player && hudTarget.getVal()) {
            TargetPositionableHud.setTarget(player);
        }
    }

    public int getArgb() {
        return color.getVal().getHex();
    }

    public enum ClockDisplay {
        HOUR_12,
        HOUR_24
    }
}
