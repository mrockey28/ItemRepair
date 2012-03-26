package com.mrockey28.bukkit.ItemRepair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;



import net.milkbowl.vault.Vault;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import com.mrockey28.bukkit.ItemRepair.AutoRepairPlugin.operationType;


/**
 * 
 * @author lostaris
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

	private float CalcPercentUsed(ItemStack tool, int durability)
	{
			float percentUsed = -1;
			percentUsed = (float)tool.getDurability() / (float)durability;
			return percentUsed;
	}
	
	public boolean accountForRoundingType (int slot, ArrayList<ItemStack> req, String itemName)
	{

		return true;
	}
	
	public void toolReq(ItemStack tool, int slot) {
		
		doRepairOperation(tool, slot, operationType.QUERY);
		/*
		
		//if (!AutoRepairPlugin.isPermissions || AutoRepairPlugin.Permissions.has(player, "AutoRepair.info")) {
		if (AutoRepairPlugin.isAllowed(player, "info")) {
			String toolString = tool.getType().toString();
			// just icon cost
			if (AutoRepairPlugin.getiSICon().compareToIgnoreCase("true") == 0) {
				if (AutoRepairPlugin.getiConCosts().containsKey(toolString)) {
					player.sendMessage("§6It costs " +  AutoRepairPlugin.econ.format((double)AutoRepairPlugin.getiConCosts().get(toolString))
							+ " to repair " + tool.getType());
				}
				
				// icon cost and item cost
			} else if (AutoRepairPlugin.getiSICon().compareToIgnoreCase("both") == 0) {
				if (AutoRepairPlugin.getRepairRecipies().containsKey(toolString) &&
						AutoRepairPlugin.getiConCosts().containsKey(toolString)) {
					player.sendMessage("§6To repair " + tool.getType() + " you need: " 
							+ AutoRepairPlugin.econ.format((double)AutoRepairPlugin.getiConCosts().get(toolString)) + " and");
					player.sendMessage("§6" + printFormatReqs(AutoRepairPlugin.getRepairRecipies().get(toolString)));
				}
				// just item cost
			} else if (AutoRepairPlugin.isRepairCosts()) {
				//tests to see if the config file has a repair reference to the item they wish to repair
				if (AutoRepairPlugin.getRepairRecipies().containsKey(toolString)) {
					player.sendMessage("§6To repair " + tool.getType() + " you need:");
					player.sendMessage("§6" + printFormatReqs(AutoRepairPlugin.getRepairRecipies().get(toolString)));
				}
			} else {
				player.sendMessage("§3No materials needed to repair");
				//return true;
			} 
			if (!AutoRepairPlugin.getRepairRecipies().containsKey(toolString)) {
				player.sendMessage("§6This is not a tool.");
			}

		} else {
			player.sendMessage("§cYou dont have permission to do the ? or dmg commands.");
		}
		*/
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
	
	public void doRepairOperation(ItemStack tool, int slot, AutoRepairPlugin.operationType op)
	{
		double balance = AutoRepairPlugin.econ.getBalance(player.getName());
		if (!AutoRepairPlugin.isOpAllowed(getPlayer(), op)) {
			return;
		}
		if (op == operationType.WARN && !AutoRepairPlugin.isRepairCosts() && !AutoRepairPlugin.isAutoRepair()) {
			player.sendMessage("§6WARNING: " + tool.getType() + " will break soon");
		}
		if (op == operationType.WARN && !warning) warning = true;					
		else if (op == operationType.WARN) return;


		PlayerInventory inven = getPlayer().getInventory();
		HashMap<String, ArrayList<ItemStack> > recipies = AutoRepairPlugin.getRepairRecipies();
		String itemName = Material.getMaterial(tool.getTypeId()).toString();
		HashMap<String, Integer> durabilities = AutoRepairPlugin.getDurabilityCosts();
		ArrayList<ItemStack> req = new ArrayList<ItemStack>(recipies.get(itemName).size());	
		ArrayList<ItemStack> neededItems = new ArrayList<ItemStack>(0);
		
		//do a deep copy of the required items list so we can modify it temporarily for rounding purposes
		for (ItemStack i: recipies.get(itemName)) {
		  req.add((ItemStack)i.clone());
		}
			
		String toolString = tool.getType().toString();
		int durability = durabilities.get(itemName);
		double cost = 0;
		if (AutoRepairPlugin.getiConCosts().containsKey(toolString)) {
			cost = (double)AutoRepairPlugin.getiConCosts().get(itemName);
		}
		else
		{
			player.sendMessage("§cThis item is not in the AutoRepair database.");
			return;
		}

		//do rounding based on dmg already done to item, if called for by config
		if (op != operationType.AUTO_REPAIR && AutoRepairPlugin.rounding != "flat")
		{
			
			float percentUsed = CalcPercentUsed(inven.getItem(slot), durability);
			for (int index = 0; index < req.size(); index++) {
				float amnt = req.get(index).getAmount();
				int amntInt;
				
				amnt = amnt * percentUsed;
				cost = cost * percentUsed;
				amnt = Math.round(amnt);
				amntInt = (int)amnt;
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
		try {
			//No repair costs
			if (!AutoRepairPlugin.isRepairCosts()) {
				switch (op)
				{
					case WARN:
						break;
					case AUTO_REPAIR:
					case MANUAL_REPAIR:
						getPlayer().sendMessage("§3Repaired " + itemName);
						inven.setItem(slot, repItem(tool));
						break;
					case QUERY:
						getPlayer().sendMessage("§3No materials needed to repair.");
						break;
						
				}
				
			}
			
			//Using economy to pay only
			else if (AutoRepairPlugin.getiSICon().compareToIgnoreCase("true") == 0)
			{
				switch (op)
				{	case QUERY:
						player.sendMessage("§6It costs " +  AutoRepairPlugin.econ.format((double)cost)
							+ " to repair " + tool.getType());
						break;
					case AUTO_REPAIR:
					case MANUAL_REPAIR:
						if (cost <= balance) {
							//balance = iConomy.db.get_balance(player.getName());
							AutoRepairPlugin.econ.withdrawPlayer(player.getName(), cost);
							player.sendMessage("§3Using " + AutoRepairPlugin.econ.format((double)cost) + " to repair " + itemName);
							//inven.setItem(slot, repItem(tool));
							inven.setItem(slot, repItem(tool));
						} else {
							iConWarn(itemName, cost);
						}
						break;
					case WARN:
						if (cost > balance) {
							if (!AutoRepairPlugin.isAutoRepair()) player.sendMessage("§6WARNING: " + tool.getType() + " will break soon, no auto repairing!");
							else player.sendMessage("§6WARNING: " + tool.getType() + " will break soon");
							iConWarn(toolString, cost);
						}	
						break;
				} 
			} 
			
			//Using both economy and item costs to pay
			else if (AutoRepairPlugin.getiSICon().compareToIgnoreCase("both") == 0) 
			{	
				switch (op)
				{
					case QUERY:
						player.sendMessage("§6To repair " + tool.getType() + " you need: " 
								+ AutoRepairPlugin.econ.format((double)AutoRepairPlugin.getiConCosts().get(toolString)) + " and");
						player.sendMessage("§6" + printFormatReqs(AutoRepairPlugin.getRepairRecipies().get(toolString)));
						break;
					case AUTO_REPAIR:
					case MANUAL_REPAIR:
						if (cost <= balance && isEnoughItems(req, neededItems)) {
							//balance = iConomy.db.get_balance(player.getName());
							AutoRepairPlugin.econ.withdrawPlayer(player.getName(), cost);
							deduct(req);
							player.sendMessage("§3Using " + AutoRepairPlugin.econ.format((double)cost) + " and");
							player.sendMessage("§3" + printFormatReqs(req) + " to repair "  + itemName);
							inven.setItem(slot, repItem(tool));
						} else {
							if (op == operationType.MANUAL_REPAIR || !getLastWarning()) {
								if (AutoRepairPlugin.isAllowed(player, "warn")) {
									bothWarn(itemName, cost, req);					
								}
								if (op == operationType.AUTO_REPAIR) setLastWarning(true);							
							}
						}
						break;
					case WARN:
							if (cost > balance || !isEnoughItems(req, neededItems)) {
								if (!AutoRepairPlugin.isAutoRepair()) player.sendMessage("§6WARNING: " + tool.getType() + " will break soon, no auto repairing!");
								else player.sendMessage("§6WARNING: " + tool.getType() + " will break soon");
								bothWarn(toolString, cost, req);
							}
						break;
				}
			} 
			
			//Just using item costs to pay
			else 
			{
				switch (op)
				{
					case QUERY:
						player.sendMessage("§6To repair " + tool.getType() + " you need:");
						player.sendMessage("§6" + printFormatReqs(AutoRepairPlugin.getRepairRecipies().get(toolString)));
						break;
					case AUTO_REPAIR:
					case MANUAL_REPAIR:
						if (isEnoughItems(req, neededItems)) {
							deduct(req);
							player.sendMessage("§3Using " + printFormatReqs(req) + " to repair " + itemName);
							inven.setItem(slot, repItem(tool));
						} else {
							if (op == operationType.MANUAL_REPAIR || !getLastWarning()) {
								if (AutoRepairPlugin.isAllowed(player, "warn")) {
									justItemsWarn(itemName, req);					
								}
								if (op == operationType.AUTO_REPAIR) setLastWarning(true);							
							}
						}
						break;
					case WARN:
						if (!AutoRepairPlugin.isAutoRepair()) player.sendMessage("§6WARNING: " + tool.getType() + " will break soon, no auto repairing!");
						else player.sendMessage("§6WARNING: " + tool.getType() + " will break soon");
						System.out.println(toolString + " " + AutoRepairPlugin.getRepairRecipies().get(toolString));
						justItemsWarn(toolString, req);
						break;
				
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void repairWarn(ItemStack tool, int slot) {
		
		doRepairOperation(tool, slot, operationType.WARN);
		/*
		if (!AutoRepairPlugin.isAllowed(player, "warn")) { 
			return;
		}

		HashMap<String, ArrayList<ItemStack>> repairRecipies;
		if (!warning) {					
			warning = true;		
			try {				
				repairRecipies = AutoRepairPlugin.getRepairRecipies();
				String toolString = tool.getType().toString();
				//tests to see if the config file has a repair reference to the item they wish to repair
				if (repairRecipies.containsKey(toolString)) {
					// there is no repair costs and no auto repair
					if (!AutoRepairPlugin.isRepairCosts() && !AutoRepairPlugin.isAutoRepair()) {
						player.sendMessage("§6WARNING: " + tool.getType() + " will break soon");
						// if there is repair costs  and no auto repair
					} else if (AutoRepairPlugin.isRepairCosts() && !AutoRepairPlugin.isAutoRepair()) {	
						double balance;
						
						//just economy
						if (AutoRepairPlugin.getiSICon().compareToIgnoreCase("true") == 0)
						{
							int cost = AutoRepairPlugin.getiConCosts().get(toolString);
							balance = AutoRepairPlugin.econ.getBalance(player.getName());
							if (cost > balance) {
								player.sendMessage("§6WARNING: " + tool.getType() + " will break soon, no auto repairing");
								iConWarn(toolString, cost);
							}							
						} else  {
							int cost = AutoRepairPlugin.getiConCosts().get(toolString);
							ArrayList<ItemStack> reqItems = new ArrayList<ItemStack>(AutoRepairPlugin.getRepairRecipies().get(toolString).size());	
							for (ItemStack i: AutoRepairPlugin.getRepairRecipies().get(toolString)) {
							  reqItems.add((ItemStack)i.clone());
							}
							ArrayList<ItemStack> neededItems = new ArrayList<ItemStack>(0);
						
							accountForRoundingType (slot, reqItems, toolString);
							
							balance = AutoRepairPlugin.econ.getBalance(player.getName());
							
							//both economy and items
							if (AutoRepairPlugin.getiSICon().compareToIgnoreCase("both") == 0)
							{
								if (cost > balance || !isEnoughItems(reqItems, neededItems)) {
									player.sendMessage("§6WARNING: " + tool.getType() + " will break soon, no auto repairing");
									bothWarn(toolString, cost, reqItems);
								}
							} 
							//just items
							else 
							{
								if (!isEnoughItems(reqItems, neededItems)) {								
									player.sendMessage("§6WARNING: " + tool.getType() + " will break soon, no auto repairing");
									justItemsWarn(toolString, reqItems);
								}
							}
						}
						
					} else {
						
						double balance;
						if (AutoRepairPlugin.getiSICon().compareToIgnoreCase("true") == 0){
							int cost = AutoRepairPlugin.getiConCosts().get(toolString);
							balance = AutoRepairPlugin.econ.getBalance(player.getName());
							if (cost > balance) {
								player.sendMessage("§6WARNING: " + tool.getType() + " will break soon");
								iConWarn(toolString, cost);
							}							
						} else if (AutoRepairPlugin.getiSICon().compareToIgnoreCase("both") == 0) {
							int cost = AutoRepairPlugin.getiConCosts().get(toolString);
							ArrayList<ItemStack> reqItems = AutoRepairPlugin.getRepairRecipies().get(toolString);
							balance = AutoRepairPlugin.econ.getBalance(player.getName());
							if (cost > balance || !isEnoughItems(reqItems)) {
								player.sendMessage("§6WARNING: " + tool.getType() + " will break soon");
								bothWarn(toolString, cost, reqItems);
							} 
						} else{
							ArrayList<ItemStack> reqItems = AutoRepairPlugin.getRepairRecipies().get(toolString);
							if (!isEnoughItems(reqItems)) {								
								player.sendMessage("§6WARNING: " + tool.getType() + " will break soon");
								System.out.println(toolString + " " + AutoRepairPlugin.getRepairRecipies().get(toolString));
								justItemsWarn(toolString, reqItems);
							}
						}
					}
				} else {
					// item does not have a repair reference in config
					player.sendMessage("§6" +toolString + " not found in config file.");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		*/
	}

	public boolean repArmourInfo(String query) {
		if (AutoRepairPlugin.isRepairCosts()) {
			try {
				char getRecipe = query.charAt(0);
				if (getRecipe == '?') {
					//ArrayList<ItemStack> req = this.repArmourAmount(player);
					//player.sendMessage("§6To repair all your armour you need:");
					//player.sendMessage("§6" + this.printFormatReqs(req));
					int total =0;
					ArrayList<ItemStack> req = repArmourAmount();
					PlayerInventory inven = player.getInventory();
					
					if (AutoRepairPlugin.getiSICon().compareToIgnoreCase("true") == 0){

						for (ItemStack i : inven.getArmorContents()) {				
							if (AutoRepairPlugin.getiConCosts().containsKey(i.getType().toString())) {
								total += AutoRepairPlugin.getiConCosts().get(i.getType().toString());
							}				
						}
						player.sendMessage("§6To repair all your armour you need: "
								+ AutoRepairPlugin.econ.format((double)total));					
						// icon and item cost
					} else if (AutoRepairPlugin.getiSICon().compareToIgnoreCase("both") == 0) {
						for (ItemStack i : inven.getArmorContents()) {				
							if (AutoRepairPlugin.getiConCosts().containsKey(i.getType().toString())) {
								total += AutoRepairPlugin.getiConCosts().get(i.getType().toString());
							}				
						}						
						player.sendMessage("§6To repair all your armour you need: "
								+ AutoRepairPlugin.econ.format((double)total));
						player.sendMessage("§6" + this.printFormatReqs(req));		
						// just item cost
					} else {
						player.sendMessage("§6To repair all your armour you need:");
						player.sendMessage("§6" + this.printFormatReqs(req));
					}
				}
			} catch (Exception e) {
				return false;
			}
		} else {
			player.sendMessage("§3No materials needed to repair");
		}
		return true;
	}

	public ArrayList<ItemStack> repArmourAmount() {
		HashMap<String, ArrayList<ItemStack> > recipies = AutoRepairPlugin.getRepairRecipies();
		PlayerInventory inven = player.getInventory();
		ItemStack[] armour = inven.getArmorContents();
		HashMap<String, Integer> totalCost = new HashMap<String, Integer>();
		for (int i=0; i<armour.length; i++) {
			String item = armour[i].getType().toString();
			if (recipies.containsKey(item)) {
				ArrayList<ItemStack> reqItems = recipies.get(item);
				for (int j =0; j<reqItems.size(); j++) {
					if(totalCost.containsKey(reqItems.get(j).getType().toString())) {
						int amount = totalCost.get(reqItems.get(j).getType().toString());
						totalCost.remove(reqItems.get(j).getType().toString());
						int newAmount = amount + reqItems.get(j).getAmount();
						totalCost.put(reqItems.get(j).getType().toString(), newAmount);
					} else {
						totalCost.put(reqItems.get(j).getType().toString(), reqItems.get(j).getAmount());
					}
				}
			}
		}
		ArrayList<ItemStack> req = new ArrayList<ItemStack>();
		for (Object key: totalCost.keySet()) {
			req.add(new ItemStack(Material.getMaterial(key.toString()), totalCost.get(key)));
		}
		return req;
	}

	public ItemStack repItem(ItemStack item) {
		item.setDurability((short) 0);
		return item;
	}

	//prints the durability left of the current tool to the player
	public void durabilityLeft(ItemStack tool) {
		if (AutoRepairPlugin.isAllowed(player, "info")) { //!AutoRepairPlugin.isPermissions || AutoRepairPlugin.Permissions.has(player, "AutoRepair.info")) {
			int usesLeft = this.returnUsesLeft(tool);
			if (usesLeft != -1) {
				player.sendMessage("§3" + usesLeft + " blocks left untill this tool breaks." );
			} else {
				player.sendMessage("§6This is not a tool.");
			}
		} else {
			player.sendMessage("§cYou dont have permission to do the ? or dmg commands.");
		}

	}

	public int returnUsesLeft(ItemStack tool) {
		int usesLeft = -1;
		HashMap<String, Integer> durabilities = AutoRepairPlugin.getDurabilityCosts();
		String itemName = Material.getMaterial(tool.getTypeId()).toString();
		int durability = durabilities.get(itemName);
		usesLeft = durability - tool.getDurability();
		return usesLeft;
	}

	@SuppressWarnings("unchecked")
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

	@SuppressWarnings("unchecked")
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

	// checks to see if the player has enough of a list of items
	public boolean isEnough(String itemName) {
		ArrayList<ItemStack> reqItems = AutoRepairPlugin.getRepairRecipies().get(itemName);
		boolean enoughItemFlag = true;
		for (int i =0; i < reqItems.size(); i++) {
			ItemStack currItem = new ItemStack(reqItems.get(i).getTypeId(), reqItems.get(i).getAmount());

			int neededAmount = reqItems.get(i).getAmount();
			int currTotal = getTotalItems(currItem);
			if (neededAmount > currTotal) {
				enoughItemFlag = false;
			}
		}
		return enoughItemFlag;
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
		getPlayer().sendMessage("§cYou are cannot afford to repair "  + itemName);
		getPlayer().sendMessage("§cNeed: " + AutoRepairPlugin.econ.format((double)total));
	}

	public void bothWarn(String itemName, double total, ArrayList<ItemStack> req) {
		getPlayer().sendMessage("§cYou are missing one or more items to repair " + itemName);
		getPlayer().sendMessage("§cNeed: " + printFormatReqs(req) + " and " +
				AutoRepairPlugin.econ.format((double)total));
	}
	
	public void justItemsWarn(String itemName, ArrayList<ItemStack> req) {
		player.sendMessage("§cYou are missing one or more items to repair " + itemName);
		player.sendMessage("§cNeed: " + printFormatReqs(req));
	}

	public String printFormatReqs(ArrayList<ItemStack> items) {
		StringBuffer string = new StringBuffer();
		string.append(" ");
		for (int i = 0; i < items.size(); i++) {
			string.append(items.get(i).getAmount() + " " + items.get(i).getType() + " ");
		}
		return string.toString();
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

