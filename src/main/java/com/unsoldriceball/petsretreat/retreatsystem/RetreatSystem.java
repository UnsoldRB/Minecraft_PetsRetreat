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




    //Entityが撤退するに値するかを判断する関数(FriendlyFire, Totem適用, 攻撃無効化も担う。)
    //他のダメージイベントだとLittleMaidに対してMPの方のEntityを取得してしまうのでこちらを採用。
    @SubscribeEvent
    public void onEntityTakeAttack(LivingAttackEvent event)
    {
        //victimがEntityLivingBaseを継承かつ、イベントがServer側で発生している場合
        if (!(event.getEntity() instanceof EntityLivingBase)) return;
        if (event.getEntityLiving().getEntityWorld().isRemote) return;

        final EntityLivingBase _VICTIM = event.getEntityLiving();
        final Entity _ATTACKER = event.getSource().getTrueSource();

        //retreatPotion効果を受けている場合は攻撃を無効化して終了
        if (_VICTIM.isPotionActive(f_Potion_CancelUpdate))
        {
            event.setCanceled(true);
            return;
        }
        //---

        //_VICTIMのTagからUUIDを抜き取る。(_VICTIMがEntityPlayerでなく、撤退のトーテム適用済みの場合のみ)
        final UUID _UUID_OWNER_VICTIMHAS = hasTotemPower(_VICTIM);

        //攻撃したのがプレイヤーなら...
        if (_ATTACKER instanceof EntityPlayer && !(_ATTACKER instanceof FakePlayer))
        {
            final EntityPlayer _ATTACKER_EP = (EntityPlayer) _ATTACKER;
            final ItemStack _ITEM_ATTACKER_EP = _ATTACKER_EP.getHeldItemMainhand();

            // _VICTIMがEntityPlayerを除くEntityLivingBase(もしくはFakePlayer)であるなら
            if (_VICTIM instanceof FakePlayer || !(_VICTIM instanceof EntityPlayer))
            {
                //攻撃したプレイヤーの持っているアイテムがretreatTotemかrevokeTotemであれば、関数をトリガーして終了。
                final Item _ITEM_ATTACKERHELD = _ITEM_ATTACKER_EP.getItem();

                if (_ITEM_ATTACKERHELD instanceof Item_TotemOfRetreat || _ITEM_ATTACKERHELD instanceof Item_TotemOfRevoke)
                {
                    //もし、configでbossにトーテムを使えないようにしていて、かつ、対象がボスなら、このif内は処理しない。
                    if (!(!RetreatConfig.c_Totem.totemCanApplyToBoss && !_VICTIM.isNonBoss()))
                    {
                        Effect_Totem.TotemInteractionForEntity(_ATTACKER_EP, _VICTIM, _ITEM_ATTACKER_EP);
                        event.setCanceled(true);
                        return;
                    }
                }
                //FRIENDLYFIRE無効時、攻撃したプレイヤーとVictimの持っているTagのUUIDが一致するなら、フレンドリーファイアを無効化して終了。
                //(OwnerからPetへの攻撃無効化処理。)
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

        //攻撃したのがEntityLivingBaseなら...(このif内はFriendryFireに関する処理のみ。)
        else if (_ATTACKER instanceof EntityLivingBase)
        {
            final UUID _UUID_OWNER_ATTACKERHAS = hasTotemPower((EntityLivingBase) _ATTACKER);

            //_ATTACKERがトーテムの効果を持っているなら。
            if (_UUID_OWNER_ATTACKERHAS != null)
            {
                //_VICTIMがEntityPlayerかつ、攻撃したEntityが持っているTagのUUIDと、_VICTIMのUUIDが一致するなら、フレンドリーファイアを無効化して終了。
                //(PetからOwnerへの攻撃無効化処理。)
                if (_VICTIM instanceof EntityPlayer && !RetreatConfig.c_System.totemFriendlyFire_toOwner)
                {

                    if (_UUID_OWNER_ATTACKERHAS.equals(_VICTIM.getUniqueID()))
                    {
                        event.setCanceled(true);
                        return;
                    }
                }
                //攻撃したEntityが持っているTagのUUIDと、_VICTIMが持っているTagのUUIDが一致するなら、フレンドリーファイアを無効化して終了。
                //(PetからPetへの攻撃無効化処理。)
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

        //この攻撃で死亡する場合
        if ((_VICTIM.getHealth() - event.getAmount()) <= 0)
        {
            //_VICTIMがトーテムの効果を持っているのかをこれで判定できる。(ownerの有無)
            if (_UUID_OWNER_VICTIMHAS != null)
            {
                //死亡をキャンセルして撤退させる。
                event.setCanceled(true);
                retreatEntity(_VICTIM, _UUID_OWNER_VICTIMHAS, event.getSource(), true);
            }
        }
    }




    //プレイヤーが死亡したとき、周囲のペットを撤退させる関数。(config有効時のみ)
    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event)
    {
        //configで0に設定されている場合は無効とみなす。
        if (RetreatConfig.c_System.maxDistanceOfRetreat_OnOwnerDeath == 0) return;
        if (!(event.getEntityLiving() instanceof EntityPlayer)) return;

        final EntityPlayer _PLAYER = (EntityPlayer) event.getEntityLiving();

        //周囲のentityのうち、自分のペット(撤退のトーテム適用済みのentity)を取得する。
        List<EntityLivingBase> _pets = searchEntities(_PLAYER, RetreatConfig.c_System.maxDistanceOfRetreat_OnOwnerDeath, _PLAYER.getPositionVector());
        for (EntityLivingBase __e : _pets)
        {
            retreatEntity(__e, _PLAYER.getUniqueID(), null, false);
        }
    }



    //メイン機能の関数
    public static void retreatEntity(EntityLivingBase entity, UUID ownerUUID, DamageSource damageSource, boolean isForced)
    {
        final World _WORLD = entity.getEntityWorld();
        final Vec3d _LOC = getEntityLoc(entity);

        //以下、entityへの撤退処理開始後の、プレイヤーに対する処理...
        EntityPlayer _owner = _WORLD.getPlayerEntityByUUID(ownerUUID);
        String _message;

        //ownerがnullでない(プレイヤーがオンライン)の場合
        if (_owner != null)
        {
            //entityが他者によって強制的に(攻撃などによって)撤退した場合と、
            //オーナーによって普通に撤退した場合でメッセージが異なる。
            if (isForced)
            {
                _message = RetreatConfig.c_Message.msg_Retreated;
            }
            else
            {
                _message = RetreatConfig.c_Message.msg_Retreated_byOwner;
            }
        }
        //ownerがnull(プレイヤーがオフライン)の場合
        //普通に撤退する場合において、オーナーが不在ということはありえないのでこのelse内でそれは考慮しない。
        else
        {
            _message = RetreatConfig.c_Message.msg_RetreatedWhenOwnerOffline;

            //全オンラインプレイヤーの中からランダムで一人選出。
            final PlayerList _PLAYERLIST = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList();
            final List<EntityPlayerMP> _PLAYERS = _PLAYERLIST.getPlayers();

            //オンラインのプレイヤーが一人もいない状態でペットが死亡した際の処理。(未検証)
            //たぶんそんなことは起こらないだろうとタカを括っている。
            if (_PLAYERS.isEmpty())
            {
                //この関数が呼ばれる前に、死亡の原因となった攻撃がキャンセルされているので、
                //おそらく「何も起こらない」という結果が生まれる。
                return;
            }
            //オンラインのプレイヤーが存在した場合。
            else
            {
                final Random _RAND = new Random();
                final int _RANDOMINDEX = _RAND.nextInt(_PLAYERS.size());

                _owner = _PLAYERS.get(_RANDOMINDEX);
            }
        }

        //死亡ログ生成。
        //entityが他者によって強制的に(攻撃などによって)撤退した場合と、
        //オーナーによって普通に撤退した場合でメッセージが異なる。
        final String _NAME_VICTIM = getName(entity);

        if (isForced)
        {
            final String _NAME_ATTACKER;

            //大元の攻撃者が存在する場合
            if (damageSource.getTrueSource() != null)
            {
                _NAME_ATTACKER = getName(damageSource.getTrueSource());
            }
            //実際に、直接攻撃したentityが存在する場合
            else if (damageSource.getImmediateSource() != null)
            {
                _NAME_ATTACKER = getName(damageSource.getImmediateSource());
            }
            //仕方がないのでDamageTypeをAttackerとする
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
        //以上、entity死亡後のプレイヤーに対する処理

        //演出再生
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

        //撤退処理(Teleport)
        final int _DURATION_RETREATPOTION = 20;    //RetreatPotionの効果時間。どっちみちLivingUpdateが停止されるので0でなければ値はなんでもいい。
        final List<Integer> _LOC_RETREAT = getRetreatingPoint(_owner, entity);

        if (entity.dimension == _LOC_RETREAT.get(0))
        {
            entity.setPosition(_LOC_RETREAT.get(1), _LOC_RETREAT.get(2), _LOC_RETREAT.get(3));

            //LivingUpdate停止用のポーション効果
            if (RetreatConfig.c_System.doFreezeRetreateds)
            {
                entity.addPotionEffect(new PotionEffect(f_Potion_CancelUpdate, _DURATION_RETREATPOTION, 1));
            }
            else
            {
                //LivingUpdateを停止しない場合はこの時点でEntityを初期化する。
                resetEntityStatus(entity);
            }
        }
        else
        {
            //Dimensionを、getRetreatingPointの返り値と等しくない場合は変更する。
            entity.changeDimension(_LOC_RETREAT.get(0),
                    new Teleporter_Retreat(_LOC_RETREAT.get(1), _LOC_RETREAT.get(2), _LOC_RETREAT.get(3),
                            RetreatConfig.c_System.doFreezeRetreateds, _DURATION_RETREATPOTION, f_Potion_CancelUpdate));
        }
        //---
    }



    //Entity撤退後にpetの状態を初期化する関数
    public static void resetEntityStatus(EntityLivingBase entity)
    {
        // 回復と消火を行い、ポーション効果と慣性を削除
        float _retreatHealth = 0.0f;
        if (hasHardTotem(entity))
        {
            _retreatHealth = entity.getMaxHealth() * RetreatConfig.c_System.healPercentage_Hard;
        }
        else
        {
            _retreatHealth = entity.getMaxHealth() * RetreatConfig.c_System.healPercentage;
        }
        //HPは最低でも1必要。
        if (_retreatHealth < 1.0f)
        {
            _retreatHealth = 1.0f;
        }
        //entityの現在のhpが、設定している回復量を下回ったときにのみ回復させる。
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

        //演出・リスキル防止用ポーション効果
        if (RetreatConfig.c_System.doApplyResistPotion)
        {
            final List<PotionEffect> _POTION_EFFECTS = new ArrayList<>(); //初期化して、ポーション効果を配列に格納する。

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




    //別Dimensionも検索範囲に含めて最適な撤退先を見つける関数
    private static List<Integer> getRetreatingPoint(EntityPlayer owner, Entity entity)
    {
        //最適な撤退先を見つける(Server Only)
        final int _ID_DIM = DimensionManager.getProvider(entity.dimension).getRespawnDimension((EntityPlayerMP) owner); //entityのいるDimensionでownerが死亡した場合のRespawn先
        final World _WORLD = DimensionManager.getWorld(_ID_DIM);
        BlockPos _loc = owner.getBedLocation(_ID_DIM);      //_ID_DIMにおけるownerのベッド

        if (_loc == null || DimensionManager.getWorld(_ID_DIM).getBlockState(_loc).getBlock() != Blocks.BED)
        {
            //DimensionID、_ID_DIMにおける初期リスポーン先(ベッドが存在しない場合)
            _loc = DimensionManager.getProvider(_ID_DIM).getSpawnPoint();
        }

        //以下、窒息防止処理
        //locの位置にあるブロックがairになるまでループ
        while (!_WORLD.isAirBlock(_loc))
        {
            _loc = _loc.up(1);
        }
        //SPAWN_HEIGHT分Y座標を上げる
        _loc = new BlockPos(_loc.getX(), (_loc.getY() + RetreatConfig.c_System.spawnHeight), _loc.getZ());
        //---

        List<Integer> _result = new ArrayList<>();
        _result.add(_ID_DIM);
        _result.add(_loc.getX());
        _result.add(_loc.getY());
        _result.add(_loc.getZ());
        return _result;
    }



    //Ownerとの距離がUPDATECANCEL_DISTANCEになるまでpetのLivingUpdateを停止する関数。
    @SubscribeEvent
    public void RetreatPotion_LUEvent(LivingEvent.LivingUpdateEvent event)
    {
        /*以下の条件を満たすEntityのUpdateを停止する。(ServerOnly)
        ・EntityLivingBaseを継承している
        ・ownerとの距離がUPDATECANCEL_DISTANCE以上
        ・撤退済み(retreatPotion効果を受けている)
         */
        //また、UPDATECANCEL_DISTANCEよりもownerとの距離が縮まった場合、retreatPotionを除去する。
        if (RetreatConfig.c_System.doFreezeRetreateds)
        {
            final EntityLivingBase _ENTITY = event.getEntityLiving();
            final World _WORLD = _ENTITY.getEntityWorld();

            if (_WORLD.isRemote) return;
            if (!_ENTITY.isPotionActive(f_Potion_CancelUpdate)) return;

            //ENTITYのTagからOwnerのUUIDを抜き取る
            final UUID _UUID_OWNER = hasTotemPower(_ENTITY);

            //一度撤退したことがあるのにTagからUUIDを抜き取れない場合(例外)
            if (_UUID_OWNER == null) {
                return;
            }
            //UUIDを抜き取れた場合
            else {
                final EntityPlayer _OWNER = _WORLD.getPlayerEntityByUUID(_UUID_OWNER);

                //ownerがオフラインの場合
                if (_OWNER == null) {
                    event.setCanceled(true);
                }
                //ownerとの距離が十分近くない場合
                else if (getSquareDistance(_ENTITY.getPositionVector(), _OWNER.getPositionVector()) > (Math.pow(RetreatConfig.c_System.unfreezeDistance, 2.0d))) {
                    event.setCanceled(true);
                }
                //UPDATECANCEL_DISTANCEよりもownerとpetの距離が縮まったとき
                else {
                    resetEntityStatus(_ENTITY);

                    final Vec3d _LOC = getEntityLoc(_ENTITY);

                    //以下、演出再生
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



    //petRegeneration_Interval ticksごとにトーテムの効果を受けたペットを回復させる関数。(Server only)
    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event)
    {
        if (event.phase == TickEvent.Phase.START)
        {
            tick_count_petregeneration++;

            if (tick_count_petregeneration >= RetreatConfig.c_System.petRegeneration_Interval)
            {
                tick_count_petregeneration = 0;

                //オンラインのプレイヤーをすべて取得
                final PlayerList _PLAYERLIST = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList();
                for (EntityPlayerMP __t : _PLAYERLIST.getPlayers())
                {
                    //各プレイヤーを中心として、半径petRegeneration_Radius以内にいるペットを取得。
                    final List<EntityLivingBase> __PETS = searchEntities(__t, RetreatConfig.c_System.petRegeneration_Radius, __t.getPositionVector());
                    if (__PETS.isEmpty()) continue;

                    for (EntityLivingBase ___e : __PETS)
                    {
                        //HPが満タンでなく、撤退後でもない場合のみ、
                        //petRegeneration_Amount分だけ回復。
                        if (___e.getHealth() == ___e.getMaxHealth()) continue;
                        if (___e.isPotionActive(f_Potion_CancelUpdate)) continue;

                        //hardとnormalのトーテムで回復量が異なる。
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
