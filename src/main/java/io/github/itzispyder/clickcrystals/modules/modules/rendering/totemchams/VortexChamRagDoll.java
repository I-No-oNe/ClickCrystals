package io.github.itzispyder.clickcrystals.modules.modules.rendering.totemchams;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.itzispyder.clickcrystals.modules.modules.rendering.totemchams.parts.ChamPart;
import io.github.itzispyder.clickcrystals.modules.modules.rendering.totemchams.parts.VortexChamPart;
import net.minecraft.world.entity.player.Player;

public class VortexChamRagDoll extends ChamRagDoll<VortexChamPart> {

    public VortexChamRagDoll(Player player, int maxAge) {
        super(player, maxAge);
    }

    @Override
    protected void initializeParts(Player player) {
        // spread the parts evenly around the vortex so they spiral out from different angles
        VortexChamPart head = new VortexChamPart(-ChamPart.B4, ChamPart.B12 + ChamPart.B12, -ChamPart.B4, ChamPart.B4, ChamPart.B12 + ChamPart.B12 + ChamPart.B8, ChamPart.B4, 0F);
        head.pitch = player.getXRot();

        parts.put("head", head);
        parts.put("bod", new VortexChamPart(-ChamPart.B4, ChamPart.B12, -ChamPart.B2, ChamPart.B4, ChamPart.B12 + ChamPart.B12, ChamPart.B2, 60F));
        parts.put("leftArm", new VortexChamPart(-ChamPart.B8, ChamPart.B12, -ChamPart.B2, -ChamPart.B4, ChamPart.B12 + ChamPart.B12, ChamPart.B2, 120F));
        parts.put("rightArm", new VortexChamPart(ChamPart.B8, ChamPart.B12, -ChamPart.B2, ChamPart.B4, ChamPart.B12 + ChamPart.B12, ChamPart.B2, 180F));
        parts.put("leftLeg", new VortexChamPart(-ChamPart.B4, ChamPart.B0, -ChamPart.B2, ChamPart.B0, ChamPart.B12, ChamPart.B2, 240F));
        parts.put("rightLeg", new VortexChamPart(ChamPart.B4, ChamPart.B0, -ChamPart.B2, ChamPart.B0, ChamPart.B12, ChamPart.B2, 300F));
    }

    @Override
    public void tick(float gravity, float maxVelocity) {
        float ageDelta = getAgeDelta();
        for (VortexChamPart part : parts.values())
            part.tick(gravity, ageDelta);
        age++;
    }

    @Override
    protected void renderPart(VortexChamPart part, PoseStack matrices, int color, float tickDelta) {
        part.render(matrices, color, tickDelta, age);
    }
}
