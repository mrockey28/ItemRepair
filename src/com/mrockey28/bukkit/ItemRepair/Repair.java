package com.mrockey28.bukkit.ItemRepair;


import java.util.ArrayList;
import java.util.logging.Logger;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import com.mrockey28.bukkit.ItemRepair.AutoRepairPlugin.operationType;


public class Repair extends AutoRepairSupport{

	public static final Logger log = Logger.getLogger("Minecraft");

	public Repair(AutoRepairPlugin instance) {
		super(instance, getPlayer());
	}
	
	public boolean manualRepair(ItemStack tool) {
		doRepairOperation(tool, operationType.MANUAL_REPAIR);
		return false;		
	}

	public boolean autoRepairTool(ItemStack tool) {
		
		doRepairOperation(tool, operationType.AUTO_REPAIR);
		return false;
	}
	public void repairAll(Player player) {		
		
		ArrayList<ItemStack> couldNotRepair = new ArrayList<ItemStack> (0);
		for (ItemStack item : player.getInventory().getContents())
		{
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
			
			for (ItemStack item : couldNotRepair)
			{
				itemsNotRepaired += (item.getType().toString() + ", ");
			}
			
			player.sendMessage("§cDid not repair the following items: ");
			player.sendMessage("§c" + itemsNotRepaired);
		}
		
	}
	
	public void repairArmour() {
		if (!AutoRepairPlugin.isAllowed(player, "repair")) {
			player.sendMessage("§cYou dont have permission to do the repair command.");
			return;
		}

		PlayerInventory inven = player.getInventory();
		ArrayList<ItemStack> req = repArmourAmount();
		ArrayList<ItemStack> neededItems = new ArrayList<ItemStack>(0);
		double total =0;
		double balance;
		
		if (!AutoRepairPlugin.isRepairCosts()) {
			player.sendMessage("§3Repaired your armour");
			repArm();	
			// just icon cost
		} else if (AutoRepairPlugin.getUseEcon().compareToIgnoreCase("true") == 0){
			balance = AutoRepairPlugin.econ.getBalance(player.getName());

			for (ItemStack i : inven.getArmorContents()) {				
				if (AutoRepairPlugin.getiConCosts().containsKey(i.getType().toString())) {
					total += AutoRepairPlugin.getiConCosts().get(i.getType().toString());
				}				
			}
			if (total <= balance) {
				AutoRepairPlugin.econ.withdrawPlayer(player.getName(), total);
				player.sendMessage("§3Using " + AutoRepairPlugin.econ.format((double)total) + " to repair your armour");
				repArm();
			} else {
				player.sendMessage("§cYou are cannot afford to repair your armour");
				player.sendMessage("§cNeed: " + AutoRepairPlugin.econ.format((double)total));
			}
			//both icon and item cost
		} else if (AutoRepairPlugin.getUseEcon().compareToIgnoreCase("both") == 0) {
			balance = AutoRepairPlugin.econ.getBalance(player.getName());
			for (ItemStack i : inven.getArmorContents()) {				
				if (AutoRepairPlugin.getiConCosts().containsKey(i.getType().toString())) {
					total += AutoRepairPlugin.getiConCosts().get(i.getType().toString());
				}				
			}						
			if (total <= balance && isEnoughItems(req, neededItems)) {
				AutoRepairPlugin.econ.withdrawPlayer(player.getName(), total);
				deduct(req);
				player.sendMessage("§3Using " + AutoRepairPlugin.econ.format((double)total) + " and");
				player.sendMessage("§3" + printFormatReqs(req) + " to repair your armour");
				repArm();
			} else {
				player.sendMessage("§cYou are missing one or more items to repair your armour");
				player.sendMessage("§cNeed: " + printFormatReqs(req) + " and " +
						AutoRepairPlugin.econ.format((double)total));
			}			
			// just item cost
		} else {
			if (isEnoughItems(req, neededItems)) {
				deduct(req);
				player.sendMessage("§3Using " + printFormatReqs(req) + " to repair your armour");
				repArm();
			} else {
				player.sendMessage("§cYou are missing one or more items to repair your armour");
				player.sendMessage("§cNeed: " + printFormatReqs(req));
			}
		}
	}

	public void repArm () {
		PlayerInventory inven = player.getInventory();
		if(inven.getBoots().getTypeId() != 0 ) {inven.setBoots(repItem(inven.getBoots()));}
		if(inven.getChestplate().getTypeId() != 0 ) {inven.setChestplate(repItem(inven.getChestplate()));}
		if(inven.getHelmet().getTypeId() != 0 ) {inven.setHelmet(repItem(inven.getHelmet()));}
		if(inven.getLeggings().getTypeId() != 0 ) {inven.setLeggings(repItem(inven.getLeggings()));}
	}


}

