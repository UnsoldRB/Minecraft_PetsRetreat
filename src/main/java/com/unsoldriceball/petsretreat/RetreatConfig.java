package com.unsoldriceball.petsretreat;

import net.minecraftforge.common.config.Config;

import static com.unsoldriceball.petsretreat.RetreatMain.MOD_ID;


@SuppressWarnings("unused")
@Config(modid = MOD_ID)
final public class RetreatConfig {
    @Config.RequiresMcRestart
    @Config.Comment("If set to false, this mod will not work.")
    public static boolean C_DO_INIT = true;

    public static category_system system = new category_system(
            true,
            true,
            true,
            true,
            16,
            0.05,
            0.5);

    public static category_item item = new category_item(
            true,
            true,
            false,
            true,
            "Attack the entity with this totem...");

    public static category_message message = new category_message(
            "Applied a totem to the %target%.",
            "Revoked a totem from the %target%.",
            "%target% has already had a totem effect applied by another player.",
            "The %victim% was forced to retreat by %attacker%.",
            "%victim% has retreated your spawn point. (Because owner offline.)");


    private static class category_system {
        @Config.Comment("If set to false, this mod will not spawn particle when entities retreat.")
        public boolean C_DO_PLAYPARTICLE;
        @Config.Comment("If set to false, this mod will not play sound when entities retreat.")
        public boolean C_DO_PLAYSOUND;
        @Config.Comment("If set to false, this mod will not apply the Resistance(Lv.5, 60ticks) and Invisible(2ticks) potion effects to retreated entities.")
        public boolean C_DO_APPLYPOTION;
        @Config.RequiresMcRestart
        @Config.Comment("If set to false, this mod will not apply the potion effect witch is added by this mod to entities that have retreated.")
        public boolean C_DO_APPLYRETREATPOTION;
        @Config.RequiresMcRestart
        @Config.RangeInt(min = 1, max = 32768)
        @Config.Comment("When the distance between a retreated entity and its owner is lower than this value, the potion effect witch addedby this mod will be removed.")
        public int C_UPDATECANCEL_DISTANCE;
        @Config.RequiresMcRestart
        @Config.RangeDouble(min = 0.0, max = 1.0)
        @Config.Comment("When entities retreat, their health(%) will be set to this value.(If set to 0, their health will be set to 1.)")
        public Double C_HEALING_PERCENTAGE;
        @Config.RangeDouble(min = 0.0)
        @Config.Comment("When entities retreat, they will spawn at theirs respawn point with the added this value to the Y coordinate.")
        public double C_SPAWN_HEIGHT;

        public category_system(boolean c_DO_PLAYPARTICLE, boolean c_DO_PLAYSOUND, boolean c_DO_APPLYPOTION, boolean c_DO_APPLYRETREATPOTION, int c_UPDATECANCEL_DISTANCE, Double c_HEALING_PERCENTAGE, double c_SPAWN_HEIGHT) {
            C_DO_PLAYPARTICLE = c_DO_PLAYPARTICLE;
            C_DO_PLAYSOUND = c_DO_PLAYSOUND;
            C_DO_APPLYPOTION = c_DO_APPLYPOTION;
            C_DO_APPLYRETREATPOTION = c_DO_APPLYRETREATPOTION;
            C_UPDATECANCEL_DISTANCE = c_UPDATECANCEL_DISTANCE;
            C_HEALING_PERCENTAGE = c_HEALING_PERCENTAGE;
            C_SPAWN_HEIGHT = c_SPAWN_HEIGHT;
        }
    }
    private static class category_item {
        @Config.Comment("If set to false, entities that have applied the Totem of Retreat will not take any damage from their owner and his pets.")
        public boolean C_TOTEM_FRIENDLYFIRE;
        @Config.RequiresMcRestart
        @Config.Comment("If set to true, the Totem of Retreat will be reverted upon use.")
        public boolean C_TOTEM_ONETIME;
        @Config.RequiresMcRestart
        @Config.Comment("If set to true, crafting Totem of Retreat becomes harder.")
        public boolean C_TOTEM_HARDRECIPE;
        @Config.RequiresMcRestart
        @Config.Comment("If set to false, this mod will not apply the Regeneration(Lv.2, 600 ticks) potion effects to entities that have retreated using the Totem of Retreat crafted with the hard recipe version.")
        public boolean C_TOTEM_HARDRECIPE_REGENERATION;
        @Config.Comment("Tooltip for Totem of Retreat.")
        public String C_TOTEM_TOOLTIP;

        public category_item(boolean c_TOTEM_FRIENDLYFIRE, boolean c_TOTEM_ONETIME, boolean c_TOTEM_HARDRECIPE, boolean c_TOTEM_HARDRECIPE_REGENERATION, String c_TOTEM_TOOLTIP) {
            C_TOTEM_FRIENDLYFIRE = c_TOTEM_FRIENDLYFIRE;
            C_TOTEM_ONETIME = c_TOTEM_ONETIME;
            C_TOTEM_HARDRECIPE = c_TOTEM_HARDRECIPE;
            C_TOTEM_HARDRECIPE_REGENERATION = c_TOTEM_HARDRECIPE_REGENERATION;
            C_TOTEM_TOOLTIP = c_TOTEM_TOOLTIP;
        }
    }
    private static class category_message {
        @Config.Comment("The message sent to the player who applied the Totem of Retreat to the entity.(You can use %target%.)")
        public String C_MESSAGE_TOTEM_APPLY;
        @Config.Comment("The message sent to the player who revoked the Totem of Retreat from the entity.(You can use %target%.)")
        public String C_MESSAGE_TOTEM_REVOKE;
        @Config.Comment("The message sent to the player who failed to apply the Totem of Retreat because the entity already has another one.(You can use %target%.)")
        public String C_MESSAGE_TOTEM_FAILED;
        @Config.Comment("The message that is sent to the owner of the retreated entity.(You can use %victim% and %attacker%.)")
        public String C_MESSAGE_RETREATED;
        @Config.Comment("The message that is sent to a random player when the owner of the retreated entity is offline.(You can use %victim% and %attacker%.)")
        public String C_MESSAGE_RETREATED_OFFLINE;

        public category_message(String c_MESSAGE_TOTEM_APPLY, String c_MESSAGE_TOTEM_REVOKE, String c_MESSAGE_TOTEM_FAILED, String c_MESSAGE_RETREATED, String c_MESSAGE_RETREATED_OFFLINE) {
            C_MESSAGE_TOTEM_APPLY = c_MESSAGE_TOTEM_APPLY;
            C_MESSAGE_TOTEM_REVOKE = c_MESSAGE_TOTEM_REVOKE;
            C_MESSAGE_TOTEM_FAILED = c_MESSAGE_TOTEM_FAILED;
            C_MESSAGE_RETREATED = c_MESSAGE_RETREATED;
            C_MESSAGE_RETREATED_OFFLINE = c_MESSAGE_RETREATED_OFFLINE;
        }
    }
}