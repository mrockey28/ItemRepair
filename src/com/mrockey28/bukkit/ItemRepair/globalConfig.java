package com.mrockey28.bukkit.ItemRepair;

import java.util.HashMap;
import java.util.logging.Logger;

import org.bukkit.configuration.file.FileConfiguration;

public class globalConfig {

	public boolean repairOfEnchantedItems_allow;
	public boolean repairOfEnchantedItems_loseEnchantment;
	public boolean usePermissions;
	public boolean automaticRepair_allow;
	public boolean automaticRepair_noWarnings;
	public boolean allowAnvilUse;
	public boolean econCostUse;
	public boolean econCostAdjust;
	public boolean xpCostUse;
	public boolean xpCostAdjust;
	public boolean itemCostUse;
	public boolean itemCostAdjust;
	public static final Logger log = Logger.getLogger("Minecraft");
	private static String configSectionName = "config";
	
	public HashMap<String, Object> serialize() {
		
		HashMap<String, Object> serialOutput = new HashMap<String, Object>();
		
		//This will all be empty if there is nothing of relevance to do
		serialOutput.put("repairOfEnchantedItems.allow", repairOfEnchantedItems_allow);
		serialOutput.put("repairOfEnchantedItems.lose-enchantment", repairOfEnchantedItems_loseEnchantment);
		serialOutput.put("usePermissions", usePermissions);
		serialOutput.put("automaticRepair.allow", automaticRepair_allow);
		serialOutput.put("automaticRepair.no-warnings", automaticRepair_noWarnings);
		serialOutput.put("allowAnvilUse", allowAnvilUse);
		serialOutput.put("econCost.use", econCostUse);
		serialOutput.put("econCost.adjust-for-damage", econCostAdjust);
		serialOutput.put("xpCost.use", xpCostUse);
		serialOutput.put("xpCost.adjust-for-damage", xpCostAdjust);
		serialOutput.put("itemCost.use", itemCostUse);
		serialOutput.put("itemCost.adjust-for-damage", itemCostAdjust);
		
		return serialOutput;
	}
		
	private void setDefaults()
	{
		repairOfEnchantedItems_allow = true;
		repairOfEnchantedItems_loseEnchantment = false;
		usePermissions = false;
		automaticRepair_allow = true;
		automaticRepair_noWarnings = false;
		allowAnvilUse = true;
		econCostUse = true;
		econCostAdjust = false;
		xpCostUse = true;
		xpCostAdjust = false;
		itemCostUse = true;
		itemCostAdjust = false;
	}
	
	public globalConfig() {
		this.setDefaults();
	}
	
	/**Sets all fields in the config to defaults.
	 * @return void
	 * 
	 */
	public void clear() {
		this.setDefaults();
	}
		
	public globalConfig(FileConfiguration config) {
		
		//Initialize variable in case of not being initialized by config
		this();

		String pathPrefix = configSectionName + ".";

		repairOfEnchantedItems_allow = config.getBoolean(pathPrefix + "repairOfEnchantedItems.allow");
		repairOfEnchantedItems_loseEnchantment = config.getBoolean(pathPrefix + "repairOfEnchantedItems.lose-enchantment");
		usePermissions = config.getBoolean(pathPrefix + "usePermissions");
		automaticRepair_allow = config.getBoolean(pathPrefix + "automaticRepair.allow");
		automaticRepair_noWarnings = config.getBoolean(pathPrefix + "automaticRepair.no-warnings");
		allowAnvilUse = config.getBoolean(pathPrefix + "allowAnvilUse");
		econCostUse = config.getBoolean(pathPrefix + "econCost.use");
		econCostAdjust = config.getBoolean(pathPrefix + "econCost.adjust-for-damage");
		xpCostUse = config.getBoolean(pathPrefix + "xpCost.use");
		xpCostAdjust = config.getBoolean(pathPrefix + "xpCost.adjust-for-damage");
		itemCostUse = config.getBoolean(pathPrefix + "itemCost.use");
		itemCostAdjust = config.getBoolean(pathPrefix + "itemCost.adjust-for-damage");
	}
		
	public boolean isAnyCost() {
		return (econCostUse || xpCostUse || itemCostUse);
	}
	
	public void setEconCostOff()
	{
		econCostUse = false;
	}
	
	public void setAutoRepairOff()
	{
		automaticRepair_allow = false;
	}
}
