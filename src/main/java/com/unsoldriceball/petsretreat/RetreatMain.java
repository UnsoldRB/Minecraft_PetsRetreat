package com.unsoldriceball.petsretreat;


import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.*;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


import java.util.*;

import static com.unsoldriceball.petsretreat.RetreatItem.HARD_TAG;
import static com.unsoldriceball.petsretreat.RetreatUtils.*;




@Mod(modid = RetreatMain.MOD_ID, acceptableRemoteVersions = "*")
public class RetreatMain
{
    public static final String MOD_ID = "petsretreat";

    private Configuration cfg;
    private boolean cfg_isInit = true;
    public static RetreatItem f_retreatItem;
    public static RetreatItem f_retreatItem_hard;
    public static Potion f_retreatPotion;
    private boolean DO_INIT;
    private boolean DO_PLAYSOUND;
    private boolean DO_PLAYPARTICLE;
    private static boolean DO_APPLYPOTION;
    private boolean DO_APPLYRETREATPOTION;
    private int UPDATECANCEL_DISTANCE; //この値よりも撤退後のentityに近づくと、potion効果が解除される。
    public static double HEALING_PERCENTAGE; //撤退後のHP
    public double SPAWN_HEIGHT; //撤退後にスポーンする高さ。ベッドのY座標にこの値が加わる。
    private boolean TOTEM_FRIENDLYFIRE;
    public static boolean TOTEM_ONETIME;
    public static boolean TOTEM_HARDRECIPE;
    public static boolean TOTEM_HARDRECIPE_REGENERATION;
    public static String TOTEM_TOOLTIP;
    public static String MESSAGE_TOTEM_APPLY;
    public static String MESSAGE_TOTEM_REVOKE;
    public static String MESSAGE_TOTEM_FAILED; //RetreatTotemを使おうとした対象が、既に別PlayerによってRetreatTotemを使用されていた時のメッセージ
    private String MESSAGE_RETREATED;
    private String MESSAGE_RETREATED_OFFLINE; //ownerがオフラインだった場合に、ランダムで選ばれた代理のプレイヤーに送信されるメッセージ




    //ModがInitializeを呼び出す前に発生するイベント。
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        //これでこのクラス内でForgeのイベントが動作するようになるらしい。
        MinecraftForge.EVENT_BUS.register(this);

        //Configを起動
        cfg = new Configuration(event.getSuggestedConfigurationFile());
        this.loadConfig();
        cfg_isInit = false;
        //---

        //そもそも起動させるのか。(Debug用)
        if (DO_INIT)
        {
            //撤退対象を設定するアイテムを登録
            f_retreatItem = new RetreatItem(false);
            f_retreatItem_hard = new RetreatItem(true);
            ForgeRegistries.ITEMS.registerAll(f_retreatItem, f_retreatItem_hard);
            //---

            //アイテムのレシピをconfigに基づいて登録
            final RetreatItem L_R_RESULT;
            final Ingredient L_R_AIR = Ingredient.fromItem(Items.AIR);
            final Ingredient L_R_DYE1 = Ingredient.fromStacks(new ItemStack(Items.DYE, 1, 5));
            final Ingredient L_R_DYE2 = Ingredient.fromStacks(new ItemStack(Items.DYE, 1, 13));
            final Ingredient L_R_CORE;

            if (TOTEM_HARDRECIPE)
            {
                L_R_RESULT = f_retreatItem_hard;
                L_R_CORE = Ingredient.fromItem(Items.TOTEM_OF_UNDYING);
            }
            else
            {
                L_R_RESULT = f_retreatItem;
                L_R_CORE = Ingredient.fromItem(Item.getItemFromBlock(Blocks.GOLD_BLOCK));
            }

            NonNullList<Ingredient> recipe_input = NonNullList.create();
            recipe_input.add(L_R_AIR);
            recipe_input.add(L_R_DYE1);
            recipe_input.add(L_R_AIR);
            recipe_input.add(L_R_DYE2);
            recipe_input.add(L_R_CORE);
            recipe_input.add(L_R_DYE2);
            recipe_input.add(L_R_AIR);
            recipe_input.add(L_R_DYE1);
            recipe_input.add(L_R_AIR);

            final IRecipe L_RECIPE = new ShapedRecipes(MOD_ID, 3, 3, recipe_input, new ItemStack(L_R_RESULT));

            L_RECIPE.setRegistryName(new ResourceLocation(MOD_ID, L_R_RESULT.f_id_TotemOfRetreat));
            ForgeRegistries.RECIPES.register(L_RECIPE);
            //---

            //AI無効化用のポーション効果を登録
            if (DO_APPLYRETREATPOTION)
            {
                final String L_POTION_ID = "RetreatPet.Cancel.LivingUpdate";
                f_retreatPotion = new RetreatPotionEffect(L_POTION_ID, true, 6580840, 0, 0);
                ForgeRegistries.POTIONS.register(f_retreatPotion);
            }
            //---
        }
    }



    //アイテムのモデル登録用イベント。
    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void registerModels(ModelRegistryEvent event)
    {
        ModelLoader.setCustomModelResourceLocation(f_retreatItem, 0, new ModelResourceLocation(new ResourceLocation(MOD_ID, f_retreatItem.f_id_TotemOfRetreat), "inventory"));
        ModelLoader.setCustomModelResourceLocation(f_retreatItem_hard, 0, new ModelResourceLocation(new ResourceLocation(MOD_ID, f_retreatItem.f_id_TotemOfRetreat), "inventory"));
    }



    //Config読み込み関数
    private void loadConfig()
    {
        cfg.load();

        //起動時にのみ読み込むConfig。本来はいつでも再読み込みできるけど、ころころ変更できてしまうとゲームが面白くなくなるものも含まれる。
        if (cfg_isInit)
        {
            DO_INIT = cfg.get("general", "C_DO_INIT", true).getBoolean();
            DO_APPLYRETREATPOTION = cfg.get("general.system", "C_DO_APPLYRETREATPOTION", true).getBoolean();
            UPDATECANCEL_DISTANCE = cfg.get("general.system", "C_UPDATECANCEL_DISTANCE", 8).getInt();
            HEALING_PERCENTAGE = cfg.get("general.system", "C_HEALING_PERCENTAGE", 0.05).getDouble();
            TOTEM_ONETIME = cfg.get("general.item", "C_TOTEM_ONETIME", true).getBoolean();
            TOTEM_HARDRECIPE = cfg.get("general.item", "C_TOTEM_HARDRECIPE", false).getBoolean();
            TOTEM_HARDRECIPE_REGENERATION = cfg.get("general.item", "C_TOTEM_HARDRECIPE_REGENERATION", true).getBoolean();
        }
        DO_PLAYPARTICLE = cfg.get("general.system", "C_DO_PLAYPARTICLE",  true).getBoolean();
        DO_PLAYPARTICLE = cfg.get("general.system", "C_DO_PLAYPARTICLE", true).getBoolean();
        DO_PLAYSOUND = cfg.get("general.system", "C_DO_PLAYSOUND", true).getBoolean();
        DO_APPLYPOTION = cfg.get("general.system", "C_DO_APPLYPOTION", true).getBoolean();
        SPAWN_HEIGHT = cfg.get("general.system", "C_SPAWN_HEIGHT", 0.5).getDouble();
        TOTEM_FRIENDLYFIRE = cfg.get("general.item", "C_TOTEM_FRIENDLYFIRE", true).getBoolean();
        TOTEM_TOOLTIP = cfg.get("general.item", "C_TOTEM_TOOLTIP", "Click the entity with this totem...").getString();
        MESSAGE_TOTEM_APPLY = cfg.get("general.message", "C_MESSAGE_TOTEM_APPLY", "Applied a totem to the %target%.").getString();
        MESSAGE_TOTEM_REVOKE = cfg.get("general.message", "C_MESSAGE_TOTEM_REVOKE", "Revoked a totem from the %target%.").getString();
        MESSAGE_TOTEM_FAILED = cfg.get("general.message", "C_MESSAGE_TOTEM_FAILED", "%target% has already had a totem effect applied by another player.").getString();
        MESSAGE_RETREATED = cfg.get("general.message", "C_MESSAGE_RETREATED", "The %victim% was forced to retreat by %attacker%.").getString();
        MESSAGE_RETREATED_OFFLINE = cfg.get("general.message", "C_MESSAGE_RETREATED_OFFLINE", "%victim% has retreated your spawn point. (Because owner offline.)").getString();
    }




    //ゲーム内からConfigを変更したときのイベント
    @SubscribeEvent
    public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event)
    {
        if (event.getModID().equals(MOD_ID))
        {
            ConfigManager.sync(MOD_ID, Config.Type.INSTANCE);
            loadConfig();
        }
    }



    //Entityが撤退するに値するかを判断する関数(FriendlyFire, Totem適用, 攻撃無効化のトリガーも担う。)
    //他のダメージイベントだとLittleMaidに対してMPの方のEntityを取得してしまうのでこちらを採用。
    @SubscribeEvent
    public void onEntityTakeAttack(LivingAttackEvent event)
    {
        //entityがEntityLivingBaseを継承かつ、イベントがServer側で発生している場合
        if (!(event.getEntity() instanceof EntityLivingBase)) return;
        if (event.getEntityLiving().getEntityWorld().isRemote) return;

        final EntityLivingBase L_ENTITY = event.getEntityLiving();
        final Entity L_ATTACKER = event.getSource().getTrueSource();

        //retreatPotion効果を受けている場合は攻撃を無効化して終了
        if (L_ENTITY.isPotionActive(f_retreatPotion))
        {
            event.setCanceled(true);
            return;
        }
        //---

        //ENTITYのTagからUUIDを抜き取る。
        UUID uuid_player = null;

        for(String _t : L_ENTITY.getTags())
        {
            if (!_t.contains("@" + MOD_ID)) continue;
            uuid_player = UUID.fromString(_t.replace("@" + MOD_ID + "_", ""));
            break;
        }
        //---

        //攻撃したのがプレイヤーなら...
        if (L_ATTACKER instanceof EntityPlayer)
        {
            final EntityPlayer L_ATTACKER_P = (EntityPlayer) L_ATTACKER;
            final ItemStack L_ATTACKER_P_ITEM = L_ATTACKER_P.getHeldItemMainhand();

            //攻撃したプレイヤーの持っているアイテムがretreatTotemであれば、関数をトリガーして終了。
            if (L_ATTACKER_P_ITEM.getItem() instanceof RetreatItem)
            {
                ((RetreatItem) L_ATTACKER_P_ITEM.getItem()).TotemInteractionForEntity(L_ATTACKER_P_ITEM, L_ATTACKER_P, L_ENTITY);
                event.setCanceled(true);
                return;
            }
            //FRIENDLYFIRE無効時、攻撃したプレイヤーとEntityの持っているTagのUUIDが一致するなら、フレンドリーファイアを無効化して終了。
            else if  (!TOTEM_FRIENDLYFIRE)
            {
                if (L_ATTACKER_P.getUniqueID().equals(uuid_player))
                {
                    event.setCanceled(true);
                    return;
                }
            }
        }
        //---

        //攻撃したのがEntityLivingBaseなら... (FRIENDLYFIRE無効時)
        else if (L_ATTACKER instanceof EntityLivingBase && (!TOTEM_FRIENDLYFIRE))
        {
            //攻撃したEntityのUUIDと攻撃されたEntityの持っているTagのUUIDが一致するなら、フレンドリーファイアを無効化して終了。
            if (L_ATTACKER.getTags().contains("@" + MOD_ID + "_" + uuid_player))
            {
                event.setCanceled(true);
                return;
            }
        }
        //---

        //この攻撃で死亡するかどうかの判定
        if ((L_ENTITY.getHealth() - event.getAmount()) > 0) return;

        //手懐けられているのかどうかをこれで判定できる。(ownerの有無)
        if (uuid_player != null)
        {
            event.setCanceled(true);
            retreatEntity(L_ENTITY, uuid_player, event.getSource());
        }
    }



    //メイン機能の関数
    private void retreatEntity(EntityLivingBase entity, UUID ownerUUID, DamageSource damageSource)
    {
        final World L_WORLD = entity.getEntityWorld();
        final Vec3d L_LOC = getEntityLoc(entity);

        // 死亡後、プレイヤーに対する処理
        EntityPlayer owner = L_WORLD.getPlayerEntityByUUID(ownerUUID);
        String message;

        //ownerがnullでない(プレイヤーがオンライン)の場合
        if (owner != null)
        {
            message = MESSAGE_RETREATED;
        }
        //ownerがnull(プレイヤーがオフライン)の場合
        else
        {
            message = MESSAGE_RETREATED_OFFLINE;

            //全オンラインプレイヤーの中からランダムで一人選出。
            final PlayerList L_PLAYERLIST = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList();
            final List<EntityPlayerMP> L_PLAYERS = L_PLAYERLIST.getPlayers();

            //オンラインのプレイヤーが一人もいない状態でペットが死亡した際の処理。(未検証)
            if (L_PLAYERS.isEmpty())
            {
                return;
            }

            Random random = new Random();
            final int L_RANDOMINDEX = random.nextInt(L_PLAYERS.size());

            owner = L_PLAYERS.get(L_RANDOMINDEX);
            //---
        }

        //死亡ログ生成
        String attacerName;

        //大元の攻撃者が存在する場合
        if (damageSource.getTrueSource() != null)
        {
            attacerName = getName(damageSource.getTrueSource());
        }
        //実際に、直接攻撃したentityが存在する場合
        else if (damageSource.getImmediateSource() != null)
        {
            attacerName = getName(damageSource.getImmediateSource());
        }
        //仕方がないのでDamageTypeをAttackerとする
        else
        {
            attacerName = damageSource.getDamageType();
        }

        message = message.replace("%victim%", getName(entity));
        message = message.replace("%attacker%", attacerName);

        owner.sendMessage(new TextComponentString(message));
        //---

        //---

        //演出再生
        if (DO_PLAYSOUND)
        {
            L_WORLD.playSound(null, L_LOC.x, L_LOC.y, L_LOC.z, SoundEvents.ITEM_TOTEM_USE, SoundCategory.PLAYERS, 0.8f, 1.2f);
        }
        if (DO_PLAYPARTICLE)
        {
            initPlayParticle(owner, L_LOC, EnumParticleTypes.TOTEM);
        }
        //---

        //撤退処理(Teleport)
        final int L_RETREATPOTION_DURATION = 20;    //RetreatPotionの効果時間。どっちみちLivingUpdateが停止されるので0でなければ値はなんでもいい。
        final List<Integer> L_LOC_RETREAT = this.getRetreatingPoint(owner, entity);

        if (entity.dimension == L_LOC_RETREAT.get(0))
        {
            entity.setPosition(L_LOC_RETREAT.get(1), L_LOC_RETREAT.get(2), L_LOC_RETREAT.get(3));

            //LivingUpdate停止用のポーション効果
            if (DO_APPLYRETREATPOTION)
            {
                entity.addPotionEffect(new PotionEffect(f_retreatPotion, L_RETREATPOTION_DURATION, 1));
            }
            else
            {
                //LivingUpdateを停止しない場合はこの時点でEntityを初期化する。
                resetEntityStatus(entity);
            }
        }
        else
        {
            //Dimentionを、getRetreatingPointの返り値と等しくない場合は変更する。
            entity.changeDimension(L_LOC_RETREAT.get(0),
                    new RetreatTeleporter(L_LOC_RETREAT.get(1), L_LOC_RETREAT.get(2), L_LOC_RETREAT.get(3),
                            DO_APPLYRETREATPOTION, L_RETREATPOTION_DURATION, f_retreatPotion));
        }
        //---
    }



    //Entity撤退後にpetの状態を初期化する関数
    public static void resetEntityStatus(EntityLivingBase entity)
    {
        // 回復と消火とポーション効果と慣性を削除
        float retreatHealth = (float) Math.ceil(entity.getMaxHealth() * HEALING_PERCENTAGE);
        if (retreatHealth < 1.0f)
        {
            retreatHealth = 1.0f;
        }
        entity.setHealth(retreatHealth);

        entity.extinguish();
        entity.clearActivePotions();

        entity.motionX = randomVec(1);
        entity.motionY = 0.5;
        entity.motionZ = randomVec(2);
        //---

        //演出・リスキル防止用ポーション効果
        if (DO_APPLYPOTION)
        {
            final List<PotionEffect> L_POTION_EFFECTS = new ArrayList<>(); //初期化して、ポーション効果を配列に格納する。

            L_POTION_EFFECTS.add(new PotionEffect(MobEffects.RESISTANCE, 60, 5));
            L_POTION_EFFECTS.add(new PotionEffect(MobEffects.INVISIBILITY, 2, 1));
            if (TOTEM_HARDRECIPE_REGENERATION)
            {
                if (entity.getTags().contains(HARD_TAG))
                {
                    L_POTION_EFFECTS.add(new PotionEffect(MobEffects.REGENERATION, 600, 2));
                }
            }
            for(PotionEffect p : L_POTION_EFFECTS)
            {
                entity.addPotionEffect(p);
            }
        }
    }




    //別Dimentionも検索範囲に含めて最適な撤退先を見つける関数
    @SuppressWarnings("ConstantConditions") //if (loc == null) {}に対して使用。
    private List<Integer> getRetreatingPoint(EntityPlayer owner, Entity entity)
    {
        //最適な撤退先を見つける(Server Only)
        final int L_ID_DIm = DimensionManager.getProvider(entity.dimension).getRespawnDimension((EntityPlayerMP) owner); //entityのいるDimentionでownerが死亡した場合のRespawn先
        BlockPos loc = owner.getBedLocation(L_ID_DIm);      //L_ID_DIMにおけるownerのベッド

        if (loc == null || DimensionManager.getWorld(L_ID_DIm).getBlockState(loc).getBlock() != Blocks.BED)
        {
            //DimentionID、L_ID_DIMにおける初期リスポーン先(ベッドが存在しない場合)
            loc = DimensionManager.getProvider(L_ID_DIm).getSpawnPoint();
        }

        //窒息防止処理
        final World L_WORLD = DimensionManager.getWorld(L_ID_DIm);

        //locの位置にあるブロックがairになるまでループ
        while (!L_WORLD.isAirBlock(loc))
        {
            loc = loc.up(1);
        }
        //SPAWN_HEIGHT分Y座標を上げる
        loc = new BlockPos(loc.getX(), (loc.getY() + SPAWN_HEIGHT), loc.getZ());
        //---

        List<Integer> result = new ArrayList<>();
        result.add(L_ID_DIm);
        result.add(loc.getX());
        result.add(loc.getY());
        result.add(loc.getZ());
        return result;
    }



    //Ownerとの距離がUPDATECANCEL_DISTANCEになるまでpetのLivingUpdateを停止する関数
    @SubscribeEvent
    public void RetreatPotion_LELUE(LivingEvent.LivingUpdateEvent event)
    {
        /*以下の条件を満たすEntityのUpdateを停止する。(ServerOnly)
        ・EntityLivingBaseを継承している
        ・ownerとの距離がUPDATECANCEL_DISTANCE以上
        ・撤退済み(retreatPotion効果を受けている)
         */
        //また、UPDATECANCEL_DISTANCEよりもownerとの距離が縮まった場合、retreatPotionを除去する。
        if (!DO_APPLYRETREATPOTION) return;
        final EntityLivingBase L_ENTITY = event.getEntityLiving();
        final World L_WORLD = L_ENTITY.getEntityWorld();
        if (L_WORLD.isRemote) return;
        if (!L_ENTITY.isPotionActive(f_retreatPotion)) return;

        //ENTITYのTagからOwnerのUUIDを抜き取る
        UUID owner_uuid = null;
        EntityPlayer owner;

        for(String t : L_ENTITY.getTags())
        {
            if (!t.contains("@" + MOD_ID)) continue;
            owner_uuid = UUID.fromString(t.replace("@" + MOD_ID + "_", ""));
            break;
        }
        //一度撤退したことがあるのにTagからUUIDを抜き取れない場合(例外)
        if (owner_uuid == null)
        {
            return;
        }
        //UUIDを抜き取れた場合
        else
        {
            owner = L_WORLD.getPlayerEntityByUUID(owner_uuid);
        }
        //---

        //ownerがオフラインの場合
        if (owner == null)
        {
            event.setCanceled(true);
        }
        //ownerとの距離が十分近くない場合
        else if (L_ENTITY.getDistanceSq(owner) > (UPDATECANCEL_DISTANCE^2))
        {
            event.setCanceled(true);
        }
        //UPDATECANCEL_DISTANCEよりもownerとpetの距離が縮まったとき
        else
        {
            resetEntityStatus(L_ENTITY);

            final Vec3d L_LOc = getEntityLoc(L_ENTITY);

            //以下、演出再生
            if (DO_PLAYSOUND)
            {
                L_WORLD.playSound(null, L_LOc.x, L_LOc.y, L_LOc.z, SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 0.8f, 1.78f);
            }
            if (DO_PLAYPARTICLE)
            {
                initPlayParticle(owner, L_LOc, EnumParticleTypes.VILLAGER_HAPPY);
            }
        }
    }



    //Particle生成関数
    public static void initPlayParticle (EntityPlayer player, Vec3d loc, EnumParticleTypes pType)
    {
        //わざわざPacketを使うのも面倒なのでサーバーからChatを送信してRPCを再現する。(ServerOnly)
        //座標に関しては、サーバーとクライアント間でズレがあるので、プレイヤーを原点とした差を渡す。
        if (!player.getEntityWorld().isRemote)
        {
            final int L_DIM = player.dimension;
            final Vec3d L_LOC_P = getEntityLoc(player);

            String message = "@" + MOD_ID;
            message += "_" + player.getUniqueID();
            message += "_" + (L_LOC_P.x - loc.x);
            message += "_" + (L_LOC_P.y - loc.y);
            message += "_" + (L_LOC_P.z - loc.z);
            message += "_" + pType.getParticleID();

            //dimentionが一致する全PlayerにChatmessageを送信する。
            final PlayerList _L_PLAYERLIST = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList();

            for (EntityPlayerMP _p : _L_PLAYERLIST.getPlayers())
            {
                if (_p.dimension == L_DIM)
                {
                    _p.sendMessage(new TextComponentString(message));
                }
            }
        }
    }
    //チャットメッセージを受け取った時のイベント
    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void playParticle(ClientChatReceivedEvent event)
    {
        //Chat内容に"@MOD_ID"を含む場合(ClientOnly)
        final String L_MSG = event.getMessage().getUnformattedText();

        if (L_MSG.contains("@" + MOD_ID))
        {
            event.setCanceled(true);

            //Chatmessageを分解して、　定数を定義する。
            final List<String> L_SPLIT = Arrays.asList(L_MSG.split("_"));

            final World L_WORLD = Minecraft.getMinecraft().world;
            final EntityPlayer L_PLAYER = L_WORLD.getPlayerEntityByUUID(UUID.fromString(L_SPLIT.get(1)));
            assert L_PLAYER != null;
            final Vec3d L_LOC_P = getEntityLoc(L_PLAYER);
            final Vec3d L_LOC_EFFECT = new Vec3d(
                    L_LOC_P.x - Double.parseDouble(L_SPLIT.get(2)),
                    L_LOC_P.y - Double.parseDouble(L_SPLIT.get(3)),
                    L_LOC_P.z - Double.parseDouble(L_SPLIT.get(4)));
            final EnumParticleTypes L_TYPE_EFFECT = EnumParticleTypes.getParticleFromId(Integer.parseInt(L_SPLIT.get(5)));
            //---

            //Totemとそれ以外のParticleの生成数を指定
            final int L_PARTICLECOUNT_TOTEM = 60;
            final int L_PARTICLECOUNT_OTHER = 6;

            //Particle再生
            assert L_TYPE_EFFECT != null;
            if (L_TYPE_EFFECT == EnumParticleTypes.TOTEM)
            {
                for (int i = 0; i < L_PARTICLECOUNT_TOTEM; i++)
                {
                    L_WORLD.spawnParticle(L_TYPE_EFFECT, L_LOC_EFFECT.x, L_LOC_EFFECT.y + 0.5, L_LOC_EFFECT.z, randomVec(i - 1), (Math.abs(randomVec(i)) * 1.75), randomVec(i + 2));
                }
            }
            else
            {
                for (int i = 0; i < L_PARTICLECOUNT_OTHER; i++)
                {
                    L_WORLD.spawnParticle(L_TYPE_EFFECT, L_LOC_EFFECT.x + (randomVec(i) * 2), L_LOC_EFFECT.y + 0.5 + (randomVec(i + 1) * 2), L_LOC_EFFECT.z + (randomVec(i + 2) * 2), 0, 0, 0);
                }
            }
        }
    }
}
