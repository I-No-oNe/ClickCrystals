package io.github.itzispyder.clickcrystals.modules.modules.rendering;

import io.github.itzispyder.clickcrystals.events.EventHandler;
import io.github.itzispyder.clickcrystals.events.events.networking.PacketReceiveEvent;
import io.github.itzispyder.clickcrystals.modules.Categories;
import io.github.itzispyder.clickcrystals.modules.ModuleSetting;
import io.github.itzispyder.clickcrystals.modules.modules.ListenerModule;
import io.github.itzispyder.clickcrystals.modules.settings.EnumSetting;
import io.github.itzispyder.clickcrystals.modules.settings.SettingSection;
import io.github.itzispyder.clickcrystals.util.minecraft.PlayerUtils;
import net.minecraft.client.option.ParticlesMode;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.BlockPos;

public class DeathParticles extends ListenerModule {

    private final SettingSection scGeneral = getGeneralSection();
    public final ModuleSetting<Entities> entitySelection = scGeneral.add(EnumSetting.create(Entities.class)
            .name("entity-selection")
            .description("Choose which entity will have this effect.")
            .def(Entities.BOTH)
            .build()
    );
    public final ModuleSetting<Particles> particlesType = scGeneral.add(EnumSetting.create(Particles.class)
            .name("particle-type")
            .description("Choose the particle effect shown when the entity dies.")
            .def(Particles.FLASH)
            .build()
    );
    public final ModuleSetting<Boolean> moduleFix = scGeneral.add(createBoolSetting()
            .name("turn-on-particles")
            .description("Turn automatically on every particles so the module will work right")
            .def(true)
            .build()
    );

    ParticlesMode particlesOldSetting;

    @Override
    public void onEnable(){
        if (PlayerUtils.invalid()) return;
        if (moduleFix.getVal()) mc.options.getParticles().setValue(ParticlesMode.ALL);
    }

    @Override
    public void onDisable(){
        if (PlayerUtils.invalid()) return;
        particlesOldSetting = mc.options.getParticles().getValue();
        if (moduleFix.getVal() && particlesOldSetting != null) mc.options.getParticles().setValue(particlesOldSetting);
    }

    @EventHandler
    private void onReceivePacket(PacketReceiveEvent event) {
        if (!(event.getPacket() instanceof EntityStatusS2CPacket packet))
            return;
        if (packet.getStatus() != 3)
            return;

        Entity entity = packet.getEntity(mc.player.getWorld());
        if (!shouldApplyEffect(entity)) {
            return;
        }

        ParticleEffect p = particlesType.getVal().getParticleEffect();
        BlockPos e = entity.getBlockPos();
        mc.world.addParticle(p, e.getX(), e.getY(), e.getZ(), 0, 0, 0);
    }

    public boolean shouldApplyEffect(Entity entity) {
        return switch (entitySelection.getVal()) {
            case PLAYERS -> entity instanceof PlayerEntity;
            case ENTITIES -> !(entity instanceof PlayerEntity);
            case BOTH -> true;
        };
    }

    public DeathParticles() {
        super("death-particles", Categories.RENDER, "Spawn particles upon entity death");
    }

    public enum Entities {
        PLAYERS,
        ENTITIES,
        BOTH
    }
    public enum Particles {
        TOTEM(ParticleTypes.ELECTRIC_SPARK),
        FIREWORK(ParticleTypes.SOUL_FIRE_FLAME),
        BIG_EXPLOSION(ParticleTypes.EXPLOSION_EMITTER),
        SMALL_EXPLOSION(ParticleTypes.EXPLOSION),
        FLASH(ParticleTypes.FLASH),
        WARDEN_BEAM(ParticleTypes.SONIC_BOOM);

        private final ParticleEffect particleEffect;

        Particles(ParticleEffect particleEffect) {
            this.particleEffect = particleEffect;
        }

        public ParticleEffect getParticleEffect() {
            return particleEffect;
        }
    }
}