package com.unsoldriceball.petsretreat.retreatsystem;

import com.unsoldriceball.petsretreat.RetreatConfig;
import com.unsoldriceball.petsretreat.RetreatUtils;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;


import java.util.List;

import static com.unsoldriceball.petsretreat.RetreatMain.*;




public class Item_TotemOfRetreat extends Item
{
    public String f_id_TotemOfRetreat = "totem_of_retreat";




    //コンストラクタ
    public Item_TotemOfRetreat(boolean isHardRecipe)
    {
        super();

        //Hard用とNormal用でアイテムのIDを変える。
        if (isHardRecipe)
        {
            f_id_TotemOfRetreat += "_hard";
        }
        else
        {
            f_id_TotemOfRetreat += "_normal";
        }
        this.setRegistryName(ID_MOD, f_id_TotemOfRetreat);
        this.setUnlocalizedName(f_id_TotemOfRetreat);
        this.setCreativeTab(CreativeTabs.TOOLS);
        this.setMaxStackSize(1);
    }


    //Tooltipを登録
    @Override
    public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn)
    {
        if (f_id_TotemOfRetreat.contains("hard"))
        {
            tooltip.add(RetreatUtils.encodeString(RetreatConfig.c_Totem.totemToolTip_Hard));
        }
        else
        {
            tooltip.add(RetreatUtils.encodeString(RetreatConfig.c_Totem.totemToolTip_Normal));
        }
    }
}
