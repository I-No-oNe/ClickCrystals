package io.github.itzispyder.clickcrystals.modules.modules.rendering;

import io.github.itzispyder.clickcrystals.events.EventHandler;
import io.github.itzispyder.clickcrystals.events.events.networking.PacketReceiveEvent;
import io.github.itzispyder.clickcrystals.modules.Categories;
import io.github.itzispyder.clickcrystals.modules.ModuleSetting;
import io.github.itzispyder.clickcrystals.modules.modules.ListenerModule;
import io.github.itzispyder.clickcrystals.modules.settings.DoubleSetting;
import io.github.itzispyder.clickcrystals.modules.settings.EnumSetting;
import io.github.itzispyder.clickcrystals.modules.settings.SettingSection;
import net.minecraft.client.world.ClientWorld;
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

    public final ModuleSetting<Double> particleVelocity = scGeneral.add(DoubleSetting.create()
            .name("particles-velocity")
            .description("Set the speed of the particles.")
            .def(1.0)
            .min(1.0)
            .max(6.0)
            .build()
    );

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

        ClientWorld w = mc.world;
        ParticleEffect p = particlesType.getVal().getParticleEffect();
        var v = particleVelocity.getVal();
        BlockPos e = entity.getBlockPos();
        w.addImportantParticle(p, e.getX(), e.getY(), e.getZ(), system.random.getRandomDouble(-v, v), system.random.getRandomDouble(v * 0.5), system.random.getRandomDouble(-v, v));
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
        TOTEM(ParticleTypes.TOTEM_OF_UNDYING),
        FIREWORK(ParticleTypes.FIREWORK),
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