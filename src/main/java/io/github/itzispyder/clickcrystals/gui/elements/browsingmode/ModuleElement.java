package io.github.itzispyder.clickcrystals.gui.elements.browsingmode;

import io.github.itzispyder.clickcrystals.gui.GuiElement;
import io.github.itzispyder.clickcrystals.gui.misc.Color;
import io.github.itzispyder.clickcrystals.gui.misc.Shades;
import io.github.itzispyder.clickcrystals.gui.misc.animators.Animations;
import io.github.itzispyder.clickcrystals.gui.misc.animators.Animator;
import io.github.itzispyder.clickcrystals.gui.misc.animators.Hover;
import io.github.itzispyder.clickcrystals.gui.screens.ModuleEditScreen;
import io.github.itzispyder.clickcrystals.gui.screens.scripts.ClickScriptIDE;
import io.github.itzispyder.clickcrystals.modrinth.ModrinthSupport;
import io.github.itzispyder.clickcrystals.modules.Module;
import io.github.itzispyder.clickcrystals.modules.modules.ScriptedModule;
import io.github.itzispyder.clickcrystals.util.minecraft.render.RenderUtils;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class ModuleElement extends GuiElement {

    private final Module module;
    private final boolean blacklisted;
    private final Hover hover = new Hover(140);
    private Animator animator;

    public ModuleElement(Module module, int x, int y) {
        super(x, y, 300, 15);
        super.setTooltip("§eLEFT-CLICK§7 to toggle, §eRIGHT-CLICK§7 to edit");
        this.module = module;
        this.animator = new Animator(400, Animations.FADE_IN_AND_OUT);

        if (module == null) {
            this.blacklisted = false;
            return;
        }

        this.blacklisted = ModrinthSupport.active && ModrinthSupport.isBlacklisted(module);
        if (blacklisted)
            setTooltip(ModrinthSupport.warning);
        else if (module instanceof ScriptedModule)
            setTooltip(getTooltip().concat(", §6MIDDLE-CLICK§7 to open IDE"));
    }

    @Override
    public void onRender(GuiGraphicsExtractor context, int mouseX, int mouseY) {
        boolean isAnimating = animator != null && !animator.isFinished();
        if (isAnimating) {
            context.pose().pushMatrix();
            context.pose().translate(-(float)(width * 0.5 * animator.getAnimationReversed()), 0);
        }
        else {
            animator = null;
        }

        // the row fades and slides in under the mouse instead of flashing a flat box
        double glow = hover.update(!blacklisted && isHovered(mouseX, mouseY));
        int slide = (int)Math.round(glow * 2);
        if (glow > 0.01) {
            RenderUtils.fillRoundRect(context, x, y, width - 6, height, 3, Color.blend(0x00FFFFFF, 0x60FFFFFF, glow));
        }

        String text;

        if (module != null) {
            if (module.isEnabled()) {
                RenderUtils.fillRoundVertLine(context, x, y + 2, height - 4, 2, Shades.GENERIC);
            }
            text = "  %s".formatted(module.getOnOrOff());
            RenderUtils.drawText(context, text, x + slide, y + height / 3, 0.7F, false);
            text = " §8|   §f%s".formatted(module.getNameLimited());
            RenderUtils.drawText(context, text, x + 20 + slide, y + height / 3, 0.7F, false);
            text = "§7- %s".formatted(module.getDescriptionLimited());
            RenderUtils.drawText(context, text, x + width / 3 + slide, y + height / 3, 0.7F, false);
        }

        if (isAnimating) {
            context.pose().popMatrix();
        }
        if (blacklisted) {
            RenderUtils.fillRect(context, x, y, width, height, 0x60000000);
        }
    }

    @Override
    public void onClick(double mouseX, double mouseY, int button) {
        if (blacklisted)
            return;

        if (button == 0) {
            module.setEnabled(!module.isEnabled(), false);
        }
        else if (button == 1) {
            mc.setScreenAndShow(new ModuleEditScreen(module));
        }
        else if (button == 2 && module instanceof ScriptedModule m) {
            mc.setScreenAndShow(new ClickScriptIDE(m));
        }
    }

    @Override
    public boolean isHovered(int mouseX, int mouseY) {
        return rendering && mouseX > x && mouseX < x + (width - 6) && mouseY > y && mouseY < y + height;
    }

    public Module getModule() {
        return module;
    }

    public Animator getAnimator() {
        return animator;
    }

    public void setAnimator(Animator animator) {
        this.animator = animator;
    }
}
