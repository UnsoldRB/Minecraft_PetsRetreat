package com.unsoldriceball.petsretreat;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import static com.unsoldriceball.petsretreat.RetreatMain.ID_MOD;




@Config(modid = ID_MOD)
public class RetreatConfig
{
    public static System c_System = new System();
    public static Totem c_Totem = new Totem();
    public static Rod c_Rod = new Rod();
    public static Message c_Message = new Message();
    public static KillLog c_KillLog = new KillLog();

    public static class System
    {
        @Config.RequiresMcRestart
        @Config.Comment("If set to false, this mod will not add the guide book.")
        public boolean enableGuideBook = true;
        @Config.Comment("If set to false, this mod will not spawn particles.")
        public boolean doPlayParticles = true;
        @Config.Comment("If set to false, this mod will not play sounds.")
        public boolean doPlaySounds = true;
        @Config.RequiresMcRestart
        @Config.Comment("If set to false, this mod will not apply a Resistance(Lv.5, 3 seconds) and Invisible(2ticks) potion effects to retreated entities.")
        public boolean doApplyResistPotion = true;
        @Config.RequiresMcRestart
        @Config.Comment("If set to false, a retreated entity will not freeze.")
        public boolean doFreezeRetreateds = true;
        @Config.RequiresMcRestart
        @Config.RangeInt(min = 1, max = 32768)
        @Config.Comment("When the distance between a retreated entity and its owner is lower than this value, the entity will unfreeze.")
        public int unfreezeDistance = 8;
        @Config.RequiresMcRestart
        @Config.RangeDouble(min = 0.0d, max = 1.0d)
        @Config.Comment("When entities retreat, their health(%) will be set to this value.(If set to 0, their health will be set to 1.)")
        public float healPercentage = 0.05f;
        @Config.RequiresMcRestart
        @Config.RangeDouble(min = 0.0d, max = 1.0d)
        @Config.Comment("When entities that is applied the totem that is more difficult to craft retreat, their health(%) will be set to this value.(If set to 0, their health will be set to 1.)")
        public float healPercentage_Hard = 0.25f;
        @Config.RequiresMcRestart
        @Config.Comment("If set to false, this mod will not apply the Regeneration(Lv.2, 1 minute) potion effects to entities that have retreated using the totem that is more difficult to craft.")
        public boolean hardTotem_Regeneration = true;
        @Config.RangeDouble(min = 0.0)
        @Config.Comment("When entities retreat, they will spawn at theirs respawn point with the added this value to the Y coordinate.")
        public double spawnHeight = 0.5;
        @Config.RequiresMcRestart
        @Config.RangeInt(min = 0)
        @Config.Comment("When you die, your pets within this distance will retreat.(0 = disable.)")
        public int maxDistanceOfRetreat_OnOwnerDeath = 64;
        @Config.RequiresMcRestart
        @Config.RangeDouble(min = 0.0d)
        @Config.Comment("Your pets that is applied the totem around you will continue to heal by this amount every petRegeneration_Interval ticks.(0 = disable.)")
        public float petRegeneration_Amount = 1.0f;
        @Config.RequiresMcRestart
        @Config.RangeDouble(min = 0.0d)
        @Config.Comment("Your pets that is applied the totem that is more difficult to craft around you will continue to heal by this amount every petRegeneration_Interval ticks.(0 = disable.)")
        public float petRegeneration_Amount_Hard = 2.0f;
        @Config.RequiresMcRestart
        @Config.RangeInt(min = 1)
        @Config.Comment("Your pets that is applied the totem around you will continue to heal every this number of ticks.")
        public int petRegeneration_Interval = 30;
        @Config.RangeInt(min = 1)
        @Config.Comment("The radius centered around you where petRegeneration_Amount is applied.")
        public int petRegeneration_Radius = 32;
        @Config.RequiresMcRestart
        @Config.Comment("If set to true, pets will be enabled to attack their owner.")
        public boolean totemFriendlyFire_toOwner = true;
        @Config.RequiresMcRestart
        @Config.Comment("If set to true, you and your pets will be enabled to attack your pets.")
        public boolean totemFriendlyFire_toPet = false;
    }



    public static class Totem
    {
        @Config.RequiresMcRestart
        @Config.Comment("If set to false, the totem will not disappear upon use.")
        public boolean totemOneTime = true;
        @Config.RequiresMcRestart
        @Config.Comment("If set to true, the totem can apply to the boss.")
        public boolean totemCanApplyToBoss = false;
        @Config.RequiresMcRestart
        @Config.Comment("If set to true, the totem will be reverted upon use.")
        public boolean totemOneTime_revert = false;
        @Config.RequiresMcRestart
        @Config.Comment("If set to false, disable the totem that is standard.")
        public boolean enableNormalTotem = true;
        @Config.RequiresMcRestart
        @Config.Comment("If set to false, disable the totem that is more difficult to craft.")
        public boolean enableHardTotem = true;
        @Config.RequiresMcRestart
        @Config.Comment("If set to false, disable the totem that to revoke the effect of the Totem of Retreat.")
        public boolean enableRevokeTotem = true;
        @Config.RequiresMcRestart
        @Config.Comment("If set to false, disable the recipe of the totem that is standard.")
        public boolean enableNormalTotem_Recipe = true;
        @Config.RequiresMcRestart
        @Config.Comment("If set to false, disable the recipe of the totem that is more difficult to craft.")
        public boolean enableHardTotem_Recipe = true;
        @Config.RequiresMcRestart
        @Config.Comment("If set to false, disable the recipe of the totem that to revoke the effect of the Totem of Retreat.")
        public boolean enableRevokeTotem_Recipe = true;
        @Config.RequiresMcRestart
        @Config.Comment("Tooltip for the totem.(Can use %nl%.)")
        public String totemToolTip_Normal = "If you wield this, you feel like it will protect your comrades...";
        @Config.RequiresMcRestart
        @Config.Comment("Tooltip for the totem that is more difficult to craft.(Can use %nl%.)")
        public String totemToolTip_Hard = "If you wield this, you feel like it will protect your comrades...%nl%You can sense that much power from this totem.";
        @Config.RequiresMcRestart
        @Config.Comment("Tooltip for the totem that to revoke the effect of the Totem of Retreat.(Can use %nl%.)")
        public String totemToolTip_Revoke = "You should be able to negate the effect of the Totem of Retreat by wielding this.";

    }



    public static class Message
    {
        @Config.Comment("This message is displayed when the player applies the totem to an entity.(Can use %target%.)")
        public String msg_ApplyTotem = "Applied the totem effect to %target%.";
        @Config.Comment("This message is displayed when the player tries to apply the totem to an entity which already has it.(Can use %target%.)")
        public String msg_AlreadyHas = "%target% already has the totem effect.";
        @Config.Comment("This message is displayed when the player revokes the totem from the entity.(Can use %target%.)")
        public String msg_RevokeTotem = "Revoked the totem effect from %target%.";
        @Config.Comment("This message is displayed when a player attempts to revoke the totem despite the entity not having it.(Can use %target%.)")
        public String msg_NoAppliedTotem = "%target% doesn't have the totem.";
        @Config.Comment("This message is displayed when the player fails to apply the totem because the entity has another one.(Can use %target%.)")
        public String msg_FailedApplyTotem = "%target% has already had the totem effect applied by another player.";
        @Config.Comment("This message is displayed when the player fails to revoke the totem because the entity has another one.(Can use %target%.)")
        public String msg_FailedRevokeTotem = "The %target%'s totem effect is applied by another player.";
        @Config.Comment("This message is displayed to the owner when the entity retreated.(Can use %victim% and %attacker%.)")
        public String msg_Retreated = "%victim% were forced to retreat by %attacker%.";
        @Config.Comment("This message is sent to a random player when an owner of the retreated entity is offline.(Can use %victim% and %attacker%.)")
        public String msg_RetreatedWhenOwnerOffline = "%victim% has retreated your spawn point. (Because owner offline.)";
        @Config.Comment("This message is displayed when you retreat your pets.(Can use %target%.)")
        public String msg_Retreated_byOwner = "%target% has retreated your spawn point.";
        @Config.Comment("This message is displayed when you using the Pet Command Rod.(Can use %number%.)")
        public String msg_UseCommandRod = "%number% pets following.";
        @Config.Comment("This message is displayed when you swing the Pet Transfer Rod.(Can use %number%.)")
        public String msg_SwingTransferRod = "%number% pets transferred.";
        @Config.Comment("This message is displayed when you use the Pet Transfer Rod.")
        public String msg_UseTransferRod = "Location registered.";
        @Config.Comment("This message is displayed when you swing the Pet Retreat Rod while sneaking.(Can use %number%.)")
        public String msg_SneakSwingRetreatRod = "%number% pets retreated.";
        @Config.Comment("This message is displayed when you use the Pet Retreat Rod.(Can use %number%.)")
        public String msg_UseRetreatRod = "%number% pets highlighted.";
        @Config.Comment("This message is displayed when you use the Pet Retreat Rod while sneaking.(Can use %number%.)")
        public String msg_SneakUseRetreatRod = "%number% pets detected.";

    }



    public static class Rod
    {
        @Config.RequiresMcRestart
        @Config.Comment("If set to false, disable the Pet Command Rod.")
        public boolean enableCommandRod = true;
        @Config.RequiresMcRestart
        @Config.Comment("If set to false, disable the Pet Transfer Rod.")
        public boolean enableTransferRod = true;
        @Config.RequiresMcRestart
        @Config.Comment("If set to false, disable the Pet Retreat Rod.")
        public boolean enableRetreatRod = true;
        @Config.RequiresMcRestart
        @Config.Comment("If set to false, disable the recipe of the Pet Command Rod.")
        public boolean enableCommandRod_Recipe = true;
        @Config.RequiresMcRestart
        @Config.Comment("If set to false, disable the recipe of the Pet Transfer Rod.")
        public boolean enableTransferRod_Recipe = true;
        @Config.RequiresMcRestart
        @Config.Comment("If set to false, disable the recipe of the Pet Retreat Rod.")
        public boolean enableRetreatRod_Recipe = true;
        @Config.RangeInt(min = 1)
        @Config.Comment("The radius within which the effect of the command rod is applied when swung.")
        public int radiusSwingCommandRod = 64;
        @Config.RangeInt(min = 1)
        @Config.Comment("The radius within which the effect of the command rod is applied when used.")
        public int radiusUseCommandRod = 28;
        @Config.RangeInt(min = 0)
        @Config.Comment("The radius within which the effect of the command rod is not applied when used.(Need a lower value than radiusUseCommandRod.)")
        public int ignoreRadiusUseCommandRod = 16;
        @Config.RequiresMcRestart
        @Config.RangeInt(min = 1)
        @Config.Comment("The effect of using the command rod will continue to cast with this number of ticks.(Higher value = Higher load)")
        public int intervalUseCommandRod = 10;
        @Config.RangeInt(min = 1)
        @Config.Comment("The duration which the keep location data in transfer rod.")
        public int effectDurationTransferRod = 200;
        @Config.RangeInt(min = 1)
        @Config.Comment("The radius within which the transfer rod's effect is applied.")
        public int radiusTransferRod = 8;
        @Config.RangeInt(min = 1)
        @Config.Comment("The range within which the retreat rod's effect(Swing) is applied.(Higher value = Higher load)")
        public int rangeSwingRetreatRod = 64;
        @Config.RangeInt(min = 1)
        @Config.Comment("The radius within which the retreat rod's effect(Sneak+Swing) is applied.")
        public int radiusSneakSwingRetreatRod = 64;
        @Config.RangeInt(min = 0)
        @Config.Comment("The radius within which the retreat rod's effect(Sneak+Use) is applied.(0 = Disable.)")
        public int radiusUseRetreatRod = 128;
        @Config.RangeInt(min = 0)
        @Config.Comment("The radius within which the retreat rod's effect(Sneak+Use) is applied.(0 = Disable.)")
        public int radiusSneakUseRetreatRod = 128;
        @Config.RequiresMcRestart
        @Config.Comment("Tooltip for Pet Command Rod.(Can use %nl%, %swing-radius% and %use-radius%.)")
        public String commandRodToolTip = "A rod to command pets bound with the totem power.%nl%%nl%Swing: Calls pets within a %swing-radius%-block radius.%nl%Use: Keeps pets within an %use-radius%-block radius.";
        @Config.RequiresMcRestart
        @Config.Comment("Tooltip for Pet Transfer Rod.(Can use %nl% and %radius%.)")
        public String transferRodToolTip = "A rod to transfer pets bound with the totem power.%nl%%nl%Swing: Teleports pets within a %radius%-block radius to the registered location.%nl%Use: Temporarily registers the location where is the location of you.";
        @Config.RequiresMcRestart
        @Config.Comment("Tooltip for Pet Retreat Rod.(Can use %nl%, %swing-range%, %sneakswing-radius%, %use-radius% and %sneakuse-radius%.)")
        public String retreatRodToolTip = "A rod to retreat pets bound with the totem power.%nl%%nl%Swing: Retreats target.%nl%Sneak+Swing: Retreats the pets within a %sneakswing-radius%-block radius.%nl%Use: Highlights the pets within a %use-radius%-block radius.%nl%Sneak+Use: Get a coordinate of the pets within a %sneakuse-radius%-block radius.";
    }



    public static class KillLog
    {
        @Config.Comment("If set to true, when you or your pets kill an entity, it will display the kill log on the action bar.")
        public boolean enableKillLog = true;
        @Config.Comment("If set to true, it will display the kill log only when your pet kills.")
        public boolean KillLogOnlyPets = true;
        @Config.Comment("The format of the kill log generated by this mod.(Can use %attacker% and %victim%.)")
        public String killLogFormat = "%attacker%-> %victim%";
    }



    //ゲーム内からConfigを変更したときのイベント
    @Mod.EventBusSubscriber(modid = ID_MOD)
    private static class EventHandler
    {
        @SubscribeEvent
        public static void onConfigChanged(final ConfigChangedEvent.OnConfigChangedEvent event)
        {
            if (event.getModID().equals(ID_MOD))
            {
                ConfigManager.sync(ID_MOD, Config.Type.INSTANCE);
            }
        }
    }
}