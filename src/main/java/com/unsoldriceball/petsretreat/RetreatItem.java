package com.unsoldriceball.petsretreat;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
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
import net.minecraftforge.common.util.FakePlayer;


import java.util.List;

import static com.unsoldriceball.petsretreat.RetreatMain.*;
import static com.unsoldriceball.petsretreat.RetreatUtils.*;

public class RetreatItem extends Item {
    public String ID_TOTEM_OF_RETREAT = "totem_of_retreat";
    final public static String HARD_TAG = "@`" + MOD_ID + "_Hard"; //HardRecipe版RetreatTotemを使った時に追加で付与されるタグ。これがあると撤退時に再生効果が付く。

    public RetreatItem(boolean isHardRecipe) {
        super();

        if (isHardRecipe) {
            ID_TOTEM_OF_RETREAT += "_hard";
        }
        this.setRegistryName(MOD_ID, ID_TOTEM_OF_RETREAT);
        this.setUnlocalizedName(ID_TOTEM_OF_RETREAT);
        this.setCreativeTab(CreativeTabs.COMBAT);
        this.setMaxStackSize(1);
    }


    //Tooltipを登録
    @Override
    public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        tooltip.add(TOTEM_TOOLTIP);
    }



    //Entityに撤退を可能にするTagを刻み込む関数(ServerOnly)
    public void TotemInteractionForEntity(ItemStack stack, EntityPlayer playerIn, EntityLivingBase target) {
        // 設定の対象がEntityPlayerを除くEntityLivingBase(もしくはFakePlayer)で、
        // かつアイテムとしてRetreatTotem(Damage値が0)を持っている場合(ServerOnly)
        if (playerIn.getEntityWorld().isRemote) return;
        if ((target instanceof EntityPlayer) && !(target instanceof FakePlayer)) return;

        final World WORLD = target.getEntityWorld();
        final Vec3d LOC = getEntityLoc(target);
        String message;
        final String UUID_TAG = "@" + MOD_ID + "_" + playerIn.getUniqueID(); //EntityのTagにplayerのUUIDを刻み込むことでownerを、MOD_IDを刻み込むことで撤退の対象であることをそれぞれ識別可能にする。


        //そもそも誰かのUUID_TAGを持っているかどうか。
        boolean hasTag = false;
        for (String t : target.getTags()) {
            if (!t.contains("@" + MOD_ID)) continue;
            hasTag = true;
            break;
        }

        if (!hasTag) {  //持ってない
            //EntityのTagにUUID_TAGを刻み込む。
            target.addTag(UUID_TAG);
            if (stack.getItem() == retreatItem_hard) {
                target.addTag(HARD_TAG);
            }

            //Actionbarにメッセージを表示
            message = MESSAGE_TOTEM_APPLY;
            message = message.replace("%target%", getName(target));

            playerIn.sendStatusMessage(new TextComponentString(message), true);
            //---

            //以下、演出
            WORLD.playSound(null, LOC.x, LOC.y, LOC.z, SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 0.8f, 1.78f);
            initPlayParticle(playerIn, LOC, EnumParticleTypes.VILLAGER_HAPPY);

        } else { //持ってる
            if (target.getTags().contains(UUID_TAG)) { //TagのUUIDがPLAYERと一致
                //EntityのTagからUUID_TAGを削除。
                target.removeTag(UUID_TAG);
                if (target.getTags().contains(HARD_TAG)) {
                    target.removeTag(HARD_TAG);
                }

                //Actionbarにメッセージを表示
                message = MESSAGE_TOTEM_REVOKE;
                message = message.replace("%target%", getName(target));

                playerIn.sendStatusMessage(new TextComponentString(message), true);
                //---

                //以下、演出
                WORLD.playSound(null, LOC.x, LOC.y, LOC.z, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.PLAYERS, 0.8f, 1.2f);
                initPlayParticle(playerIn, LOC, EnumParticleTypes.SMOKE_NORMAL);
            } else { //Tagは持ってるけど他人のUUID
                //Actionbarにメッセージを表示
                message = MESSAGE_TOTEM_FAILED;
                message = message.replace("%target%", getName(target));

                playerIn.sendStatusMessage(new TextComponentString(message), true);
                //---

                //以下、演出
                WORLD.playSound(null, LOC.x, LOC.y, LOC.z, SoundEvents.ENTITY_VILLAGER_NO, SoundCategory.PLAYERS, 0.8f, 0.8f);

                return;
            }
        }


        if (TOTEM_ONETIME) { //config設定に基づいてアイテムを還元
            Item REDUCEDITEM;

            //使用したアイテムに応じたアイテムを還元する。
            if (stack.getItem() == retreatItem_hard) {
                REDUCEDITEM = Items.TOTEM_OF_UNDYING;
            } else {
                REDUCEDITEM = Item.getItemFromBlock(Blocks.GOLD_BLOCK);
            }

            playerIn.setHeldItem(EnumHand.MAIN_HAND, new ItemStack(REDUCEDITEM, 1));
        }

        //以下、攻撃によって発生したEntityの移動を上書きする処理
        target.motionX = 0.0;
        target.motionY = 0.25;
        target.motionZ = 0.0;

    }
}
