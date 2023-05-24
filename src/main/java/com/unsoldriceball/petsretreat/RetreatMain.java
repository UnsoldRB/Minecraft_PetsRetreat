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
public class RetreatMain {
    public static final String MOD_ID = "petsretreat";
    private Configuration cfg;
    private boolean cfg_isInit = true;
    public static RetreatItem retreatItem;
    public static RetreatItem retreatItem_hard;
    public static Potion retreatPotion;
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



    //Mod起動時の関数
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event){ //ModがInitializeを呼び出す前に発生するイベント。
        MinecraftForge.EVENT_BUS.register(this); //これでこのクラス内でForgeのイベントが動作するようになるらしい。

        //Configを起動
        cfg = new Configuration(event.getSuggestedConfigurationFile());
        this.loadConfig();
        cfg_isInit = false;
        //---

        if (!DO_INIT) return; //そもそも起動させるのか。(Debug用)

        //撤退対象を設定するアイテムを登録
        retreatItem = new RetreatItem(false);
        retreatItem_hard = new RetreatItem(true);
        ForgeRegistries.ITEMS.registerAll(retreatItem, retreatItem_hard);
        //---

        //アイテムのレシピをconfigに基づいて登録
        final RetreatItem R_RESULT;
        final Ingredient R_AIR = Ingredient.fromItem(Items.AIR);
        final Ingredient R_DYE1 = Ingredient.fromStacks(new ItemStack(Items.DYE, 1, 5));
        final Ingredient R_DYE2 = Ingredient.fromStacks(new ItemStack(Items.DYE, 1, 13));
        final Ingredient R_CORE;
        if (TOTEM_HARDRECIPE) {
            R_RESULT = retreatItem_hard;
            R_CORE = Ingredient.fromItem(Items.TOTEM_OF_UNDYING);
        }
        else {
            R_RESULT = retreatItem;
            R_CORE = Ingredient.fromItem(Item.getItemFromBlock(Blocks.GOLD_BLOCK));
        }

        NonNullList<Ingredient> recipe_input = NonNullList.create();
        recipe_input.add(R_AIR);        recipe_input.add(R_DYE1);        recipe_input.add(R_AIR);
        recipe_input.add(R_DYE2);       recipe_input.add(R_CORE);        recipe_input.add(R_DYE2);
        recipe_input.add(R_AIR);        recipe_input.add(R_DYE1);        recipe_input.add(R_AIR);


        final IRecipe RECIPE = new ShapedRecipes(MOD_ID, 3, 3, recipe_input, new ItemStack(R_RESULT));
        RECIPE.setRegistryName(new ResourceLocation(MOD_ID, R_RESULT.ID_TOTEM_OF_RETREAT));
        ForgeRegistries.RECIPES.register(RECIPE);
        //---

        //AI無効化用のポーション効果を登録
        if (DO_APPLYRETREATPOTION) {
            final String POTION_ID = "RetreatPet.Cancel.LivingUpdate";
            retreatPotion = new RetreatPotionEffect(POTION_ID, true, 6580840, 0, 0);
            ForgeRegistries.POTIONS.register(retreatPotion);
        }
        //---
    }



    //アイテムのモデル登録用イベント。
    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void registerModels(ModelRegistryEvent event) {
        ModelLoader.setCustomModelResourceLocation(retreatItem, 0, new ModelResourceLocation(new ResourceLocation(MOD_ID, retreatItem.ID_TOTEM_OF_RETREAT), "inventory"));
        ModelLoader.setCustomModelResourceLocation(retreatItem_hard, 0, new ModelResourceLocation(new ResourceLocation(MOD_ID, retreatItem.ID_TOTEM_OF_RETREAT), "inventory"));
    }



    //Config読み込み関数
    private void loadConfig(){
        cfg.load();

        //起動時にのみ読み込むConfig。本来はいつでも再読み込みできるけど、ころころ変更できてしまうとゲームが面白くなくなるものも含まれる。
        if (cfg_isInit) {
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
    public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (!event.getModID().equals(MOD_ID)) return;

        ConfigManager.sync(MOD_ID, Config.Type.INSTANCE);
        loadConfig();
    }



    //Entityが撤退するに値するかを判断する関数(FriendlyFire, Totem適用, 攻撃無効化のトリガーも担う。)
    @SubscribeEvent
    public void onEntityTakeAttack(LivingAttackEvent event) { //他のダメージイベントだとLittleMaidに対してMPの方のEntityを取得してしまうのでこちらを採用。
        //entityがEntityLivingBaseを継承かつ、イベントがServer側で発生している場合
        if (!(event.getEntity() instanceof EntityLivingBase)) return;
        if (event.getEntityLiving().getEntityWorld().isRemote) return;

        final EntityLivingBase ENTITY = event.getEntityLiving();

        //retreatPotion効果を受けている場合は攻撃を無効化して終了
        if (ENTITY.isPotionActive(retreatPotion)) {
            event.setCanceled(true);
            return;
        }
        //---

        //ENTITYのTagからUUIDを抜き取る。
        UUID uuid_Player = null;
        for(String t : ENTITY.getTags()) {
            if (!t.contains("@" + MOD_ID)) continue;
            uuid_Player = UUID.fromString(t.replace("@" + MOD_ID + "_", ""));
            break;
        }
        //---

        //攻撃したのがプレイヤーなら...
        if (event.getSource().getTrueSource() instanceof EntityPlayer) {

            final EntityPlayer ATTACKER_P = (EntityPlayer) event.getSource().getTrueSource();
            final ItemStack ATTACKER_I = ATTACKER_P.getHeldItemMainhand();

            //攻撃したプレイヤーの持っているアイテムがretreatTotemであれば、関数をトリガーして終了。
            if (ATTACKER_I.getItem() instanceof RetreatItem) {
                ((RetreatItem) ATTACKER_I.getItem()).TotemInteractionForEntity(ATTACKER_I, ATTACKER_P, ENTITY);
                event.setCanceled(true);
                return;
            }
            //FRIENDLYFIRE無効時、攻撃したプレイヤーとEntityの持っているTagのUUIDが一致するなら、フレンドリーファイアを無効化して終了。
            else if  (!TOTEM_FRIENDLYFIRE) {
                if (ATTACKER_P.getUniqueID().equals(uuid_Player)) {
                    event.setCanceled(true);
                    return;
                }
            }
        }
        //---

        //攻撃したのがEntityLivingBaseなら... (FRIENDLYFIRE無効時)
        else if (event.getSource().getTrueSource() instanceof EntityLivingBase && (!TOTEM_FRIENDLYFIRE)) {
            //攻撃したEntityのUUIDと攻撃されたEntityの持っているTagのUUIDが一致するなら、フレンドリーファイアを無効化して終了。
            if (event.getSource().getTrueSource().getTags().contains("@" + MOD_ID + "_" + uuid_Player)) {
                event.setCanceled(true);
                return;
            }
        }
        //---

        //この攻撃で死亡するかどうかの判定
        if ((ENTITY.getHealth() - event.getAmount()) > 0) return;

        if (uuid_Player != null) { //手懐けられているのかどうか
            event.setCanceled(true);
            retreatEntity(ENTITY, uuid_Player, event.getSource());
        }
    }



    //メイン機能の関数
    private void retreatEntity(EntityLivingBase entity, UUID ownerUUID, DamageSource damageSource) {

        final World WORLD = entity.getEntityWorld();
        final Vec3d LOC = getEntityLoc(entity);

        // 死亡後、プレイヤーに対する処理
        EntityPlayer owner = WORLD.getPlayerEntityByUUID(ownerUUID);
        String message;

        if (owner != null) { //ownerがnullでない(プレイヤーがオンライン)の場合
            message = MESSAGE_RETREATED;
        }
        else { //ownerがnull(プレイヤーがオフライン)の場合
            message = MESSAGE_RETREATED_OFFLINE;

            //全オンラインプレイヤーの中からランダムで一人選出。
            PlayerList playerList = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList();
            List<EntityPlayerMP> players = playerList.getPlayers();

            if (players.isEmpty()) { //オンラインのプレイヤーが一人もいない状態でペットが死亡した際の処理。(未使用)
                return;
            }

            Random random = new Random();
            int randomIndex = random.nextInt(players.size());
            owner = players.get(randomIndex);
            //---
        }

        //死亡ログ生成
        String attacerName;
        if (damageSource.getTrueSource() != null) { //大元の攻撃者が存在する場合
            attacerName = getName(damageSource.getTrueSource());
        }
        else if (damageSource.getImmediateSource() != null){ //実際に、直接攻撃したentityが存在する場合
            attacerName = getName(damageSource.getImmediateSource());
        }
        else {  //仕方がないのでDamageTypeをAttackerとする
            attacerName = damageSource.getDamageType();
        }

        message = message.replace("%victim%", getName(entity));
        message = message.replace("%attacker%", attacerName);

        owner.sendMessage(new TextComponentString(message));
        //---

        //---

        //演出再生
        if (DO_PLAYSOUND) {
            WORLD.playSound(null, LOC.x, LOC.y, LOC.z, SoundEvents.ITEM_TOTEM_USE, SoundCategory.PLAYERS, 0.8f, 1.2f);
        }
        if (DO_PLAYPARTICLE) {
            initPlayParticle(owner, LOC, EnumParticleTypes.TOTEM);
        }
        //---

        //撤退処理(Teleport)
        final int RETREATPOTION_DURATION = 20; //RetreatPotionの効果時間。どっちみちLivingUpdateが停止されるので0でなければ値はなんでもいい。

        List<Integer> retreatLoc = this.getRetreatingPoint(owner, entity);

        if (entity.dimension == retreatLoc.get(0)) {
            entity.setPosition(retreatLoc.get(1), retreatLoc.get(2), retreatLoc.get(3));

            //LivingUpdate停止用のポーション効果
            if (DO_APPLYRETREATPOTION) {
                entity.addPotionEffect(new PotionEffect(retreatPotion, RETREATPOTION_DURATION, 1));
            }
            else {
                //LivingUpdateを停止しない場合はこの時点でEntityを初期化する。
                resetEntityStatus(entity);
            }
        }
        else {
            //Dimentionを、getRetreatingPointの返り値と等しくない場合は変更する。
            entity.changeDimension(retreatLoc.get(0),
                    new RetreatTeleporter(retreatLoc.get(1), retreatLoc.get(2), retreatLoc.get(3),
                            DO_APPLYRETREATPOTION, RETREATPOTION_DURATION, retreatPotion));
        }
        //---
    }



    //撤退したpetの状態を初期化する関数
    public static void resetEntityStatus(EntityLivingBase entity) { //Entity撤退後の処理

        // 回復と消火とポーション効果と慣性を削除
        float retreatHealth = (float) Math.ceil(entity.getMaxHealth() * HEALING_PERCENTAGE);
        if (retreatHealth < 1.0f) retreatHealth = 1.0f;
        entity.setHealth(retreatHealth);

        entity.extinguish();
        entity.clearActivePotions();

        entity.motionX = randomVec(1);
        entity.motionY = 0.5;
        entity.motionZ = randomVec(2);
        //---

        //演出・リスキル防止用ポーション効果
        if (DO_APPLYPOTION) {
            List<PotionEffect> pEffects = new ArrayList<>(); //初期化して、ポーション効果を配列に格納する。
            pEffects.add(new PotionEffect(MobEffects.RESISTANCE, 60, 5));
            pEffects.add(new PotionEffect(MobEffects.INVISIBILITY, 2, 1));
            if (TOTEM_HARDRECIPE_REGENERATION) {
                if (entity.getTags().contains(HARD_TAG)) {
                    pEffects.add(new PotionEffect(MobEffects.REGENERATION, 600, 2));
                }
            }
            for(PotionEffect p : pEffects) {
                entity.addPotionEffect(p);
            }
        }
    }




    //別Dimentionも検索範囲に含めて最適な撤退先を見つける関数
    @SuppressWarnings("ConstantConditions") //if (loc == null) {}に対して使用。
    private List<Integer> getRetreatingPoint(EntityPlayer owner, Entity entity) { //最適な撤退先を見つける(Server Only)
        int dim = DimensionManager.getProvider(entity.dimension).getRespawnDimension((EntityPlayerMP) owner); //entityのいるDimentionでownerが死亡した場合のRespawn先
        BlockPos loc = owner.getBedLocation(dim); //DimentionID、dimにおけるownerのベッド
        if (loc == null || DimensionManager.getWorld(dim).getBlockState(loc).getBlock() != Blocks.BED) {
            loc = DimensionManager.getProvider(dim).getSpawnPoint(); //DimentionID、dimにおける初期リスポーン先(ベッドが存在しない場合)
        }

        //窒息防止処理
        World world = DimensionManager.getWorld(dim);
        while (!world.isAirBlock(loc)) { //locの位置にあるブロックがairになるまでループ
            loc = loc.up(1);
        }
        loc = new BlockPos(loc.getX(), (loc.getY() + SPAWN_HEIGHT), loc.getZ()); //SPAWN_HEIGHT分Y座標を上げる
        //---

        List<Integer> result = new ArrayList<>();
        result.add(dim);
        result.add(loc.getX());
        result.add(loc.getY());
        result.add(loc.getZ());
        return result;
    }



    //Ownerとの距離がUPDATECANCEL_DISTANCEになるまでpetのLivingUpdateを停止する関数
    @SubscribeEvent
    public void RetreatPotion_LELUE(LivingEvent.LivingUpdateEvent event) {
        /*以下の条件を満たすEntityのUpdateを停止する。(ServerOnly)
        ・EntityLivingBaseを継承している
        ・ownerとの距離がUPDATECANCEL_DISTANCE以上
        ・撤退済み(retreatPotion効果を受けている)
         */
        //また、UPDATECANCEL_DISTANCEよりもownerとの距離が縮まった場合、retreatPotionを除去する。
        if (!DO_APPLYRETREATPOTION) return;
        final EntityLivingBase ENTITY = event.getEntityLiving();
        final World WORLD = ENTITY.getEntityWorld();
        if (WORLD.isRemote) return;
        if (!ENTITY.isPotionActive(retreatPotion)) return;

        //ENTITYのTagからOwnerのUUIDを抜き取る
        UUID owner_uuid = null;
        EntityPlayer owner;

        for(String t : ENTITY.getTags()) {
            if (!t.contains("@" + MOD_ID)) continue;
            owner_uuid = UUID.fromString(t.replace("@" + MOD_ID + "_", ""));
            break;
        }
        if (owner_uuid == null) { //一度撤退したことがあるのにTagからUUIDを抜き取れない場合(例外)
            return;
        }
        else { //UUIDを抜き取れた場合
            owner = WORLD.getPlayerEntityByUUID(owner_uuid);
        }
        //---

        if (owner == null) { //ownerがオフラインの場合
            event.setCanceled(true);
        }
        else if (ENTITY.getDistanceSq(owner) > (UPDATECANCEL_DISTANCE^2)) {
            event.setCanceled(true);
        }
        else { //UPDATECANCEL_DISTANCEよりもownerとpetの距離が縮まったとき
            resetEntityStatus(ENTITY);

            final Vec3d LOC = getEntityLoc(ENTITY);
            //以下、演出再生
            if (DO_PLAYSOUND) {
                WORLD.playSound(null, LOC.x, LOC.y, LOC.z, SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 0.8f, 1.78f);
            }
            if (DO_PLAYPARTICLE) {
                initPlayParticle(owner, LOC, EnumParticleTypes.VILLAGER_HAPPY);
            }
        }
    }



    //Particle生成関数
    public static void initPlayParticle (EntityPlayer player, Vec3d loc, EnumParticleTypes pType) {
        //わざわざPacketを使うのも面倒なのでサーバーからChatを送信してRPCを再現する。(ServerOnly)
        //座標に関しては、サーバーとクライアント間でズレがあるので、プレイヤーを原点とした差を渡す。
        if (player.getEntityWorld().isRemote) return;

        final int DIM = player.dimension;
        final Vec3d PLOC = getEntityLoc(player);

        String message = "@" + MOD_ID;
        message += "_" + player.getUniqueID();
        message += "_" + (PLOC.x - loc.x);
        message += "_" + (PLOC.y - loc.y);
        message += "_" + (PLOC.z - loc.z);
        message += "_" + pType.getParticleID();

        //dimentionが一致する全PlayerにChatmessageを送信する。
        final PlayerList PLAYERLIST = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList();
        for (EntityPlayerMP p : PLAYERLIST.getPlayers()) {
            if (p.dimension == DIM) {
                p.sendMessage(new TextComponentString(message));
            }
        }
    }
    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void playParticle(ClientChatReceivedEvent event) {
        //Chat内容に"@MOD_ID"を含む場合(ClientOnly)
        final String MSG = event.getMessage().getUnformattedText();
        if (MSG.contains("@" + MOD_ID)) {
            event.setCanceled(true);

            //Chatmessageを分解して、　定数を定義する。
            final List<String> SPLIT = Arrays.asList(MSG.split("_"));

            final World WORLD = Minecraft.getMinecraft().world;
            final EntityPlayer PLAYER = WORLD.getPlayerEntityByUUID(UUID.fromString(SPLIT.get(1)));
            assert PLAYER != null;
            final Vec3d PLOC = getEntityLoc(PLAYER);
            final Vec3d LOC = new Vec3d(
                    PLOC.x - Double.parseDouble(SPLIT.get(2)),
                    PLOC.y - Double.parseDouble(SPLIT.get(3)),
                    PLOC.z - Double.parseDouble(SPLIT.get(4)));
            final EnumParticleTypes PTYPE = EnumParticleTypes.getParticleFromId(Integer.parseInt(SPLIT.get(5)));
            //---

            //Totemとそれ以外のParticleの生成数を指定
            final int PARTICLECOUNT_TOTEM = 60;
            final int PARTICLECOUNT_OTHER = 6;

            //Particle再生
            assert PTYPE != null;
            if (PTYPE == EnumParticleTypes.TOTEM) {
                for (int i = 0; i < PARTICLECOUNT_TOTEM; i++) {
                    WORLD.spawnParticle(PTYPE, LOC.x, LOC.y + 0.5, LOC.z, randomVec(i - 1), (Math.abs(randomVec(i)) * 1.75), randomVec(i + 2));
                }
            } else {
                for (int i = 0; i < PARTICLECOUNT_OTHER; i++) {
                    WORLD.spawnParticle(PTYPE, LOC.x + (randomVec(i) * 2), LOC.y + 0.5 + (randomVec(i + 1) * 2), LOC.z + (randomVec(i + 2) * 2), 0, 0, 0);
                }
            }
        }
    }
}
