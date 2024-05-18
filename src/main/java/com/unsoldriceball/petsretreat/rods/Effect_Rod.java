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
    //CommandRodポーション効果の付与・除去で、CommandRodの使用状態を切り替える。
    //そのためのポーションの効果時間。余裕をもって2倍しておく。
    private final static int COMMANDROD_POTION_DURATION = RetreatConfig.c_Rod.intervalUseCommandRod * 2;
    //CommandRodを使用中扱いのプレイヤーが格納されるList。
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



    //このイベントはクライアント側でしか発生しないので、パケットを使う必要がある。
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




    //以下、Pet Command Rodの効果を司る関数。----------------------------------------------------------------------
    //周囲のペットを、可能なら自分のところへテレポートさせる関数。
    public static void commandRod_TeleportAround(EntityPlayer p)
    {
        final List<EntityLivingBase> _TARGETS = searchEntities(p, RetreatConfig.c_Rod.radiusSwingCommandRod, p.getPositionVector());
        int _num_pets = 0;

        for (EntityLivingBase __e : _TARGETS)
        {
            //ペットが撤退後の硬直状態でないなら
            if (__e.isPotionActive(f_Potion_CancelUpdate)) continue;

            _num_pets++;
            commandRod_TeleportEntity(__e, p);
            commandRod_ApplyCommandRodPotion(__e);
        }

        if (_num_pets != 0)
        {
            //アクションバーにテレポートしたmobの数を表示する。
            String _message = RetreatConfig.c_Message.msg_UseCommandRod;
            _message = _message.replace("%number%", String.valueOf(_num_pets));
            p.sendStatusMessage(new TextComponentString(_message), true);

            //演出
            if (RetreatConfig.c_System.doPlaySounds)
            {
                p.world.playSound(null, p.posX, p.posY, p.posZ, SoundEvents.ENTITY_ENDERDRAGON_FLAP, SoundCategory.PLAYERS, 0.8f, 1.35f);
            }
        }
    }



    //テレポート処理を行う関数。
    private static void commandRod_TeleportEntity(EntityLivingBase e, EntityPlayer p)
    {
        final Vec3d _LOC_TP_TO = p.getPositionVector();
        final Vec3d _LOC_TARGET = getEntityLoc(e);
        final int _DURATION_INVISIBLE = 3;

        //複数のペットが一気にテレポートすると、視界を遮りやすいので一瞬透明化させる。
        e.addPotionEffect(new PotionEffect(MobEffects.INVISIBILITY, _DURATION_INVISIBLE));
        //落下高度をリセット
        e.fallDistance = 0.0f;

        e.setPosition(_LOC_TP_TO.x, _LOC_TP_TO.y, _LOC_TP_TO.z);
        //慣性を上書きする。
        e.motionX = 0.0d;
        e.motionY = 0.15d;
        e.motionZ = 0.0d;
        //演出
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



    //プレイヤーのRodの発動状態を切り替える関数
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



    //CommandRodPotion効果を付与する関数。
    //対象がプレイヤーならonCommandRodPotion、commandRod_onServerTickの影響もうける。
    //プレイヤーでないなら、攻撃をやめさせる。
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



    //プレイヤーのポーション効果に変化があったときに発生するイベント。
    //ServerTickイベントでループする、対象のプレイヤーを加えたり消したりする。
    @SubscribeEvent
    public void commandRod_onCommandRodPotion(PotionEvent event)
    {
        if (event.getEntity().world.isRemote) return;
        try     //getPotion()がエラーを起こす可能性があるのでtryを使う。
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



    //intervalUseCommandRod tickごとにcommandRod_KeepPets()を実行する。
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
                        //CommandRodポーション効果を延長する。(CommandRodの効果を発動し続けるようにする。)
                        commandRod_ApplyCommandRodPotion(__p);

                        //CommandRodの効果を発動させる。
                        commandRod_KeepPets(__p);
                    }
                }
            }
        }
    }



    //command rodの右クリック効果が発動中の効果。
    //自分の周囲にペットを留め、攻撃もいったん止めさせる。
    //最後に、自分に
    private static void commandRod_KeepPets(EntityPlayer p)
    {
        //configで設定されている半径を1.25倍にして余裕を持たせ、
        //その範囲内にいるペットには、攻撃をやめさせ、
        //その範囲内、かつconfigで設定されている半径を出てしまっているペットを自身へテレポートさせる。
        final float _RADIUS_AFFECT = RetreatConfig.c_Rod.radiusUseCommandRod;
        final float _RADIUS_NOTAFFECT_SQ = (float) Math.pow(RetreatConfig.c_Rod.ignoreRadiusUseCommandRod, 2.0d);
        final List<EntityLivingBase> _TARGETS = searchEntities(p, _RADIUS_AFFECT, p.getPositionVector());
        int _num_pets = 0;

        for (EntityLivingBase __e : _TARGETS)
        {
            //ペットが撤退後の硬直状態でないなら
            if (__e.isPotionActive(f_Potion_CancelUpdate)) continue;

            _num_pets++;
            commandRod_ApplyCommandRodPotion(__e);
            //取得したペットが、効果範囲外にいた場合は自身へテレポートさせる。
            if (__e.getDistanceSq(p) >= _RADIUS_NOTAFFECT_SQ)
            {
                commandRod_TeleportEntity(__e, p);
            }
        }

        //アクションバーにテレポートしたmobの数を表示する。
        if (_num_pets != 0)
        {
            String _message = RetreatConfig.c_Message.msg_UseCommandRod;
            _message = _message.replace("%number%", String.valueOf(_num_pets));
            p.sendStatusMessage(new TextComponentString(_message), true);
        }
    }



    //以下、Pet Transfer Rodの効果を司る関数。--------------------------------------
    //登録されている地点に周囲のペットを手レポートさせる関数。
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

                    //座標が変わらないうちに演出を出す、
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

                //以下、演出
                if (RetreatConfig.c_System.doPlaySounds)
                {
                    DimensionManager.getWorld(_LOC_DIMENSION).playSound(null, _LOC.x, _LOC.y, _LOC.z, SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.HOSTILE, 1.1f, 1.6f);
                }

                //アクションバーに転送したmobの数を表示する。
                String _message = RetreatConfig.c_Message.msg_SwingTransferRod;
                _message = _message.replace("%number%", String.valueOf(_num_pets));
                p.sendStatusMessage(new TextComponentString(_message), true);
            }
            //効果がペットに発動したかどうかにかかわらず、一度効果を使用したら情報を消去する。
            p.removePotionEffect(f_Potion_TransferRod);

            //以下、演出。
            if (RetreatConfig.c_System.doPlaySounds)
            {
                p.world.playSound(null, p.posX, p.posY, p.posZ, SoundEvents.ENTITY_ENDERDRAGON_FLAP, SoundCategory.PLAYERS, 0.8f, 1.35f);
            }
        }
    }



    //テレポート後の処理を行う関数。
    public static void transferRod_afterTeleport(EntityPlayer p, EntityLivingBase target)
    {
        target.motionX = 0.0d;
        target.motionY = 0.15d;
        target.motionZ = 0.0d;
    }



    //TransferRodPotion効果を付与する関数。
    public static void transferRod_ApplyTransferRodPotion(EntityPlayer p)
    {
        p.addPotionEffect(new PotionEffect(f_Potion_TransferRod, RetreatConfig.c_Rod.effectDurationTransferRod));

        //アクションバーに情報を登録したことを表示する。
        final String _MESSAGE = RetreatConfig.c_Message.msg_UseTransferRod;
        p.sendStatusMessage(new TextComponentString(_MESSAGE), true);

        //以下、演出。
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



    //プレイヤーのポーション効果に変化があったときに発生するイベント。
    //ポーションが付与されると、ペットの転送先となる地点の情報を書き込み、
    //なくなると、情報を消去する。
    @SubscribeEvent
    public void transferRod_onTransferRodPotion(PotionEvent event)
    {
        if (event.getEntity().world.isRemote) return;
        try     //getPotion()がエラーを起こす可能性があるのでtryを使う。
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
            if (transferrod_registereddatas.containsKey(_PLAYER)) //情報の削除に成功したらtrueが返る。
            {
                transferRod_RemoveLocData(_PLAYER);
                if (RetreatConfig.c_System.doPlaySounds)
                {
                    _PLAYER.world.playSound(null, _PLAYER.posX, _PLAYER.posY, _PLAYER.posZ, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.PLAYERS, 0.8f, 1.35f);
                }
            }
        }
    }



    //ArrayListに、座標情報を登録する関数。
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



    //ArrayListから、座標情報を取得する関数。
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



    //ArrayListから、座標情報を削除する関数。
    private static void transferRod_RemoveLocData(EntityPlayer p)
    {
        transferrod_registereddatas.remove(p);
    }



    //以下、Pet Retreat Rodの効果を司る関数。-------------------------------------------
    //視点の先にいるEntityが撤退可能なら撤退させる関数。
    //RetreatRod_RetreatAround発動時に、スニークしていない場合に、代わりに発動する。
    public static void retreatRod_RetreatLookingEntity(EntityPlayer p)
    {
        final EntityLivingBase _TARGET = getLookingEntity(p, RetreatConfig.c_Rod.rangeSwingRetreatRod);

        //_TARGETの取得に成功し、対象が撤退後の硬直状態でない場合。
        if (_TARGET == null) return;
        if (_TARGET.isPotionActive(f_Potion_CancelUpdate)) return;

        RetreatSystem.retreatEntity(_TARGET, p.getUniqueID(), null, false);

        //Configで有効なら音を鳴らす。
        if (RetreatConfig.c_System.doPlaySounds)
        {
            p.world.playSound(null, p.posX, p.posY, p.posZ, SoundEvents.ENTITY_ENDERDRAGON_FLAP, SoundCategory.PLAYERS, 0.8f, 1.55f);
        }
    }



    //周囲のEntityが撤退可能なら撤退させる関数。
    public static void retreatRod_RetreatAround(EntityPlayer p)
    {
        //これはスニーク状態でのみ発動する効果。
        if (p.isSneaking())
        {
            int _num_pets = 0;
            final List<EntityLivingBase> _PETS = searchEntities(p, RetreatConfig.c_Rod.radiusSneakSwingRetreatRod, p.getPositionVector());
            for (EntityLivingBase __e : _PETS)
            {
                //撤退後の硬直状態でない場合。
                if (__e.isPotionActive(f_Potion_CancelUpdate)) continue;
                RetreatSystem.retreatEntity(__e, p.getUniqueID(), null, false);
                _num_pets++;
            }

            //撤退したentityが一体でもいるなら。
            if (_num_pets != 0)
            {
                //アクションバーに撤退したmobの数を表示する。
                String _message = RetreatConfig.c_Message.msg_SneakSwingRetreatRod;
                _message = _message.replace("%number%", String.valueOf(_num_pets));
                p.sendStatusMessage(new TextComponentString(_message), true);

                //Configで有効なら音を鳴らす。
                if (RetreatConfig.c_System.doPlaySounds)
                {
                    p.world.playSound(null, p.posX, p.posY, p.posZ, SoundEvents.ENTITY_ENDERDRAGON_FLAP, SoundCategory.PLAYERS, 0.8f, 1.35f);
                }
            }
        }
        else
        {
            //スニークせずに左クリックした場合。
            retreatRod_RetreatLookingEntity(p);
        }
    }



    //周囲のpetを発光させる関数。
    //RetreatRod_GetLocations_Around発動時に、スニークしていない場合に、代わりに発動する。
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

                //アクションバーに発光させたmobの数を表示する。
                String _message = RetreatConfig.c_Message.msg_UseRetreatRod;
                _message = _message.replace("%number%", String.valueOf(_num_pets));
                p.sendStatusMessage(new TextComponentString(_message), true);

                //Configで有効なら音を鳴らす。
                if (RetreatConfig.c_System.doPlaySounds)
                {
                    p.world.playSound(null, p.posX, p.posY, p.posZ, SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 0.3f, 2.0f);
                }
            }
        }
    }



    //周囲のpetの座標を取得して、プレイヤーのチャットに送信する関数。
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

                    //アクションバーに発光させたmobの数を表示する。
                    String _message = RetreatConfig.c_Message.msg_SneakUseRetreatRod;
                    _message = _message.replace("%number%", String.valueOf(_num_pets));
                    p.sendStatusMessage(new TextComponentString(_message), true);

                    //Configで有効なら音を鳴らす。
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
