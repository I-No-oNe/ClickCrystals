package io.github.itzispyder.clickcrystals.mixins;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import io.github.itzispyder.clickcrystals.Global;
import io.github.itzispyder.clickcrystals.events.events.world.RenderWorldEvent;
import io.github.itzispyder.clickcrystals.modules.Module;
import io.github.itzispyder.clickcrystals.modules.modules.rendering.BlockOutline;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.chunk.ChunkSectionsToRender;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(LevelRenderer.class)
public abstract class MixinLevelRenderer implements Global {

    @ModifyArgs(method = "renderHitOutline", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ShapeRenderer;renderShape(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;Lnet/minecraft/world/phys/shapes/VoxelShape;DDDIF)V"))
    public void setOutlineColor(Args args) {
        BlockOutline bo = Module.get(BlockOutline.class);

        if (bo.isEnabled()) {
            args.set(6, bo.color.getVal().getHexOpaque());
        }
    }

    @Inject(method = "renderLevel", at = @At("TAIL"))
    public void render(GraphicsResourceAllocator resourceAllocator, DeltaTracker deltaTracker, boolean renderOutline, CameraRenderState cameraState, Matrix4fc modelViewMatrix, GpuBufferSlice terrainFog, Vector4f fogColor, boolean shouldRenderSky, ChunkSectionsToRender chunkSectionsToRender, CallbackInfo ci) {
        Matrix4f positionMatrix = new Matrix4f(modelViewMatrix);
        Matrix4f projectionMatrix = new Matrix4f();
        mc.gameRenderer.mainCamera().getViewRotationProjectionMatrix(projectionMatrix);

        RenderWorldEvent event = new RenderWorldEvent(mc.gameRenderer, positionMatrix, projectionMatrix, deltaTracker);
        system.eventBus.pass(event);
    }
}