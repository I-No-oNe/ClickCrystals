package io.github.itzispyder.clickcrystals.events.events.client;

import io.github.itzispyder.clickcrystals.events.Event;
import io.github.itzispyder.clickcrystals.util.minecraft.PlayerUtils;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class Render2dEvent extends Event {

    public static Render2dEvent currentEvent;

    private final GuiGraphicsExtractor context;
    private final DeltaTracker tickCounter;

    public Render2dEvent(GuiGraphicsExtractor context, DeltaTracker tickCounter) {
        this.context = context;
        this.tickCounter = tickCounter;
    }

    public GuiGraphicsExtractor getContext() {
        return context;
    }

    public DeltaTracker getTickCounter() {
        return tickCounter;
    }

    public boolean valid() {
        return PlayerUtils.valid();
    }

    public boolean invalid() {
        return PlayerUtils.invalid();
    }
}
