package com.unsoldriceball.petsretreat.retreatsystem;

import com.unsoldriceball.petsretreat.RetreatConfig;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.*;

import static com.unsoldriceball.petsretreat.RetreatMain.*;
import static com.unsoldriceball.petsretreat.RetreatUtils.*;
import static com.unsoldriceball.petsretreat.RetreatUtils.randomValue;




public class RetreatSystem
{
    private int tick_count_petregeneration = 0;




    //Entity���P�ނ���ɒl���邩�𔻒f����֐�(FriendlyFire, Totem�K�p, �U�����������S���B)
    //���̃_���[�W�C�x���g����LittleMaid�ɑ΂���MP�̕���Entity���擾���Ă��܂��̂ł�������̗p�B
    @SubscribeEvent
    public void onEntityTakeAttack(LivingAttackEvent event)
    {
        //victim��EntityLivingBase���p�����A�C�x���g��Server���Ŕ������Ă���ꍇ
        if (!(event.getEntity() instanceof EntityLivingBase)) return;
        if (event.getEntityLiving().getEntityWorld().isRemote) return;

        final EntityLivingBase _VICTIM = event.getEntityLiving();
        final Entity _ATTACKER = event.getSource().getTrueSource();

        //retreatPotion���ʂ��󂯂Ă���ꍇ�͍U���𖳌������ďI��
        if (_VICTIM.isPotionActive(f_Potion_CancelUpdate))
        {
            event.setCanceled(true);
            return;
        }
        //---

        //_VICTIM��Tag����UUID�𔲂����B(_VICTIM��EntityPlayer�łȂ��A�P�ނ̃g�[�e���K�p�ς݂̏ꍇ�̂�)
        final UUID _UUID_OWNER_VICTIMHAS = hasTotemPower(_VICTIM);

        //�U�������̂��v���C���[�Ȃ�...
        if (_ATTACKER instanceof EntityPlayer && !(_ATTACKER instanceof FakePlayer))
        {
            final EntityPlayer _ATTACKER_EP = (EntityPlayer) _ATTACKER;
            final ItemStack _ITEM_ATTACKER_EP = _ATTACKER_EP.getHeldItemMainhand();

            // _VICTIM��EntityPlayer������EntityLivingBase(��������FakePlayer)�ł���Ȃ�
            if (_VICTIM instanceof FakePlayer || !(_VICTIM instanceof EntityPlayer))
            {
                //�U�������v���C���[�̎����Ă���A�C�e����retreatTotem��revokeTotem�ł���΁A�֐����g���K�[���ďI���B
                final Item _ITEM_ATTACKERHELD = _ITEM_ATTACKER_EP.getItem();

                if (_ITEM_ATTACKERHELD instanceof Item_TotemOfRetreat || _ITEM_ATTACKERHELD instanceof Item_TotemOfRevoke)
                {
                    //�����Aconfig��boss�Ƀg�[�e�����g���Ȃ��悤�ɂ��Ă��āA���A�Ώۂ��{�X�Ȃ�A����if���͏������Ȃ��B
                    if (!(!RetreatConfig.c_Totem.totemCanApplyToBoss && !_VICTIM.isNonBoss()))
                    {
                        Effect_Totem.TotemInteractionForEntity(_ATTACKER_EP, _VICTIM, _ITEM_ATTACKER_EP);
                        event.setCanceled(true);
                        return;
                    }
                }
                //FRIENDLYFIRE�������A�U�������v���C���[��Victim�̎����Ă���Tag��UUID����v����Ȃ�A�t�����h���[�t�@�C�A�𖳌������ďI���B
                //(Owner����Pet�ւ̍U�������������B)
                else if  (_UUID_OWNER_VICTIMHAS != null && !RetreatConfig.c_System.totemFriendlyFire_toPet)
                {
                    if (_ATTACKER_EP.getUniqueID().equals(_UUID_OWNER_VICTIMHAS))
                    {
                        event.setCanceled(true);
                        return;
                    }
                }
            }
        }

        //�U�������̂�EntityLivingBase�Ȃ�...(����if����FriendryFire�Ɋւ��鏈���̂݁B)
        else if (_ATTACKER instanceof EntityLivingBase)
        {
            final UUID _UUID_OWNER_ATTACKERHAS = hasTotemPower((EntityLivingBase) _ATTACKER);

            //_ATTACKER���g�[�e���̌��ʂ������Ă���Ȃ�B
            if (_UUID_OWNER_ATTACKERHAS != null)
            {
                //_VICTIM��EntityPlayer���A�U������Entity�������Ă���Tag��UUID�ƁA_VICTIM��UUID����v����Ȃ�A�t�����h���[�t�@�C�A�𖳌������ďI���B
                //(Pet����Owner�ւ̍U�������������B)
                if (_VICTIM instanceof EntityPlayer && !RetreatConfig.c_System.totemFriendlyFire_toOwner)
                {

                    if (_UUID_OWNER_ATTACKERHAS.equals(_VICTIM.getUniqueID()))
                    {
                        event.setCanceled(true);
                        return;
                    }
                }
                //�U������Entity�������Ă���Tag��UUID�ƁA_VICTIM�������Ă���Tag��UUID����v����Ȃ�A�t�����h���[�t�@�C�A�𖳌������ďI���B
                //(Pet����Pet�ւ̍U�������������B)
                else if (_UUID_OWNER_VICTIMHAS != null && !RetreatConfig.c_System.totemFriendlyFire_toPet)
                {
                    if (_UUID_OWNER_ATTACKERHAS.equals(_UUID_OWNER_VICTIMHAS))
                    {
                        event.setCanceled(true);
                        return;
                    }
                }
            }
        }

        //���̍U���Ŏ��S����ꍇ
        if ((_VICTIM.getHealth() - event.getAmount()) <= 0)
        {
            //_VICTIM���g�[�e���̌��ʂ������Ă���̂�������Ŕ���ł���B(owner�̗L��)
            if (_UUID_OWNER_VICTIMHAS != null)
            {
                //���S���L�����Z�����ēP�ނ�����B
                event.setCanceled(true);
                retreatEntity(_VICTIM, _UUID_OWNER_VICTIMHAS, event.getSource(), true);
            }
        }
    }




    //�v���C���[�����S�����Ƃ��A���͂̃y�b�g��P�ނ�����֐��B(config�L�����̂�)
    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event)
    {
        //config��0�ɐݒ肳��Ă���ꍇ�͖����Ƃ݂Ȃ��B
        if (RetreatConfig.c_System.maxDistanceOfRetreat_OnOwnerDeath == 0) return;
        if (!(event.getEntityLiving() instanceof EntityPlayer)) return;

        final EntityPlayer _PLAYER = (EntityPlayer) event.getEntityLiving();

        //���͂�entity�̂����A�����̃y�b�g(�P�ނ̃g�[�e���K�p�ς݂�entity)���擾����B
        List<EntityLivingBase> _pets = searchEntities(_PLAYER, RetreatConfig.c_System.maxDistanceOfRetreat_OnOwnerDeath, _PLAYER.getPositionVector());
        for (EntityLivingBase __e : _pets)
        {
            retreatEntity(__e, _PLAYER.getUniqueID(), null, false);
        }
    }



    //���C���@�\�̊֐�
    public static void retreatEntity(EntityLivingBase entity, UUID ownerUUID, DamageSource damageSource, boolean isForced)
    {
        final World _WORLD = entity.getEntityWorld();
        final Vec3d _LOC = getEntityLoc(entity);

        //�ȉ��Aentity�ւ̓P�ޏ����J�n��́A�v���C���[�ɑ΂��鏈��...
        EntityPlayer _owner = _WORLD.getPlayerEntityByUUID(ownerUUID);
        String _message;

        //owner��null�łȂ�(�v���C���[���I�����C��)�̏ꍇ
        if (_owner != null)
        {
            //entity�����҂ɂ���ċ����I��(�U���Ȃǂɂ����)�P�ނ����ꍇ�ƁA
            //�I�[�i�[�ɂ���ĕ��ʂɓP�ނ����ꍇ�Ń��b�Z�[�W���قȂ�B
            if (isForced)
            {
                _message = RetreatConfig.c_Message.msg_Retreated;
            }
            else
            {
                _message = RetreatConfig.c_Message.msg_Retreated_byOwner;
            }
        }
        //owner��null(�v���C���[���I�t���C��)�̏ꍇ
        //���ʂɓP�ނ���ꍇ�ɂ����āA�I�[�i�[���s�݂Ƃ������Ƃ͂��肦�Ȃ��̂ł���else���ł���͍l�����Ȃ��B
        else
        {
            _message = RetreatConfig.c_Message.msg_RetreatedWhenOwnerOffline;

            //�S�I�����C���v���C���[�̒����烉���_���ň�l�I�o�B
            final PlayerList _PLAYERLIST = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList();
            final List<EntityPlayerMP> _PLAYERS = _PLAYERLIST.getPlayers();

            //�I�����C���̃v���C���[����l�����Ȃ���ԂŃy�b�g�����S�����ۂ̏����B(������)
            //���Ԃ񂻂�Ȃ��Ƃ͋N����Ȃ����낤�ƃ^�J�������Ă���B
            if (_PLAYERS.isEmpty())
            {
                //���̊֐����Ă΂��O�ɁA���S�̌����ƂȂ����U�����L�����Z������Ă���̂ŁA
                //�����炭�u�����N����Ȃ��v�Ƃ������ʂ����܂��B
                return;
            }
            //�I�����C���̃v���C���[�����݂����ꍇ�B
            else
            {
                final Random _RAND = new Random();
                final int _RANDOMINDEX = _RAND.nextInt(_PLAYERS.size());

                _owner = _PLAYERS.get(_RANDOMINDEX);
            }
        }

        //���S���O�����B
        //entity�����҂ɂ���ċ����I��(�U���Ȃǂɂ����)�P�ނ����ꍇ�ƁA
        //�I�[�i�[�ɂ���ĕ��ʂɓP�ނ����ꍇ�Ń��b�Z�[�W���قȂ�B
        final String _NAME_VICTIM = getName(entity);

        if (isForced)
        {
            final String _NAME_ATTACKER;

            //�匳�̍U���҂����݂���ꍇ
            if (damageSource.getTrueSource() != null)
            {
                _NAME_ATTACKER = getName(damageSource.getTrueSource());
            }
            //���ۂɁA���ڍU������entity�����݂���ꍇ
            else if (damageSource.getImmediateSource() != null)
            {
                _NAME_ATTACKER = getName(damageSource.getImmediateSource());
            }
            //�d�����Ȃ��̂�DamageType��Attacker�Ƃ���
            else
            {
                _NAME_ATTACKER = damageSource.getDamageType();
            }

            _message = _message.replace("%victim%", _NAME_VICTIM);
            _message = _message.replace("%attacker%", _NAME_ATTACKER);
        }
        else
        {
            _message = _message.replace("%target%", _NAME_VICTIM);
        }

        _owner.sendMessage(new TextComponentString(_message));
        //---
        //�ȏ�Aentity���S��̃v���C���[�ɑ΂��鏈��

        //���o�Đ�
        if (RetreatConfig.c_System.doPlaySounds)
        {
            float _pitch = 1.2f;
            if (!isForced)
            {
                _pitch += 0.5f;
            }
            _WORLD.playSound(null, _LOC.x, _LOC.y, _LOC.z, SoundEvents.ITEM_TOTEM_USE, SoundCategory.PLAYERS, 0.8f, _pitch);
        }
        if (RetreatConfig.c_System.doPlayParticles)
        {
            final int _COUNT_PARTICLE = 60;
            final float _SPEED_PARTICLE = 1.0f;
            initPlayParticle(_owner, EnumParticleTypes.TOTEM, _COUNT_PARTICLE, _SPEED_PARTICLE, false, _LOC);
        }
        //---

        //�P�ޏ���(Teleport)
        final int _DURATION_RETREATPOTION = 20;    //RetreatPotion�̌��ʎ��ԁB�ǂ����݂�LivingUpdate����~�����̂�0�łȂ���Βl�͂Ȃ�ł������B
        final List<Integer> _LOC_RETREAT = getRetreatingPoint(_owner, entity);

        if (entity.dimension == _LOC_RETREAT.get(0))
        {
            entity.setPosition(_LOC_RETREAT.get(1), _LOC_RETREAT.get(2), _LOC_RETREAT.get(3));

            //LivingUpdate��~�p�̃|�[�V��������
            if (RetreatConfig.c_System.doFreezeRetreateds)
            {
                entity.addPotionEffect(new PotionEffect(f_Potion_CancelUpdate, _DURATION_RETREATPOTION, 1));
            }
            else
            {
                //LivingUpdate���~���Ȃ��ꍇ�͂��̎��_��Entity������������B
                resetEntityStatus(entity);
            }
        }
        else
        {
            //Dimension���AgetRetreatingPoint�̕Ԃ�l�Ɠ������Ȃ��ꍇ�͕ύX����B
            entity.changeDimension(_LOC_RETREAT.get(0),
                    new Teleporter_Retreat(_LOC_RETREAT.get(1), _LOC_RETREAT.get(2), _LOC_RETREAT.get(3),
                            RetreatConfig.c_System.doFreezeRetreateds, _DURATION_RETREATPOTION, f_Potion_CancelUpdate));
        }
        //---
    }



    //Entity�P�ތ��pet�̏�Ԃ�����������֐�
    public static void resetEntityStatus(EntityLivingBase entity)
    {
        // �񕜂Ə��΂��s���A�|�[�V�������ʂƊ������폜
        float _retreatHealth = 0.0f;
        if (hasHardTotem(entity))
        {
            _retreatHealth = entity.getMaxHealth() * RetreatConfig.c_System.healPercentage_Hard;
        }
        else
        {
            _retreatHealth = entity.getMaxHealth() * RetreatConfig.c_System.healPercentage;
        }
        //HP�͍Œ�ł�1�K�v�B
        if (_retreatHealth < 1.0f)
        {
            _retreatHealth = 1.0f;
        }
        //entity�̌��݂�hp���A�ݒ肵�Ă���񕜗ʂ���������Ƃ��ɂ̂݉񕜂�����B
        if (entity.getHealth() < _retreatHealth)
        {
            entity.setHealth(_retreatHealth);
        }


        entity.extinguish();
        entity.clearActivePotions();

        entity.motionX = randomValue(1, 50);
        entity.motionY = 0.5d;
        entity.motionZ = randomValue(2, 50);
        //---

        //���o�E���X�L���h�~�p�|�[�V��������
        if (RetreatConfig.c_System.doApplyResistPotion)
        {
            final List<PotionEffect> _POTION_EFFECTS = new ArrayList<>(); //���������āA�|�[�V�������ʂ�z��Ɋi�[����B

            _POTION_EFFECTS.add(new PotionEffect(MobEffects.RESISTANCE, 60, 5));
            _POTION_EFFECTS.add(new PotionEffect(MobEffects.INVISIBILITY, 2, 1));
            if (RetreatConfig.c_System.hardTotem_Regeneration)
            {
                if (hasHardTotem(entity))
                {
                    _POTION_EFFECTS.add(new PotionEffect(MobEffects.REGENERATION, 1200, 2));
                }
            }
            for(PotionEffect __pe : _POTION_EFFECTS)
            {
                entity.addPotionEffect(__pe);
            }
        }
    }




    //��Dimension�������͈͂Ɋ܂߂čœK�ȓP�ސ��������֐�
    private static List<Integer> getRetreatingPoint(EntityPlayer owner, Entity entity)
    {
        //�œK�ȓP�ސ��������(Server Only)
        final int _ID_DIM = DimensionManager.getProvider(entity.dimension).getRespawnDimension((EntityPlayerMP) owner); //entity�̂���Dimension��owner�����S�����ꍇ��Respawn��
        final World _WORLD = DimensionManager.getWorld(_ID_DIM);
        BlockPos _loc = owner.getBedLocation(_ID_DIM);      //_ID_DIM�ɂ�����owner�̃x�b�h

        if (_loc == null || DimensionManager.getWorld(_ID_DIM).getBlockState(_loc).getBlock() != Blocks.BED)
        {
            //DimensionID�A_ID_DIM�ɂ����鏉�����X�|�[����(�x�b�h�����݂��Ȃ��ꍇ)
            _loc = DimensionManager.getProvider(_ID_DIM).getSpawnPoint();
        }

        //�ȉ��A�����h�~����
        //loc�̈ʒu�ɂ���u���b�N��air�ɂȂ�܂Ń��[�v
        while (!_WORLD.isAirBlock(_loc))
        {
            _loc = _loc.up(1);
        }
        //SPAWN_HEIGHT��Y���W���グ��
        _loc = new BlockPos(_loc.getX(), (_loc.getY() + RetreatConfig.c_System.spawnHeight), _loc.getZ());
        //---

        List<Integer> _result = new ArrayList<>();
        _result.add(_ID_DIM);
        _result.add(_loc.getX());
        _result.add(_loc.getY());
        _result.add(_loc.getZ());
        return _result;
    }



    //Owner�Ƃ̋�����UPDATECANCEL_DISTANCE�ɂȂ�܂�pet��LivingUpdate���~����֐��B
    @SubscribeEvent
    public void RetreatPotion_LUEvent(LivingEvent.LivingUpdateEvent event)
    {
        /*�ȉ��̏����𖞂���Entity��Update���~����B(ServerOnly)
        �EEntityLivingBase���p�����Ă���
        �Eowner�Ƃ̋�����UPDATECANCEL_DISTANCE�ȏ�
        �E�P�ލς�(retreatPotion���ʂ��󂯂Ă���)
         */
        //�܂��AUPDATECANCEL_DISTANCE����owner�Ƃ̋������k�܂����ꍇ�AretreatPotion����������B
        if (RetreatConfig.c_System.doFreezeRetreateds)
        {
            final EntityLivingBase _ENTITY = event.getEntityLiving();
            final World _WORLD = _ENTITY.getEntityWorld();

            if (_WORLD.isRemote) return;
            if (!_ENTITY.isPotionActive(f_Potion_CancelUpdate)) return;

            //ENTITY��Tag����Owner��UUID�𔲂����
            final UUID _UUID_OWNER = hasTotemPower(_ENTITY);

            //��x�P�ނ������Ƃ�����̂�Tag����UUID�𔲂����Ȃ��ꍇ(��O)
            if (_UUID_OWNER == null) {
                return;
            }
            //UUID�𔲂���ꂽ�ꍇ
            else {
                final EntityPlayer _OWNER = _WORLD.getPlayerEntityByUUID(_UUID_OWNER);

                //owner���I�t���C���̏ꍇ
                if (_OWNER == null) {
                    event.setCanceled(true);
                }
                //owner�Ƃ̋������\���߂��Ȃ��ꍇ
                else if (getSquareDistance(_ENTITY.getPositionVector(), _OWNER.getPositionVector()) > (Math.pow(RetreatConfig.c_System.unfreezeDistance, 2.0d))) {
                    event.setCanceled(true);
                }
                //UPDATECANCEL_DISTANCE����owner��pet�̋������k�܂����Ƃ�
                else {
                    resetEntityStatus(_ENTITY);

                    final Vec3d _LOC = getEntityLoc(_ENTITY);

                    //�ȉ��A���o�Đ�
                    if (RetreatConfig.c_System.doPlaySounds) {
                        _WORLD.playSound(null, _LOC.x, _LOC.y, _LOC.z, SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 0.8f, 1.78f);
                    }
                    if (RetreatConfig.c_System.doPlayParticles) {
                        final int _COUNT_PARTICLE = 6;
                        final float _SPEED_PARTICLE = 2.0f;
                        initPlayParticle(_OWNER, EnumParticleTypes.VILLAGER_HAPPY, _COUNT_PARTICLE, _SPEED_PARTICLE, true, _LOC);
                    }
                }
            }
        }
    }



    //petRegeneration_Interval ticks���ƂɃg�[�e���̌��ʂ��󂯂��y�b�g���񕜂�����֐��B(Server only)
    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event)
    {
        if (event.phase == TickEvent.Phase.START)
        {
            tick_count_petregeneration++;

            if (tick_count_petregeneration >= RetreatConfig.c_System.petRegeneration_Interval)
            {
                tick_count_petregeneration = 0;

                //�I�����C���̃v���C���[�����ׂĎ擾
                final PlayerList _PLAYERLIST = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList();
                for (EntityPlayerMP __t : _PLAYERLIST.getPlayers())
                {
                    //�e�v���C���[�𒆐S�Ƃ��āA���apetRegeneration_Radius�ȓ��ɂ���y�b�g���擾�B
                    final List<EntityLivingBase> __PETS = searchEntities(__t, RetreatConfig.c_System.petRegeneration_Radius, __t.getPositionVector());
                    if (__PETS.isEmpty()) continue;

                    for (EntityLivingBase ___e : __PETS)
                    {
                        //HP�����^���łȂ��A�P�ތ�ł��Ȃ��ꍇ�̂݁A
                        //petRegeneration_Amount�������񕜁B
                        if (___e.getHealth() == ___e.getMaxHealth()) continue;
                        if (___e.isPotionActive(f_Potion_CancelUpdate)) continue;

                        //hard��normal�̃g�[�e���ŉ񕜗ʂ��قȂ�B
                        if (hasHardTotem(___e))
                        {
                            ___e.heal(RetreatConfig.c_System.petRegeneration_Amount_Hard);
                        }
                        else
                        {
                            ___e.heal(RetreatConfig.c_System.petRegeneration_Amount);
                        }
                    }
                }
            }
        }
    }
}
