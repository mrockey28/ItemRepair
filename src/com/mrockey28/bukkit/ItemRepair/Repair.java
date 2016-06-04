package com.mrockey28.bukkit.ItemRepair;


import java.util.ArrayList;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.ChatColor;

import com.mrockey28.bukkit.ItemRepair.AutoRepairPlugin.operationType;


public class Repair extends AutoRepairSupport{

	public Repair(AutoRepairPlugin instance) {
		super(instance, getPlayer());
	}
	
	public boolean manualRepair(ItemStackPlus tool) {
		doRepairOperation(tool, operationType.MANUAL_REPAIR);
		return false;		
	}
	
	public boolean anvilRepair(ItemStackPlus tool) {
		doRepairOperation(tool, operationType.SIGN_REPAIR);
		return false;		
	}

	public boolean autoRepairTool(ItemStackPlus tool) {
		
		doRepairOperation(tool, operationType.AUTO_REPAIR);
		return false;
	}
	
	public void repairAll(Player player) {		
		
		ArrayList<ItemStackPlus> couldNotRepair = new ArrayList<ItemStackPlus> (0);

		for (ItemStack itemIndex : player.getInventory().getContents())
		{	
			if (itemIndex == null)
				continue;
			
			ItemStackPlus item = new ItemStackPlus(itemIndex);
			doRepairOperation(item, operationType.FULL_REPAIR);
			if (item.getDurability() != 0 && item.isRepairable)
			{
				couldNotRepair.add(item);
			}
		}
		for (ItemStack itemIndex : player.getInventory().getArmorContents())
		{
			if (itemIndex == null)
				continue;
			
			ItemStackPlus item = new ItemStackPlus(itemIndex);	
			doRepairOperation(item, operationType.FULL_REPAIR);
			if (item.getDurability() != 0 && item.isRepairable)
			{
				couldNotRepair.add(item);
			}
		}
		
		if (!couldNotRepair.isEmpty())
		{
			String itemsNotRepaired = "";
			
			for (ItemStackPlus item : couldNotRepair)
			{
				itemsNotRepaired += (item.getType().toString() + ", ");
			}
			itemsNotRepaired = itemsNotRepaired.substring(0, itemsNotRepaired.length() - 2);
			player.sendMessage(ChatColor.RED + "Did not repair the following items: ");
			player.sendMessage(ChatColor.RED + itemsNotRepaired);
		}
		
	}
	
	public void repairArmor(Player player) {		
		
		ArrayList<ItemStackPlus> couldNotRepair = new ArrayList<ItemStackPlus> (0);

		for (ItemStack itemIndex : player.getInventory().getArmorContents())
		{
			ItemStackPlus item = new ItemStackPlus(itemIndex);
			if (item == null || item.getType() == Material.AIR)
			{
				continue;
			}
			
			if (item.getDurability() != 0)
			{
				doRepairOperation(item, operationType.FULL_REPAIR);
				if (item.getDurability() != 0)
				{
					couldNotRepair.add(item);
				}
			}
		}
		
		if (!couldNotRepair.isEmpty())
		{
			String itemsNotRepaired = "";
			
			for (ItemStackPlus item : couldNotRepair)
			{
				itemsNotRepaired += (item.getType().toString() + ", ");
			}
			itemsNotRepaired = itemsNotRepaired.substring(0, itemsNotRepaired.length() - 2);
			player.sendMessage(ChatColor.RED + "Did not repair the following items: ");
			player.sendMessage(ChatColor.RED + itemsNotRepaired);
		}	
	}
}

