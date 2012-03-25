package com.mrockey28.bukkit.ItemRepair;


import java.util.ArrayList;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.logging.Logger;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;


public class Repair extends AutoRepairSupport{

	public static final Logger log = Logger.getLogger("Minecraft");

	public Repair(AutoRepairPlugin instance) {
		super(instance, getPlayer());
	}

	private float CalcPercentUsed(ItemStack tool, int durability)
	{
			float percentUsed = -1;
			percentUsed = (float)tool.getDurability() / (float)durability;
			return percentUsed;
	}
	
	public boolean manualRepair(ItemStack tool, int slot) {
		double balance;
		if (!AutoRepairPlugin.isAllowed(getPlayer(), "repair")) {
			getPlayer().sendMessage("§cYou dont have permission to do the repair command.");
			return false;
		}

		PlayerInventory inven = getPlayer().getInventory();
		HashMap<String, ArrayList<ItemStack> > recipies = AutoRepairPlugin.getRepairRecipies();
		HashMap<String, Integer> durabilities = AutoRepairPlugin.getDurabilityCosts();
		String itemName = Material.getMaterial(tool.getTypeId()).toString();
		
		ArrayList<ItemStack> req = new ArrayList<ItemStack>(recipies.get(itemName).size());	
		for (ItemStack i: recipies.get(itemName)) {
		  req.add((ItemStack)i.clone());
		}
				
		log.info("STUFF: " + recipies.get(itemName).get(0).getAmount() + recipies.get(itemName).get(1).getAmount());
		int durability = durabilities.get(itemName);
		String toolString = tool.getType().toString();
		
		if (AutoRepairPlugin.rounding != "flat")
		{
			float percentUsed = CalcPercentUsed(inven.getItem(slot), durability);
			for (int index = 0; index < req.size(); index++) {
				float amnt = req.get(index).getAmount();
				int amntInt;
				
				log.info("amnt needed:" +amnt);
				amnt = amnt * percentUsed;
				log.info("amnt before round:" + amnt);
				amnt = Math.round(amnt);
				amntInt = (int)amnt;
				log.info("amnt after round: " + amnt + " percentUsed: " + percentUsed + " amntInt: " + amntInt);
				if (AutoRepairPlugin.rounding == "min")
				{
					if (amntInt == 0)
					{
						amntInt = 1;
					}
				}
				req.get(index).setAmount(amntInt);
					
			}
		}

		if (!AutoRepairPlugin.isRepairCosts()) {
			getPlayer().sendMessage("§3Repaired " + itemName);
			inven.setItem(slot, repItem(tool));
		} else if (AutoRepairPlugin.getiSICon().compareToIgnoreCase("true") == 0){
			if (AutoRepairPlugin.getiConCosts().containsKey(toolString)) {
				balance = AutoRepairPlugin.econ.getBalance(player.getName());
				double cost = (double)AutoRepairPlugin.getiConCosts().get(itemName);
				if (cost <= balance) {
					//balance = iConomy.db.get_balance(player.getName());
					AutoRepairPlugin.econ.withdrawPlayer(player.getName(), cost);
					player.sendMessage("§3Using " + AutoRepairPlugin.econ.format((double)cost) + " to repair " + itemName);
					//inven.setItem(slot, repItem(tool));
					inven.setItem(slot, repItem(tool));
				} else {
					iConWarn(itemName, cost);
				}
			} else {
				player.sendMessage("§cThis is not a tool");
			}
			//both icon and item cost
		} else if (AutoRepairPlugin.getiSICon().compareToIgnoreCase("both") == 0) {
			if (AutoRepairPlugin.getiConCosts().containsKey(toolString)
					&& AutoRepairPlugin.getRepairRecipies().containsKey(toolString)) {
				balance = AutoRepairPlugin.econ.getBalance(player.getName());

				double cost = (double)AutoRepairPlugin.getiConCosts().get(itemName);						
				if (cost <= balance && isEnoughItems(req)) {
					//balance = iConomy.db.get_balance(player.getName());
					AutoRepairPlugin.econ.withdrawPlayer(player.getName(), cost);
					deduct(req);
					player.sendMessage("§3Using " + AutoRepairPlugin.econ.format((double)cost) + " and");
					player.sendMessage("§3" + printFormatReqs(req) + " to repair "  + itemName);
					inven.setItem(slot, repItem(tool));
				} else {
					bothWarn(itemName, cost, req);
				}
			} else {
				player.sendMessage("§cThis is not a tool");
			}
			// just item cost
		} else {
			
			if (AutoRepairPlugin.getRepairRecipies().containsKey(toolString)) {
				if (isEnoughItems(req)) {
					deduct(req);
					player.sendMessage("§3Using " + printFormatReqs(req) + " to repair " + itemName);
					inven.setItem(slot, repItem(tool));
				} else {
					justItemsWarn(itemName, req);
				}
			} else {
				player.sendMessage("§cThis is not a tool");
			}
		}

		return false;		
	}

	public boolean autoRepairTool(ItemStack tool, int slot) {
		double balance;
		if (!AutoRepairPlugin.isAllowed(player, "auto")) { 
			return false;
		}

		PlayerInventory inven = player.getInventory();
		HashMap<String, ArrayList<ItemStack> > recipies = AutoRepairPlugin.getRepairRecipies();
		String itemName = Material.getMaterial(tool.getTypeId()).toString();
		ArrayList<ItemStack> req = recipies.get(itemName);		

		if (!AutoRepairPlugin.isRepairCosts()) {
			player.sendMessage("§3Repaired " + itemName);
			inven.setItem(slot, repItem(tool));
			// just item cost
		} else if (AutoRepairPlugin.getiSICon().compareToIgnoreCase("true") == 0){
			balance = AutoRepairPlugin.econ.getBalance(player.getName());
			double cost = (double)AutoRepairPlugin.getiConCosts().get(itemName);
			if (cost <= balance) {
				AutoRepairPlugin.econ.withdrawPlayer(player.getName(), cost);
				player.sendMessage("§3Using " + AutoRepairPlugin.econ.format((double)cost) + " to repair " + itemName);
				inven.setItem(slot, repItem(tool));
			} else {
				if (!getLastWarning()) {
					if (AutoRepairPlugin.isAllowed(player, "warn")) {
						iConWarn(itemName, cost);					
					}
					setLastWarning(true);							
				}
			}
			//both icon and item cost
		} else if (AutoRepairPlugin.getiSICon().compareToIgnoreCase("both") == 0) {
			balance = AutoRepairPlugin.econ.getBalance(player.getName());
			double cost = (double)AutoRepairPlugin.getiConCosts().get(itemName);						
			if (cost <= balance && isEnoughItems(req)) {
				AutoRepairPlugin.econ.withdrawPlayer(player.getName(), cost);
				deduct(req);
				player.sendMessage("§3Using " + AutoRepairPlugin.econ.format((double)cost) + " and");
				player.sendMessage("§3" + printFormatReqs(req) + " to repair "  + itemName);
				inven.setItem(slot, repItem(tool));
			} else {
				if (!getLastWarning()) {
					if (AutoRepairPlugin.isAllowed(player, "warn")) {
						bothWarn(itemName, cost, req);					
					}
					setLastWarning(true);							
				}

			}			
			// just item cost
		} else {
			if (isEnoughItems(req)) {
				deduct(req);
				player.sendMessage("§3Using " + printFormatReqs(req) + " to repair " + itemName);
				inven.setItem(slot, repItem(tool));
			} else {
				if (!getLastWarning()) {
					if (AutoRepairPlugin.isAllowed(player, "warn")) {
						justItemsWarn(itemName, req);					
					}
					setLastWarning(true);							
				}
			}
		}
		return false;
	}

	public void repairArmour() {
		if (!AutoRepairPlugin.isAllowed(player, "repair")) {
			player.sendMessage("§cYou dont have permission to do the repair command.");
			return;
		}

		PlayerInventory inven = player.getInventory();
		ArrayList<ItemStack> req = repArmourAmount();
		double total =0;
		double balance;
		
		if (!AutoRepairPlugin.isRepairCosts()) {
			player.sendMessage("§3Repaired your armour");
			repArm();	
			// just icon cost
		} else if (AutoRepairPlugin.getiSICon().compareToIgnoreCase("true") == 0){
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
		} else if (AutoRepairPlugin.getiSICon().compareToIgnoreCase("both") == 0) {
			balance = AutoRepairPlugin.econ.getBalance(player.getName());
			for (ItemStack i : inven.getArmorContents()) {				
				if (AutoRepairPlugin.getiConCosts().containsKey(i.getType().toString())) {
					total += AutoRepairPlugin.getiConCosts().get(i.getType().toString());
				}				
			}						
			if (total <= balance && isEnoughItems(req)) {
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
			if (isEnoughItems(req)) {
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

	public void deduct(ArrayList<ItemStack> req) {
		PlayerInventory inven = player.getInventory();
		for (int i =0; i < req.size(); i++) {
			ItemStack currItem = new ItemStack(req.get(i).getTypeId(), req.get(i).getAmount());
			int neededAmount = req.get(i).getAmount();
			int smallestSlot = findSmallest(currItem);
			if (smallestSlot != -1) {
				while (neededAmount > 0) {									
					smallestSlot = findSmallest(currItem);
					ItemStack smallestItem = inven.getItem(smallestSlot);
					if (neededAmount < smallestItem.getAmount()) {
						// got enough in smallest stack deal and done
						ItemStack newSize = new ItemStack(currItem.getType(), smallestItem.getAmount() - neededAmount);
						inven.setItem(smallestSlot, newSize);
						neededAmount = 0;										
					} else {
						// need to remove from more than one stack, deal and continue
						neededAmount -= smallestItem.getAmount();
						inven.clear(smallestSlot);
					}
				}
			}
		}
	}

}

