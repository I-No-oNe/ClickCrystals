package io.github.itzispyder.clickcrystals.modules.modules.clickcrystals;

import io.github.itzispyder.clickcrystals.events.EventHandler;
import io.github.itzispyder.clickcrystals.events.Listener;
import io.github.itzispyder.clickcrystals.events.events.PacketReceiveEvent;
import io.github.itzispyder.clickcrystals.events.events.PacketSendEvent;
import io.github.itzispyder.clickcrystals.modules.Categories;
import io.github.itzispyder.clickcrystals.modules.Module;
import io.github.itzispyder.clickcrystals.modules.settings.*;

import static io.github.itzispyder.clickcrystals.ClickCrystals.packetLogger;

public class BlankModule extends Module implements Listener {

    private final SettingSection scGeneral = getGeneralSection(); // this is the default setting section
    private final SettingSection scExample = createSettingSection("hello-world"); // this is a new setting section
    private final SettingSection scTest = createSettingSection("test-settings-and-features");
    public final ModuleSetting<Boolean> boolSetting = scGeneral.add(BooleanSetting.create() // this is a new setting
            .name("boolean-setting")
            .description("This is a boolean setting.")
            .def(false)
            .build()
    );
    public final ModuleSetting<String> strSetting = scGeneral.add(StringSetting.create()
            .name("string-setting")
            .description("This is a string setting.")
            .def("This is the default text. :)")
            .build()
    );
    public final ModuleSetting<Integer> intSettingNoMin = scGeneral.add(IntegerSetting.create()
            .max(10)
            .min(0)
            .name("integer-setting")
            .description("This is an integer setting.")
            .def(4)
            .build()
    );
    public final ModuleSetting<Integer> intSettingWithMin = scGeneral.add(IntegerSetting.create()
            .max(30)
            .min(5)
            .name("integer-setting-with-minimum")
            .description("This is an integer setting with a minimum value.")
            .def(10)
            .build()
    );
    public final ModuleSetting<Double> doubleSettingTwoDecimal = scExample.add(DoubleSetting.create()
            .max(101.21)
            .min(75.43)
            .decimalPlaces(2)
            .name("double-setting-two-decimals")
            .description("This is a double setting with two decimal places.")
            .def(87.34)
            .build()
    );
    public final ModuleSetting<Double> doubleSettingOneDecimal = scExample.add(DoubleSetting.create()
            .max(34.7)
            .decimalPlaces(1)
            .name("double-setting-one-decimals")
            .description("This is a double setting with one decimal places.")
            .def(5.67)
            .build()
    );
    /*
    public final ModuleSetting<Boolean> showPackets = scTest.add(BooleanSetting.create() // this is a new setting
            .name("show-packet-interactions")
            .description("Shows packets interactions and debug stuff.")
            .def(false)
            .build()
    );
     */

    public BlankModule() {
        super("test-module", Categories.CLICKCRYSTALS, "This module does nothing, serves as a testing purpose for development.");
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
    private void onPacketReceive(PacketReceiveEvent e) {
        packetLogger.log(e.getPacket(), false);
    }

    @EventHandler
    private void onPacketSend(PacketSendEvent e) {
        packetLogger.log(e.getPacket(), false);
    }
}