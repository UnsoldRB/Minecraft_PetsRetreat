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




public class RetreatItem extends Item
{
    final public static String HARD_TAG = "@`" + MOD_ID + "_Hard"; //HardRecipe版RetreatTotemを使った時に追加で付与されるタグ。これがあると撤退時に再生効果が付く。

    public String f_id_TotemOfRetreat = "totem_of_retreat";




    //コンストラクタ
    public RetreatItem(boolean isHardRecipe)
    {
        super();

        if (isHardRecipe)
        {
            f_id_TotemOfRetreat += "_hard";
        }
        this.setRegistryName(MOD_ID, f_id_TotemOfRetreat);
        this.setUnlocalizedName(f_id_TotemOfRetreat);
        this.setCreativeTab(CreativeTabs.COMBAT);
        this.setMaxStackSize(1);
    }


    //Tooltipを登録
    @Override
    public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn)
    {
        tooltip.add(TOTEM_TOOLTIP);
    }



    //Entityに撤退を可能にするTagを刻み込む関数(ServerOnly)
    public void TotemInteractionForEntity(ItemStack stack, EntityPlayer playerIn, EntityLivingBase target)
    {
        // 設定の対象がEntityPlayerを除くEntityLivingBase(もしくはFakePlayer)で、
        // かつアイテムとしてRetreatTotem(Damage値が0)を持っている場合(ServerOnly)
        if (playerIn.getEntityWorld().isRemote) return;
        if ((target instanceof EntityPlayer) && !(target instanceof FakePlayer)) return;

        final String L_UUID_TAG = "@" + MOD_ID + "_" + playerIn.getUniqueID(); //EntityのTagにplayerのUUIDを刻み込むことでownerを、MOD_IDを刻み込むことで撤退の対象であることをそれぞれ識別可能にする。
        final World L_WORLD = target.getEntityWorld();
        final Vec3d L_LOC = getEntityLoc(target);

        String message;


        //そもそも誰かのUUID_TAGを持っているかどうか。
        boolean hasTag = false;

        for (String _t : target.getTags())
        {
            if (!_t.contains("@" + MOD_ID)) continue;
            hasTag = true;
            break;
        }

        //持ってない
        if (!hasTag)
        {
            //EntityのTagにUUID_TAGを刻み込む。
            target.addTag(L_UUID_TAG);
            if (stack.getItem() == f_retreatItem_hard)
            {
                target.addTag(HARD_TAG);
            }

            //Actionbarにメッセージを表示
            message = MESSAGE_TOTEM_APPLY;
            message = message.replace("%target%", getName(target));

            playerIn.sendStatusMessage(new TextComponentString(message), true);
            //---

            //以下、演出
            L_WORLD.playSound(null, L_LOC.x, L_LOC.y, L_LOC.z, SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 0.8f, 1.78f);
            initPlayParticle(playerIn, L_LOC, EnumParticleTypes.VILLAGER_HAPPY);

        }
        //持ってる
        else
        {
            //TagのUUIDがPLAYERと一致
            if (target.getTags().contains(L_UUID_TAG))
            {
                //EntityのTagからUUID_TAGを削除。
                target.removeTag(L_UUID_TAG);
                if (target.getTags().contains(HARD_TAG))
                {
                    target.removeTag(HARD_TAG);
                }

                //Actionbarにメッセージを表示
                message = MESSAGE_TOTEM_REVOKE;
                message = message.replace("%target%", getName(target));

                playerIn.sendStatusMessage(new TextComponentString(message), true);
                //---

                //以下、演出
                L_WORLD.playSound(null, L_LOC.x, L_LOC.y, L_LOC.z, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.PLAYERS, 0.8f, 1.2f);
                initPlayParticle(playerIn, L_LOC, EnumParticleTypes.SMOKE_NORMAL);
            }
            //Tagは持ってるけど他人のUUID
            else
            {
                //Actionbarにメッセージを表示
                message = MESSAGE_TOTEM_FAILED;
                message = message.replace("%target%", getName(target));

                playerIn.sendStatusMessage(new TextComponentString(message), true);
                //---

                //以下、演出
                L_WORLD.playSound(null, L_LOC.x, L_LOC.y, L_LOC.z, SoundEvents.ENTITY_VILLAGER_NO, SoundCategory.PLAYERS, 0.8f, 0.8f);

                return;
            }
        }

        //config設定に基づいてアイテムを還元
        if (TOTEM_ONETIME)
        {
            Item F_REDUCEDITEM;

            //使用したアイテムに応じたアイテムを還元する。
            if (stack.getItem() == f_retreatItem_hard)
            {
                F_REDUCEDITEM = Items.TOTEM_OF_UNDYING;
            }
            else
            {
                F_REDUCEDITEM = Item.getItemFromBlock(Blocks.GOLD_BLOCK);
            }

            playerIn.setHeldItem(EnumHand.MAIN_HAND, new ItemStack(F_REDUCEDITEM, 1));
        }

        //以下、攻撃によって発生したEntityの移動を上書きする処理
        target.motionX = 0.0;
        target.motionY = 0.25;
        target.motionZ = 0.0;

    }
}
