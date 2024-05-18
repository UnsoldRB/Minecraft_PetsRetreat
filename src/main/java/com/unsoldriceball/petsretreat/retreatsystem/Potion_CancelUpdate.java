package com.unsoldriceball.petsretreat.retreatsystem;

import net.minecraft.potion.Potion;
import net.minecraft.util.ResourceLocation;

import static com.unsoldriceball.petsretreat.RetreatMain.ID_MOD;




public class Potion_CancelUpdate extends Potion
{
    public final static String ID_POTION = "cancelupdatepotion";




    public Potion_CancelUpdate(String name, boolean isBadEffectIn, int liquidColorIn, int iconIndexX, int iconIndexY)
    {
        //�����|�[�V������o�^���邾��
        super(isBadEffectIn, liquidColorIn);
        setPotionName("effect." + name);
        setIconIndex(iconIndexX, iconIndexY);
        setRegistryName(new ResourceLocation(ID_MOD + ":" + name));
    }



    //�|�[�V�������ʂ̃A�C�R����o�^����֐��B�A�C�R���𖳌������邽�߂Ɏg���B
    @Override
    public boolean hasStatusIcon()
    {
        return false;
    }
}