package com.unsoldriceball.petsretreat.killlog;

import com.unsoldriceball.petsretreat.RetreatConfig;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.UUID;

import static com.unsoldriceball.petsretreat.RetreatUtils.hasTotemPower;




public class KillLogSystem
{
    //Entity�����S�����Ƃ��̃C�x���g
    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event)
    {
        if (!RetreatConfig.c_KillLog.enableKillLog) return;
        if (event.getEntity().world.isRemote) return;
        if (event.getSource().getTrueSource() == null) return;
        if (event.getEntityLiving() == null) return;

        final Entity _ATTACKER = event.getSource().getTrueSource();
        final EntityLivingBase _VICTIM = event.getEntityLiving();


        //�U�������̂��v���C���[�Ȃ�A��ʓI�ȃL�����O�Ƃ��ċ@�\������B(config�ŗL���ȏꍇ�̂�)
        if (_ATTACKER instanceof EntityPlayer)
        {
            if (!RetreatConfig.c_KillLog.KillLogOnlyPets)
            {
                showKillLog((EntityPlayer) _ATTACKER, _ATTACKER, _VICTIM);
            }
        }
        //Pet���U�������ꍇ�̏���
        else if (_ATTACKER instanceof EntityLivingBase)
        {
            final EntityLivingBase _ATTACKER_ELB = (EntityLivingBase) _ATTACKER;

            //Entity����owner��UUID�𔲂����
            final UUID _UUID_OWNER = hasTotemPower(_ATTACKER_ELB);

            //owner�����݂����ꍇ(�P�ނ̃g�[�e���K�p�ς݂̏ꍇ)
            if (_UUID_OWNER != null)
            {
                final EntityPlayer _OWNER = _ATTACKER_ELB.world.getPlayerEntityByUUID(_UUID_OWNER);

                if (_OWNER != null)
                {
                    showKillLog(_OWNER, _ATTACKER_ELB, _VICTIM);
                }
            }
        }
    }



    private void showKillLog(EntityPlayer p, Entity attacker, Entity victim)
    {
        final String NAME_ATTACKER = attacker.getDisplayName().getFormattedText();
        final String NAME_VICTIM = victim.getDisplayName().getFormattedText();
        String msg = RetreatConfig.c_KillLog.killLogFormat;

        msg = msg.replace("%attacker%", NAME_ATTACKER);
        msg = msg.replace("%victim%", NAME_VICTIM);

        p.sendStatusMessage(new TextComponentString(msg), true);
    }
}
