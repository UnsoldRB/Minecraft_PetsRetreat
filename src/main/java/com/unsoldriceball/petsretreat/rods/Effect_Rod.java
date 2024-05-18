package com.unsoldriceball.petsretreat.rods;

import com.unsoldriceball.petsretreat.RetreatConfig;
import com.unsoldriceball.petsretreat.RetreatUtils;
import com.unsoldriceball.petsretreat.retreatsystem.RetreatSystem;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.living.PotionEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.unsoldriceball.petsretreat.RetreatMain.*;
import static com.unsoldriceball.petsretreat.RetreatUtils.*;


public class Effect_Rod
{
    //CommandRod�|�[�V�������ʂ̕t�^�E�����ŁACommandRod�̎g�p��Ԃ�؂�ւ���B
    //���̂��߂̃|�[�V�����̌��ʎ��ԁB�]�T��������2�{���Ă����B
    private final static int COMMANDROD_POTION_DURATION = RetreatConfig.c_Rod.intervalUseCommandRod * 2;
    //CommandRod���g�p�������̃v���C���[���i�[�����List�B
    private static List<EntityPlayer> commandrod_using_players = new ArrayList<>();
    private static int commandrod_tick_count = 0;
    private static Map<EntityPlayer, String> transferrod_registereddatas = new HashMap<>();



    @SubscribeEvent
    public void onPlayerInteract_R(PlayerInteractEvent.RightClickItem event)
    {
        if (!event.getWorld().isRemote)
        {
            final EntityPlayer _PLAYER = event.getEntityPlayer();
            final Item _HELDITEM = event.getItemStack().getItem();

            if (_HELDITEM instanceof Item_CommandRod)
            {
                commandRod_switchRodState(_PLAYER);
            }
            else if (_HELDITEM instanceof Item_TransferRod)
            {
                transferRod_ApplyTransferRodPotion(_PLAYER);
            }
            else if (_HELDITEM instanceof Item_RetreatRod)
            {
                retreatRod_GetLocations_Around(_PLAYER);
            }
        }
    }



    @SubscribeEvent
    public void onPlayerInteract_L_Block(PlayerInteractEvent.LeftClickBlock event)
    {
        if (!event.getWorld().isRemote)
        {
            final EntityPlayer _PLAYER = event.getEntityPlayer();
            final Item _HELDITEM = _PLAYER.getHeldItemMainhand().getItem();

            if (_HELDITEM instanceof Item_CommandRod)
            {
                commandRod_TeleportAround(_PLAYER);
            }
            else if (_HELDITEM instanceof Item_TransferRod)
            {
                transferRod_TeleportPets(_PLAYER);
            }
            else if (_HELDITEM instanceof Item_RetreatRod)
            {
                retreatRod_RetreatAround(_PLAYER);
            }
        }
    }



    //���̃C�x���g�̓N���C�A���g���ł����������Ȃ��̂ŁA�p�P�b�g���g���K�v������B
    @SubscribeEvent
    public void onPlayerInteract_L_Air(PlayerInteractEvent.LeftClickEmpty event)
    {
        if (event.getWorld().isRemote)
        {
            final EntityPlayer _PLAYER = event.getEntityPlayer();
            final Item _HELDITEM = _PLAYER.getHeldItemMainhand().getItem();

            if (_HELDITEM instanceof Item_CommandRod)
            {
                f_wrapper_rod.sendToServer(new Packet_Rod(_PLAYER.dimension, _PLAYER.getUniqueID(), Enum_Rod.ROD_COMMAND));
            }
            else if (_HELDITEM instanceof Item_TransferRod)
            {
                f_wrapper_rod.sendToServer(new Packet_Rod(_PLAYER.dimension, _PLAYER.getUniqueID(), Enum_Rod.ROD_TRANSFER));
            }
            else if (_HELDITEM instanceof Item_RetreatRod)
            {
                f_wrapper_rod.sendToServer(new Packet_Rod(_PLAYER.dimension, _PLAYER.getUniqueID(), Enum_Rod.ROD_RETREAT));
            }
        }
    }



    @SubscribeEvent
    public void onPlayerInteract_L_Entity(AttackEntityEvent event)
    {
        final EntityPlayer _PLAYER = event.getEntityPlayer();

        if (!_PLAYER.world.isRemote && !(_PLAYER instanceof FakePlayer))
        {
            final Item _HELDITEM = _PLAYER.getHeldItemMainhand().getItem();

            if (_HELDITEM instanceof Item_CommandRod)
            {
                commandRod_TeleportAround(_PLAYER);
            }
            else if (_HELDITEM instanceof Item_TransferRod)
            {
                transferRod_TeleportPets(_PLAYER);
            }
            else if (_HELDITEM instanceof Item_RetreatRod)
            {
                retreatRod_RetreatAround(_PLAYER);
            }
        }
    }




    //�ȉ��APet Command Rod�̌��ʂ��i��֐��B----------------------------------------------------------------------
    //���͂̃y�b�g���A�\�Ȃ玩���̂Ƃ���փe���|�[�g������֐��B
    public static void commandRod_TeleportAround(EntityPlayer p)
    {
        final List<EntityLivingBase> _TARGETS = searchEntities(p, RetreatConfig.c_Rod.radiusSwingCommandRod, p.getPositionVector());
        int _num_pets = 0;

        for (EntityLivingBase __e : _TARGETS)
        {
            //�y�b�g���P�ތ�̍d����ԂłȂ��Ȃ�
            if (__e.isPotionActive(f_Potion_CancelUpdate)) continue;

            _num_pets++;
            commandRod_TeleportEntity(__e, p);
            commandRod_ApplyCommandRodPotion(__e);
        }

        if (_num_pets != 0)
        {
            //�A�N�V�����o�[�Ƀe���|�[�g����mob�̐���\������B
            String _message = RetreatConfig.c_Message.msg_UseCommandRod;
            _message = _message.replace("%number%", String.valueOf(_num_pets));
            p.sendStatusMessage(new TextComponentString(_message), true);

            //���o
            if (RetreatConfig.c_System.doPlaySounds)
            {
                p.world.playSound(null, p.posX, p.posY, p.posZ, SoundEvents.ENTITY_ENDERDRAGON_FLAP, SoundCategory.PLAYERS, 0.8f, 1.35f);
            }
        }
    }



    //�e���|�[�g�������s���֐��B
    private static void commandRod_TeleportEntity(EntityLivingBase e, EntityPlayer p)
    {
        final Vec3d _LOC_TP_TO = p.getPositionVector();
        final Vec3d _LOC_TARGET = getEntityLoc(e);
        final int _DURATION_INVISIBLE = 3;

        //�����̃y�b�g����C�Ƀe���|�[�g����ƁA���E���Ղ�₷���̂ň�u������������B
        e.addPotionEffect(new PotionEffect(MobEffects.INVISIBILITY, _DURATION_INVISIBLE));
        //�������x�����Z�b�g
        e.fallDistance = 0.0f;

        e.setPosition(_LOC_TP_TO.x, _LOC_TP_TO.y, _LOC_TP_TO.z);
        //�������㏑������B
        e.motionX = 0.0d;
        e.motionY = 0.15d;
        e.motionZ = 0.0d;
        //���o
        if (RetreatConfig.c_System.doPlaySounds)
        {
            p.world.playSound(null, _LOC_TARGET.x, _LOC_TARGET.y, _LOC_TARGET.z, SoundEvents.ENTITY_BAT_LOOP, SoundCategory.HOSTILE, 0.2f, 1.78f);
        }
        if (RetreatConfig.c_System.doPlayParticles)
        {
            final int _COUNT_PARTICLE = 6;
            final float _SPEED_PARTICLE = 0.4f;
            initPlayParticle(p, EnumParticleTypes.CLOUD, _COUNT_PARTICLE, _SPEED_PARTICLE, false, _LOC_TARGET);
        }
    }



    //�v���C���[��Rod�̔�����Ԃ�؂�ւ���֐�
    private static void commandRod_switchRodState(EntityPlayer p)
    {
        final boolean _IS_ACTIVATED = p.isPotionActive(f_Potion_CommandRod);

        if (_IS_ACTIVATED)
        {
            p.removePotionEffect(f_Potion_CommandRod);
            if (RetreatConfig.c_System.doPlaySounds)
            {
                p.world.playSound(null, p.posX, p.posY, p.posZ, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.PLAYERS, 0.8f, 1.35f);
            }
        }
        else {
            p.addPotionEffect(new PotionEffect(f_Potion_CommandRod, COMMANDROD_POTION_DURATION));
            if (RetreatConfig.c_System.doPlaySounds)
            {
                p.world.playSound(null, p.posX, p.posY, p.posZ, SoundEvents.ENTITY_ENDERDRAGON_FLAP, SoundCategory.PLAYERS, 0.8f, 1.85f);
            }
        }
    }



    //CommandRodPotion���ʂ�t�^����֐��B
    //�Ώۂ��v���C���[�Ȃ�onCommandRodPotion�AcommandRod_onServerTick�̉e����������B
    //�v���C���[�łȂ��Ȃ�A�U������߂�����B
    public static void commandRod_ApplyCommandRodPotion(EntityLivingBase e)
    {
        if (e instanceof EntityPlayer)
        {
            final EntityPlayer _TARGET = (EntityPlayer) e;

            _TARGET.addPotionEffect(new PotionEffect(f_Potion_CommandRod, COMMANDROD_POTION_DURATION));
        }
        else if (e instanceof EntityLiving)
        {
            final EntityLiving _TARGET = (EntityLiving) e;

            _TARGET.addPotionEffect(new PotionEffect(f_Potion_CommandRod, COMMANDROD_POTION_DURATION));
            _TARGET.setAttackTarget(null);
        }
    }



    //�v���C���[�̃|�[�V�������ʂɕω����������Ƃ��ɔ�������C�x���g�B
    //ServerTick�C�x���g�Ń��[�v����A�Ώۂ̃v���C���[����������������肷��B
    @SubscribeEvent
    public void commandRod_onCommandRodPotion(PotionEvent event)
    {
        if (event.getEntity().world.isRemote) return;
        try     //getPotion()���G���[���N�����\��������̂�try���g���B
        {
            if (!event.getPotionEffect().getPotion().equals(f_Potion_CommandRod)) return;
        }
        catch (NullPointerException exc)
        {
            return;
        }
        if (!(event.getEntityLiving() instanceof EntityPlayer && !(event.getEntityLiving() instanceof FakePlayer))) return;

        final EntityPlayer _PLAYER = (EntityPlayer) event.getEntityLiving();

        if (event.getClass() == PotionEvent.PotionAddedEvent.class && !commandrod_using_players.contains(_PLAYER))
        {
            commandrod_using_players.add(_PLAYER);
        }
        else if (event.getClass() == PotionEvent.PotionExpiryEvent.class || event.getClass() == PotionEvent.PotionRemoveEvent.class)
        {
            commandrod_using_players.remove(_PLAYER);
        }
    }



    //intervalUseCommandRod tick���Ƃ�commandRod_KeepPets()�����s����B
    @SubscribeEvent
    public void commandRod_onServerTick(TickEvent.ServerTickEvent event)
    {
        if (event.phase == TickEvent.Phase.START)
        {
            commandrod_tick_count++;

            if (commandrod_tick_count >= RetreatConfig.c_Rod.intervalUseCommandRod)
            {
                commandrod_tick_count = 0;

                if (!commandrod_using_players.isEmpty())
                {
                    for (EntityPlayer __p : commandrod_using_players)
                    {
                        //CommandRod�|�[�V�������ʂ���������B(CommandRod�̌��ʂ𔭓���������悤�ɂ���B)
                        commandRod_ApplyCommandRodPotion(__p);

                        //CommandRod�̌��ʂ𔭓�������B
                        commandRod_KeepPets(__p);
                    }
                }
            }
        }
    }



    //command rod�̉E�N���b�N���ʂ��������̌��ʁB
    //�����̎��͂Ƀy�b�g�𗯂߁A�U������������~�߂�����B
    //�Ō�ɁA������
    private static void commandRod_KeepPets(EntityPlayer p)
    {
        //config�Őݒ肳��Ă��锼�a��1.25�{�ɂ��ė]�T���������A
        //���͈͓̔��ɂ���y�b�g�ɂ́A�U������߂����A
        //���͈͓̔��A����config�Őݒ肳��Ă��锼�a���o�Ă��܂��Ă���y�b�g�����g�փe���|�[�g������B
        final float _RADIUS_AFFECT = RetreatConfig.c_Rod.radiusUseCommandRod;
        final float _RADIUS_NOTAFFECT_SQ = (float) Math.pow(RetreatConfig.c_Rod.ignoreRadiusUseCommandRod, 2.0d);
        final List<EntityLivingBase> _TARGETS = searchEntities(p, _RADIUS_AFFECT, p.getPositionVector());
        int _num_pets = 0;

        for (EntityLivingBase __e : _TARGETS)
        {
            //�y�b�g���P�ތ�̍d����ԂłȂ��Ȃ�
            if (__e.isPotionActive(f_Potion_CancelUpdate)) continue;

            _num_pets++;
            commandRod_ApplyCommandRodPotion(__e);
            //�擾�����y�b�g���A���ʔ͈͊O�ɂ����ꍇ�͎��g�փe���|�[�g������B
            if (__e.getDistanceSq(p) >= _RADIUS_NOTAFFECT_SQ)
            {
                commandRod_TeleportEntity(__e, p);
            }
        }

        //�A�N�V�����o�[�Ƀe���|�[�g����mob�̐���\������B
        if (_num_pets != 0)
        {
            String _message = RetreatConfig.c_Message.msg_UseCommandRod;
            _message = _message.replace("%number%", String.valueOf(_num_pets));
            p.sendStatusMessage(new TextComponentString(_message), true);
        }
    }



    //�ȉ��APet Transfer Rod�̌��ʂ��i��֐��B--------------------------------------
    //�o�^����Ă���n�_�Ɏ��͂̃y�b�g���背�|�[�g������֐��B
    public static void transferRod_TeleportPets(EntityPlayer p)
    {
        if (transferrod_registereddatas.containsKey(p))
        {
            final double[] _DATAS = transferRod_GetLocData(p);
            final List<EntityLivingBase> _PETS = searchEntities(p, RetreatConfig.c_Rod.radiusTransferRod, p.getPositionVector());

            if (!_PETS.isEmpty() && _DATAS != null)
            {
                final int _LOC_DIMENSION = (int) _DATAS[0];
                final Vec3d _LOC = new Vec3d(_DATAS[1], _DATAS[2], _DATAS[3]);
                final boolean _IS_SAME_DIMENSION = (p.dimension == _LOC_DIMENSION);
                int _num_pets = 0;

                for (EntityLivingBase __e : _PETS)
                {
                    if (__e.isPotionActive(f_Potion_CancelUpdate)) continue;
                    _num_pets++;

                    //���W���ς��Ȃ������ɉ��o���o���A
                    if (RetreatConfig.c_System.doPlaySounds)
                    {
                        __e.world.playSound(null, __e.posX, __e.posY, __e.posZ, SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.HOSTILE, 0.5f, 1.2f);
                    }
                    if (RetreatConfig.c_System.doPlayParticles)
                    {
                        final int _COUNT_PARTICLE = 2;
                        final float _SPEED_PARTICLE = 0.4f;
                        initPlayParticle(p, EnumParticleTypes.CLOUD, _COUNT_PARTICLE, _SPEED_PARTICLE, false, __e.getPositionVector());
                    }

                    if (!_IS_SAME_DIMENSION)
                    {
                        __e.changeDimension(_LOC_DIMENSION, new Teleporter_TransferRod(p, _LOC));
                    }
                    else
                    {
                        __e.setPosition(_LOC.x, _LOC.y, _LOC.z);
                        transferRod_afterTeleport(p, __e);
                    }
                }

                //�ȉ��A���o
                if (RetreatConfig.c_System.doPlaySounds)
                {
                    DimensionManager.getWorld(_LOC_DIMENSION).playSound(null, _LOC.x, _LOC.y, _LOC.z, SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.HOSTILE, 1.1f, 1.6f);
                }

                //�A�N�V�����o�[�ɓ]������mob�̐���\������B
                String _message = RetreatConfig.c_Message.msg_SwingTransferRod;
                _message = _message.replace("%number%", String.valueOf(_num_pets));
                p.sendStatusMessage(new TextComponentString(_message), true);
            }
            //���ʂ��y�b�g�ɔ����������ǂ����ɂ�����炸�A��x���ʂ��g�p�����������������B
            p.removePotionEffect(f_Potion_TransferRod);

            //�ȉ��A���o�B
            if (RetreatConfig.c_System.doPlaySounds)
            {
                p.world.playSound(null, p.posX, p.posY, p.posZ, SoundEvents.ENTITY_ENDERDRAGON_FLAP, SoundCategory.PLAYERS, 0.8f, 1.35f);
            }
        }
    }



    //�e���|�[�g��̏������s���֐��B
    public static void transferRod_afterTeleport(EntityPlayer p, EntityLivingBase target)
    {
        target.motionX = 0.0d;
        target.motionY = 0.15d;
        target.motionZ = 0.0d;
    }



    //TransferRodPotion���ʂ�t�^����֐��B
    public static void transferRod_ApplyTransferRodPotion(EntityPlayer p)
    {
        p.addPotionEffect(new PotionEffect(f_Potion_TransferRod, RetreatConfig.c_Rod.effectDurationTransferRod));

        //�A�N�V�����o�[�ɏ���o�^�������Ƃ�\������B
        final String _MESSAGE = RetreatConfig.c_Message.msg_UseTransferRod;
        p.sendStatusMessage(new TextComponentString(_MESSAGE), true);

        //�ȉ��A���o�B
        if (RetreatConfig.c_System.doPlaySounds)
        {
            p.world.playSound(null, p.posX, p.posY, p.posZ, SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.PLAYERS, 0.8f, 1.35f);
        }
        if (RetreatConfig.c_System.doPlayParticles)
        {
            final int _COUNT_PARTICLE = 12;
            final float _SPEED_PARTICLE = 0.5f;
            initPlayParticle(p, EnumParticleTypes.SPELL_WITCH, _COUNT_PARTICLE, _SPEED_PARTICLE, true, p.getPositionVector());
        }
    }



    //�v���C���[�̃|�[�V�������ʂɕω����������Ƃ��ɔ�������C�x���g�B
    //�|�[�V�������t�^�����ƁA�y�b�g�̓]����ƂȂ�n�_�̏����������݁A
    //�Ȃ��Ȃ�ƁA������������B
    @SubscribeEvent
    public void transferRod_onTransferRodPotion(PotionEvent event)
    {
        if (event.getEntity().world.isRemote) return;
        try     //getPotion()���G���[���N�����\��������̂�try���g���B
        {
            if (!event.getPotionEffect().getPotion().equals(f_Potion_TransferRod)) return;
        }
        catch (NullPointerException exc)
        {
            return;
        }
        if (!(event.getEntityLiving() instanceof EntityPlayer && !(event.getEntityLiving() instanceof FakePlayer))) return;

        final EntityPlayer _PLAYER = (EntityPlayer) event.getEntityLiving();


        if (event.getClass() == PotionEvent.PotionAddedEvent.class)
        {
            transferRod_AddLocData(_PLAYER);
        }
        else if (event.getClass() == PotionEvent.PotionExpiryEvent.class || event.getClass() == PotionEvent.PotionRemoveEvent.class)
        {
            if (transferrod_registereddatas.containsKey(_PLAYER)) //���̍폜�ɐ���������true���Ԃ�B
            {
                transferRod_RemoveLocData(_PLAYER);
                if (RetreatConfig.c_System.doPlaySounds)
                {
                    _PLAYER.world.playSound(null, _PLAYER.posX, _PLAYER.posY, _PLAYER.posZ, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.PLAYERS, 0.8f, 1.35f);
                }
            }
        }
    }



    //ArrayList�ɁA���W����o�^����֐��B
    private static void transferRod_AddLocData(EntityPlayer p)
    {
        final Vec3d _LOC = p.getPositionVector();
        StringBuilder _locationdata = new StringBuilder();
        _locationdata.append((double) p.dimension);
        _locationdata.append("_");
        _locationdata.append(_LOC.x);
        _locationdata.append("_");
        _locationdata.append(_LOC.y);
        _locationdata.append("_");
        _locationdata.append(_LOC.z);

        transferrod_registereddatas.put(p, String.valueOf(_locationdata));
    }



    //ArrayList����A���W�����擾����֐��B
    private static double[] transferRod_GetLocData(EntityPlayer p)
    {
        final String _DATA_RAW = transferrod_registereddatas.get(p);
        if (_DATA_RAW != null)
        {
            final String[] _DATA_SPLITTED = _DATA_RAW.split("_");
            return new double[]
                    {
                            Double.parseDouble(_DATA_SPLITTED[0]),
                            Double.parseDouble(_DATA_SPLITTED[1]),
                            Double.parseDouble(_DATA_SPLITTED[2]),
                            Double.parseDouble(_DATA_SPLITTED[3])
                    };
        }
        return null;
    }



    //ArrayList����A���W�����폜����֐��B
    private static void transferRod_RemoveLocData(EntityPlayer p)
    {
        transferrod_registereddatas.remove(p);
    }



    //�ȉ��APet Retreat Rod�̌��ʂ��i��֐��B-------------------------------------------
    //���_�̐�ɂ���Entity���P�މ\�Ȃ�P�ނ�����֐��B
    //RetreatRod_RetreatAround�������ɁA�X�j�[�N���Ă��Ȃ��ꍇ�ɁA����ɔ�������B
    public static void retreatRod_RetreatLookingEntity(EntityPlayer p)
    {
        final EntityLivingBase _TARGET = getLookingEntity(p, RetreatConfig.c_Rod.rangeSwingRetreatRod);

        //_TARGET�̎擾�ɐ������A�Ώۂ��P�ތ�̍d����ԂłȂ��ꍇ�B
        if (_TARGET == null) return;
        if (_TARGET.isPotionActive(f_Potion_CancelUpdate)) return;

        RetreatSystem.retreatEntity(_TARGET, p.getUniqueID(), null, false);

        //Config�ŗL���Ȃ特��炷�B
        if (RetreatConfig.c_System.doPlaySounds)
        {
            p.world.playSound(null, p.posX, p.posY, p.posZ, SoundEvents.ENTITY_ENDERDRAGON_FLAP, SoundCategory.PLAYERS, 0.8f, 1.55f);
        }
    }



    //���͂�Entity���P�މ\�Ȃ�P�ނ�����֐��B
    public static void retreatRod_RetreatAround(EntityPlayer p)
    {
        //����̓X�j�[�N��Ԃł̂ݔ���������ʁB
        if (p.isSneaking())
        {
            int _num_pets = 0;
            final List<EntityLivingBase> _PETS = searchEntities(p, RetreatConfig.c_Rod.radiusSneakSwingRetreatRod, p.getPositionVector());
            for (EntityLivingBase __e : _PETS)
            {
                //�P�ތ�̍d����ԂłȂ��ꍇ�B
                if (__e.isPotionActive(f_Potion_CancelUpdate)) continue;
                RetreatSystem.retreatEntity(__e, p.getUniqueID(), null, false);
                _num_pets++;
            }

            //�P�ނ���entity����̂ł�����Ȃ�B
            if (_num_pets != 0)
            {
                //�A�N�V�����o�[�ɓP�ނ���mob�̐���\������B
                String _message = RetreatConfig.c_Message.msg_SneakSwingRetreatRod;
                _message = _message.replace("%number%", String.valueOf(_num_pets));
                p.sendStatusMessage(new TextComponentString(_message), true);

                //Config�ŗL���Ȃ特��炷�B
                if (RetreatConfig.c_System.doPlaySounds)
                {
                    p.world.playSound(null, p.posX, p.posY, p.posZ, SoundEvents.ENTITY_ENDERDRAGON_FLAP, SoundCategory.PLAYERS, 0.8f, 1.35f);
                }
            }
        }
        else
        {
            //�X�j�[�N�����ɍ��N���b�N�����ꍇ�B
            retreatRod_RetreatLookingEntity(p);
        }
    }



    //���͂�pet�𔭌�������֐��B
    //RetreatRod_GetLocations_Around�������ɁA�X�j�[�N���Ă��Ȃ��ꍇ�ɁA����ɔ�������B
    private static void retreatRod_HighlightAround(EntityPlayer p)
    {
        final int _RADIUS_CONFIG = RetreatConfig.c_Rod.radiusUseRetreatRod;
        if (_RADIUS_CONFIG != 0)
        {
            final List<EntityLivingBase> _PETS = searchEntities(p, _RADIUS_CONFIG, p.getPositionVector());
            if (!_PETS.isEmpty())
            {
                int _num_pets = 0;
                for (EntityLivingBase __e : _PETS)
                {
                    final int __DURATION_GLOWING = 40;
                    __e.addPotionEffect(new PotionEffect(MobEffects.GLOWING, __DURATION_GLOWING));
                    _num_pets++;
                }

                //�A�N�V�����o�[�ɔ���������mob�̐���\������B
                String _message = RetreatConfig.c_Message.msg_UseRetreatRod;
                _message = _message.replace("%number%", String.valueOf(_num_pets));
                p.sendStatusMessage(new TextComponentString(_message), true);

                //Config�ŗL���Ȃ特��炷�B
                if (RetreatConfig.c_System.doPlaySounds)
                {
                    p.world.playSound(null, p.posX, p.posY, p.posZ, SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 0.3f, 2.0f);
                }
            }
        }
    }



    //���͂�pet�̍��W���擾���āA�v���C���[�̃`���b�g�ɑ��M����֐��B
    private static void retreatRod_GetLocations_Around(EntityPlayer p)
    {
        if (p.isSneaking())
        {
            final int _RADIUS_CONFIG = RetreatConfig.c_Rod.radiusSneakUseRetreatRod;
            if (_RADIUS_CONFIG != 0)
            {
                final List<EntityLivingBase> _PETS = searchEntities(p, _RADIUS_CONFIG, p.getPositionVector());
                if (!_PETS.isEmpty())
                {
                    int _num_pets = 0;
                    for (EntityLivingBase __e : _PETS)
                    {
                        final String __NAME_TARGET = RetreatUtils.getName(__e);
                        final BlockPos __POS_TARGET = __e.getPosition();
                        p.sendMessage(new TextComponentString(__NAME_TARGET + ": " + __POS_TARGET));
                        _num_pets++;
                    }

                    //�A�N�V�����o�[�ɔ���������mob�̐���\������B
                    String _message = RetreatConfig.c_Message.msg_SneakUseRetreatRod;
                    _message = _message.replace("%number%", String.valueOf(_num_pets));
                    p.sendStatusMessage(new TextComponentString(_message), true);

                    //Config�ŗL���Ȃ特��炷�B
                    if (RetreatConfig.c_System.doPlaySounds)
                    {
                        p.world.playSound(null, p.posX, p.posY, p.posZ, SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 0.3f, 2.0f);
                    }
                }
            }
        }
        else
        {
            retreatRod_HighlightAround(p);
        }
    }
}
