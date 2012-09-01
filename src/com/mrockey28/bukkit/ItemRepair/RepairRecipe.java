package com.mrockey28.bukkit.ItemRepair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class RepairRecipe implements ConfigurationSerializable{

	public recipe normal;
	public recipe enchanted;
	
	private static String configSectionName = "recipes";
	public static final Logger log = Logger.getLogger("Minecraft");
	
	public HashMap<String, Object> serialize() {
		
		HashMap<String, Object> serialOutput = new HashMap<String, Object>();
		
		if (!normal.serialize().isEmpty()) {
			serialOutput.put("normal", normal.serialize());
		}
		
		if (!enchanted.serialize().isEmpty()) {
			serialOutput.put("enchanted", enchanted.serialize());
		}
		
		return serialOutput;
	}
	
	public RepairRecipe () {
		normal = new recipe();
		enchanted = new recipe();
	}
	
	public RepairRecipe(FileConfiguration config, String itemName) {
		
		this();
		
		normal = new recipe(config, itemName, "normal");
		enchanted = new recipe(config, itemName, "enchanted");
	}
	
	public recipe getNormalCost() {
		return normal;
	}
	
	public recipe getEnchantedCost() {
		return enchanted;
	}	
	
	
	public class recipe implements Cloneable{
		private String costType;
		private Material material;
		private ArrayList<ItemStack> repairItems;
		private double econCost;
		private double econCostMin;
		private int xpCost;
		private int xpCostMin;
		public boolean valid;
		
		
		
		
		private recipe() {
			costType = "";
			material = Material.AIR;
			repairItems = new ArrayList<ItemStack>(0);
			econCost = 0;
			econCostMin = 0;
			xpCost = 0;
			xpCostMin = 0;
			valid = false;
		}
		
		@SuppressWarnings("unchecked")
		public recipe clone()
		{
			recipe result = new recipe();
			result.costType = costType;
			result.material = material;
			result.repairItems = (ArrayList<ItemStack>) repairItems.clone(); 
			result.econCost = econCost;
			result.econCostMin = econCostMin;
			result.xpCost = xpCost;
			result.xpCostMin = xpCostMin; 
			return result;
		}
		private HashMap<String, Object> serialize() {
			
			HashMap<String, Object> serialOutput = new HashMap<String, Object>();
			
			//This will all be empty if there is nothing of relevance to do
			if (costType != "")
			{
				serialOutput.put("cost-type", costType);
			}
			for (ItemStack i : repairItems)
			{
				serialOutput.put(i.getType().toString(), i.getAmount());
			}
			if (econCost > 0) {
				serialOutput.put("econ-cost", econCost);
			}
			
			return serialOutput;
		}
		
		private recipe(FileConfiguration config, String itemName, String enchantStatus) {
			
			//Initialize, in case of empty field
			this();
			
			String pathPrefix = configSectionName + "." + itemName + "." + enchantStatus + ".";
			
			//If we don't check to make sure this configuration section is here,
			//it'll cause a Null pointer error when we come across items that don't have "enchanted" specifications
			if (!config.isConfigurationSection(pathPrefix))
			{
				return;
			}
			
			material = Material.getMaterial(itemName);
			//If we got past the check for the actual configuration section, we can confirm that there will be a cost
			valid = true;
			
			ArrayList <String> keys = new ArrayList <String> (config.getConfigurationSection(pathPrefix).getKeys(false));
			
			//We step through, find the individual unique fields, and then remove them as we assign them,
			//because at the end we need to be able to step through the list of items that make up the rest of
			//the repair costs
			if (keys.contains("econ-cost")) {
				econCost = config.getDouble(pathPrefix + "econ-cost");
				keys.remove("econ-cost");
			}
			if (keys.contains("econ-cost-min")) {
				econCostMin = config.getDouble(pathPrefix + "econ-cost-min");
				keys.remove("econ-cost-min");
			}
			if (keys.contains("cost-type")) {
				costType = config.getString(pathPrefix + "cost-type");
				keys.remove("cost-type");
			}
			if (keys.contains("xp-cost")) {
				xpCost = config.getInt(pathPrefix + "xp-cost");
				keys.remove("xp-cost");
			}
			if (keys.contains("xp-cost-min")) {
				xpCostMin = config.getInt(pathPrefix + "xp-cost-min");
				keys.remove("xp-cost-min");
			}
			for (String i : keys) {
				ItemStack item = new ItemStack(0);
				item.setType(Material.getMaterial(i));
				item.setAmount(config.getInt(pathPrefix + "." + i));
				repairItems.add(item.clone());
			}
		}
		
		
		public void adjustRepairCost(ItemStackPlus item)
		{
			if (AutoRepairPlugin.config.isXpCostOff())
			{
				xpCost = 0;
			}
			else if (AutoRepairPlugin.config.isXpCostAdjusted())
			{
		    	xpCost = (int)adjustCost ((double)xpCost, item);
	    		if (xpCost < xpCostMin)
	    		{
	    			xpCost = xpCostMin;
	    		}
		    }
			
			if (AutoRepairPlugin.config.isEconCostOff())
			{
				econCost = 0;
			}
			else if (AutoRepairPlugin.config.isEconCostAdjusted())
			{
		    	econCost = adjustCost (econCost, item);
	    		if (econCost < econCostMin)
	    		{
	    			econCost = econCostMin;
	    		}
		    }
		    //repeat this pattern for item cost and econ cost.
		}

		private double adjustCost (double cost, ItemStackPlus item)
		{
		    float fraction = item.getDurability() / item.getMaxDurability();
		    return cost * fraction;
		}
		
		//This function HAS to assume that there will be no overflow conditions;
		//that there has already been a check done to make sure the cost CAN be deducted. We just need to do it now.
		public void ApplyCost (ItemStackPlus item, Player player)
		{
			//Deduct XP cost
			player.setExp(player.getExp() - xpCost);
			
			//Deduct Econ cost
			AutoRepairPlugin.econ.withdrawPlayer(player.getName(), econCost);
		    
		    //since we can't do player.item -= itemCost,
		    //the loop below will have to suffice.
		    for (ItemStack i : repairItems)
		    {
		    	
		        int invenIndex = player.getInventory().first(i.getType());
		        while (player.getInventory().getItem(invenIndex).getAmount() < i.getAmount())
		        {
		        	i.setAmount(i.getAmount() - player.getInventory().getItem(invenIndex).getAmount());
		            player.getInventory().clear(invenIndex);
		            invenIndex = player.getInventory().first(i.getType());
		        }    
		        player.getInventory().getItem(invenIndex).setAmount(player.getInventory().getItem(invenIndex).getAmount() - i.getAmount());
		    }

		    if (AutoRepairPlugin.config.removeEnchantmentsOnRepair())
		    {
		        item.deleteAllEnchantments();
		    }

		}
		
		
		void setCostType (String newCostType) {
			if (newCostType == "item" || newCostType == "econ" || newCostType == "both")
			{
				costType = newCostType;
			}
		}
		
		public String getCostType () {
			return costType;
		}
		
		
		void addItemCost (ItemStack newItem) {
			repairItems.add(newItem);
		}
		
		public ArrayList<ItemStack> getItemCost () {
			return repairItems;
		}
		
		void setEconCost(double newCost) {
			econCost = newCost;
		}
		
		public double getEconCost()
		{
			return econCost;
		}
		
		public boolean isXpCostMin()
		{
			if (xpCostMin > 0)
			{
				return true;
			}
			return false;
		}
		
		public int getXpCostMin()
		{
			return xpCostMin;
		}
		
		public boolean isXpCost()
		{
			if (xpCost > 0)
			{
				return true;
			}
			return false;
		}
		
		public int getXpCost()
		{
			return xpCost;
		}
	}
	
}
