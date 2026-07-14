package io.github.itzispyder.clickcrystals.modules.modules.rendering;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import io.github.itzispyder.clickcrystals.events.EventHandler;
import io.github.itzispyder.clickcrystals.events.events.world.RenderWorldEvent;
import io.github.itzispyder.clickcrystals.gui.misc.Color;
import io.github.itzispyder.clickcrystals.modules.Categories;
import io.github.itzispyder.clickcrystals.modules.ModuleSetting;
import io.github.itzispyder.clickcrystals.modules.modules.ListenerModule;
import io.github.itzispyder.clickcrystals.modules.settings.SettingSection;
import io.github.itzispyder.clickcrystals.util.MathUtils;
import io.github.itzispyder.clickcrystals.util.minecraft.PlayerUtils;
import io.github.itzispyder.clickcrystals.util.minecraft.render.RenderLayers;
import io.github.itzispyder.clickcrystals.util.minecraft.render.RenderUtils;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public class ElytraShadow extends ListenerModule {

    private final SettingSection scGeneral = getGeneralSection();
    public final ModuleSetting<Color> color = scGeneral.add(createColorSetting()
            .name("shadow-color")
            .description("Color of the shadow")
            .def(0xFF00B7FF)
            .build());

    public ElytraShadow() {
        super("elytra-shadow", Categories.RENDER, "Renders a shadow directly below a gliding player on the ground.");
    }

    @EventHandler
    public void onWorldRender(RenderWorldEvent e) {
        Color color = this.color.getVal();
        for (AbstractClientPlayer player : PlayerUtils.getClientWorld().players())
            if (player.isFallFlying())
                renderPlayerShadow(e, player, color);
    }

    private void renderPlayerShadow(RenderWorldEvent e, Player ent, Color color) {
        Vec3 pos = MathUtils.lerpEntityPosVec(ent, e.getTickCounter().getGameTimeDeltaPartialTick(true));
        Vec3 pnt1, pnt2, pnt3, pnt4;

        Matrix4f mat = e.getMatrices().last().pose();
        BufferBuilder buf = RenderUtils.getBuffer(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        float rad = 1F;
        float dTheta = Mth.PI / 16; // i chose this because mc pixels are 1/16 of a block, if ur computer lags womp womp
        float dRad = rad / 16;
        for (float i = 0; i < Mth.TWO_PI; i += dTheta) {
            for (float r = dRad; r <= rad; r += dRad) {
                pnt1 = pos.add(r * Mth.cos(i), 0, r * Mth.sin(i));
                pnt1 = castShadowVertex(pnt1);

                pnt2 = pos.add((r + dRad) * Mth.cos(i), 0, (r + dRad) * Mth.sin(i));
                pnt2 = castShadowVertex(pnt2);

                pnt3 = pos.add((r + dRad) * Mth.cos(i + dTheta), 0, (r + dRad) * Mth.sin(i + dTheta));
                pnt3 = castShadowVertex(pnt3);

                pnt4 = pos.add(r * Mth.cos(i + dTheta), 0, r * Mth.sin(i + dTheta));
                pnt4 = castShadowVertex(pnt4);

                if (pnt1 == null || pnt2 == null || pnt3 == null || pnt4 == null)
                    continue;

                pnt1 = e.getOffsetPos(pnt1);
                pnt2 = e.getOffsetPos(pnt2);
                pnt3 = e.getOffsetPos(pnt3);
                pnt4 = e.getOffsetPos(pnt4);
                buf.addVertex(mat, (float) pnt1.x, (float) pnt1.y, (float) pnt1.z).setColor(color.getHexCustomAlpha(r / rad));
                buf.addVertex(mat, (float) pnt2.x, (float) pnt2.y, (float) pnt2.z).setColor(color.getHexCustomAlpha((r + dRad) / rad));
                buf.addVertex(mat, (float) pnt3.x, (float) pnt3.y, (float) pnt3.z).setColor(color.getHexCustomAlpha((r + dRad) / rad));
                buf.addVertex(mat, (float) pnt4.x, (float) pnt4.y, (float) pnt4.z).setColor(color.getHexCustomAlpha(r / rad));
            }
        }
        RenderUtils.drawBuffer(buf, RenderLayers.QUADS);
    }

    private Vec3 castShadowVertex(Vec3 pnt) {
        Level world = PlayerUtils.getWorld();
        double y = pnt.y + 0.001;

        if (!world.getBlockState(BlockPos.containing(pnt.x, y, pnt.z)).isAir())
            return null; // already in a block (no shadow calculations)

        while (y > -64 && world.getBlockState(BlockPos.containing(pnt.x, y, pnt.z)).isAir())
            y--;

        if (world.getBlockState(BlockPos.containing(pnt.x, y, pnt.z)).isAir())
            return null; // no blocks to cast shadow on
        else
            return new Vec3(pnt.x, Math.ceil(y) + 0.001, pnt.z); // adding 0.001 to prevent z-fighting
    }
}