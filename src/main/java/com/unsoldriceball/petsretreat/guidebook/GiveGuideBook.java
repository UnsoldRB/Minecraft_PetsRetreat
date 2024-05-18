package com.unsoldriceball.petsretreat.guidebook;

import com.unsoldriceball.petsretreat.RetreatConfig;
import com.unsoldriceball.petsretreat.RetreatRecipes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

import static com.unsoldriceball.petsretreat.RetreatMain.ID_MOD;




public class GiveGuideBook
{
    final private static String GUIDEBOOK_TAG = "@`" + ID_MOD + "_GuideBook"; //プレイヤーが初めてワールドに参加したときに付与されるタグ。このタグを持っている場合はガイドブックを配布しない。
    private static ItemStack f_item_guidebook;




    //mod起動時にconfigに基づいてガイドブックのインスタンスを作成しておく関数。RetreatMainから呼び出される。。
    public static void createGuideBookInstance()
    {
        f_item_guidebook = CreateGuideBook.createGuideBook();
    }



    //初回ログイン時にガイドブックを与える。
    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event)
    {
        if (!event.player.world.isRemote)
        {
            final EntityPlayer _PLAYER = event.player;
            if (!hasGBTag(_PLAYER) && RetreatConfig.c_System.enableGuideBook)
            {
                _PLAYER.inventory.addItemStackToInventory(f_item_guidebook);

                //ついでにレシピブックにアイテムを登録しておく。RetreatRecipes.registerRecipe_RegisterToForge()で作成された変数にレシピが入っている。
                if (!RetreatRecipes.registered_recipes.isEmpty())
                {
                    _PLAYER.unlockRecipes(RetreatRecipes.registered_recipes);
                }

                writeGBTag(_PLAYER);
            }
        }
    }



    //GUIDEBOOK_TAGを持っているか判別する関数。
    private static boolean hasGBTag(EntityPlayer p)
    {
        return p.getTags().contains(GUIDEBOOK_TAG);
    }



    //GUIDEBOOK_TAGを持っていなければ、タグを刻み込む関数。。
    private static void writeGBTag(EntityPlayer p)
    {
        p.addTag(GUIDEBOOK_TAG);
    }
}
