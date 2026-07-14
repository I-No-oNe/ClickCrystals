package io.github.itzispyder.clickcrystals.modules.modules.rendering.totemchams.parts;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.itzispyder.clickcrystals.util.MathUtils;
import io.github.itzispyder.clickcrystals.util.minecraft.render.RenderUtils3d;
import org.joml.Quaternionf;

// Soul-like rise: the part drifts upward while gently swaying and slowly spinning.
public class RisingChamPart extends ChamPart {

    private final float delay, sway, swayPhase, riseSpeed;
    private float progress;

    public RisingChamPart(float minX, float minY, float minZ, float maxX, float maxY, float maxZ, float delay) {
        super(minX, minY, minZ, maxX, maxY, maxZ);
        this.delay = delay;
        this.sway = 0.01F + (float) Math.random() * 0.01F;
        this.swayPhase = (float) (Math.random() * Math.PI * 2);
        this.riseSpeed = 0.012F + (float) Math.random() * 0.008F;
    }

    @Override
    public void tick(float gravity, float ageDelta) {
        progress = ageDelta;
        if (ageDelta < delay)
            return;

        prevX = x;
        prevY = y;
        prevZ = z;
        prevPitch = pitch;
        prevYaw = yaw;

        y += riseSpeed;
        x += (float) Math.sin(ageDelta * Math.PI * 6 + swayPhase) * sway;
        z += (float) Math.cos(ageDelta * Math.PI * 5 + swayPhase) * sway;
        yaw += 1.5F;
        pitch += 0.6F;
    }

    public Quaternionf getRotation(float tickDelta) {
        float pitch = (float) MathUtils.lerp(prevPitch, this.pitch, tickDelta);
        float yaw = (float) MathUtils.lerp(prevYaw, this.yaw, tickDelta);

        Quaternionf qPitch = new Quaternionf().rotationX((float) Math.toRadians(pitch));
        Quaternionf qYaw = new Quaternionf().rotationY((float) Math.toRadians(yaw));
        return qYaw.mul(qPitch);
    }

    @Override
    public void render(PoseStack matrices, int color, float tickDelta, int age) {
        float x = (float) MathUtils.lerp(prevX, this.x, tickDelta);
        float y = (float) MathUtils.lerp(prevY, this.y, tickDelta);
        float z = (float) MathUtils.lerp(prevZ, this.z, tickDelta);

        float cx = minX + x + (maxX - minX) / 2;
        float cy = minY + y + (maxY - minY) / 2;
        float cz = minZ + z + (maxZ - minZ) / 2;

        matrices.pushPose();
        matrices.rotateAround(getRotation(tickDelta), cx, cy, cz);
        matrices.translate(x, y, z);

        RenderUtils3d.fillRectPrism(matrices, minX, minY, minZ, maxX, maxY, maxZ, color, true);

        int alpha = Math.min((color >> 24 & 0xFF) * 5, 0xFF);
        int outline = (alpha << 24) | (color & 0x00FFFFFF);
        RenderUtils3d.drawRectPrism(matrices, minX, minY, minZ, maxX, maxY, maxZ, outline, true);

        matrices.popPose();
    }
}
