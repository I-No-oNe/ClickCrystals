package io.github.itzispyder.clickcrystals.modules.modules.rendering.totemchams;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.itzispyder.clickcrystals.modules.modules.rendering.totemchams.parts.ChamPart;
import io.github.itzispyder.clickcrystals.modules.modules.rendering.totemchams.parts.RisingChamPart;
import net.minecraft.world.entity.player.Player;

public class RisingChamRagDoll extends ChamRagDoll<RisingChamPart> {

    public RisingChamRagDoll(Player player, int maxAge) {
        super(player, maxAge);
    }

    @Override
    protected void initializeParts(Player player) {
        RisingChamPart head = new RisingChamPart(-ChamPart.B4, ChamPart.B12 + ChamPart.B12, -ChamPart.B4, ChamPart.B4, ChamPart.B12 + ChamPart.B12 + ChamPart.B8, ChamPart.B4, 0.0F);
        head.pitch = player.getXRot();

        parts.put("head", head);
        parts.put("bod", new RisingChamPart(-ChamPart.B4, ChamPart.B12, -ChamPart.B2, ChamPart.B4, ChamPart.B12 + ChamPart.B12, ChamPart.B2, 0.05F));
        parts.put("leftArm", new RisingChamPart(-ChamPart.B8, ChamPart.B12, -ChamPart.B2, -ChamPart.B4, ChamPart.B12 + ChamPart.B12, ChamPart.B2, 0.1F));
        parts.put("rightArm", new RisingChamPart(ChamPart.B8, ChamPart.B12, -ChamPart.B2, ChamPart.B4, ChamPart.B12 + ChamPart.B12, ChamPart.B2, 0.1F));
        parts.put("leftLeg", new RisingChamPart(-ChamPart.B4, ChamPart.B0, -ChamPart.B2, ChamPart.B0, ChamPart.B12, ChamPart.B2, 0.15F));
        parts.put("rightLeg", new RisingChamPart(ChamPart.B4, ChamPart.B0, -ChamPart.B2, ChamPart.B0, ChamPart.B12, ChamPart.B2, 0.15F));
    }

    @Override
    public void tick(float gravity, float maxVelocity) {
        float ageDelta = getAgeDelta();
        for (RisingChamPart part : parts.values())
            part.tick(gravity, ageDelta);
        age++;
    }

    @Override
    protected void renderPart(RisingChamPart part, PoseStack matrices, int color, float tickDelta) {
        part.render(matrices, color, tickDelta, age);
    }
}
