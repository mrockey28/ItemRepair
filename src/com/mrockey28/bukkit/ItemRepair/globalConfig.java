package com.mrockey28.bukkit.ItemRepair;

import java.util.HashMap;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

/**This is a very specific class to define the particular configuration of the ItemRepair plugin. 
 * It contains only fields relevant to that plugin.
 * @author Matt Rockey
 *
 */
/**
 * @author Matt Rockey
 *
 */
public class globalConfig {

	//All these parameters should be self-explaining. If it's not clear, see the README.
	public boolean repairOfEnchantedItems_allow;
	public boolean repairOfEnchantedItems_loseEnchantment;
	public boolean usePermissions;
	public boolean automaticRepair_allow;
	public boolean automaticRepair_noWarnings;
	public boolean automaticRepair_noNotifications;
	public boolean anvilUse_allow;
	public Material anvilUse_anvilBlockType;
	public boolean econCostUse;
	public boolean econCostAdjust;
	public boolean xpCostUse;
	public boolean xpCostAdjust;
	public boolean itemCostUse;
	public boolean itemCostAdjust;

	//The configuration section of "config.yml" is under config.<whatever>
	private static String configSectionName = "config";
	
	/**Method to allow FileConfiguration for bukkit to do its thing.
	 * serialize
	 * HashMap<String,Object> globalConfig serialize
	 * @return Serialized hashmap of config parameters, ready for output to file.
	 */
	public HashMap<String, Object> serialize() {
		
		HashMap<String, Object> serialOutput = new HashMap<String, Object>();
		
		//This will all be empty if there is nothing of relevance to do
		serialOutput.put("repairOfEnchantedItems.allow", repairOfEnchantedItems_allow);
		serialOutput.put("repairOfEnchantedItems.lose-enchantment", repairOfEnchantedItems_loseEnchantment);
		serialOutput.put("usePermissions", usePermissions);
		serialOutput.put("automaticRepair.allow", automaticRepair_allow);
		serialOutput.put("automaticRepair.no-warnings", automaticRepair_noWarnings);
		serialOutput.put("automaticRepair.no-notifications", automaticRepair_noNotifications);
		serialOutput.put("anvilUse.allow", anvilUse_allow);
		serialOutput.put("anvilUse.anvilBlockType", anvilUse_anvilBlockType.toString());
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
		automaticRepair_noNotifications = false;
		anvilUse_allow = true;
		anvilUse_anvilBlockType = Material.IRON_BLOCK;
		econCostUse = true;
		econCostAdjust = false;
		xpCostUse = true;
		xpCostAdjust = false;
		itemCostUse = true;
		itemCostAdjust = false;
	}
	
	/**Initial default constructor sets all the fields to defaults.
	 * 
	 */
	public globalConfig() {
		this.setDefaults();
	}
	
	/**Sets all fields in the config to defaults.
	 * 
	 */
	public void clear() {
		this.setDefaults();
	}
		
	/**Set the configuration parameters based on the config.yml input file.
	 * @param Specific plugin configuration 
	 */
	public globalConfig(FileConfiguration config) {
		
		//Initialize variable in case of not being initialized by config
		this();

		String pathPrefix = configSectionName + ".";

		repairOfEnchantedItems_allow = config.getBoolean(pathPrefix + "repairOfEnchantedItems.allow");
		repairOfEnchantedItems_loseEnchantment = config.getBoolean(pathPrefix + "repairOfEnchantedItems.lose-enchantment");
		usePermissions = config.getBoolean(pathPrefix + "usePermissions");
		automaticRepair_allow = config.getBoolean(pathPrefix + "automaticRepair.allow");
		automaticRepair_noWarnings = config.getBoolean(pathPrefix + "automaticRepair.no-warnings");
		automaticRepair_noNotifications = config.getBoolean(pathPrefix + "automaticRepair.no-notifications");
		anvilUse_allow = config.getBoolean(pathPrefix + "anvilUse.allow");
		anvilUse_anvilBlockType = Material.getMaterial(config.getString(pathPrefix + "anvilUse.anvilBlockType"));
		econCostUse = config.getBoolean(pathPrefix + "econCost.use");
		econCostAdjust = config.getBoolean(pathPrefix + "econCost.adjust-for-damage");
		xpCostUse = config.getBoolean(pathPrefix + "xpCost.use");
		xpCostAdjust = config.getBoolean(pathPrefix + "xpCost.adjust-for-damage");
		itemCostUse = config.getBoolean(pathPrefix + "itemCost.use");
		itemCostAdjust = config.getBoolean(pathPrefix + "itemCost.adjust-for-damage");
		
		if (anvilUse_anvilBlockType == null)
		{
			anvilUse_anvilBlockType = Material.IRON_BLOCK;
		}
	}
		
	/**Returns an analysis of whether any cost type is enabled in the configuration.
	 * boolean globalConfig isAnyCost
	 * @return true if any cost type is enabled under the current configuration, else false
	 */
	public boolean isAnyCost() {
		return (econCostUse || xpCostUse || itemCostUse);
	}
	
	/**Allow external users to turn econ cost off. Basically, allow this in case an economy plugin isn't linked.
	 * setEconCostOff
	 * void globalConfig setEconCostOff
	 */
	public void setEconCostOff()
	{
		econCostUse = false;
	}
	
	
	/**If the repair recipe file failed to load, we don't want to allow autorepair, since the user won't have any prior notification that his tool is going to break before it does.
	 * If we set this to off (and he hasn't disabled warnings) it'll warn the user that auto-repair is off (if he has it on in the config, this will indicate something is wrong).
	 * setAutoRepairOff
	 * void globalConfig setAutoRepairOff
	 */
	public void setAutoRepairOff()
	{
		automaticRepair_allow = false;
	}
}
