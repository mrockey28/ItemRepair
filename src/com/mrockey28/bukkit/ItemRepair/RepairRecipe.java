package com.mrockey28.bukkit.ItemRepair;

import java.util.ArrayList;
import java.util.HashMap;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;

/**This is a container class for containing the (multiple) recipes that may be possible in this specific plugin.
 * In this case, I've only implemented two recipe types: enchanted, and normal (unenchanted). In addition, 
 * permission-group is specified at the item level, not recipe level, so this seemed the natural place to put it.
 * @author Matt Rockey
 *
 */
public class RepairRecipe implements ConfigurationSerializable, Cloneable{

	public recipe normal; //Non-enchanted items
	public recipe enchanted; //Enchanted items (if not specified for item, defaults to normal)
	private int permGroup; //Permission group is set at item level
	
	//The recipe section of "config.yml" is under recipes.<whatever>
	private static String configSectionName = "recipes";
	
	@Override
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
	
	/**initializes everything to defaults (mostly 0's).
	 * 
	 */
	public RepairRecipe () {
		permGroup = 0;
		normal = new recipe();
		enchanted = new recipe();
	}
	
	@Override
	public RepairRecipe clone()
	{
		RepairRecipe result = new RepairRecipe();
		result.enchanted = enchanted.clone();
		result.normal = normal.clone(); 
		result.permGroup = permGroup;
		
		return result;	
	}
	
	public RepairRecipe(FileConfiguration config, String itemName) {
		
		this();
		
		//If a permission group is set for this item, then set it appropriately within the recipe.
		if (config.isInt(configSectionName + "." + itemName + ".permission-group"))
			permGroup = config.getInt(configSectionName + "." + itemName + ".permission-group");
		
		normal = new recipe(config, itemName, "normal");
		enchanted = new recipe(config, itemName, "enchanted");
	}
	
	public recipe getNormalCost() {
		return normal;
	}
	
	public recipe getEnchantedCost() {
		return enchanted;
	}	
	
	public int getPermGroup()
	{
		return permGroup;
	}
	
	public void setEconAdjustedCosts(float percentUsed)
	{
		normal.setEconAdjustedCosts(percentUsed);
		enchanted.setEconAdjustedCosts(percentUsed);
	}
	
	public void setItemAdjustedCosts(float percentUsed)
	{
		normal.setItemAdjustedCosts(percentUsed);
		enchanted.setItemAdjustedCosts(percentUsed);
	}
	
	public void setXpAdjustedCosts(float percentUsed)
	{
		normal.setXpAdjustedCosts(percentUsed);
		enchanted.setXpAdjustedCosts(percentUsed);
	}
	
	public class recipe implements Cloneable{

		private ArrayList<ItemStack> repairItems;
		private double econCost;
		private double econCostMin;
		private int xpCost;
		private int xpCostMin;
		private int itemCostMin;
		public boolean valid;
		
		
		
		
		private recipe() {
			repairItems = new ArrayList<ItemStack>(0);
			econCost = 0;
			econCostMin = 0;
			xpCost = 0;
			xpCostMin = 0;
			valid = false;
			itemCostMin = 0;
		}
		
		@Override
		public recipe clone()
		{
			recipe result = new recipe();
			for (ItemStack i : repairItems)
			{
				result.repairItems.add(i.clone());
			}
			result.econCost = econCost;
			result.econCostMin = econCostMin;
			result.xpCost = xpCost;
			result.xpCostMin = xpCostMin; 
			result.itemCostMin = itemCostMin;
			result.valid = valid;
			return result;
		}
		private HashMap<String, Object> serialize() {
			
			HashMap<String, Object> serialOutput = new HashMap<String, Object>();
			
			//This will all be empty if there is nothing of relevance to do
			for (ItemStack i : repairItems)
			{
				serialOutput.put(i.getType().toString(), i.getAmount());
			}
			if (econCost > 0) {
				serialOutput.put("econ-cost", econCost);
			}
			if (itemCostMin > 0) {
				serialOutput.put("item-cost-min", itemCostMin);
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
			if (keys.contains("xp-cost")) {
				xpCost = config.getInt(pathPrefix + "xp-cost");
				keys.remove("xp-cost");
			}
			if (keys.contains("xp-cost-min")) {
				xpCostMin = config.getInt(pathPrefix + "xp-cost-min");
				keys.remove("xp-cost-min");
			}
			if (keys.contains("item-cost-min")) {
				itemCostMin = config.getInt(pathPrefix + "item-cost-min");
				keys.remove("item-cost-min");
			}
			if (keys.contains("permission-group")) {
				permGroup = config.getInt(pathPrefix + "permission-group");
				keys.remove("permission-group");
			}
			for (String i : keys) {
			    Material material = Material.getMaterial(i);
			    if (material == null)
			    {
			    	AutoRepairPlugin.log.severe(String.format("Material \"%s\" is not a valid material for use in repairs.", i));
			    }
			    else
			    {
			    	ItemStack item = new ItemStack(Material.getMaterial(i));
			    	item.setAmount(config.getInt(pathPrefix + "." + i));
			    	repairItems.add(item.clone());
			    }
			}

			checkForValidState();
		}
		
		public void checkForValidState()
		{
			if (econCost == 0 && xpCost == 0 && repairItems.isEmpty())
			{
				valid = false;
			}
		}
		
		public void setEconAdjustedCosts(float percentUsed)
		{
			econCost= econCost * percentUsed;
			if (econCost < econCostMin) econCost = econCostMin;
			
			checkForValidState();
		}
		
		public void setItemAdjustedCosts(float percentUsed)
		{
			int i = 0;
			
			while (i < repairItems.size() && repairItems.size() != 0)
			{
				ItemStack item = repairItems.get(i);
				item.setAmount(Math.round(item.getAmount() * percentUsed));
				if (item.getAmount() < itemCostMin) item.setAmount(itemCostMin);
				if (item.getAmount() == 0) repairItems.remove(item);
				else i++;
			}
			
			checkForValidState();
		}
		
		public void setXpAdjustedCosts(float percentUsed)
		{
			xpCost = Math.round(xpCost * percentUsed);
			if (xpCost < xpCostMin) xpCost = xpCostMin;
			
			checkForValidState();
		}
		
		public void setItemMinCost(int cost) {
			itemCostMin = cost;
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
