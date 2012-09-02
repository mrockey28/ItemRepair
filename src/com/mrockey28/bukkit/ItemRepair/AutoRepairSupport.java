package com.mrockey28.bukkit.ItemRepair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import com.mrockey28.bukkit.ItemRepair.AutoRepairPlugin.operationType;


/**
 * 
 * @author lostaris, mrockey28
 */
public class AutoRepairSupport {
	private final AutoRepairPlugin plugin;
	protected static Player player;

	public AutoRepairSupport(AutoRepairPlugin instance, Player player) {
		plugin = instance;
		AutoRepairSupport.player = player;
	}

	private boolean warning = false;
	private boolean lastWarning = false;
	
	public void toolReq(ItemStackPlus tool) {
		
		doQueryOperation(tool);
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
	
	public void doQueryOperation(ItemStackPlus tool)
	{
		tool.setAdjustedCosts(AutoRepairPlugin.config);
		
		if (!AutoRepairPlugin.config.isAnyCost() || tool.freeRepairs()) {
			getPlayer().sendMessage("§3No materials needed to repair.");
			return;
		}
		
		String queryResponse = "§6It costs";
		if (AutoRepairPlugin.config.isEconCostOn() && tool.getRepairCosts().getEconCost() != 0)	
			queryResponse = queryResponse + " " + AutoRepairPlugin.econ.format(tool.getRepairCosts().getEconCost()) + ",";

		if (AutoRepairPlugin.config.isXpCostOn() && tool.getRepairCosts().getXpCost() != 0)
			queryResponse = queryResponse + " " +  tool.getRepairCosts().getXpCost() + " xp,";
		
		if (AutoRepairPlugin.config.isItemCostOn())
			queryResponse = queryResponse + " " +  printFormatReqs(tool.getRepairCosts().getItemCost());
				
		queryResponse = queryResponse.substring(0, queryResponse.length() -1) + " to repair " + tool.getType();
		getPlayer().sendMessage(queryResponse);
	}
	
	public void doRepairOperation(ItemStackPlus tool, AutoRepairPlugin.operationType op)
	{
		//If you don't have repair, warn, access, repair.enchanted permissions (whatever necessary to get in here)
		//this won't let you in. If autorepair is turned off, and you're trying to do an auto-repair operation,
		//this won't let you past
		if (!AutoRepairPlugin.isOpAllowed(getPlayer(), op, tool.isEnchanted())) {
			return;
		}
		
		//handle warning flag stuff, to prevent log spam
		if (op == operationType.WARN && !warning) warning = true;					
		else if (op == operationType.WARN) return;
		
		//Prevent manual repairs on fully repaired tools
		if ((op == operationType.MANUAL_REPAIR || op == operationType.SIGN_REPAIR) && tool.getDurability() == 0)
		{
			player.sendMessage("§3" + tool.getType().toString() + " is already fully repaired.");
			return;
		}
		
		//If we're using economy costs, we need to get the player's economy balance
		double balance = 0;
		if (AutoRepairPlugin.config.isEconCostOn()) {
			balance = AutoRepairPlugin.econ.getBalance(player.getName());
		}

		
		String itemName = Material.getMaterial(tool.getTypeId()).toString();
		ArrayList<ItemStack> neededItems = new ArrayList<ItemStack>(0);
		/*
		try {
			//No repair costs
			if (!AutoRepairPlugin.config.isAnyCost() && tool.freeRepairs()) {
				switch (op)
				{
					case WARN:
						if (!AutoRepairPlugin.isAutoRepair() && !AutoRepairPlugin.suppressWarningsWithNoAutoRepair()) {
							player.sendMessage("§6WARNING: " + tool.getType() + " will break soon");
						}
						break;
					case AUTO_REPAIR:
					case SIGN_REPAIR:
					case MANUAL_REPAIR:
						if (AutoRepairPlugin.issueRepairedNotificationWhenNoRepairCost == true) getPlayer().sendMessage("§3Repaired " + itemName);
					case FULL_REPAIR:
						repItem(tool);
						break;
				}	
			}
			
			//Using economy to pay only
			if (AutoRepairPlugin.config.isEconCostOn())
			{
				switch (op)
				{	
					case AUTO_REPAIR:
					case MANUAL_REPAIR:
					case SIGN_REPAIR:
					case FULL_REPAIR:
						if (cost.cost <= balance) {
							//balance = iConomy.db.get_balance(player.getName());
							AutoRepairPlugin.econ.withdrawPlayer(player.getName(), cost.cost);
							if (op != operationType.FULL_REPAIR) {
								player.sendMessage("§3Using " + AutoRepairPlugin.econ.format(cost.cost) + " to repair " + itemName);
							}
							//inven.setItem(slot, repItem(tool));
							repItem(tool);
						} else if (op != operationType.FULL_REPAIR){
							if (op == operationType.MANUAL_REPAIR || op == operationType.SIGN_REPAIR || !getLastWarning()) {
								if (AutoRepairPlugin.isAllowed(player, "warn")) {
									iConWarn(itemName, cost.cost);				
								}
								if (op == operationType.AUTO_REPAIR) setLastWarning(true);							
							}
						}
						break;
					case WARN:
						if (!AutoRepairPlugin.isAutoRepair() && !AutoRepairPlugin.suppressWarningsWithNoAutoRepair()) {
							player.sendMessage("§6WARNING: " + tool.getType() + " will break soon, no auto repairing!");
						}
						else if (cost.cost > balance) {
							player.sendMessage("§6WARNING: " + tool.getType() + " will break soon");
							iConWarn(itemName, cost.cost);
						}	
						break;
				} 
			} 
			
			//Using both economy and item costs to pay
			if (costType.toString().equals("both")) 
			{	
				switch (op)
				{
					case AUTO_REPAIR:
					case MANUAL_REPAIR:
					case SIGN_REPAIR:
					case FULL_REPAIR:
						if (cost.cost <= balance && isEnoughItems(req, neededItems)) {
							//balance = iConomy.db.get_balance(player.getName());
							AutoRepairPlugin.econ.withdrawPlayer(player.getName(), cost.cost);
							deduct(req);
							repItem(tool);
							if (op != operationType.FULL_REPAIR) {
								player.sendMessage("§3Using " + AutoRepairPlugin.econ.format(cost.cost) + " and");
								player.sendMessage("§3" + printFormatReqs(req) + " to repair "  + itemName);
							}
							
						} else if (op != operationType.FULL_REPAIR){
							if (op == operationType.MANUAL_REPAIR || op == operationType.SIGN_REPAIR || !getLastWarning()) {
								if (AutoRepairPlugin.isAllowed(player, "warn")) {
									if (cost.cost > balance && !isEnoughItems(req, neededItems)) bothWarn(itemName, cost.cost, neededItems);
									else if (cost.cost > balance) iConWarn(itemName, cost.cost);
									else justItemsWarn(itemName, neededItems);
								}
								if (op == operationType.AUTO_REPAIR) setLastWarning(true);							
							}
						}
						break;
					case WARN:
							if (!AutoRepairPlugin.isAutoRepair() && !AutoRepairPlugin.suppressWarningsWithNoAutoRepair()) {
								player.sendMessage("§6WARNING: " + tool.getType() + " will break soon, no auto repairing!");
							}
							else if (cost.cost > balance || !isEnoughItems(req, neededItems)) {
								player.sendMessage("§6WARNING: " + tool.getType() + " will break soon");
								if (cost.cost > balance && !isEnoughItems(req, neededItems)) bothWarn(itemName, cost.cost, neededItems);
								else if (cost.cost > balance) iConWarn(itemName, cost.cost);
								else justItemsWarn(itemName, neededItems);
							}
						break;
				}
			} 
			
			//Just using item costs to pay
			if (AutoRepairPlugin.config.isItemCostOn()) 
			{
				switch (op)
				{
					case AUTO_REPAIR:
					case MANUAL_REPAIR:
					case SIGN_REPAIR:
					case FULL_REPAIR:
						if (isEnoughItems(req, neededItems)) {
							deduct(req);
							repItem(tool);
							if (op != operationType.FULL_REPAIR) {
								player.sendMessage("§3Using " + printFormatReqs(req) + " to repair " + itemName);
							}
						} else if (op != operationType.FULL_REPAIR){
							if (op == operationType.MANUAL_REPAIR || op == operationType.SIGN_REPAIR || !getLastWarning()) {
								if (AutoRepairPlugin.isAllowed(player, "warn")) {
									justItemsWarn(itemName, neededItems);					
								}
								if (op == operationType.AUTO_REPAIR) setLastWarning(true);							
							}
						}
						break;
					case WARN:
						if (!AutoRepairPlugin.isAutoRepair() && !AutoRepairPlugin.suppressWarningsWithNoAutoRepair()) {
							player.sendMessage("§6WARNING: " + tool.getType() + " will break soon, no auto repairing!");
						}
						else if (!isEnoughItems(req, neededItems)) {
							player.sendMessage("§6WARNING: " + tool.getType() + " will break soon");
							justItemsWarn(itemName, neededItems);
						}
						break;
				
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		*/
	}
	
	public void repairWarn(ItemStackPlus tool) {
		doRepairOperation(tool, operationType.WARN);
	}
/*
	public boolean wrapRepeatedGetRepairCostCalls (ItemStackPlus armor, ArrayList<ItemStack> req, costClass cost)
	{
		ArrayList<ItemStack> reqTemp = new ArrayList<ItemStack> (0);
		StringBuilder costType = new StringBuilder();
		//Handle switching the cost type, since it varies for each item we have to do it here so that we don't mess up the actual config setting
		
		if (getRepairCost (armor, reqTemp, costTemp, costType, false) == false) return false;
		
		boolean replaced = false;
		for (ItemStack i : reqTemp)
		{
			for (ItemStack j : req)
			{
				if (j.getType() == i.getType())
				{
					j.setAmount(j.getAmount() + i.getAmount());
					replaced = true;
					break;
				}
			}
			if (replaced == false)
			{
				req.add(i.clone());
			}
		}
		reqTemp.clear();
		cost.cost += costTemp.cost;
		return true;
	}
	*/
	public boolean repArmourInfo(String query) {
		/*
		try {
			char getRecipe = query.charAt(0);
			if (getRecipe == '?') {
				if (AutoRepairPlugin.isRepairCosts()) {
					
					ArrayList<ItemStack> req = new ArrayList<ItemStack> (0);
					PlayerInventory inven = player.getInventory();
					costClass cost = new costClass();
					
					for (ItemStack armor : inven.getArmorContents()) {
						
						 if (wrapRepeatedGetRepairCostCalls (ItemStackPlus.convert(armor), req, cost) == false)
						 {
							 continue;
						 }
					}
					
					//There were econ and item costs
					if (!req.isEmpty() && cost.cost != 0) {					
						player.sendMessage("§6To repair all your armour you need: "
								+ AutoRepairPlugin.econ.format(cost.cost));
						player.sendMessage("§6and " + this.printFormatReqs(req));		

					} 
					//There were only econ costs
					else if (req.isEmpty()){
						player.sendMessage("§6To repair all your armour you need: "
								+ AutoRepairPlugin.econ.format(cost.cost));					

					}
					//There were only item costs
					else {
						player.sendMessage("§6To repair all your armour you need:");
						player.sendMessage("§6" + this.printFormatReqs(req));
					}
				} else {
					player.sendMessage("§3No materials needed to repair");
				}
			}
				
		} catch (Exception e) {
			return false;
		}
		return true;
		*/
		return true;
	}

	public ItemStack repItem(ItemStack item) {
		item.setDurability((short) 0);
		plugin.checkForRemoveEnchantment(item);
		return item;
	}

	//prints the durability left of the current tool to the player
	public void durabilityLeft(ItemStackPlus tool) {
		if (AutoRepairPlugin.isAllowed(player, "info")) {
			int usesLeft = tool.getUsesLeft();
			if (usesLeft != -1) {
				player.sendMessage("§3" + usesLeft + " uses left untill this tool breaks." );
			} else {
				player.sendMessage("§6Can't get uses left for this item.");
			}
		} else {
			player.sendMessage("§cYou dont have permission to do the ? or dmg commands.");
		}

	}

	@SuppressWarnings("rawtypes")
	public int findSmallest(ItemStack item) {
		PlayerInventory inven = player.getInventory();
		HashMap<Integer, ? extends ItemStack> items = inven.all(item.getTypeId());
		int slot = -1;
		int smallest = 64;
		//iterator for the hashmap
		Set<?> set = items.entrySet();
		Iterator<?> i = set.iterator();
		//ItemStack highest = new ItemStack(repairItem.getType(), 0);
		while(i.hasNext()){
			Map.Entry me = (Map.Entry)i.next();
			ItemStack item1 = (ItemStack) me.getValue();
			//if the player has doesn't not have enough of the item used to repair
			if (item1.getAmount() <= smallest) {
				smallest = item1.getAmount();
				slot = (Integer)me.getKey();
			}
		}		
		return slot;
	}

	@SuppressWarnings("rawtypes")
	public int getTotalItems(ItemStack item) {
		int total = 0;
		PlayerInventory inven = player.getInventory();
		HashMap<Integer, ? extends ItemStack> items = inven.all(item.getTypeId());
		//iterator for the hashmap
		Set<?> set = items.entrySet();
		Iterator<?> i = set.iterator();
		//ItemStack highest = new ItemStack(repairItem.getType(), 0);
		while(i.hasNext()){
			Map.Entry me = (Map.Entry)i.next();
			ItemStack item1 = (ItemStack) me.getValue();
			//if the player has doesn't not have enough of the item used to repair
			total += item1.getAmount();					
		}
		return total;
	}

	public boolean isEnoughItems (ArrayList<ItemStack> req, ArrayList<ItemStack> neededItems) {
		boolean enough = true;
		for (int i =0; i<req.size(); i++) {
			ItemStack currItem = new ItemStack(req.get(i).getTypeId(), req.get(i).getAmount());
			int neededAmount = req.get(i).getAmount();
			int currTotal = getTotalItems(currItem);
			if (neededAmount > currTotal) {
				neededItems.add(req.get(i).clone());
				enough = false;
			}
		}
		return enough;
	}

	public void iConWarn(String itemName, double total) {
		getPlayer().sendMessage("§cYou cannot afford to repair "  + itemName);
		getPlayer().sendMessage("§cYou need: " + AutoRepairPlugin.econ.format((double)total));
	}

	public void bothWarn(String itemName, double total, ArrayList<ItemStack> req) {
		getPlayer().sendMessage("§cYou still need the following to be able to repair ");
		getPlayer().sendMessage("§cyour " + itemName + ": " + printFormatReqs(req) + " and " +
				AutoRepairPlugin.econ.format((double)total));
	}
	
	public void justItemsWarn(String itemName, ArrayList<ItemStack> req) {
		player.sendMessage("§cYou still need the following to be able to repair ");
		player.sendMessage("§cyour " + itemName + ": " + printFormatReqs(req));
	}

	public String printFormatReqs(ArrayList<ItemStack> items) {
		StringBuffer string = new StringBuffer();
		for (int i = 0; i < items.size(); i++) {
			if (items.get(i).getAmount() != 0)
					string.append(items.get(i).getAmount() + " " + items.get(i).getType() + ", ");
		}
		String returnString = string.toString();
		if (returnString.length() != 0) returnString = returnString.substring(0, returnString.length() - 1);
		return returnString;
	}
	
	public void checkForAnvilRepair(PlayerInteractEvent event)
	{
		if (plugin.anvilsAllowed() == false)
		{
			return;
		}
		
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK)
		{
			Block block = event.getClickedBlock();
			if (block.getType() == Material.IRON_BLOCK)
			{
				World world = block.getWorld();
				ArrayList<Block> maybeSign = new ArrayList<Block> (0);
				maybeSign.add(world.getBlockAt(block.getX() + 1, block.getY(), block.getZ()));
				maybeSign.add(world.getBlockAt(block.getX() - 1, block.getY(), block.getZ()));
				maybeSign.add(world.getBlockAt(block.getX(), block.getY(), block.getZ() + 1));
				maybeSign.add(world.getBlockAt(block.getX(), block.getY(), block.getZ() - 1));
				maybeSign.add(world.getBlockAt(block.getX(), block.getY() + 1, block.getZ()));
				maybeSign.add(world.getBlockAt(block.getX(), block.getY() - 1, block.getZ()));
				
				for (Block mSign : maybeSign) 
				{
					if (mSign.getType() == Material.SIGN || mSign.getType() == Material.SIGN_POST)
					{
						if (((Sign)mSign.getState()).getLine(0).equalsIgnoreCase("Anvil"))
						{
							setPlayer(event.getPlayer());
							plugin.repair.anvilRepair(ItemStackPlus.convert(event.getPlayer().getItemInHand()));
							return;
						}
					}
				}
			}
		}
	}

	public boolean getWarning() {
		return warning;
	}

	public boolean getLastWarning() {
		return lastWarning;
	}

	public void setWarning(boolean newValue) {
		this.warning = newValue;
	}

	public void setLastWarning(boolean newValue) {
		this.lastWarning = newValue;
	}

	public AutoRepairPlugin getPlugin() {
		return plugin;
	}

	public static Player getPlayer() {
		return player;
	}
	public void setPlayer(Player player) {
		AutoRepairSupport.player = player;
	}
	
	
}

