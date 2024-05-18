package com.unsoldriceball.petsretreat;


import com.unsoldriceball.petsretreat.guidebook.GiveGuideBook;
import com.unsoldriceball.petsretreat.killlog.KillLogSystem;
import com.unsoldriceball.petsretreat.retreatsystem.Item_TotemOfRetreat;
import com.unsoldriceball.petsretreat.retreatsystem.Item_TotemOfRevoke;
import com.unsoldriceball.petsretreat.retreatsystem.Potion_CancelUpdate;
import com.unsoldriceball.petsretreat.retreatsystem.RetreatSystem;
import com.unsoldriceball.petsretreat.rods.*;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.potion.Potion;
import net.minecraft.util.*;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;




@Mod(modid = RetreatMain.ID_MOD, acceptableRemoteVersions = "*")
public class RetreatMain
{
    public static final String ID_MOD = "petsretreat";

    public static Item_TotemOfRetreat f_Item_Totem_Normal;
    public static Item_TotemOfRetreat f_Item_Totem_Hard;
    public static Item_TotemOfRevoke f_Item_Totem_Revoke;
    public static Item_CommandRod f_Item_CommandRod;
    public static Item_TransferRod f_Item_TransferRod;
    public static Item_RetreatRod f_Item_RetreatRod;
    public static Potion f_Potion_CancelUpdate;
    public static Potion f_Potion_CommandRod;
    public static Potion f_Potion_TransferRod;
    public static SimpleNetworkWrapper f_wrapper_particle;
    public static SimpleNetworkWrapper f_wrapper_rod;




    //ModがInitializeを呼び出す前に発生するイベント。
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        //これで指定したクラス内でForgeのイベントが動作するようになるらしい。
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new RetreatSystem());
        MinecraftForge.EVENT_BUS.register(new KillLogSystem());
        MinecraftForge.EVENT_BUS.register(new Effect_Rod());
        MinecraftForge.EVENT_BUS.register(new GiveGuideBook());
        //---

        //Packetを使えるようにしておく。
        f_wrapper_particle = NetworkRegistry.INSTANCE.newSimpleChannel("petsretreat_particle");
        f_wrapper_particle.registerMessage(Packet_Particle.Handler.class, Packet_Particle.class, 1, Side.CLIENT);
        f_wrapper_rod = NetworkRegistry.INSTANCE.newSimpleChannel("petsretreat_rod");
        f_wrapper_rod.registerMessage(Packet_Rod.Handler.class, Packet_Rod.class, 2, Side.SERVER);
        //---

        //アイテムを登録
        List<Item> _registeritems = new ArrayList<>();

        // Configでアイテムが有効なら登録する。
        if (RetreatConfig.c_Totem.enableNormalTotem)
        {
            f_Item_Totem_Normal = new Item_TotemOfRetreat(false);
            _registeritems.add(f_Item_Totem_Normal);
        }
        // Configでアイテムが有効なら登録する。
        if (RetreatConfig.c_Totem.enableHardTotem)
        {
            f_Item_Totem_Hard = new Item_TotemOfRetreat(true);
            _registeritems.add(f_Item_Totem_Hard);
        }
        // Configでアイテムが有効なら登録する。
        if (RetreatConfig.c_Totem.enableRevokeTotem)
        {
            f_Item_Totem_Revoke = new Item_TotemOfRevoke();
            _registeritems.add(f_Item_Totem_Revoke);
        }
        // Configでアイテムが有効なら登録する。
        if (RetreatConfig.c_Rod.enableCommandRod)
        {
            f_Item_CommandRod = new Item_CommandRod();
            _registeritems.add(f_Item_CommandRod);
        }
        // Configでアイテムが有効なら登録する。
        if (RetreatConfig.c_Rod.enableTransferRod)
        {
            f_Item_TransferRod = new Item_TransferRod();
            _registeritems.add(f_Item_TransferRod);
        }
        // Configでアイテムが有効なら登録する。
        if (RetreatConfig.c_Rod.enableRetreatRod)
        {
            f_Item_RetreatRod = new Item_RetreatRod();
            _registeritems.add(f_Item_RetreatRod);
        }

        for (Item __i : _registeritems)
        {
            ForgeRegistries.ITEMS.register(__i);
        }
        //---

        //アイテムのレシピを登録
        if (RetreatConfig.c_Totem.enableNormalTotem || RetreatConfig.c_Totem.enableHardTotem || RetreatConfig.c_Totem.enableRevokeTotem)
        {
            RetreatRecipes.registerRecipes_Totems();
        }
        if (RetreatConfig.c_Rod.enableCommandRod || RetreatConfig.c_Rod.enableTransferRod || RetreatConfig.c_Rod.enableRetreatRod)
        {
            RetreatRecipes.registerRecipes_Rods();
        }

        //ポーションを登録
        List<Potion> _registerpotions = new ArrayList<>();
        //LivingUpdate無効化用のポーション(Configで機能そのものが停止されている場合は登録しない。)
        if (RetreatConfig.c_System.doFreezeRetreateds)
        {
            f_Potion_CancelUpdate = new Potion_CancelUpdate(Potion_CancelUpdate.ID_POTION, false, 6580840, 0, 0);
            _registerpotions.add(f_Potion_CancelUpdate);
        }
        f_Potion_CommandRod = new Potion_CommandRod(Potion_CommandRod.ID_POTION, false, 6580840, 0, 0);
        _registerpotions.add(f_Potion_CommandRod);
        f_Potion_TransferRod = new Potion_TransferRod(Potion_TransferRod.ID_POTION, false, 6580840, 0, 0);
        _registerpotions.add(f_Potion_TransferRod);

        for (Potion __p : _registerpotions)
        {
            ForgeRegistries.POTIONS.register(__p);
        }
        //---

        //ガイドブックのインスタンスを作成。
        if (RetreatConfig.c_System.enableGuideBook)
        {
            GiveGuideBook.createGuideBookInstance();
        }
    }



    //アイテムのモデル登録用イベント。
    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void registerModels(ModelRegistryEvent event)
    {
        if (RetreatConfig.c_Totem.enableNormalTotem)
        {
            ModelLoader.setCustomModelResourceLocation(f_Item_Totem_Normal, 0, new ModelResourceLocation(new ResourceLocation(ID_MOD, f_Item_Totem_Normal.f_id_TotemOfRetreat), "inventory"));
        }
        if (RetreatConfig.c_Totem.enableHardTotem)
        {
            ModelLoader.setCustomModelResourceLocation(f_Item_Totem_Hard, 0, new ModelResourceLocation(new ResourceLocation(ID_MOD, f_Item_Totem_Hard.f_id_TotemOfRetreat), "inventory"));
        }
        if (RetreatConfig.c_Totem.enableRevokeTotem)
        {
            ModelLoader.setCustomModelResourceLocation(f_Item_Totem_Revoke, 0, new ModelResourceLocation(new ResourceLocation(ID_MOD, Item_TotemOfRevoke.f_id_TotemOfRevoke), "inventory"));
        }
        if (RetreatConfig.c_Rod.enableCommandRod)
        {
            ModelLoader.setCustomModelResourceLocation(f_Item_CommandRod, 0, new ModelResourceLocation(new ResourceLocation(ID_MOD, Item_CommandRod.ID_ROD), "inventory"));
        }
        if (RetreatConfig.c_Rod.enableTransferRod)
        {
            ModelLoader.setCustomModelResourceLocation(f_Item_TransferRod, 0, new ModelResourceLocation(new ResourceLocation(ID_MOD, Item_TransferRod.ID_ROD), "inventory"));
        }
        if (RetreatConfig.c_Rod.enableRetreatRod)
        {
            ModelLoader.setCustomModelResourceLocation(f_Item_RetreatRod, 0, new ModelResourceLocation(new ResourceLocation(ID_MOD, Item_RetreatRod.ID_ROD), "inventory"));
        }
    }
}
