package com.unsoldriceball.petsretreat.rods;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;

import static com.unsoldriceball.petsretreat.RetreatMain.ID_MOD;




public class Potion_CommandRod extends Potion
{
    public static final String ID_POTION = "petcommandrodpotion";
    final ResourceLocation ICON = new ResourceLocation(ID_MOD, "textures/gui/" + ID_POTION + ".png");




    public Potion_CommandRod(String name, boolean isBadEffectIn, int liquidColorIn, int iconIndexX, int iconIndexY)
    {
        super(isBadEffectIn, liquidColorIn);

        //ÇΩÇæÉ|Å[ÉVÉáÉìÇìoò^Ç∑ÇÈÇæÇØ
        setPotionName("effect." + name);
        setIconIndex(iconIndexX, iconIndexY);
        setRegistryName(new ResourceLocation(ID_MOD + ":" + name));
    }



    @Override
    public boolean hasStatusIcon()
    {
        Minecraft.getMinecraft().getTextureManager().bindTexture(ICON);
        return true;
    }



    @Override
    public void renderInventoryEffect(int x, int y, PotionEffect effect, Minecraft mc)
    {
        mc.renderEngine.bindTexture(ICON);
        Gui.drawModalRectWithCustomSizedTexture(x + 6, y + 7, 0, 0, 18, 18, 18, 18);
    }



    @Override
    public void renderHUDEffect(int x, int y, PotionEffect effect, Minecraft mc, float alpha)
    {
        mc.renderEngine.bindTexture(ICON);
        Gui.drawModalRectWithCustomSizedTexture(x + 3, y + 3, 0, 0, 18, 18, 18, 18);
    }
}
