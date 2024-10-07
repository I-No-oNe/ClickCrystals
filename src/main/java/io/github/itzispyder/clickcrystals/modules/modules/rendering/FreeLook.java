package io.github.itzispyder.clickcrystals.modules.modules.rendering;

import io.github.itzispyder.clickcrystals.events.EventHandler;
import io.github.itzispyder.clickcrystals.events.events.world.ClientTickEndEvent;
import io.github.itzispyder.clickcrystals.modules.Categories;
import io.github.itzispyder.clickcrystals.modules.ModuleSetting;
import io.github.itzispyder.clickcrystals.modules.modules.ListenerModule;
import io.github.itzispyder.clickcrystals.modules.settings.EnumSetting;
import io.github.itzispyder.clickcrystals.modules.settings.SettingSection;
import io.github.itzispyder.clickcrystals.util.minecraft.InteractionUtils;
import io.github.itzispyder.clickcrystals.util.minecraft.PlayerUtils;
import net.minecraft.client.option.Perspective;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

public class FreeLook extends ListenerModule {

    private final SettingSection scGeneral = getGeneralSection();
    public final ModuleSetting<POV> PerspectivePoint = scGeneral.add(EnumSetting.create(POV.class)
            .name("camera-perspective")
            .description("The Perspective Which Lock The Camera.")
            .def(POV.THIRD_PERSON_FOV)
            .build()
    );

    public final ModuleSetting<Boolean> arrowKeys = scGeneral.add(createBoolSetting()
            .name("arrows-control-opposite")
            .description("Allows you to rotate the player with the arrow keys.")
            .def(true)
            .build()
    );

    private final ModuleSetting<Double> arrowSpeed = scGeneral.add(createDoubleSetting()
            .name("arrow-speed")
            .description("Set rotation speed with arrow keys.")
            .def(4.0)
            .max(10)
            .min(0)
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
        if (arrowKeys.getVal() && mc.options.getPerspective() == PerspectivePoint.getVal().getPerspective()) {
            for (int i = 0; i < (arrowSpeed.getVal() * 2); i++) {
                if (InteractionUtils.isKeyPressed(GLFW.GLFW_KEY_LEFT)) cY -= 0.5;
                if (InteractionUtils.isKeyPressed(GLFW.GLFW_KEY_RIGHT)) cY += 0.5;
                if (InteractionUtils.isKeyPressed(GLFW.GLFW_KEY_UP)) cP -= 0.5;
                if (InteractionUtils.isKeyPressed(GLFW.GLFW_KEY_DOWN)) cP += 0.5;
            }
        }

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