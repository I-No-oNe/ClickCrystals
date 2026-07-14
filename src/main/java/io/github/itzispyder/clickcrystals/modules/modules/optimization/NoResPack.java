package io.github.itzispyder.clickcrystals.modules.modules.optimization;

import io.github.itzispyder.clickcrystals.events.EventHandler;
import io.github.itzispyder.clickcrystals.events.Listener;
import io.github.itzispyder.clickcrystals.events.events.networking.PacketReceiveEvent;
import io.github.itzispyder.clickcrystals.modules.Categories;
import io.github.itzispyder.clickcrystals.modules.Module;
import io.github.itzispyder.clickcrystals.util.minecraft.ChatUtils;
import io.github.itzispyder.clickcrystals.util.minecraft.PlayerUtils;
import net.minecraft.network.protocol.common.ClientboundResourcePackPushPacket;
import net.minecraft.network.protocol.common.ServerboundResourcePackPacket;
import net.minecraft.network.protocol.common.ServerboundResourcePackPacket.Action;

import java.util.UUID;

public class NoResPack extends Module implements Listener {

    public NoResPack() {
        super("no-server-packs", Categories.LAG,"Prevents servers from forcing you to download their bad resource pack");
    }

    @Override
    protected void onEnable() {
        system.addListener(this);
    }

    @Override
    protected void onDisable() {
        system.removeListener(this);
    }

    @EventHandler
    private void onResourceReceive(PacketReceiveEvent e) {
        if (!(e.getPacket() instanceof ClientboundResourcePackPushPacket packet) || PlayerUtils.invalid())
            return;

        // drop the pack (never download it), but spoof the full accept/load handshake so the
        // server thinks we loaded it and doesn't hang or kick us off forced packs
        e.setCancelled(true);
        UUID id = packet.id();
        respond(id, Action.ACCEPTED);
        respond(id, Action.DOWNLOADED);
        respond(id, Action.SUCCESSFULLY_LOADED);

        String status = packet.required() ? "forced" : "suggested";
        ChatUtils.sendPrefixMessage("Blocked 1 " + status + " resource pack");
    }

    private void respond(UUID id, Action action) {
        PlayerUtils.player().connection.send(new ServerboundResourcePackPacket(id, action));
    }
}
