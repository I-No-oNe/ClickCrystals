package io.github.itzispyder.clickcrystals.modules.modules.rendering;

import io.github.itzispyder.clickcrystals.events.EventHandler;
import io.github.itzispyder.clickcrystals.events.events.world.ClientTickEndEvent;
import io.github.itzispyder.clickcrystals.modules.Categories;
import io.github.itzispyder.clickcrystals.modules.ModuleSetting;
import io.github.itzispyder.clickcrystals.modules.modules.ListenerModule;
import io.github.itzispyder.clickcrystals.modules.settings.EnumSetting;
import io.github.itzispyder.clickcrystals.modules.settings.SettingSection;
import io.github.itzispyder.clickcrystals.util.minecraft.PlayerUtils;
import net.minecraft.client.option.Perspective;
import net.minecraft.util.math.MathHelper;

public class FreeLook extends ListenerModule {

    private final SettingSection scGeneral = getGeneralSection();
    public final ModuleSetting<POV> PerspectivePoint = scGeneral.add(EnumSetting.create(POV.class)
            .name("camera-perspective")
            .description("The Perspective Which Lock The Camera.")
            .def(POV.THIRD_PERSON_FOV)
            .build()
    );

    public FreeLook() {
        super("free-look", Categories.RENDER, "lock your camera perspective and let you move around it");
    }

    public float cY;
    public float cP;


    @Override
    public void onEnable() {
        if (PlayerUtils.invalid()) return;
        cY = mc.player.getYaw();
        cP = mc.player.getPitch();
    }


    @EventHandler
    private void onTick(ClientTickEndEvent e) {
        PlayerUtils.player().setPitch(MathHelper.clamp(PlayerUtils.player().getPitch(), -90, 90));
        cP = MathHelper.clamp(cP, -90, 90);
    }

    public enum POV {
        FIRST_PERSON_FOV(Perspective.FIRST_PERSON),
        SECOND_PERSON_FOV(Perspective.THIRD_PERSON_BACK),
        THIRD_PERSON_FOV(Perspective.THIRD_PERSON_FRONT);

        private final Perspective perspective;

        POV(Perspective perspective) {
            this.perspective = perspective;
        }

        public Perspective getPerspective() {
            return perspective;
        }
    }
}