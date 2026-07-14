package io.github.itzispyder.clickcrystals.scripting.syntax;

import io.github.itzispyder.clickcrystals.Global;
import io.github.itzispyder.clickcrystals.mixininterfaces.AccessorMouseHandler;
import io.github.itzispyder.clickcrystals.util.minecraft.InteractionUtils;
import io.github.itzispyder.clickcrystals.util.minecraft.PlayerUtils;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;

import java.util.function.BooleanSupplier;
import java.util.function.LongConsumer;

public enum InputType implements Global {

    ATTACK(InteractionUtils::inputAttack, InteractionUtils::inputAttack, mc.options.keyAttack::isDown, mc.options.keyAttack::setDown),
    USE(InteractionUtils::inputUse, InteractionUtils::inputUse, mc.options.keyUse::isDown, mc.options.keyUse::setDown),
    FORWARD(InteractionUtils::inputForward, InteractionUtils::inputForward, mc.options.keyUp::isDown, mc.options.keyUp::setDown),
    BACKWARD(InteractionUtils::inputBackward, InteractionUtils::inputBackward, mc.options.keyDown::isDown, mc.options.keyDown::setDown),
    STRAFE_LEFT(InteractionUtils::inputStrafeLeft, InteractionUtils::inputStrafeLeft, mc.options.keyLeft::isDown, mc.options.keyLeft::setDown),
    STRAFE_RIGHT(InteractionUtils::inputStrafeRight, InteractionUtils::inputStrafeRight, mc.options.keyRight::isDown, mc.options.keyRight::setDown),
    JUMP(InteractionUtils::inputJump, InteractionUtils::inputJump, mc.options.keyJump::isDown, mc.options.keyJump::setDown),
    SPRINT(InteractionUtils::inputToggleSprint, null, mc.options.keySprint::isDown, mc.options.keySprint::setDown),
    SNEAK(InteractionUtils::inputSneak, null, mc.options.keyShift::isDown, mc.options.keyShift::setDown),
    LOCK_CURSOR(system.cameraRotator::lockCursor, null, system.cameraRotator::isCursorLocked, system.cameraRotator::setCursorLockState),
    UNLOCK_CURSOR(system.cameraRotator::unlockCursor, null, () -> !system.cameraRotator.isCursorLocked(), system.cameraRotator::setCursorLockState),
    LEFT(InteractionUtils::leftClick, null, mc.mouseHandler::isLeftPressed, ((AccessorMouseHandler) mc.mouseHandler)::clickCrystals$toggleLeft),
    RIGHT(InteractionUtils::rightClick, null, mc.mouseHandler::isRightPressed, ((AccessorMouseHandler) mc.mouseHandler)::clickCrystals$toggleRight),
    MIDDLE(InteractionUtils::middleClick, null, mc.mouseHandler::isMiddlePressed, ((AccessorMouseHandler) mc.mouseHandler)::clickCrystals$toggleMiddle),
    CONTAINER(InteractionUtils::inputInventory, null, () -> mc.gui.screen() instanceof AbstractContainerScreen<?>, InteractionUtils::toggleInventory),
    INVENTORY(InteractionUtils::inputInventory, null, () -> mc.gui.screen() instanceof InventoryScreen || mc.gui.screen() instanceof CreativeModeInventoryScreen, InteractionUtils::toggleInventory),
    MOUSE_WHEEL_UP(() -> InteractionUtils.mouseScroll(1), null, () -> false, _ -> {}),
    MOUSE_WHEEL_DOWN(() -> InteractionUtils.mouseScroll(-1), null, () -> false, _ -> {}),
    KEY(null, null, null, null);

    private final Runnable action;
    private final LongConsumer holdAction;
    private final BooleanSupplier isActive;
    private final BooleanConsumer toggleAction;

    InputType(Runnable action, LongConsumer holdAction, BooleanSupplier isActive, BooleanConsumer toggleAction) {
        this.action = action;
        this.isActive = isActive;
        this.holdAction = holdAction;
        this.toggleAction = toggleAction;
    }

    public boolean isActive() {
        return !isDummy() && isActive.getAsBoolean();
    }

    public boolean isDummy() {
        return action == null || isActive == null;
    }

    public BooleanConsumer getToggleAction() {
        return toggleAction;
    }

    public void run() {
        if (isDummy() || PlayerUtils.invalid())
            return;
        action.run();
    }

    public void runFor(long ms) {
        if (isDummy() || PlayerUtils.invalid())
            return;

        if (holdAction != null)
            holdAction.accept(ms);
        else
            run();
    }

    public void toggle(boolean toggle) {
        if (PlayerUtils.invalid())
            return;

        if (toggleAction != null)
            toggleAction.accept(toggle);
    }
}
