package com.unsoldriceball.petsretreat.retreatsystem;

import net.minecraft.potion.Potion;
import net.minecraft.util.ResourceLocation;

import static com.unsoldriceball.petsretreat.RetreatMain.ID_MOD;




public class Potion_CancelUpdate extends Potion
{
    public final static String ID_POTION = "cancelupdatepotion";




    public Potion_CancelUpdate(String name, boolean isBadEffectIn, int liquidColorIn, int iconIndexX, int iconIndexY)
    {
        //ただポーションを登録するだけ
        super(isBadEffectIn, liquidColorIn);
        setPotionName("effect." + name);
        setIconIndex(iconIndexX, iconIndexY);
        setRegistryName(new ResourceLocation(ID_MOD + ":" + name));
    }



    //ポーション効果のアイコンを登録する関数。アイコンを無効化するために使う。
    @Override
    public boolean hasStatusIcon()
    {
        return false;
    }
}