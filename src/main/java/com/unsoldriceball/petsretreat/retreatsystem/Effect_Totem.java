package com.unsoldriceball.petsretreat.retreatsystem;

import com.unsoldriceball.petsretreat.RetreatConfig;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

import java.util.UUID;

import static com.unsoldriceball.petsretreat.RetreatMain.ID_MOD;
import static com.unsoldriceball.petsretreat.RetreatMain.f_Item_Totem_Hard;
import static com.unsoldriceball.petsretreat.RetreatUtils.*;




public class Effect_Totem
{
    final public static String HARD_TAG = "@`" + ID_MOD + "_Hard"; //HardRecipe版RetreatTotemを使った時に追加で付与されるタグ。これがあると撤退時に再生効果が付く。




    //各Totemの効果を発動させる関数。RetreatSystemから呼び出される。(ServerOnly)
    public static void TotemInteractionForEntity(EntityPlayer p, EntityLivingBase target, ItemStack stack)
    {
        final World _WORLD = target.getEntityWorld();
        final Vec3d _LOC_TARGET = getEntityLoc(target);
        //EntityのTagにplayerのUUIDを刻み込むことでownerを、MOD_IDを刻み込むことで撤退の対象であることをそれぞれ識別可能にする。
        final String _TAG_WITHUUID = "@" + ID_MOD + "_" + p.getUniqueID();
        //そもそも誰かのUUID_TAGを持っているかどうか。
        final UUID _HAS_UUIDTAG = hasTotemPower(target);

        //持っていたアイテムに応じて効果を発動させる。
        if (stack.getItem() instanceof Item_TotemOfRetreat)
        {
            TotemEffect_Retreat(p, target, _WORLD, _LOC_TARGET, stack, _TAG_WITHUUID, _HAS_UUIDTAG);
        }
        else if (stack.getItem() instanceof  Item_TotemOfRevoke)
        {
            TotemEffect_Revoke(p, target, _WORLD, _LOC_TARGET, _TAG_WITHUUID, _HAS_UUIDTAG);
        }
    }



    //Entityに撤退を可能にするTagを刻み込む関数(ServerOnly)
    public static void TotemEffect_Retreat(EntityPlayer p, EntityLivingBase target, World world, Vec3d loc_target, ItemStack stack, String tag_withuuid, UUID uuid_hastarget)
    {
        String message;


        //tag_withuuidを持ってない
        if (uuid_hastarget == null)
        {
            //EntityのTagにtag_withuuidを刻み込む。
            target.addTag(tag_withuuid);
            if (stack.getItem() == f_Item_Totem_Hard)
            {
                target.addTag(HARD_TAG);
            }

            //Actionbarにメッセージを表示
            message = RetreatConfig.c_Message.msg_ApplyTotem;
            message = message.replace("%target%", getName(target));

            p.sendStatusMessage(new TextComponentString(message), true);
            //---

            //config設定に基づいてアイテムを操作
            if (RetreatConfig.c_Totem.totemOneTime)
            {
                //Configで還元するように設定されているならアイテムを還元する。
                if (RetreatConfig.c_Totem.totemOneTime_revert)
                {
                    final Item F_REDUCEDITEM;

                    //使用したアイテムに応じたアイテムを還元する。
                    if (stack.getItem() == f_Item_Totem_Hard)
                    {
                        F_REDUCEDITEM = Item.getItemFromBlock(Blocks.GOLD_BLOCK);
                    }
                    else
                    {
                        F_REDUCEDITEM = Items.GOLD_INGOT;
                    }

                    p.setHeldItem(EnumHand.MAIN_HAND, new ItemStack(F_REDUCEDITEM, 1));
                }
                //還元しないなら消滅させる。
                else
                {
                    p.setHeldItem(EnumHand.MAIN_HAND, new ItemStack(Items.AIR));
                }
            }

            //以下、演出
            world.playSound(null, loc_target.x, loc_target.y, loc_target.z, SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 0.8f, 1.78f);
            final int _COUNT_PARTICLE = 6;
            final float _SPEED_PARTICLE = 2.0f;
            initPlayParticle(p, EnumParticleTypes.VILLAGER_HAPPY, _COUNT_PARTICLE, _SPEED_PARTICLE, true, loc_target);

            target.motionX = 0.0;
            target.motionY = 0.25;
            target.motionZ = 0.0;
        }
        //持ってる
        else
        {
            //TagのUUIDがPLAYERと一致
            if (target.getTags().contains(tag_withuuid))
            {
                //もうすでに適用済みであることを知らせるメッセージ
                message = RetreatConfig.c_Message.msg_AlreadyHas;
            }
            //Tagは持ってるけど他人のUUID
            else
            {
                //もうすでに他者によって適用済みであることを知らせるメッセージ
                message = RetreatConfig.c_Message.msg_FailedApplyTotem;
            }
            //Actionbarにメッセージを表示
            message = message.replace("%target%", getName(target));
            p.sendStatusMessage(new TextComponentString(message), true);
        }
    }



    //Entityの撤退を可能にするTagを取り消す関数(ServerOnly)
    public static void TotemEffect_Revoke(EntityPlayer p, EntityLivingBase target, World world, Vec3d loc_target, String tag_withuuid, UUID uuid_hastarget)
    {
        String message;


        //tag_withuuidを持ってない
        if (uuid_hastarget == null)
        {
            //Actionbarにメッセージを表示
            message = RetreatConfig.c_Message.msg_NoAppliedTotem;
            message = message.replace("%target%", getName(target));

            p.sendStatusMessage(new TextComponentString(message), true);
        }
        //持ってる
        else
        {
            //TagのUUIDがPLAYERと一致
            if (target.getTags().contains(tag_withuuid))
            {
                //EntityのTagからUUID_TAGを削除。
                target.removeTag(tag_withuuid);
                if (hasHardTotem(target))
                {
                    target.removeTag(HARD_TAG);
                }

                //config設定に基づいてアイテムを操作
                if (RetreatConfig.c_Totem.totemOneTime)
                {
                    //Configで還元するように設定されているならアイテムを還元する。
                    if (RetreatConfig.c_Totem.totemOneTime_revert)
                    {
                        final Item F_REDUCEDITEM = Item.getItemFromBlock(Blocks.LOG2);
                        p.setHeldItem(EnumHand.MAIN_HAND, new ItemStack(F_REDUCEDITEM, 1));
                    }
                    //還元しないなら消滅させる。
                    else
                    {
                        p.setHeldItem(EnumHand.MAIN_HAND, new ItemStack(Items.AIR));
                    }
                }

                //Actionbarにメッセージを表示
                message = RetreatConfig.c_Message.msg_RevokeTotem;
                message = message.replace("%target%", getName(target));

                p.sendStatusMessage(new TextComponentString(message), true);
                //---

                //以下、演出
                world.playSound(null, loc_target.x, loc_target.y, loc_target.z, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.PLAYERS, 0.8f, 1.2f);
                final int _COUNT_PARTICLE = 20;
                final float _SPEED_PARTICLE = 1.0f;
                initPlayParticle(p, EnumParticleTypes.SMOKE_NORMAL, _COUNT_PARTICLE, _SPEED_PARTICLE, true, loc_target);

                target.motionX = 0.0;
                target.motionY = 0.25;
                target.motionZ = 0.0;
            }
            //Tagは持ってるけど他人のUUID
            else
            {
                //Actionbarにメッセージを表示
                message = RetreatConfig.c_Message.msg_FailedRevokeTotem;
                message = message.replace("%target%", getName(target));

                p.sendStatusMessage(new TextComponentString(message), true);
            }
        }
    }

}
