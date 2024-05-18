package com.unsoldriceball.petsretreat.rods;

import com.unsoldriceball.petsretreat.RetreatConfig;
import com.unsoldriceball.petsretreat.RetreatUtils;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

import static com.unsoldriceball.petsretreat.RetreatMain.ID_MOD;




public class Item_CommandRod extends Item
{
    public final static String ID_ROD = "pet_command_rod";



    //コンストラクタ
    public Item_CommandRod()
    {
        super();

        this.setRegistryName(ID_MOD, ID_ROD);
        this.setUnlocalizedName(ID_ROD);
        this.setCreativeTab(CreativeTabs.TOOLS);
        this.setMaxStackSize(1);
    }



    //Tooltipを登録
    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn)
    {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        String _itemLore = RetreatUtils.encodeString(RetreatConfig.c_Rod.commandRodToolTip);
        _itemLore = _itemLore.replaceAll("%swing-radius%", String.valueOf(RetreatConfig.c_Rod.radiusSwingCommandRod));
        _itemLore = _itemLore.replaceAll("%use-radius%", String.valueOf(RetreatConfig.c_Rod.radiusUseCommandRod));
        tooltip.add(_itemLore);
    }
}
