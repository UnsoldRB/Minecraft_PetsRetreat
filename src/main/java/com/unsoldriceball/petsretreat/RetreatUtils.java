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
    //Particle�����֐�
    public static void initPlayParticle (EntityPlayer player, EnumParticleTypes ptype, int count, float speed, boolean spread, Vec3d loc)
    {
        //�p�[�e�B�N���̐����̓N���C�A���g�Ŏ��s���Ȃ��Ɣ��f����Ȃ��̂ŁA�T�[�o�[����p�P�b�g�𑗐M����B
        //���W�Ɋւ��ẮA�T�[�o�[�ƃN���C�A���g�ԂŃY��������̂ŁA�v���C���[�����_�Ƃ�������n���B
        if (!player.getEntityWorld().isRemote)
        {
            final int _DIM = player.dimension;
            final UUID _UUID_PLAYER = player.getUniqueID();
            final int _ID_PARTICLE = ptype.getParticleID();
            final Vec3d _LOC_P = getEntityLoc(player);
            final float _X = (float) (_LOC_P.x - loc.x);
            final float _Y = (float) (_LOC_P.y - loc.y);
            final float _Z = (float) (_LOC_P.z - loc.z);

            //�ȉ��Adimension����v����SPlayer��Packet�𑗐M����B
            f_wrapper_particle.sendToDimension((new Packet_Particle(_UUID_PLAYER, _ID_PARTICLE, count, speed, spread, _X, _Y, _Z)), _DIM);
        }
    }



    //�P�ނ̃g�[�e�����K�p����Ă���mob���A�w�肳�ꂽ���a�Ō�������B
    public static List<EntityLivingBase> searchEntities(EntityPlayer p, float radius, Vec3d loc)
    {
        final World _WORLD = p.world;
        final UUID _UUID_OWNER = p.getUniqueID();
        List<EntityLivingBase> _result = new ArrayList<>();


        //�v���C���[�𒆐S�Ƃ��āAradius����EntityLivingBase�S�Ă��擾����B
        final List<EntityLivingBase> _TARGETS = _WORLD.getEntitiesWithinAABB
                (
                    EntityLivingBase.class, new AxisAlignedBB
                        (
                            loc.x - radius, loc.y - radius, loc.z - radius,
                            loc.x + radius, loc.y + radius, loc.z + radius
                        )
                );

        //L_TARGETS���̃g�[�e���p���[���t�^���ꂽ�Ap��mob(�y�b�g)���擾����B
        for (EntityLivingBase __e: _TARGETS)
        {
            UUID _uuid = hasTotemPower(__e);

            if (_uuid == null) continue;
            if (!_uuid.equals(_UUID_OWNER)) continue;

            _result.add(__e);
        }
        return _result;
    }



    //�v���C���[�̎��_�̐�ɂ���A�v���C���[�̃y�b�g��1�̎擾����֐��B
    public static EntityLivingBase getLookingEntity(EntityPlayer p, int range)
    {
        //Player�̎��_�̐�ɂ���Entity���擾����B
        final Vec3d _VECTOR_LOOK = p.getLook(1.0f);
        final Vec3d _LOC = new Vec3d(p.posX, p.posY + p.getEyeHeight(), p.posZ);
        final float _THICKNESS_LINE = 0.3f;                 //�u���_�̐�v�Ɋ܂߂镝�B0.1f�Ƃ��ɂ����當���ʂ莋�_�̐�ɂȂ�񂾂낤���Ǖ��ׂ������B
        final int _NUM_LOOP = (int) Math.ceil(range / _THICKNESS_LINE);    //�K�v��Loop�񐔁B_THICKNESS_LINE���������قǃ��[�v�񐔂������Ȃ�B

        //_THICKNESS_LINE�̒l���A���_�̐�Ɉړ����Ȃ���A���a_THICKNESS_LINE�͈̔͂�EntityLivingBase���擾��������B
        //range�ɒB����܂ŁA���邢��EntityLivingBase�����o����܂ő�����B
        // calculateIntercept���g�����ق����y���񂾂낤���B�ǂ��Ȃ񂾂낤�B
        for (int __i = 1; __i <= _NUM_LOOP; __i++)
        {
            Vec3d __point_line = _LOC.add(new Vec3d(_VECTOR_LOOK.x * __i, _VECTOR_LOOK.y * __i, _VECTOR_LOOK.z * __i));
            List<EntityLivingBase> __result = RetreatUtils.searchEntities(p, _THICKNESS_LINE, __point_line);
            if (!__result.isEmpty())
            {
                //���o�����Ώۂ̒��ŁA�����Ƃ��v���C���[�ɋ߂����̂�Ԃ��B
                EntityLivingBase __nearest_entity = null;
                double __distance_sq_nearest_entity = Math.pow(range, 2.0d);    //���߂͍Œ��̌����͈͂�ݒ肵�Ă����B

                for (EntityLivingBase ___e: __result)
                {
                    double ___distance_target = ___e.getDistanceSq(p);

                    if (___distance_target < __distance_sq_nearest_entity)
                    {
                        __nearest_entity = ___e;
                        __distance_sq_nearest_entity = ___distance_target;
                    }
                }

                if (__nearest_entity != null)   //null�ł��邱�Ƃ͍l���ɂ�������ǁA�ꉞif������B
                {
                    return __nearest_entity;
                }
            }
        }
        return null;
    }



    //�����Ɏw�肵��Entity���A�P�ނ̃g�[�e���K�p�ς݂Ȃ�A�K�p���UUID��Ԃ��֐��B
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



    //�����Ɏw�肵��Entity���AHard�ł̓P�ނ̃g�[�e����K�p�ς݂Ȃ�true��Ԃ��֐��B
    public static boolean hasHardTotem(EntityLivingBase e)
    {
        return e.getTags().contains(HARD_TAG);
    }



    //entity�̍��W�����Ƃ��Ăł��擾����֐�
    @Nonnull
    public static Vec3d getEntityLoc(EntityLivingBase entity)
    {
        Vec3d _loc = entity.getPositionVector();

        //���炩�̌����ō��W�������Ȃ������ꍇ�B(FakePlayer��)
        if (_loc.equals(new Vec3d(0.0, 0.0, 0.0)))
        {
            _loc = new Vec3d(entity.posX, entity.posY, entity.posZ);
        }

        return _loc;
    }



    //����mod�ɂƂ��ēs���̂���������Ԃ��֐�
    public static double randomValue(int seed, int max)
    {
        //-max/100�`max/100�̗����𐶐�����
        final Random _RAND = new Random(seed * System.currentTimeMillis()); //����seed��System.currentTimeMillis()����Z���ċꂵ����Ƀ����_��������������B
        //�����_���ɐ��������l�ɑ΂��Ĉ����₷���悤�ɔ��������s��
        final int _RANDOMVALUE = _RAND.nextInt(max * 2);
        return (double) (_RANDOMVALUE - max) / 100;
    }



    //�����ɓn���ꂽString�̉��s�R�[�h�𐳎��Ȃ��̂ɒu������֐��B
    public static String encodeString(String data)
    {
        return data.replaceAll("%nl%", "\n");
    }



    //entity��DisplayName���擾���邾���̊֐��B�ǐ��̓s���ō���Ă݂��B
    @Nonnull
    public static String getName(Entity entity)
    {
        return entity.getDisplayName().getUnformattedText();
    }




    //ordinal����RetreatEnum�ɕϊ�����֐��B
    public static Enum_Rod toRetreatEnum(int ordinal)
    {
        return  Enum_Rod.values()[ordinal];
    }



    //2�_�Ԃ̋�����2���Ԃ��֐��B
    public static double getSquareDistance(Vec3d a, Vec3d b)
    {
        return a.squareDistanceTo(b);
    }
}
