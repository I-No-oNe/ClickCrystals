package io.github.itzispyder.clickcrystals.mixins;

import io.github.itzispyder.clickcrystals.gui.misc.brushes.MobHeadBrush;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityTypes.class)
public abstract class MixinEntityType {

    @Inject(method = "register(Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/world/entity/EntityType$Builder;)Lnet/minecraft/world/entity/EntityType;", at = @At("RETURN"))
    private static <T extends Entity> void register(ResourceKey<EntityType<?>> key, EntityType.Builder<T> builder, CallbackInfoReturnable<EntityType<T>> cir) {
        MobHeadBrush.init(cir.getReturnValue(), key.identifier().getPath());
    }
}