package com.unsoldriceball.petsretreat;

import com.unsoldriceball.petsretreat.retreatsystem.Item_TotemOfRevoke;
import com.unsoldriceball.petsretreat.rods.Item_CommandRod;
import com.unsoldriceball.petsretreat.rods.Item_RetreatRod;
import com.unsoldriceball.petsretreat.rods.Item_TransferRod;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

import static com.unsoldriceball.petsretreat.RetreatMain.*;

public class RetreatRecipes
{
    //作成したレシピが格納される変数。GiveGuideBookクラスで使用される。
    public static List<IRecipe> registered_recipes = new ArrayList<>();




    //トーテム系のレシピの登録の関数。
    public static void registerRecipes_Totems()
    {
        //アイテムのレシピをconfigに基づいて登録
        final Ingredient _R_AIR = Ingredient.fromItem(Items.AIR);
        final Ingredient _R_DYE1 = Ingredient.fromStacks(new ItemStack(Items.DYE, 1, 5));
        final Ingredient _R_DYE2 = Ingredient.fromStacks(new ItemStack(Items.DYE, 1, 13));
        final Ingredient _R_GOLDBLOCK = Ingredient.fromItem(Item.getItemFromBlock(Blocks.GOLD_BLOCK));
        final Ingredient _R_TOTEM = Ingredient.fromItem(Items.TOTEM_OF_UNDYING);
        final Ingredient _R_LOG2 = Ingredient.fromItem(Item.getItemFromBlock(Blocks.LOG2));

        if (RetreatConfig.c_Totem.enableNormalTotem_Recipe && RetreatConfig.c_Totem.enableNormalTotem)
        {
            NonNullList<Ingredient> _recipe_input_normalTotem = NonNullList.create();
            _recipe_input_normalTotem.add(_R_AIR);
            _recipe_input_normalTotem.add(_R_DYE1);
            _recipe_input_normalTotem.add(_R_AIR);
            _recipe_input_normalTotem.add(_R_DYE2);
            _recipe_input_normalTotem.add(_R_GOLDBLOCK);
            _recipe_input_normalTotem.add(_R_DYE2);
            _recipe_input_normalTotem.add(_R_AIR);
            _recipe_input_normalTotem.add(_R_DYE1);
            _recipe_input_normalTotem.add(_R_AIR);
            registerRecipe_RegisterToForge(f_Item_Totem_Normal, f_Item_Totem_Normal.f_id_TotemOfRetreat, _recipe_input_normalTotem);
        }

        if (RetreatConfig.c_Totem.enableHardTotem_Recipe && RetreatConfig.c_Totem.enableHardTotem)
        {
            NonNullList<Ingredient> _recipe_input_hardTotem = NonNullList.create();
            _recipe_input_hardTotem.add(_R_AIR);
            _recipe_input_hardTotem.add(_R_DYE1);
            _recipe_input_hardTotem.add(_R_AIR);
            _recipe_input_hardTotem.add(_R_DYE2);
            _recipe_input_hardTotem.add(_R_TOTEM);
            _recipe_input_hardTotem.add(_R_DYE2);
            _recipe_input_hardTotem.add(_R_AIR);
            _recipe_input_hardTotem.add(_R_DYE1);
            _recipe_input_hardTotem.add(_R_AIR);
            registerRecipe_RegisterToForge(f_Item_Totem_Hard, f_Item_Totem_Hard.f_id_TotemOfRetreat, _recipe_input_hardTotem);
        }

        if (RetreatConfig.c_Totem.enableRevokeTotem_Recipe && RetreatConfig.c_Totem.enableRevokeTotem)
        {
            NonNullList<Ingredient> _recipe_input_revokeTotem = NonNullList.create();
            _recipe_input_revokeTotem.add(_R_AIR);
            _recipe_input_revokeTotem.add(_R_DYE1);
            _recipe_input_revokeTotem.add(_R_AIR);
            _recipe_input_revokeTotem.add(_R_DYE2);
            _recipe_input_revokeTotem.add(_R_LOG2);
            _recipe_input_revokeTotem.add(_R_DYE2);
            _recipe_input_revokeTotem.add(_R_AIR);
            _recipe_input_revokeTotem.add(_R_DYE1);
            _recipe_input_revokeTotem.add(_R_AIR);
            registerRecipe_RegisterToForge(f_Item_Totem_Revoke, Item_TotemOfRevoke.f_id_TotemOfRevoke, _recipe_input_revokeTotem);
        }

    }



    //ロッド系のレシピの登録の関数。
    public static void registerRecipes_Rods()
    {
        final Ingredient _R_AIR = Ingredient.fromItem(Items.AIR);
        final Ingredient _R_BONE = Ingredient.fromItem(Items.BONE);
        final Ingredient _R_BLAZE_ROD = Ingredient.fromItem(Items.BLAZE_ROD);
        final Ingredient _R_END_ROD = Ingredient.fromItem(Item.getItemFromBlock(Blocks.END_ROD));
        final Ingredient _R_ENDERPEARL = Ingredient.fromItem(Items.ENDER_PEARL);
        final Ingredient _R_IRONINGOT = Ingredient.fromItem(Items.IRON_INGOT);
        final Ingredient _R_GOLDINGOT = Ingredient.fromItem(Items.GOLD_INGOT);
        final Ingredient _R_ENDEREYE = Ingredient.fromItem(Items.ENDER_EYE);
        final Ingredient _R_OBSIDIAN = Ingredient.fromItem(Item.getItemFromBlock(Blocks.OBSIDIAN));


        if (RetreatConfig.c_Rod.enableCommandRod_Recipe && RetreatConfig.c_Rod.enableCommandRod)
        {
            NonNullList<Ingredient> _recipe_input_commandRod = NonNullList.create();
            _recipe_input_commandRod.add(_R_AIR);
            _recipe_input_commandRod.add(_R_IRONINGOT);
            _recipe_input_commandRod.add(_R_ENDERPEARL);
            _recipe_input_commandRod.add(_R_IRONINGOT);
            _recipe_input_commandRod.add(_R_BONE);
            _recipe_input_commandRod.add(_R_IRONINGOT);
            _recipe_input_commandRod.add(_R_ENDERPEARL);
            _recipe_input_commandRod.add(_R_IRONINGOT);
            _recipe_input_commandRod.add(_R_AIR);
            registerRecipe_RegisterToForge(f_Item_CommandRod, Item_CommandRod.ID_ROD, _recipe_input_commandRod);
        }

        if (RetreatConfig.c_Rod.enableTransferRod_Recipe && RetreatConfig.c_Rod.enableTransferRod)
        {
            NonNullList<Ingredient> _recipe_input_transferRod = NonNullList.create();
            _recipe_input_transferRod.add(_R_AIR);
            _recipe_input_transferRod.add(_R_OBSIDIAN);
            _recipe_input_transferRod.add(_R_ENDEREYE);
            _recipe_input_transferRod.add(_R_OBSIDIAN);
            _recipe_input_transferRod.add(_R_BLAZE_ROD);
            _recipe_input_transferRod.add(_R_OBSIDIAN);
            _recipe_input_transferRod.add(_R_ENDEREYE);
            _recipe_input_transferRod.add(_R_OBSIDIAN);
            _recipe_input_transferRod.add(_R_AIR);
            registerRecipe_RegisterToForge(f_Item_TransferRod, Item_TransferRod.ID_ROD, _recipe_input_transferRod);
        }

        if (RetreatConfig.c_Rod.enableRetreatRod_Recipe && RetreatConfig.c_Rod.enableRetreatRod)
        {
            NonNullList<Ingredient> _recipe_input_retreatRod = NonNullList.create();
            _recipe_input_retreatRod.add(_R_AIR);
            _recipe_input_retreatRod.add(_R_IRONINGOT);
            _recipe_input_retreatRod.add(_R_ENDEREYE);
            _recipe_input_retreatRod.add(_R_GOLDINGOT);
            if (RetreatConfig.c_Rod.enableTransferRod)
            {
                _recipe_input_retreatRod.add(Ingredient.fromItem(f_Item_TransferRod));
            }
            else
            {
                _recipe_input_retreatRod.add(_R_END_ROD);
            }
            _recipe_input_retreatRod.add(_R_GOLDINGOT);
            _recipe_input_retreatRod.add(_R_ENDEREYE);
            _recipe_input_retreatRod.add(_R_IRONINGOT);
            _recipe_input_retreatRod.add(_R_AIR);
            registerRecipe_RegisterToForge(f_Item_RetreatRod, Item_RetreatRod.ID_ROD, _recipe_input_retreatRod);
        }
    }



    //アイテムのインスタンスとアイテムIDとレシピから、レシピの登録を完了させる関数。
    //ついでにIRecipeを変数に格納しておく。(初回ログイン時にレシピを開放させるため。)
    public static void registerRecipe_RegisterToForge(Item item, String id_item, NonNullList<Ingredient> recipe)
    {
        final IRecipe _RECIPE = new ShapedRecipes(ID_MOD, 3, 3, recipe, new ItemStack(item));

        _RECIPE.setRegistryName(new ResourceLocation(ID_MOD, id_item));
        ForgeRegistries.RECIPES.register(_RECIPE);

        registered_recipes.add(_RECIPE);
    }
}
