package io.github.itzispyder.clickcrystals.modules.modules.rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.itzispyder.clickcrystals.events.EventHandler;
import io.github.itzispyder.clickcrystals.events.events.client.EntityDamageEvent;
import io.github.itzispyder.clickcrystals.events.events.networking.PacketReceiveEvent;
import io.github.itzispyder.clickcrystals.events.events.world.ClientTickEndEvent;
import io.github.itzispyder.clickcrystals.events.events.world.RenderWorldEvent;
import io.github.itzispyder.clickcrystals.gui.misc.Color;
import io.github.itzispyder.clickcrystals.modules.Categories;
import io.github.itzispyder.clickcrystals.modules.Module;
import io.github.itzispyder.clickcrystals.modules.ModuleSetting;
import io.github.itzispyder.clickcrystals.modules.modules.ListenerModule;
import io.github.itzispyder.clickcrystals.modules.modules.rendering.totemchams.ChamRagDoll;
import io.github.itzispyder.clickcrystals.modules.modules.rendering.totemchams.*;
import io.github.itzispyder.clickcrystals.modules.settings.SettingSection;
import io.github.itzispyder.clickcrystals.util.minecraft.PlayerUtils;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityEvent;
import net.minecraft.world.entity.player.Player;

import java.util.concurrent.ConcurrentLinkedQueue;

public class TotemChams extends ListenerModule {

    private final SettingSection scGeneral = getGeneralSection();
    public final ModuleSetting<RagDoll> ragDollState = scGeneral.add(createEnumSetting(RagDoll.class)
            .name("rag-doll-type")
            .description("How the rag doll should look like")
            .def(RagDoll.EXPLODING)
            .build());
    public final ModuleSetting<Boolean> showSelf = scGeneral.add(createBoolSetting()
            .name("show-self")
            .description("Render own totem chams")
            .def(true)
            .build()
    );
    public final ModuleSetting<Double> maxVelocity = scGeneral.add(createDoubleSetting()
            .name("max-velocity")
            .description("Max velocity of the flying parts")
            .visibleWhen(()-> ragDollState.getVal().usesMaxVel())
            .max(1.0)
            .min(0.0)
            .def(0.1)
            .build()
    );
    public final ModuleSetting<Double> gravity = scGeneral.add(createDoubleSetting()
            .name("gravity")
            .description("Gravity of the visuals")
            .visibleWhen(() -> ragDollState.getVal().usesGravity())
            .max(0.05)
            .min(0.0)
            .def(0.01)
            .decimalPlaces(2)
            .build()
    );
    public final ModuleSetting<Double> maxAge = scGeneral.add(createDoubleSetting()
            .name("max-age")
            .description("How long in seconds the visuals show for")
            .max(5.0)
            .min(3.0)
            .def(5.0)
            .build()
    );
    public final ModuleSetting<Color> color = scGeneral.add(createColorSetting()
            .name("color")
            .description("Color of the totem chams")
            .def(0xFFFF7D7D)
            .build()
    );
    private final SettingSection scExtra = createSettingSection("extra");
    public final ModuleSetting<Boolean> chamOnDamage = scExtra.add(createBoolSetting()
            .name("cham-on-damage")
            .description("Also display cham visuals on player damage")
            .def(false)
            .build()
    );

    private final ConcurrentLinkedQueue<ChamRagDoll<?>> ragDolls = new ConcurrentLinkedQueue<>();

    public TotemChams() {
        super("totem-chams", Categories.RENDER, "Renders a nice visual whenever a player's totem pops");
    }

    @EventHandler
    private void onEntityStatus(PacketReceiveEvent e) {
        if (PlayerUtils.invalid())
            return;
        if (!(e.getPacket() instanceof ClientboundEntityEventPacket packet))
            return;
        if (packet.getEventId() != EntityEvent.PROTECTED_FROM_DEATH)
            return;

        Entity entity = packet.getEntity(PlayerUtils.getWorld());
        if (entity instanceof Player player)
            if (player != mc.player || showSelf.getVal())
                ragDolls.add(ragDollState.getVal().get(player));
    }

    @EventHandler
    private void onEntityDamage(EntityDamageEvent e) {
        if (PlayerUtils.invalid() || !chamOnDamage.getVal())
            return;

        DamageSource source = e.getSource();
        Entity entity = e.getEntity();

        if (!e.isSelf() && source.getEntity() == PlayerUtils.player() && entity instanceof Player player)
            ragDolls.add(ragDollState.getVal().get(player));
    }

    @EventHandler
    private void onTick(ClientTickEndEvent e) {
        if (PlayerUtils.invalid())
            return;

        for (ChamRagDoll<?> doll : ragDolls) {
            if (doll.isAlive())
                doll.tick(gravity.getVal().floatValue(), maxVelocity.getVal().floatValue());
            else
                ragDolls.remove(doll);
        }
    }

    @EventHandler
    private void onRenderWorld(RenderWorldEvent e) {
        if (PlayerUtils.invalid())
            return;

        PoseStack matrices = e.getMatrices();
        float tickDelta = e.getTickCounter().getGameTimeDeltaPartialTick(true);

        for (ChamRagDoll<?> doll : ragDolls)
            doll.render(matrices, getColor(), tickDelta);
    }

    public int getColor() {
        return color.getVal().getHexCustomAlpha(0x40);
    }

    public enum RagDoll {
        EXPLODING(ExplodingChamRagDoll.class, true, true),
        FADING(FadingChamRagDoll.class, true, false),
        RISING(RisingChamRagDoll.class, false, false),
        VORTEX(VortexChamRagDoll.class, false, false);

        private final Class<? extends ChamRagDoll<?>> clazz;
        private final boolean usesGravity, usesMaxVel;

        RagDoll(Class<? extends ChamRagDoll<?>> clazz, boolean usesGravity, boolean usesMaxVel) {
            this.clazz = clazz;
            this.usesGravity = usesGravity;
            this.usesMaxVel = usesMaxVel;
        }

        public boolean usesGravity() {
            return usesGravity;
        }

        public boolean usesMaxVel() {
            return usesMaxVel;
        }

        public ChamRagDoll<?> get(Player player) {
            try {
                int maxAge = Module.get(TotemChams.class).maxAge.getVal().intValue() * 20;
                return clazz.getConstructor(Player.class, int.class).newInstance(player, maxAge);
            }
            catch (Exception e) {
                return null;
            }
        }
    }
}