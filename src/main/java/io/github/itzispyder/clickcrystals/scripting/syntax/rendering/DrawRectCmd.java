package io.github.itzispyder.clickcrystals.scripting.syntax.rendering;

import io.github.itzispyder.clickcrystals.events.events.client.Render2dEvent;
import io.github.itzispyder.clickcrystals.gui.misc.Color;
import io.github.itzispyder.clickcrystals.scripting.ScriptArgs;
import io.github.itzispyder.clickcrystals.scripting.ScriptCommand;
import io.github.itzispyder.clickcrystals.util.minecraft.render.RenderUtils;

// @Format draw_rect <x> <y> <w> <h> <color>
public class DrawRectCmd extends ScriptCommand {

    public DrawRectCmd() {
        super("draw_rect");
    }

    @Override
    public void onCommand(ScriptCommand command, String line, ScriptArgs args) {
        if (Render2dEvent.currentEvent == null) {
            return;
        }

        try {
            int x = (int) Math.round(RenderParser.parseVal(args.get(0).toString()));
            int y = (int) Math.round(RenderParser.parseVal(args.get(1).toString()));
            int w = (int) Math.round(RenderParser.parseVal(args.get(2).toString()));
            int h = (int) Math.round(RenderParser.parseVal(args.get(3).toString()));
            int color = Color.parse(args.get(4).toString()).getHex();

            RenderUtils.drawRect(Render2dEvent.currentEvent.getContext(), x, y, w, h, color);
        } catch (Exception ignored) {}

        if (args.match(5, "then")) {
            args.executeAll(6);
        }
    }
}
