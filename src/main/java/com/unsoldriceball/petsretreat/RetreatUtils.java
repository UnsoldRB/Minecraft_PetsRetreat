package com.unsoldriceball.petsretreat;


import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nonnull;
import java.util.Random;

public class RetreatUtils {


    //このmodにとって都合のいい乱数を返す関数
    public static double randomVec(int seed) { //-0.5～0.5の乱数を生成する
        final Random RAND = new Random(seed * System.currentTimeMillis()); //引数seedとSystem.currentTimeMillis()を乗算して苦し紛れにランダム性を持たせる。
        return (double) (RAND.nextInt(100) - 50) / 100; //ランダムに生成した値に対して扱いやすいように微調整を行う
    }



    @Nonnull
    //entityの座標を何としてでも取得する関数
    public static Vec3d getEntityLoc(EntityLivingBase entity) {
        Vec3d loc;
        loc = entity.getPositionVector();

        //何らかの原因で座標が得られなかった場合。(FakePlayer等)
        if (loc.equals(new Vec3d(0.0, 0.0, 0.0))) {
            loc = new Vec3d(entity.posX, entity.posY, entity.posZ);
        }

        return loc;
    }



    @Nonnull
    //entityのDisplayNameを取得するだけの関数。可読性の都合で作ってみた。
    public static String getName(Entity entity) {
        return entity.getDisplayName().getUnformattedText();
    }
}
