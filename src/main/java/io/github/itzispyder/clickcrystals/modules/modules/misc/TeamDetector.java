package io.github.itzispyder.clickcrystals.modules.modules.misc;

import io.github.itzispyder.clickcrystals.events.EventHandler;
import io.github.itzispyder.clickcrystals.events.events.client.MouseClickEvent;
import io.github.itzispyder.clickcrystals.events.events.client.PlayerAttackEntityEvent;
import io.github.itzispyder.clickcrystals.gui.ClickType;
import io.github.itzispyder.clickcrystals.modules.Categories;
import io.github.itzispyder.clickcrystals.modules.Module;
import io.github.itzispyder.clickcrystals.modules.ModuleSetting;
import io.github.itzispyder.clickcrystals.modules.modules.ListenerModule;
import io.github.itzispyder.clickcrystals.modules.settings.EnumSetting;
import io.github.itzispyder.clickcrystals.modules.settings.SettingSection;
import io.github.itzispyder.clickcrystals.util.minecraft.ChatUtils;
import io.github.itzispyder.clickcrystals.util.minecraft.PlayerUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;

import java.util.HashSet;
import java.util.Set;

public class TeamDetector extends ListenerModule {

    private final SettingSection scGeneral = getGeneralSection();

    // parsed manual list, rebuilt only when the raw setting string changes
    private String cachedNamesRaw;
    private Set<String> cachedNames = Set.of();

    public final ModuleSetting<TeamsMethod> teamFindingMethod = scGeneral.add(EnumSetting.create(TeamsMethod.class)
            .name("team-detection-mode")
            .description("Method used to automatically detect teams.")
            .def(TeamsMethod.COLOR_NAME)
            .build()
    );

    public final ModuleSetting<String> playerNames = scGeneral.add(createStringSetting()
            .name("manual-team-players")
            .description("Manually add players by their exact usernames. Separate names using commas.")
            .visibleWhen(() -> teamFindingMethod.getVal() == TeamsMethod.MANUAL)
            .def("")
            .build()
    );

    public final ModuleSetting<Boolean> protectTeammates = scGeneral.add(createBoolSetting()
            .name("protect-teammates")
            .description("Cancels target tracking and melee attacks on detected teammates.")
            .def(true)
            .build()
    );

    public final ModuleSetting<Boolean> cancelCcs = scGeneral.add(createBoolSetting()
            .name("cancel-ccs")
            .description("Prevents internal ClickCrystals scripting triggers from attacking friendly targets.")
            .def(true)
            .build()
    );

    public TeamDetector() {
        super("team-detector", Categories.MISC, "Detect team alignments and prevent friendly fire against teammates");
    }

    @EventHandler
    private void onPlayerAttackEntityEvent(PlayerAttackEntityEvent e) {
        if (!protectTeammates.getVal())
            return;
        if (e.getEntity() instanceof Player p && isTeammate(p))
            e.cancel();
    }

    @EventHandler
    private void onMiddleClick(MouseClickEvent e) {
        if (e.getButton() != 2 || e.getAction() != ClickType.CLICK || !e.isScreenNull())
            return;
        if (!(mc.crosshairPickEntity instanceof Player player))
            return;

        String playerName = player.getName().getString();
        String current = playerNames.getVal();

        if (current.toLowerCase().contains(playerName.toLowerCase())) {
            String updated = current.replaceAll("(?i),?\\b" + playerName + "\\b,?", "")
                    .replaceAll("^,", "")
                    .replaceAll(",$", "");
            playerNames.setVal(updated);
            ChatUtils.sendPrefixMessage("§cRemoved " + playerName + " from team");
        }
        else {
            String updated = current.isEmpty() ? playerName : current + "," + playerName;
            playerNames.setVal(updated);
            ChatUtils.sendPrefixMessage("§aAdded " + playerName + " to team");
        }
        e.setCancelled(true);
    }

    public static boolean isTeammate(Player target) {
        // never flag yourself as a teammate
        if (PlayerUtils.invalid() || target == null || target == PlayerUtils.player())
            return false;

        TeamDetector td = Module.get(TeamDetector.class);
        if (!td.isEnabled())
            return false;

        // manual list (cached, O(1) lookup)
        if (td.teamNames().contains(target.getName().getString().toLowerCase()))
            return true;

        // automatic detection
        return switch (td.teamFindingMethod.getVal()) {
            case SCOREBOARD -> isSameScoreboardTeam(target);
            case COLOR_NAME -> isSameColorNameTeam(target);
            default -> false;
        };
    }

    public static boolean shouldCancelCcsAttack(Player target) {
        // isTeammate already gates on enabled/valid; only the ccs toggle is extra here
        return Module.get(TeamDetector.class).cancelCcs.getVal() && isTeammate(target);
    }

    private Set<String> teamNames() {
        String raw = playerNames.getVal();
        if (!raw.equals(cachedNamesRaw)) {
            cachedNamesRaw = raw;
            Set<String> names = new HashSet<>();
            for (String name : raw.split(",")) {
                String trimmed = name.trim().toLowerCase();
                if (!trimmed.isEmpty())
                    names.add(trimmed);
            }
            cachedNames = names;
        }
        return cachedNames;
    }

    private static boolean isSameScoreboardTeam(Player player) {
        Scoreboard scoreboard = PlayerUtils.getWorld().getScoreboard();
        PlayerTeam playerTeam = scoreboard.getPlayerTeam(PlayerUtils.player().getName().getString());
        PlayerTeam otherPlayerTeam = scoreboard.getPlayerTeam(player.getName().getString());
        return playerTeam != null && playerTeam.equals(otherPlayerTeam);
    }

    private static boolean isSameColorNameTeam(Player player) {
        int playerColor = PlayerUtils.player().getTeamColor();
        int targetColor = player.getTeamColor();
        return playerColor == targetColor && playerColor != ChatUtils.getFormattingChar(ChatFormatting.WHITE);
    }

    public enum TeamsMethod {
        SCOREBOARD,
        COLOR_NAME,
        MANUAL
    }
}