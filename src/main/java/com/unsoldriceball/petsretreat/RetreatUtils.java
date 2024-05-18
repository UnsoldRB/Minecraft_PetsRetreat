package com.unsoldriceball.petsretreat;


import com.unsoldriceball.petsretreat.rods.Enum_Rod;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.*;

import static com.unsoldriceball.petsretreat.RetreatMain.ID_MOD;
import static com.unsoldriceball.petsretreat.RetreatMain.f_wrapper_particle;
import static com.unsoldriceball.petsretreat.retreatsystem.Effect_Totem.HARD_TAG;


public class RetreatUtils
{
    //Particle生成関数
    public static void initPlayParticle (EntityPlayer player, EnumParticleTypes ptype, int count, float speed, boolean spread, Vec3d loc)
    {
        //パーティクルの生成はクライアントで実行しないと反映されないので、サーバーからパケットを送信する。
        //座標に関しては、サーバーとクライアント間でズレがあるので、プレイヤーを原点とした差を渡す。
        if (!player.getEntityWorld().isRemote)
        {
            final int _DIM = player.dimension;
            final UUID _UUID_PLAYER = player.getUniqueID();
            final int _ID_PARTICLE = ptype.getParticleID();
            final Vec3d _LOC_P = getEntityLoc(player);
            final float _X = (float) (_LOC_P.x - loc.x);
            final float _Y = (float) (_LOC_P.y - loc.y);
            final float _Z = (float) (_LOC_P.z - loc.z);

            //以下、dimensionが一致する全PlayerにPacketを送信する。
            f_wrapper_particle.sendToDimension((new Packet_Particle(_UUID_PLAYER, _ID_PARTICLE, count, speed, spread, _X, _Y, _Z)), _DIM);
        }
    }



    //撤退のトーテムが適用されているmobを、指定された半径で検索する。
    public static List<EntityLivingBase> searchEntities(EntityPlayer p, float radius, Vec3d loc)
    {
        final World _WORLD = p.world;
        final UUID _UUID_OWNER = p.getUniqueID();
        List<EntityLivingBase> _result = new ArrayList<>();


        //プレイヤーを中心として、radius内のEntityLivingBase全てを取得する。
        final List<EntityLivingBase> _TARGETS = _WORLD.getEntitiesWithinAABB
                (
                    EntityLivingBase.class, new AxisAlignedBB
                        (
                            loc.x - radius, loc.y - radius, loc.z - radius,
                            loc.x + radius, loc.y + radius, loc.z + radius
                        )
                );

        //L_TARGETS内のトーテムパワーが付与された、pのmob(ペット)を取得する。
        for (EntityLivingBase __e: _TARGETS)
        {
            UUID _uuid = hasTotemPower(__e);

            if (_uuid == null) continue;
            if (!_uuid.equals(_UUID_OWNER)) continue;

            _result.add(__e);
        }
        return _result;
    }



    //プレイヤーの視点の先にいる、プレイヤーのペットを1体取得する関数。
    public static EntityLivingBase getLookingEntity(EntityPlayer p, int range)
    {
        //Playerの視点の先にいるEntityを取得する。
        final Vec3d _VECTOR_LOOK = p.getLook(1.0f);
        final Vec3d _LOC = new Vec3d(p.posX, p.posY + p.getEyeHeight(), p.posZ);
        final float _THICKNESS_LINE = 0.3f;                 //「視点の先」に含める幅。0.1fとかにしたら文字通り視点の先になるんだろうけど負荷が高い。
        final int _NUM_LOOP = (int) Math.ceil(range / _THICKNESS_LINE);    //必要なLoop回数。_THICKNESS_LINEが小さいほどループ回数が多くなる。

        //_THICKNESS_LINEの値分、視点の先に移動しながら、半径_THICKNESS_LINEの範囲のEntityLivingBaseを取得し続ける。
        //rangeに達するまで、あるいはEntityLivingBaseを検出するまで続ける。
        // calculateInterceptを使ったほうが軽いんだろうか。どうなんだろう。
        for (int __i = 1; __i <= _NUM_LOOP; __i++)
        {
            Vec3d __point_line = _LOC.add(new Vec3d(_VECTOR_LOOK.x * __i, _VECTOR_LOOK.y * __i, _VECTOR_LOOK.z * __i));
            List<EntityLivingBase> __result = RetreatUtils.searchEntities(p, _THICKNESS_LINE, __point_line);
            if (!__result.isEmpty())
            {
                //検出した対象の中で、もっともプレイヤーに近いものを返す。
                EntityLivingBase __nearest_entity = null;
                double __distance_sq_nearest_entity = Math.pow(range, 2.0d);    //初めは最長の検索範囲を設定しておく。

                for (EntityLivingBase ___e: __result)
                {
                    double ___distance_target = ___e.getDistanceSq(p);

                    if (___distance_target < __distance_sq_nearest_entity)
                    {
                        __nearest_entity = ___e;
                        __distance_sq_nearest_entity = ___distance_target;
                    }
                }

                if (__nearest_entity != null)   //nullであることは考えにくいけれど、一応ifを入れる。
                {
                    return __nearest_entity;
                }
            }
        }
        return null;
    }



    //引数に指定したEntityが、撤退のトーテム適用済みなら、適用主のUUIDを返す関数。
    public static UUID hasTotemPower(EntityLivingBase e)
    {
        for(String _t : e.getTags())
        {
            if (!_t.contains("@" + ID_MOD)) continue;
            if (_t.contains(HARD_TAG)) continue;
            return UUID.fromString(_t.replace("@" + ID_MOD + "_", ""));
        }
        return null;
    }



    //引数に指定したEntityが、Hard版の撤退のトーテムを適用済みならtrueを返す関数。
    public static boolean hasHardTotem(EntityLivingBase e)
    {
        return e.getTags().contains(HARD_TAG);
    }



    //entityの座標を何としてでも取得する関数
    @Nonnull
    public static Vec3d getEntityLoc(EntityLivingBase entity)
    {
        Vec3d _loc = entity.getPositionVector();

        //何らかの原因で座標が得られなかった場合。(FakePlayer等)
        if (_loc.equals(new Vec3d(0.0, 0.0, 0.0)))
        {
            _loc = new Vec3d(entity.posX, entity.posY, entity.posZ);
        }

        return _loc;
    }



    //このmodにとって都合のいい乱数を返す関数
    public static double randomValue(int seed, int max)
    {
        //-max/100～max/100の乱数を生成する
        final Random _RAND = new Random(seed * System.currentTimeMillis()); //引数seedとSystem.currentTimeMillis()を乗算して苦し紛れにランダム性を持たせる。
        //ランダムに生成した値に対して扱いやすいように微調整を行う
        final int _RANDOMVALUE = _RAND.nextInt(max * 2);
        return (double) (_RANDOMVALUE - max) / 100;
    }



    //引数に渡されたStringの改行コードを正式なものに置換する関数。
    public static String encodeString(String data)
    {
        return data.replaceAll("%nl%", "\n");
    }



    //entityのDisplayNameを取得するだけの関数。可読性の都合で作ってみた。
    @Nonnull
    public static String getName(Entity entity)
    {
        return entity.getDisplayName().getUnformattedText();
    }




    //ordinalからRetreatEnumに変換する関数。
    public static Enum_Rod toRetreatEnum(int ordinal)
    {
        return  Enum_Rod.values()[ordinal];
    }



    //2点間の距離の2乗を返す関数。
    public static double getSquareDistance(Vec3d a, Vec3d b)
    {
        return a.squareDistanceTo(b);
    }
}
