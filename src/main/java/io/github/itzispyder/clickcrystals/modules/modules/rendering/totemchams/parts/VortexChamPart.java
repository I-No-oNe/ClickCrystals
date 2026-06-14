package io.github.itzispyder.clickcrystals.modules.modules.rendering.totemchams.parts;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.itzispyder.clickcrystals.util.MathUtils;
import io.github.itzispyder.clickcrystals.util.minecraft.render.RenderUtils3d;
import org.joml.Quaternionf;

// Vortex: the part spirals outward and upward around the doll's axis while spinning.
public class VortexChamPart extends ChamPart {

    private final float spinSpeed, riseSpeed;
    private float angle, radius;

    public VortexChamPart(float minX, float minY, float minZ, float maxX, float maxY, float maxZ, float startAngleDeg) {
        super(minX, minY, minZ, maxX, maxY, maxZ);
        this.angle = (float) Math.toRadians(startAngleDeg);
        this.spinSpeed = 0.18F + (float) Math.random() * 0.06F;
        this.riseSpeed = 0.012F + (float) Math.random() * 0.006F;
    }

    @Override
    public void tick(float gravity, float ageDelta) {
        prevX = x;
        prevY = y;
        prevZ = z;
        prevPitch = pitch;
        prevYaw = yaw;

        angle += spinSpeed;
        radius += 0.012F;

        x = (float) (Math.cos(angle) * radius);
        z = (float) (Math.sin(angle) * radius);
        y += riseSpeed;

        yaw += 15F;
        pitch += 8F;
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
