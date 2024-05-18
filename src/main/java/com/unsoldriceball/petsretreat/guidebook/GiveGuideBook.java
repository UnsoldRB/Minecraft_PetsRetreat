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
    final private static String GUIDEBOOK_TAG = "@`" + ID_MOD + "_GuideBook"; //�v���C���[�����߂ă��[���h�ɎQ�������Ƃ��ɕt�^�����^�O�B���̃^�O�������Ă���ꍇ�̓K�C�h�u�b�N��z�z���Ȃ��B
    private static ItemStack f_item_guidebook;




    //mod�N������config�Ɋ�Â��ăK�C�h�u�b�N�̃C���X�^���X���쐬���Ă����֐��BRetreatMain����Ăяo�����B�B
    public static void createGuideBookInstance()
    {
        f_item_guidebook = CreateGuideBook.createGuideBook();
    }



    //���񃍃O�C�����ɃK�C�h�u�b�N��^����B
    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event)
    {
        if (!event.player.world.isRemote)
        {
            final EntityPlayer _PLAYER = event.player;
            if (!hasGBTag(_PLAYER) && RetreatConfig.c_System.enableGuideBook)
            {
                _PLAYER.inventory.addItemStackToInventory(f_item_guidebook);

                //���łɃ��V�s�u�b�N�ɃA�C�e����o�^���Ă����BRetreatRecipes.registerRecipe_RegisterToForge()�ō쐬���ꂽ�ϐ��Ƀ��V�s�������Ă���B
                if (!RetreatRecipes.registered_recipes.isEmpty())
                {
                    _PLAYER.unlockRecipes(RetreatRecipes.registered_recipes);
                }

                writeGBTag(_PLAYER);
            }
        }
    }



    //GUIDEBOOK_TAG�������Ă��邩���ʂ���֐��B
    private static boolean hasGBTag(EntityPlayer p)
    {
        return p.getTags().contains(GUIDEBOOK_TAG);
    }



    //GUIDEBOOK_TAG�������Ă��Ȃ���΁A�^�O�����ݍ��ފ֐��B�B
    private static void writeGBTag(EntityPlayer p)
    {
        p.addTag(GUIDEBOOK_TAG);
    }
}
