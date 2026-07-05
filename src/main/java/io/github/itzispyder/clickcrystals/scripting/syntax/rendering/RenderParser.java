package io.github.itzispyder.clickcrystals.scripting.syntax.rendering;

import io.github.itzispyder.clickcrystals.Global;
import io.github.itzispyder.clickcrystals.scripting.expressions.ExpressionScope;
import io.github.itzispyder.clickcrystals.scripting.expressions.ExpressionEvaluator;
import io.github.itzispyder.clickcrystals.util.minecraft.PlayerUtils;
import net.minecraft.world.entity.player.Player;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RenderParser implements Global {

    private static final Pattern PLACEHOLDER = Pattern.compile("\\$\\{([^}]*)}");

    public static double parseVal(String val) {
        try {
            return Double.parseDouble(val);
        } catch (NumberFormatException e) {
            try {
                return new ExpressionEvaluator(buildScope()).eval(val);
            } catch (Exception ex) {
                return 0.0;
            }
        }
    }

    // Replaces ${expr} placeholders in text with the evaluated, formatted value.
    public static String interpolate(String text) {
        if (text == null || !text.contains("${"))
            return text;

        Matcher m = PLACEHOLDER.matcher(text);
        StringBuilder sb = new StringBuilder();
        while (m.find())
            m.appendReplacement(sb, Matcher.quoteReplacement(format(parseVal(m.group(1).trim()))));
        m.appendTail(sb);
        return sb.toString();
    }

    private static String format(double v) {
        if (v == Math.rint(v) && !Double.isInfinite(v))
            return Long.toString((long) v);
        return String.valueOf(Math.round(v * 10.0) / 10.0);
    }

    private static ExpressionScope buildScope() {
        ExpressionScope scope = new ExpressionScope();
        scope.defVar("screen_width", () -> mc.getWindow().getGuiScaledWidth());
        scope.defVar("screen_height", () -> mc.getWindow().getGuiScaledHeight());
        scope.defVar("width", () -> mc.getWindow().getGuiScaledWidth());
        scope.defVar("height", () -> mc.getWindow().getGuiScaledHeight());

        if (PlayerUtils.valid()) {
            Player p = PlayerUtils.player();
            scope.defVar("player_x", p::getX);
            scope.defVar("player_y", p::getY);
            scope.defVar("player_z", p::getZ);
            scope.defVar("health", p::getHealth);
            scope.defVar("max_health", p::getMaxHealth);
            scope.defVar("food", () -> p.getFoodData().getFoodLevel());
            scope.defVar("armor", p::getArmorValue);
            scope.defVar("air", p::getAirSupply);
            scope.defVar("max_air", p::getMaxAirSupply);
            scope.defVar("xp_level", () -> p.experienceLevel);
            scope.defVar("yaw", p::getYRot);
            scope.defVar("pitch", p::getXRot);
            scope.defVar("held_count", () -> p.getMainHandItem().getCount());
        }
        return scope;
    }
}
