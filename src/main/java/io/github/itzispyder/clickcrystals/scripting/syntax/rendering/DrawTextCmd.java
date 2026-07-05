package io.github.itzispyder.clickcrystals.scripting.syntax.rendering;

import io.github.itzispyder.clickcrystals.events.events.client.Render2dEvent;
import io.github.itzispyder.clickcrystals.gui.misc.Color;
import io.github.itzispyder.clickcrystals.scripting.ScriptArgs;
import io.github.itzispyder.clickcrystals.scripting.ScriptArgsReader;
import io.github.itzispyder.clickcrystals.scripting.ScriptCommand;
import io.github.itzispyder.clickcrystals.util.minecraft.render.RenderUtils;
import net.minecraft.network.chat.Component;

// @Format draw_text <"text"> <x> <y> [color] [scale] [shadow]
public class DrawTextCmd extends ScriptCommand {

    public DrawTextCmd() {
        super("draw_text");
    }

    @Override
    public void onCommand(ScriptCommand command, String line, ScriptArgs args) {
        if (Render2dEvent.currentEvent == null) {
            return;
        }

        try {
            ScriptArgsReader sar = args.getReader();
            String text = sar.nextQuote();
            int x = (int) Math.round(RenderParser.parseVal(sar.nextStr()));
            int y = (int) Math.round(RenderParser.parseVal(sar.nextStr()));

            int color = 0xFFFFFFFF;
            float scale = 1.0F;
            boolean shadow = true;

            int remaining = args.getSize() - sar.getIndex();
            if (remaining > 0) {
                String possibleThen = args.get(sar.getIndex()).toString();
                if (!possibleThen.equalsIgnoreCase("then")) {
                    color = Color.parse(sar.nextStr()).getHex();
                    remaining = args.getSize() - sar.getIndex();
                    if (remaining > 0) {
                        possibleThen = args.get(sar.getIndex()).toString();
                        if (!possibleThen.equalsIgnoreCase("then")) {
                            scale = (float) RenderParser.parseVal(sar.nextStr());
                            remaining = args.getSize() - sar.getIndex();
                            if (remaining > 0) {
                                possibleThen = args.get(sar.getIndex()).toString();
                                if (!possibleThen.equalsIgnoreCase("then")) {
                                    shadow = Boolean.parseBoolean(sar.nextStr());
                                }
                            }
                        }
                    }
                }
            }

            RenderUtils.drawDefaultScaledText(
                    Render2dEvent.currentEvent.getContext(),
                    Component.literal(RenderParser.interpolate(text)),
                    x, y,
                    scale,
                    shadow,
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
