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

// @Format draw_line_3d <x1> <y1> <z1> <x2> <y2> <z2> <color>
public class DrawLine3dCmd extends ScriptCommand {

    public DrawLine3dCmd() {
        super("draw_line_3d");
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

            VectorParser vp1 = new VectorParser(x1, y1, z1, PlayerUtils.player());
            VectorParser vp2 = new VectorParser(x2, y2, z2, PlayerUtils.player());

            Vec3 offset1 = RenderWorldEvent.currentEvent.getOffsetPos(vp1.getVector());
            Vec3 offset2 = RenderWorldEvent.currentEvent.getOffsetPos(vp2.getVector());

            RenderUtils3d.drawLine(
                    RenderWorldEvent.currentEvent.getMatrices(),
                    offset1.x, offset1.y, offset1.z,
                    offset2.x, offset2.y, offset2.z,
                    color
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
