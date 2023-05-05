package io.github.itzispyder.clickcrystals;

import io.github.itzispyder.clickcrystals.client.CCKeybindings;
import io.github.itzispyder.clickcrystals.client.ClickCrystalsSystem;
import io.github.itzispyder.clickcrystals.commands.commands.*;
import io.github.itzispyder.clickcrystals.data.Configuration;
import io.github.itzispyder.clickcrystals.events.events.ClientTickEndEvent;
import io.github.itzispyder.clickcrystals.events.events.ClientTickStartEvent;
import io.github.itzispyder.clickcrystals.events.listeners.ChatEventListener;
import io.github.itzispyder.clickcrystals.events.listeners.NetworkEventListener;
import io.github.itzispyder.clickcrystals.events.listeners.TickEventListener;
import io.github.itzispyder.clickcrystals.gui.hud.ClickCrystalIconHud;
import io.github.itzispyder.clickcrystals.gui.hud.ClickPerSecondHud;
import io.github.itzispyder.clickcrystals.gui.hud.ModuleListTextHud;
import io.github.itzispyder.clickcrystals.gui.screens.ClickCrystalMenuScreen;
import io.github.itzispyder.clickcrystals.gui.screens.ClickCrystalsModuleScreen;
import io.github.itzispyder.clickcrystals.modules.Module;
import io.github.itzispyder.clickcrystals.modules.modules.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;

import java.io.File;

/**
 * ClickCrystals main
 */
public final class ClickCrystals implements ModInitializer {

    public static final File configFile = new File("ClickCrystalsClient/game_config.dat");
    public static final Configuration config = Configuration.load(configFile);
    public static final MinecraftClient mc = MinecraftClient.getInstance();
    public static final ClickCrystalsSystem system = new ClickCrystalsSystem();
    public static final ClickCrystalsModuleScreen CC_MODULE_SCREEN = new ClickCrystalsModuleScreen();
    @Deprecated public static final ClickCrystalMenuScreen CC_MENU_SCREEN = new ClickCrystalMenuScreen();

    @SuppressWarnings("unused")
    public static final String
            MOD_ID = "clickcrystals",
            PREFIX = "[ClickCrystals] ",
            STARTER = "§7[§bClick§3Crystals§7] §r";

    /**
     * Runs the mod initializer.
     */
    @Override
    public void onInitialize() {
        // Mod initialization
        System.out.println(PREFIX + "Loading ClickCrystals by ImproperIssues");
        this.init();
        CCKeybindings.init();
        this.startTicking();
    }

    /**
     * Start click tick events
     */
    public void startTicking() {
        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            ClientTickStartEvent event = new ClientTickStartEvent();
            system.eventBus.pass(event);
        });
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            ClientTickEndEvent event = new ClientTickEndEvent();
            system.eventBus.pass(event);
        });
    }

    public void init() {
        // Listeners
        system.addListener(new ChatEventListener());
        system.addListener(new NetworkEventListener());
        system.addListener(new TickEventListener());

        // Commands
        system.addCommand(new ClickCrystalToggleCommand());
        system.addCommand(new GmcCommand());
        system.addCommand(new GmsCommand());
        system.addCommand(new GmaCommand());
        system.addCommand(new GmspCommand());

        // Module
        system.addModule(new ClickCrystal());
        system.addModule(new AnchorSwitch());
        system.addModule(new PearlSwitchS());
        system.addModule(new BreakDelay());
        system.addModule(new FullBright());
        system.addModule(new NoHurtCam());
        system.addModule(new SlowSwing());
        system.addModule(new TrueSight());
        system.addModule(new NoLoading());
        system.addModule(new NoOverlay());
        system.addModule(new ObiSwitch());
        system.addModule(new CrystalSwitch());
        system.addModule(new AntiCrash());
        system.addModule(new PearlSwitchT());
        system.addModule(new NoResPack());
        system.addModule(new ToolSwitcher());
        system.addModule(new CrystAnchor());
        system.addModule(new IconHud());
        system.addModule(new ModulesList());
        system.addModule(new SilkTouch());
        system.addModule(new TotemPops());
        system.addModule(new CrystPerSec());
        Module.loadConfigModules();

        // Hud
        system.addHud(new ClickCrystalIconHud());
        system.addHud(new ModuleListTextHud());
        system.addHud(new ClickPerSecondHud());
    }

    public void initRpc() {

    }
}
