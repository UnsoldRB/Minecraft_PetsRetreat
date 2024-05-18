package com.unsoldriceball.petsretreat.rods;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ITeleporter;

import static com.unsoldriceball.petsretreat.rods.Effect_Rod.transferRod_afterTeleport;


public class Teleporter_TransferRod implements ITeleporter
{
    private static EntityPlayer f_p;
    private static double f_lx;
    private static double f_ly;
    private static double f_lz;




    //コンストラクタ。
    public Teleporter_TransferRod(EntityPlayer p, Vec3d loc)
    {
        f_p = p;
        f_lx = loc.x;
        f_ly = loc.y;
        f_lz = loc.z;
    }



    //dimensionの移動処理が完了した場合に呼び出される。
    @Override
    public void placeEntity(World world, Entity entity, float v)
    {
        if (world.isRemote) return;
        if (!(entity instanceof EntityLivingBase)) return;

        final EntityLivingBase _TARGET = (EntityLivingBase) entity;

        _TARGET.setPosition(f_lx, f_ly, f_lz);
        transferRod_afterTeleport(f_p, _TARGET);
    }
}
