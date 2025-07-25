package io.github.itzispyder.clickcrystals.modules.scripts;

import io.github.itzispyder.clickcrystals.Global;
import io.github.itzispyder.clickcrystals.client.clickscript.ScriptArgs;
import io.github.itzispyder.clickcrystals.client.clickscript.ScriptParser;
import io.github.itzispyder.clickcrystals.interfaces.HandledScreenAccessor;
import io.github.itzispyder.clickcrystals.modules.Module;
import io.github.itzispyder.clickcrystals.modules.scripts.syntax.AsCmd;
import io.github.itzispyder.clickcrystals.util.minecraft.*;
import io.github.itzispyder.clickcrystals.util.misc.Dimensions;
import net.minecraft.block.BlockState;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.GameMode;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public class Conditionals implements Global {

    // registry

    private static final Map<String, Conditional> registry = new HashMap<>();

    private static Conditional register(String name, Conditional conditional) {
        if (name != null && conditional != null && !name.isEmpty())
            registry.put(name.toLowerCase(), conditional);
        return conditional;
    }

    public static boolean isRegistered(String name) {
        return registry.containsKey(name.toLowerCase());
    }

    public static Result parseCondition(Entity ref, ScriptArgs args, int beginIndex) {
        String arg = args.get(beginIndex).toString();
        Conditional condition = registry.get(arg.toLowerCase());

        if (condition == null)
            throw new IllegalArgumentException("cannot recognize conditional [%s]".formatted(arg));

        Result result = condition.test(ref, args, beginIndex);
        if (!AsCmd.hasCurrentReferenceEntity() && result.requiresDirectReference())
            result.setValue(false);
        AsCmd.resetReferenceEntity();
        return result;
    }

    public static Result parseCondition(ScriptArgs args, int beginIndex) {
        return parseCondition(PlayerUtils.player(), args, beginIndex);
    }


    // conditionals
    public static final Conditional TRUE;
    public static final Conditional FALSE;
    public static final Conditional HOLDING;
    public static final Conditional OFF_HOLDING;
    public static final Conditional TARGET_BLOCK;
    public static final Conditional TARGET_ENTITY;
    public static final Conditional TARGETING_BLOCK;
    public static final Conditional TARGETING_ENTITY;
    public static final Conditional INVENTORY_HAS;
    public static final Conditional EQUIPMENT_HAS;
    public static final Conditional HOTBAR_HAS;
    public static final Conditional INPUT_ACTIVE;
    public static final Conditional BLOCK_IN_RANGE;
    public static final Conditional ENTITY_IN_RANGE;
    public static final Conditional ATTACK_PROGRESS;
    public static final Conditional HEALTH;
    public static final Conditional HUNGER;
    public static final Conditional HURT_TIME;
    public static final Conditional ARMOR;
    public static final Conditional POS_X;
    public static final Conditional POS_Y;
    public static final Conditional POS_Z;
    public static final Conditional VEL_X;
    public static final Conditional VEL_Y;
    public static final Conditional VEL_Z;
    public static final Conditional MODULE_ENABLED;
    public static final Conditional MODULE_DISABLED;
    public static final Conditional BLOCK;
    public static final Conditional ENTITY;
    public static final Conditional DIMENSION;
    public static final Conditional EFFECT_AMPLIFIER;
    public static final Conditional EFFECT_DURATION;
    public static final Conditional IN_GAME;
    public static final Conditional IN_SINGLEPLAYER;
    public static final Conditional PLAYING;
    public static final Conditional IN_SCREEN;
    public static final Conditional CHANCE_OF;
    public static final Conditional COLLIDING;
    public static final Conditional COLLIDING_HORIZONTALLY;
    public static final Conditional COLLIDING_VERTICALLY;
    public static final Conditional JUMPING;
    public static final Conditional MOVING;
    public static final Conditional BLOCKING;
    public static final Conditional ON_GROUND;
    public static final Conditional ON_FIRE;
    public static final Conditional FROZEN;
    public static final Conditional DEAD;
    public static final Conditional ALIVE;
    public static final Conditional FALLING;
    public static final Conditional CURSOR_ITEM;
    public static final Conditional HOVERING_OVER;
    public static final Conditional REFERENCE_ENTITY;
    public static final Conditional ITEM_COUNT;
    public static final Conditional ITEM_DURABILITY;
    public static final Conditional GAMEMODE;


    static {
        TRUE = register("true", (ref, args, i) -> pair(true, i + 1));
        FALSE = register("false", (ref, args, i) -> pair(false, i + 1));
        HOLDING = register("holding", (ref, args, i) -> pair(true, EntityUtils.isHolding(ref, ScriptParser.parseItemPredicate(args.get(i + 1).toString())), i + 2));
        OFF_HOLDING = register("off_holding", (ref, args, i) -> pair(true, EntityUtils.isOffHolding(ref, ScriptParser.parseItemPredicate(args.get(i + 1).toString())), i + 2));
        TARGET_BLOCK = register("target_block", (ref, args, i) -> {
            boolean bl = EntityUtils.getTarget(ref) instanceof BlockHitResult hit && ScriptParser.parseBlockPredicate(args.get(i + 1).toString()).test(PlayerUtils.getWorld().getBlockState(hit.getBlockPos()));
            return pair(true, bl, i + 2);
        });
        TARGET_ENTITY = register("target_entity", (ref, args, i) -> {
            boolean bl = EntityUtils.getTarget(ref) instanceof EntityHitResult hit && ScriptParser.parseEntityPredicate(args.get(i + 1).toString()).test(hit.getEntity());
            return pair(true, bl, i + 2);
        });
        TARGETING_BLOCK = register("targeting_block", (ref, args, i) -> pair(true, EntityUtils.getTarget(ref) instanceof BlockHitResult hit && !PlayerUtils.getWorld().getBlockState(hit.getBlockPos()).isAir(), i + 1));
        TARGETING_ENTITY = register("targeting_entity", (ref, args, i) -> pair(true, EntityUtils.getTarget(ref) instanceof EntityHitResult hit && hit.getEntity().isAlive(), i + 1));
        INVENTORY_HAS = register("inventory_has", (ref, args, i) -> {
            assertClientPlayer();
            return pair(InvUtils.has(ScriptParser.parseItemPredicate(args.get(i + 1).toString())), i + 2);
        });
        EQUIPMENT_HAS = register("equipment_has", (ref, args, i) -> pair(true, EntityUtils.hasEquipment(ref, ScriptParser.parseItemPredicate(args.get(i + 1).toString())), i + 2));
        HOTBAR_HAS = register("hotbar_has", (ref, args, i) -> {
            assertClientPlayer();
            return pair(HotbarUtils.has(ScriptParser.parseItemPredicate(args.get(i + 1).toString())), i + 2);
        });
        INPUT_ACTIVE = register("input_active", (ref, args, i) -> {
            assertClientPlayer();
            InputType a = args.get(i + 1).toEnum(InputType.class, null);
            if (a != InputType.KEY)
                return pair(a.isActive(), i + 2);
            else
                return pair(InteractionUtils.isKeyExtendedNamePressed(args.get(i + 2).toString()), i + 3);
        });
        BLOCK_IN_RANGE = register("block_in_range", (ref, args, i) -> {
            Predicate<BlockState> filter = args.match(i + 1, "any_block") ? state -> true : ScriptParser.parseBlockPredicate(args.get(i + 1).toString());
            double range = args.get(i + 2).toDouble();
            boolean[] bl = { false };
            EntityUtils.runOnNearestBlock(ref, range, filter, (pos, state) -> {
                bl[0] = pos.toCenterPos().distanceTo(PlayerUtils.getPos()) <= range;
            });
            return pair(true, bl[0], i + 3);
        });
        ENTITY_IN_RANGE = register("entity_in_range", (ref, args, i) -> {
            Predicate<Entity> filter = args.match(i + 1, "any_entity") ? entity -> true : ScriptParser.parseEntityPredicate(args.get(i + 1).toString());
            double range = args.get(i + 2).toDouble();
            boolean[] bl = { false };
            EntityUtils.runOnNearestEntity(ref, range, filter, entity -> {
                bl[0] = entity.distanceTo(PlayerUtils.player()) <= range;
            });
            return pair(true, bl[0], i + 3);
        });
        ATTACK_PROGRESS = register("attack_progress", (ref, args, i) -> {
            assertClientPlayer();
            return pair(evalIntegers(PlayerUtils.player().getAttackCooldownProgress(1.0F), args.get(i + 1).toString()), i + 2);
        });
        HEALTH = register("health", (ref, args, i) -> pair(true, ref instanceof LivingEntity liv && evalIntegers((int) liv.getHealth(), args.get(i + 1).toString()), i + 2));
        HUNGER = register("hunger", (ref, args, i) -> pair(true, ref instanceof PlayerEntity liv && evalIntegers(liv.getHungerManager().getFoodLevel(), args.get(i + 1).toString()), i + 2));
        HURT_TIME = register("hurt_time", (ref, args, i) -> pair(true, ref instanceof LivingEntity liv && evalIntegers(liv.hurtTime, args.get(i + 1).toString()), i + 2));
        ARMOR = register("armor", (ref, args, i) -> pair(true, ref instanceof LivingEntity liv && evalIntegers(liv.getArmor(), args.get(i + 1).toString()), i + 2));
        POS_X = register("pos_x", (ref, args, i) -> pair(true, evalIntegers((int) ref.getX(), args.get(i + 1).toString()), i + 2));
        POS_Y = register("pos_y", (ref, args, i) -> pair(true, evalIntegers((int) ref.getY(), args.get(i + 1).toString()), i + 2));
        POS_Z = register("pos_z", (ref, args, i) -> pair(true, evalIntegers((int) ref.getZ(), args.get(i + 1).toString()), i + 2));
        VEL_X = register("vel_x", (ref, args, i) -> pair(true, evalIntegers((int) ref.getVelocity().getX(), args.get(i + 1).toString()), i + 2));
        VEL_Y = register("vel_y", (ref, args, i) -> pair(true, evalIntegers((int) ref.getVelocity().getY(), args.get(i + 1).toString()), i + 2));
        VEL_Z = register("vel_z", (ref, args, i) -> pair(true, evalIntegers((int) ref.getVelocity().getZ(), args.get(i + 1).toString()), i + 2));
        MODULE_ENABLED = register("module_enabled", (ref, args, i) -> {
            Module m = system.getModuleById(args.get(i + 1).toString());
            return pair(m != null && m.isEnabled(), i + 2);
        });
        MODULE_DISABLED = register("module_disabled", (ref, args, i) -> {
            Module m = system.getModuleById(args.get(i + 1).toString());
            return pair(m != null && !m.isEnabled(), i + 2);
        });
        BLOCK = register("block", (ref, args, i) -> {
            VectorParser loc = new VectorParser(
                    args.get(i + 1).toString(),
                    args.get(i + 2).toString(),
                    args.get(i + 3).toString(),
                    ref
            );
            Predicate<BlockState> pre = ScriptParser.parseBlockPredicate(args.get(i + 4).toString());
            return pair(true, pre.test(loc.getBlock(PlayerUtils.getWorld())), i + 5);
        });
        ENTITY = register("entity", (ref, args, i) -> {
            VectorParser loc = new VectorParser(
                    args.get(i + 1).toString(),
                    args.get(i + 2).toString(),
                    args.get(i + 3).toString(),
                    ref
            );
            Predicate<Entity> pre = ScriptParser.parseEntityPredicate(args.get(i + 4).toString());
            return pair(true, EntityUtils.checkEntityAt(loc.getBlockPos(), pre), i + 5);
        });
        DIMENSION = register("dimension", (ref, args, i) -> {
            boolean bl = false;
            switch (args.get(i + 1).toEnum(Dimensions.class, null)) {
                case OVERWORLD -> bl = Dimensions.isOverworld();
                case THE_NETHER -> bl = Dimensions.isNether();
                case THE_END -> bl = Dimensions.isEnd();
            }
            return pair(bl, i + 2);
        });
        EFFECT_AMPLIFIER = register("effect_amplifier", (ref, args, i) -> {
            StatusEffectInstance effect = EntityUtils.getEffect(ref, ScriptParser.parseStatusEffect(args.get(i + 1).toString()));
            return pair(true, evalIntegers(effect.getAmplifier(), args.get(i + 2).toString()), i + 3);
        });
        EFFECT_DURATION = register("effect_duration", (ref, args, i) -> {
            StatusEffectInstance effect = EntityUtils.getEffect(ref, ScriptParser.parseStatusEffect(args.get(i + 1).toString()));
            return pair(true, evalIntegers(effect.getDuration(), args.get(i + 2).toString()), i + 3);
        });
        IN_GAME = register("in_game", (ref, args, i) -> pair(PlayerUtils.valid(), i + 1));
        IN_SINGLEPLAYER = register("in_singleplayer", (ref, args, i) -> pair(mc.isInSingleplayer(), i + 1));
        PLAYING = register("playing", (ref, args, i) -> pair(PlayerUtils.valid() && mc.currentScreen == null, i + 1));
        IN_SCREEN = register("in_screen", (ref, args, i) -> pair(PlayerUtils.valid() && mc.currentScreen != null, i + 1));
        CHANCE_OF = register("chance_of", (ref, args, i) -> pair(Math.random() * 100 < args.get(i + 1).toDouble(), i + 2));
        COLLIDING = register("colliding", (ref, args, i) -> pair(true, EntityUtils.isColliding(ref), i + 1));
        COLLIDING_HORIZONTALLY = register("colliding_horizontally", (ref, args, i) -> pair(true, EntityUtils.isCollidingHorizontally(ref), i + 1));
        COLLIDING_VERTICALLY = register("colliding_vertically", (ref, args, i) -> pair(true, EntityUtils.isCollidingVertically(ref), i + 1));
        JUMPING = register("jumping", (ref, args, i) -> pair(true, PlayerUtils.valid() && mc.player.input.playerInput.jump() && !mc.player.isSubmergedInWater(), i + 1));
        MOVING = register("moving", (ref, args, i) -> pair(true, EntityUtils.isMoving(ref), i + 1));
        BLOCKING = register("blocking", (ref, args, i) -> pair(true, EntityUtils.isBlocking(ref), i + 1));
        ON_GROUND = register("on_ground", (ref, args, i) -> pair(true, ref.isOnGround(), i + 1));
        ON_FIRE = register("on_fire", (ref, args, i) -> pair(true, ref.isOnFire(), i + 1));
        FROZEN = register("frozen", (ref, args, i) -> pair(true, ref.isFrozen(), i + 1));
        DEAD = register("dead", (ref, args, i) -> pair(true, ref instanceof LivingEntity liv && liv.isDead(), i + 1));
        ALIVE = register("alive", (ref, args, i) -> pair(true, ref instanceof LivingEntity liv && liv.isAlive(), i + 1));
        FALLING = register("falling", (ref, args, i) -> pair(true, ref instanceof LivingEntity liv && !liv.isOnGround() && liv.fallDistance > 0.0, i + 1));
        CURSOR_ITEM = register("cursor_item", (ref, args, i) -> {
            ClientPlayerEntity p = PlayerUtils.player();
            Predicate<ItemStack> item = ScriptParser.parseItemPredicate(args.get(i + 1).toString());
            if (p == null || p.currentScreenHandler == null)
                return pair(false, i + 2);
            ItemStack stack = p.currentScreenHandler.getCursorStack();
            return pair(stack != null && item.test(stack), i + 2);
        });
        HOVERING_OVER = register("hovering_over", (ref, args, i) -> {
            ClientPlayerEntity p = PlayerUtils.player();
            Predicate<ItemStack> item = ScriptParser.parseItemPredicate(args.get(i + 1).toString());
            if (p == null || p.currentScreenHandler == null || !(mc.currentScreen instanceof HandledScreen<?> handle))
                return pair(false, i + 2);

            HandledScreenAccessor screen = (HandledScreenAccessor) handle;
            Point cursor = InteractionUtils.getCursor();

            for (Slot slot : p.currentScreenHandler.slots) {
                if (!screen.isHovered(slot, cursor.x, cursor.y))
                    continue;
                ItemStack stack = slot.getStack();
                if (stack == null || !item.test(stack))
                    continue;
                return pair(true, i + 2);
            }
            return pair(false, i + 2);
        });
        REFERENCE_ENTITY = register("reference_entity", (ref, args, i) -> {
            if (args.match(i + 1, "client")) {
                return pair(ref == PlayerUtils.player(), i + 2);
            }
            Predicate<Entity> filter = args.match(i + 1, "any_entity") ? entity -> true : ScriptParser.parseEntityPredicate(args.get(i + 1).toString());
            return pair(filter.test(ref), i + 2);
        });
        ITEM_COUNT = register("item_count", (ref, args, i) -> {
            ItemStack item = ScriptParser.parseItemStack(args.get(i + 1).toString());
            return pair(item != null && evalIntegers(item.getCount(), args.get(i + 2).toString()), i + 3);
        });
        ITEM_DURABILITY = register("item_durability", (ref, args, i) -> {
            ItemStack item = ScriptParser.parseItemStack(args.get(i + 1).toString());
            return pair(item != null && evalIntegers(1 - item.getDamage() / (double) item.getMaxDamage(), args.get(i + 2).toString()), i + 3);
        });
        GAMEMODE = register("gamemode", (ref, args, i) -> {
            GameMode gm = args.get(i + 1).toEnum(GameMode.class);
            return pair(ref instanceof PlayerEntity p && p.getGameMode() == gm, i + 2);
        });
    }



    // helper methods

    private static void assertClientPlayer() {
        if (AsCmd.getCurrentReferenceEntity() != PlayerUtils.player())
            throw new IllegalArgumentException("unsupported action on non-client player or entity; did you unintentionally use the 'as' command?");
    }

    /**
     * Evaluates if input [>, <, =, >=, <=, ==, !=] other number
     * @param input a number
     * @param other other number as string
     * @return
     */
    private static boolean evalIntegers(double input, String other) {
        if (other.isEmpty())
            return false;

        else if (other.startsWith("<="))
            return input <= Double.parseDouble(other.substring(2));
        else if (other.startsWith(">="))
            return input >= Double.parseDouble(other.substring(2));
        else if (other.startsWith("!="))
            return input != Double.parseDouble(other.substring(2));
        else if (other.startsWith("=="))
            return input == Double.parseDouble(other.substring(2));
        else if (other.startsWith("<"))
            return input < Double.parseDouble(other.substring(1));
        else if (other.startsWith(">"))
            return input > Double.parseDouble(other.substring(1));
        else if (other.startsWith("!"))
            return input != Double.parseDouble(other.substring(1));
        else if (other.startsWith("="))
            return input == Double.parseDouble(other.substring(1));
        return input == Double.parseDouble(other);
    }

    @FunctionalInterface
    public interface Conditional {

        /**
         * Function interface that represents an abstract conditional
         * @param ref The reference Entity set in AsCmd
         * @param args Script arguments
         * @param i Script arguments begin index
         * @return returns a boolean representing the test result of the condition, along with the next begin index
         */
        Result test(Entity ref, ScriptArgs args, int i);
    }

    private static Result pair(boolean value, int nextIndex) {
        return new Result(value, nextIndex);
    }

    private static Result pair(boolean refDirect, boolean value, int nextIndex) {
        return new Result(refDirect, value, nextIndex);
    }

    public static class Result {
        private final boolean requiresDirectReference;
        private boolean value;
        private final int nextIndex;

        public Result(boolean refDirect, boolean value, int nextIndex) {
            this.requiresDirectReference = refDirect;
            this.value = value;
            this.nextIndex = nextIndex;
        }

        public Result(boolean value, int nextIndex) {
            this(false, value, nextIndex);
        }

        public boolean requiresDirectReference() {
            return requiresDirectReference;
        }

        public boolean value() {
            return value;
        }

        public void setValue(boolean value) {
            this.value = value;
        }

        public int nextIndex() {
            return nextIndex;
        }
    }
}
