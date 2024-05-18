package com.unsoldriceball.petsretreat.retreatsystem;

import com.unsoldriceball.petsretreat.RetreatConfig;
import com.unsoldriceball.petsretreat.RetreatUtils;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import java.util.List;

import static com.unsoldriceball.petsretreat.RetreatMain.ID_MOD;

public class Item_TotemOfRevoke  extends Item
{
    public final static String f_id_TotemOfRevoke = "totem_of_revoke";


    //コンストラクタ
    public Item_TotemOfRevoke() {
        super();

        this.setRegistryName(ID_MOD, f_id_TotemOfRevoke);
        this.setUnlocalizedName(f_id_TotemOfRevoke);
        this.setCreativeTab(CreativeTabs.TOOLS);
        this.setMaxStackSize(1);
    }


    //Tooltipを登録
    @Override
    public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn)
    {
        tooltip.add(RetreatUtils.encodeString(RetreatConfig.c_Totem.totemToolTip_Revoke));
    }
}
