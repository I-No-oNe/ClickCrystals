package io.github.itzispyder.clickcrystals.scripting.syntax.rendering;

import io.github.itzispyder.clickcrystals.events.events.world.RenderWorldEvent;
import io.github.itzispyder.clickcrystals.gui.misc.Color;
import io.github.itzispyder.clickcrystals.scripting.ScriptArgs;
import io.github.itzispyder.clickcrystals.scripting.ScriptArgsReader;
import io.github.itzispyder.clickcrystals.scripting.ScriptCommand;
import io.github.itzispyder.clickcrystals.util.minecraft.PlayerUtils;
import io.github.itzispyder.clickcrystals.util.minecraft.VectorParser;
import io.github.itzispyder.clickcrystals.util.minecraft.render.RenderUtils3d;
import net.minecraft.world.phys.Vec3;

// @Format fill_box <x1> <y1> <z1> <x2> <y2> <z2> <color> [cull]
public class FillBoxCmd extends ScriptCommand {

    public FillBoxCmd() {
        super("fill_box");
    }

    @Override
    public void onCommand(ScriptCommand command, String line, ScriptArgs args) {
        if (RenderWorldEvent.currentEvent == null) {
            return;
        }

        try {
            ScriptArgsReader sar = args.getReader();
            String x1 = sar.nextStr();
            String y1 = sar.nextStr();
            String z1 = sar.nextStr();
            String x2 = sar.nextStr();
            String y2 = sar.nextStr();
            String z2 = sar.nextStr();
            int color = Color.parse(sar.nextStr()).getHex();

            boolean cull = false;
            int remaining = args.getSize() - sar.getIndex();
            if (remaining > 0) {
                String possibleThen = args.get(sar.getIndex()).toString();
                if (!possibleThen.equalsIgnoreCase("then")) {
                    cull = Boolean.parseBoolean(sar.nextStr());
                }
            }

            VectorParser vp1 = new VectorParser(x1, y1, z1, PlayerUtils.player());
            VectorParser vp2 = new VectorParser(x2, y2, z2, PlayerUtils.player());

            Vec3 offset1 = RenderWorldEvent.currentEvent.getOffsetPos(vp1.getVector());
            Vec3 offset2 = RenderWorldEvent.currentEvent.getOffsetPos(vp2.getVector());

            RenderUtils3d.fillRectPrism(
                    RenderWorldEvent.currentEvent.getMatrices(),
                    (float) offset1.x, (float) offset1.y, (float) offset1.z,
                    (float) offset2.x, (float) offset2.y, (float) offset2.z,
                    color,
                    cull
            );
        } catch (Exception ignored) {}

        ScriptArgsReader reader = args.getReader();
        while (reader.getIndex() < args.getSize()) {
            if (reader.nextStr().equalsIgnoreCase("then")) {
                args.executeAll(reader.getIndex());
                break;
            }
        }
    }
}
