package io.github.itzispyder.clickcrystals.scripting.syntax.rendering;

import io.github.itzispyder.clickcrystals.events.events.client.Render2dEvent;
import io.github.itzispyder.clickcrystals.gui.misc.Color;
import io.github.itzispyder.clickcrystals.scripting.ScriptArgs;
import io.github.itzispyder.clickcrystals.scripting.ScriptCommand;
import io.github.itzispyder.clickcrystals.util.minecraft.render.RenderUtils;

// @Format fill_circle <x> <y> <radius> <color>
public class FillCircleCmd extends ScriptCommand {

    public FillCircleCmd() {
        super("fill_circle");
    }

    @Override
    public void onCommand(ScriptCommand command, String line, ScriptArgs args) {
        if (Render2dEvent.currentEvent == null) {
            return;
        }

        try {
            int x = (int) Math.round(RenderParser.parseVal(args.get(0).toString()));
            int y = (int) Math.round(RenderParser.parseVal(args.get(1).toString()));
            int r = (int) Math.round(RenderParser.parseVal(args.get(2).toString()));
            int color = Color.parse(args.get(3).toString()).getHex();

            RenderUtils.fillCircle(Render2dEvent.currentEvent.getContext(), x, y, r, color);
        } catch (Exception ignored) {}

        if (args.match(4, "then")) {
            args.executeAll(5);
        }
    }
}
