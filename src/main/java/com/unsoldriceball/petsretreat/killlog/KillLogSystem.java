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
    //Entityが死亡したときのイベント
    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event)
    {
        if (!RetreatConfig.c_KillLog.enableKillLog) return;
        if (event.getEntity().world.isRemote) return;
        if (event.getSource().getTrueSource() == null) return;
        if (event.getEntityLiving() == null) return;

        final Entity _ATTACKER = event.getSource().getTrueSource();
        final EntityLivingBase _VICTIM = event.getEntityLiving();


        //攻撃したのがプレイヤーなら、一般的なキルログとして機能させる。(configで有効な場合のみ)
        if (_ATTACKER instanceof EntityPlayer)
        {
            if (!RetreatConfig.c_KillLog.KillLogOnlyPets)
            {
                showKillLog((EntityPlayer) _ATTACKER, _ATTACKER, _VICTIM);
            }
        }
        //Petが攻撃した場合の処理
        else if (_ATTACKER instanceof EntityLivingBase)
        {
            final EntityLivingBase _ATTACKER_ELB = (EntityLivingBase) _ATTACKER;

            //EntityからownerのUUIDを抜き取る
            final UUID _UUID_OWNER = hasTotemPower(_ATTACKER_ELB);

            //ownerが存在した場合(撤退のトーテム適用済みの場合)
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
