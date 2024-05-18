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
    final public static String HARD_TAG = "@`" + ID_MOD + "_Hard"; //HardRecipe��RetreatTotem���g�������ɒǉ��ŕt�^�����^�O�B���ꂪ����ƓP�ގ��ɍĐ����ʂ��t���B




    //�eTotem�̌��ʂ𔭓�������֐��BRetreatSystem����Ăяo�����B(ServerOnly)
    public static void TotemInteractionForEntity(EntityPlayer p, EntityLivingBase target, ItemStack stack)
    {
        final World _WORLD = target.getEntityWorld();
        final Vec3d _LOC_TARGET = getEntityLoc(target);
        //Entity��Tag��player��UUID�����ݍ��ނ��Ƃ�owner���AMOD_ID�����ݍ��ނ��ƂœP�ނ̑Ώۂł��邱�Ƃ����ꂼ�ꎯ�ʉ\�ɂ���B
        final String _TAG_WITHUUID = "@" + ID_MOD + "_" + p.getUniqueID();
        //���������N����UUID_TAG�������Ă��邩�ǂ����B
        final UUID _HAS_UUIDTAG = hasTotemPower(target);

        //�����Ă����A�C�e���ɉ����Č��ʂ𔭓�������B
        if (stack.getItem() instanceof Item_TotemOfRetreat)
        {
            TotemEffect_Retreat(p, target, _WORLD, _LOC_TARGET, stack, _TAG_WITHUUID, _HAS_UUIDTAG);
        }
        else if (stack.getItem() instanceof  Item_TotemOfRevoke)
        {
            TotemEffect_Revoke(p, target, _WORLD, _LOC_TARGET, _TAG_WITHUUID, _HAS_UUIDTAG);
        }
    }



    //Entity�ɓP�ނ��\�ɂ���Tag�����ݍ��ފ֐�(ServerOnly)
    public static void TotemEffect_Retreat(EntityPlayer p, EntityLivingBase target, World world, Vec3d loc_target, ItemStack stack, String tag_withuuid, UUID uuid_hastarget)
    {
        String message;


        //tag_withuuid�������ĂȂ�
        if (uuid_hastarget == null)
        {
            //Entity��Tag��tag_withuuid�����ݍ��ށB
            target.addTag(tag_withuuid);
            if (stack.getItem() == f_Item_Totem_Hard)
            {
                target.addTag(HARD_TAG);
            }

            //Actionbar�Ƀ��b�Z�[�W��\��
            message = RetreatConfig.c_Message.msg_ApplyTotem;
            message = message.replace("%target%", getName(target));

            p.sendStatusMessage(new TextComponentString(message), true);
            //---

            //config�ݒ�Ɋ�Â��ăA�C�e���𑀍�
            if (RetreatConfig.c_Totem.totemOneTime)
            {
                //Config�ŊҌ�����悤�ɐݒ肳��Ă���Ȃ�A�C�e�����Ҍ�����B
                if (RetreatConfig.c_Totem.totemOneTime_revert)
                {
                    final Item F_REDUCEDITEM;

                    //�g�p�����A�C�e���ɉ������A�C�e�����Ҍ�����B
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
                //�Ҍ����Ȃ��Ȃ���ł�����B
                else
                {
                    p.setHeldItem(EnumHand.MAIN_HAND, new ItemStack(Items.AIR));
                }
            }

            //�ȉ��A���o
            world.playSound(null, loc_target.x, loc_target.y, loc_target.z, SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 0.8f, 1.78f);
            final int _COUNT_PARTICLE = 6;
            final float _SPEED_PARTICLE = 2.0f;
            initPlayParticle(p, EnumParticleTypes.VILLAGER_HAPPY, _COUNT_PARTICLE, _SPEED_PARTICLE, true, loc_target);

            target.motionX = 0.0;
            target.motionY = 0.25;
            target.motionZ = 0.0;
        }
        //�����Ă�
        else
        {
            //Tag��UUID��PLAYER�ƈ�v
            if (target.getTags().contains(tag_withuuid))
            {
                //�������łɓK�p�ς݂ł��邱�Ƃ�m�点�郁�b�Z�[�W
                message = RetreatConfig.c_Message.msg_AlreadyHas;
            }
            //Tag�͎����Ă邯�Ǒ��l��UUID
            else
            {
                //�������łɑ��҂ɂ���ēK�p�ς݂ł��邱�Ƃ�m�点�郁�b�Z�[�W
                message = RetreatConfig.c_Message.msg_FailedApplyTotem;
            }
            //Actionbar�Ƀ��b�Z�[�W��\��
            message = message.replace("%target%", getName(target));
            p.sendStatusMessage(new TextComponentString(message), true);
        }
    }



    //Entity�̓P�ނ��\�ɂ���Tag���������֐�(ServerOnly)
    public static void TotemEffect_Revoke(EntityPlayer p, EntityLivingBase target, World world, Vec3d loc_target, String tag_withuuid, UUID uuid_hastarget)
    {
        String message;


        //tag_withuuid�������ĂȂ�
        if (uuid_hastarget == null)
        {
            //Actionbar�Ƀ��b�Z�[�W��\��
            message = RetreatConfig.c_Message.msg_NoAppliedTotem;
            message = message.replace("%target%", getName(target));

            p.sendStatusMessage(new TextComponentString(message), true);
        }
        //�����Ă�
        else
        {
            //Tag��UUID��PLAYER�ƈ�v
            if (target.getTags().contains(tag_withuuid))
            {
                //Entity��Tag����UUID_TAG���폜�B
                target.removeTag(tag_withuuid);
                if (hasHardTotem(target))
                {
                    target.removeTag(HARD_TAG);
                }

                //config�ݒ�Ɋ�Â��ăA�C�e���𑀍�
                if (RetreatConfig.c_Totem.totemOneTime)
                {
                    //Config�ŊҌ�����悤�ɐݒ肳��Ă���Ȃ�A�C�e�����Ҍ�����B
                    if (RetreatConfig.c_Totem.totemOneTime_revert)
                    {
                        final Item F_REDUCEDITEM = Item.getItemFromBlock(Blocks.LOG2);
                        p.setHeldItem(EnumHand.MAIN_HAND, new ItemStack(F_REDUCEDITEM, 1));
                    }
                    //�Ҍ����Ȃ��Ȃ���ł�����B
                    else
                    {
                        p.setHeldItem(EnumHand.MAIN_HAND, new ItemStack(Items.AIR));
                    }
                }

                //Actionbar�Ƀ��b�Z�[�W��\��
                message = RetreatConfig.c_Message.msg_RevokeTotem;
                message = message.replace("%target%", getName(target));

                p.sendStatusMessage(new TextComponentString(message), true);
                //---

                //�ȉ��A���o
                world.playSound(null, loc_target.x, loc_target.y, loc_target.z, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.PLAYERS, 0.8f, 1.2f);
                final int _COUNT_PARTICLE = 20;
                final float _SPEED_PARTICLE = 1.0f;
                initPlayParticle(p, EnumParticleTypes.SMOKE_NORMAL, _COUNT_PARTICLE, _SPEED_PARTICLE, true, loc_target);

                target.motionX = 0.0;
                target.motionY = 0.25;
                target.motionZ = 0.0;
            }
            //Tag�͎����Ă邯�Ǒ��l��UUID
            else
            {
                //Actionbar�Ƀ��b�Z�[�W��\��
                message = RetreatConfig.c_Message.msg_FailedRevokeTotem;
                message = message.replace("%target%", getName(target));

                p.sendStatusMessage(new TextComponentString(message), true);
            }
        }
    }

}
