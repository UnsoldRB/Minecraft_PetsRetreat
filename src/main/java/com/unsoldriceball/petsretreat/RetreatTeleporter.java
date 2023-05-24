package com.unsoldriceball.petsretreat;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ITeleporter;

import static com.unsoldriceball.petsretreat.RetreatMain.resetEntityStatus;
import static com.unsoldriceball.petsretreat.RetreatUtils.randomVec;


public class RetreatTeleporter implements ITeleporter {

    private static int lx;
    private static int ly;
    private static int lz;
    private static boolean do_Potion;
    private static int duration_Potion;
    private static Potion potionEffect;

    public RetreatTeleporter(int i_lx, int i_ly, int i_lz, boolean i_do_Potion, int i_duration_Potion, Potion i_potionEffect) {
        //撤退に伴ってdimentionの移動が生じた場合に呼び出される
        lx = i_lx;
        ly = i_ly;
        lz = i_lz;
        do_Potion = i_do_Potion;
        duration_Potion = i_duration_Potion;
        potionEffect = i_potionEffect;
    }
    @Override
    public void placeEntity(World world, Entity entity, float yaw) {
        //dimentionの移動処理が完了した場合に呼び出される。
        if (world.isRemote) return;
        if (!(entity instanceof EntityLivingBase)) return;
        final EntityLivingBase ENTITY = (EntityLivingBase) entity;

        ENTITY.setPosition(lx, ly, lz);
        ENTITY.motionX = randomVec(1);
        ENTITY.motionY = 0.5;
        ENTITY.motionZ = randomVec(2);
        if (do_Potion) {
            ENTITY.addPotionEffect(new PotionEffect(potionEffect, duration_Potion, 1));
        }
        else {
            //LivingUpdateを停止しない場合はこの時点でEntityを初期化する。
            resetEntityStatus(ENTITY);
        }
    }
}
