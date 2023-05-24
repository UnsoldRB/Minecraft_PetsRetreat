package com.unsoldriceball.petsretreat;

import net.minecraft.potion.Potion;
import net.minecraft.util.ResourceLocation;

import static com.unsoldriceball.petsretreat.RetreatMain.MOD_ID;


public class RetreatPotionEffect extends Potion{
    protected RetreatPotionEffect(String name, boolean isBadEffectIn, int liquidColorIn, int iconIndexX, int iconIndexY) {
        //ただポーションを登録するだけ

        super(isBadEffectIn, liquidColorIn);
        setPotionName("effect." + name);
        setIconIndex(iconIndexX, iconIndexY);
        setRegistryName(new ResourceLocation(MOD_ID + ":" + name));
    }

    @Override
    public boolean hasStatusIcon() {
        //Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation(PetsRetreat.MOD_ID + "textures/gui/potion_effects.png"));
        return false;
    }
}