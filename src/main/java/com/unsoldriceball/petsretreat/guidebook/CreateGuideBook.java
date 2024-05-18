package com.unsoldriceball.petsretreat.guidebook;

import com.unsoldriceball.petsretreat.RetreatConfig;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;

import java.util.ArrayList;
import java.util.List;


public class CreateGuideBook
{
    public static ItemStack createGuideBook()
    {
        final ItemStack _guidebook_temp = new ItemStack(Items.WRITTEN_BOOK);
        final NBTTagCompound _tag = new NBTTagCompound();

        _tag.setString("title", "PetsRetreat mod");
        _tag.setString("author", "RiceBallSouls");

        NBTTagList pages = new NBTTagList();
        List<String> _contents_guidebook = createPages();
        for (String s : _contents_guidebook)
        {
            pages.appendTag(new NBTTagString(s));   
        }

        _tag.setTag("pages", pages);

        _guidebook_temp.setTagCompound(_tag);
        return _guidebook_temp;
    }



    private static List<String> createPages()
    {
        final String _TEXT_NOTE = "§oNote\n\nThis guidebook is wrote about the mod which is the default config.\n\nIf you change the config, this guidebook may include the wrong information.";
        final String _TEXT_TOTEM_NORMAL_1 = "§n§l§oTotem of Retreat§r\n\n\nThis is the main item in this mod.\nCan crafted with a gold block, and 2 types of dyes.\n\nIf you has any pets, friends, or a mob who is important to you, You should attack it with this.";
        final String _TEXT_TOTEM_NORMAL_2 = "Thereby, the mob that is attacked by this will be kept safe.\n\nWhat it means is when the mob's hp goes to 0, they will automatically return to your spawn point.\nNot only that, they will be able to regenerate their hp themselves,\nand can control with some rods by you.";
        final String _TEXT_TOTEM_NORMAL_3 = "\n\n\n§oNow you can finally go adventure with your friends!";
        final String _TEXT_TOTEM_HARD_A_1 = "§n§l§oElaborate Totem of Retreat§r\n\n\nCan crafted with a totem of undyne, and 2 types of dyes.\n\nHave you crafted the totem of retreat?\nThis totem keeps your pet's life more powerfully.";
        final String _TEXT_TOTEM_HARD_A_2 = "Have been enhanced in the following...\n\n+More regeneration.\n+Apply a regeneration lv2 potion effect after retreat for 1 minute.\n+More heal when retreat.";
        final String _TEXT_TOTEM_HARD_B_1 = "§n§l§oElaborate Totem of Retreat§r\n\n\nThis is the main item in this mod.\nCan crafted with a totem of undyne, and 2 types of dyes.\n\nIf you has any pets, friends, or a mob who is important to you, You should attack it with this.";
        final String _TEXT_TOTEM_HARD_B_2 = "Thereby, the mob that is attacked by this will be kept safe.\n\nWhat it means is when the mob's hp goes to 0, they will automatically return to your spawn point.\nNot only that, they will be able to regenerate their hp themselves,\nand can control with some rods by you.";
        final String _TEXT_TOTEM_HARD_B_3 = "\n\n\n§oNow you can finally go adventure with your friends!";
        final String _TEXT_TOTEM_REVOKE = "§n§l§oTotem of Revoke§r\n\n\nCan crafted with a log, and 2 types of dyes.\n\nIf you need to revoke the totem power from your pets, you have to attack it with this totem.\nThat's very sad. But, sometimes requires saying goodbye.";
        final String _TEXT_COMMANDROD = "§n§l§oPet Command Rod§r\n\n\nCan crafted with a bone, iron ingots, and ender pearls.\n\nWe don't know why they want to the lava and cliff.\nBut, that's why you need to command your pets.\nThis rod allows you to restrict your pet's movements.";
        final String _TEXT_TRANSFERROD = "§n§l§oPet Transfer Rod§r\n\n\nCan crafted with a blaze rod, obsidian, and ender eyes.\n\nThis world's teleporter is almost only for humans. Isn't it?\nSo, I created this rod.\n If you swing this, your pets around you will teleport. It's very simple.";
        final String _TEXT_RETREATROD = "§n§l§oPet Retreat Rod§r\n\n\nCan crafted with a pet transfer rod, iron ingots, gold ingots, and ender eyes.\n\nWhen filled your inventory with a lot of treasures, you will think Could you please take this and go home first? to your pets.\nThis rod created to that thinking.";


        ArrayList<String> _contents_guidebook_temp = new ArrayList<>();

        _contents_guidebook_temp.add(toJson_BookText(_TEXT_NOTE));
        if (RetreatConfig.c_Totem.enableNormalTotem)
        {
            _contents_guidebook_temp.add(toJson_BookText(_TEXT_TOTEM_NORMAL_1));
            _contents_guidebook_temp.add(toJson_BookText(_TEXT_TOTEM_NORMAL_2));
            _contents_guidebook_temp.add(toJson_BookText(_TEXT_TOTEM_NORMAL_3));
        }
        if (RetreatConfig.c_Totem.enableHardTotem)
        {
            //通常版トーテムの有無で内容を変える。
            if (RetreatConfig.c_Totem.enableNormalTotem)
            {
                _contents_guidebook_temp.add(toJson_BookText(_TEXT_TOTEM_HARD_A_1));
                _contents_guidebook_temp.add(toJson_BookText(_TEXT_TOTEM_HARD_A_2));
            }
            else
            {
                _contents_guidebook_temp.add(toJson_BookText(_TEXT_TOTEM_HARD_B_1));
                _contents_guidebook_temp.add(toJson_BookText(_TEXT_TOTEM_HARD_B_2));
                _contents_guidebook_temp.add(toJson_BookText(_TEXT_TOTEM_HARD_B_3));
            }
        }
        if (RetreatConfig.c_Totem.enableRevokeTotem)
        {
            _contents_guidebook_temp.add(toJson_BookText(_TEXT_TOTEM_REVOKE));
        }
        if (RetreatConfig.c_Rod.enableCommandRod)
        {
            _contents_guidebook_temp.add(toJson_BookText(_TEXT_COMMANDROD));
        }
        if (RetreatConfig.c_Rod.enableTransferRod)
        {
            _contents_guidebook_temp.add(toJson_BookText(_TEXT_TRANSFERROD));
        }
        if (RetreatConfig.c_Rod.enableRetreatRod)
        {
            _contents_guidebook_temp.add(toJson_BookText(_TEXT_RETREATROD));
        }

        return _contents_guidebook_temp;
    }



    private static String toJson_BookText(String s)
    {
        return "{\"text\":\"" + s + "\"}";
    }
}
