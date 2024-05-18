package com.unsoldriceball.petsretreat.retreatsystem;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ITeleporter;

import static com.unsoldriceball.petsretreat.RetreatUtils.randomValue;
import static com.unsoldriceball.petsretreat.retreatsystem.RetreatSystem.resetEntityStatus;




public class Teleporter_Retreat implements ITeleporter
{
    private static int f_lx;
    private static int f_ly;
    private static int f_lz;
    private static boolean f_do_Potion;
    private static int f_duration_Potion;
    private static Potion f_potionEffect;




    //撤退に伴ってdimensionの移動が生じた場合に呼び出される
    public Teleporter_Retreat(int i_lx, int i_ly, int i_lz, boolean i_do_Potion, int i_duration_Potion, Potion i_potionEffect)
    {
        f_lx = i_lx;
        f_ly = i_ly;
        f_lz = i_lz;
        f_do_Potion = i_do_Potion;
        f_duration_Potion = i_duration_Potion;
        f_potionEffect = i_potionEffect;
    }



    //dimensionの移動処理が完了した場合に呼び出される。
    @Override
    public void placeEntity(World world, Entity entity, float yaw)
    {
        if (world.isRemote) return;
        if (!(entity instanceof EntityLivingBase)) return;

        final EntityLivingBase _TARGET = (EntityLivingBase) entity;

        _TARGET.setPosition(f_lx, f_ly, f_lz);
        _TARGET.motionX = randomValue(1, 50);
        _TARGET.motionY = 0.5d;
        _TARGET.motionZ = randomValue(2, 50);

        if (f_do_Potion)
        {
            _TARGET.addPotionEffect(new PotionEffect(f_potionEffect, f_duration_Potion, 1));
        }
        else
        {
            //LivingUpdateを停止しない場合はこの時点でEntityを初期化する。
            resetEntityStatus(_TARGET);
        }
    }
}
