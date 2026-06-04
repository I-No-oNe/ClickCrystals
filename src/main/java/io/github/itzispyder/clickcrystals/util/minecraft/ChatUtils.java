package io.github.itzispyder.clickcrystals.util.minecraft;

import io.github.itzispyder.clickcrystals.Global;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

import java.util.Optional;

public final class ChatUtils implements Global {

    private static Runnable pendingClickAction;

    public static void sendMessage(String message) {
        if (message != null && PlayerUtils.valid()) {
            PlayerUtils.player().sendSystemMessage(Component.literal(message));
        }
    }

    public static void sendPrefixMessage(String message) {
        sendMessage(starter + message);
    }

    public static void sendWarningMessage(String message) {
        sendMessage(starter + "§8(§eWarning§8)§r " + message);
    }

    public static void sendRawText(Component text) {
        if (PlayerUtils.valid() && text != null) {
            PlayerUtils.player().sendSystemMessage(text);
        }
    }

    public static void sendSevereMessage(String message) {
        sendMessage(starter + "§8(§c§lError§8)§r " + message);
    }

    public static void sendChatCommand(String cmd) {
        if (PlayerUtils.valid()) {
            PlayerUtils.player().connection.sendCommand(cmd);
        }
    }

    public static void sendChatMessage(String msg) {
        if (PlayerUtils.valid()) {
            PlayerUtils.player().connection.sendChat(msg);
        }
    }

    public static void sendBlank(int lines) {
        for (int i = 0; i < lines; i++) {
            sendMessage("");
        }
    }

    public static void sendBlank() {
        sendBlank(1);
    }

    public static void pingPlayer() {
        SoundManager sm = mc.getSoundManager();
        SoundEvent event = SoundEvents.EXPERIENCE_ORB_PICKUP;
        SoundInstance sound = SimpleSoundInstance.forUI(event, 10.0F, 0.1F);
        mc.execute(() -> sm.play(sound));
    }

    public static void sendClickableMessage(String message, Runnable onClick) {
        pendingClickAction = onClick;

        Component component = Component.literal(message).withStyle(style ->
                style.withClickEvent(new ClickEvent.Custom(
                        Identifier.fromNamespaceAndPath(modId, "click"),
                        Optional.empty()
                ))
        );

        PlayerUtils.player().sendSystemMessage(component);
    }

    public static void handleCustomClick() {
        Runnable action = pendingClickAction;
        pendingClickAction = null;

        if (action != null) action.run();
    }
}