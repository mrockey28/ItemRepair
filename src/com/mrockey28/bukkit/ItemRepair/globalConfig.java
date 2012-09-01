package com.mrockey28.bukkit.ItemRepair;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.configuration.file.FileConfiguration;

public class globalConfig {

	boolean usePermissions;
	boolean automaticRepair;
	String repairOfEnchantedItems;
	String econCostType;
	String itemCostType;
	String xpCostType;
	
	private static String configSectionName = "config";
	
	public HashMap<String, Object> serialize() {
		
		HashMap<String, Object> serialOutput = new HashMap<String, Object>();
		
		//This will all be empty if there is nothing of relevance to do
		
		serialOutput.put("usePermissions", usePermissions);
		serialOutput.put("automaticRepair", automaticRepair);
		if (repairOfEnchantedItems != "")
		{
			serialOutput.put("repairOfEnchantedItems", repairOfEnchantedItems);
		}
		if (econCostType != "")
		{
			serialOutput.put("econCostType", econCostType);
		}
		if (itemCostType != "")
		{
			serialOutput.put("itemCostType", itemCostType);
		}
		if (xpCostType != "")
		{
			serialOutput.put("xpCostType", xpCostType);
		}
		
		return serialOutput;
	}
		
		
	public globalConfig() {
		
		
		usePermissions = false;
		automaticRepair = true;
		repairOfEnchantedItems = "on";
		econCostType = "on";
		itemCostType = "on";
		xpCostType = "on";
	}
		
	public globalConfig(FileConfiguration config) {
		
		//Initialize variable in case of not being initialized by config
		this();

		String pathPrefix = configSectionName + ".";
		
		ArrayList <String> keys = new ArrayList <String> (config.getConfigurationSection(pathPrefix).getKeys(false));
		
		if (keys.contains("repairOfEnchantedItems")) {
			repairOfEnchantedItems = config.getString(pathPrefix + "repairOfEnchantedItems");
		}
		if (keys.contains("usePermissions")) {
			usePermissions = config.getBoolean(pathPrefix + "usePermissions");
		}
		if (keys.contains("automaticRepair")) {
			automaticRepair = config.getBoolean(pathPrefix + "automaticRepair");
		}
		if (keys.contains("econCostType")) {
			econCostType = config.getString(pathPrefix + "econCostType");
		}
		if (keys.contains("itemCostType")) {
			itemCostType = config.getString(pathPrefix + "itemCostType");
		}
		if (keys.contains("xpCostType")) {
			xpCostType = config.getString(pathPrefix + "xpCostType");
		}
	}
		
	public boolean isXpCostOff () {
		return (xpCostType.equalsIgnoreCase("off"));
	}
	
	public boolean isXpCostAdjusted () {
		return (xpCostType.equalsIgnoreCase("adjusted"));
	}
	
	public boolean isXpCostOn () {
		return (xpCostType.equalsIgnoreCase("on"));
	}
	
	public void turnEconCostOff()
	{
		econCostType = "off";
	}
	
	public boolean isEconCostOff () {
		return (econCostType.equalsIgnoreCase("off"));
	}
	
	public boolean isEconCostAdjusted () {
		return (econCostType.equalsIgnoreCase("adjusted"));
	}
	
	public boolean isEconCostMin () {
		return (econCostType.equalsIgnoreCase("minimum"));
	}
	
	public boolean isEconCostFull () {
		return (econCostType.equalsIgnoreCase("full"));
	}
	
	public boolean isEconCostOn () {
		return (!this.isEconCostOff());
	}
	
	public boolean isItemCostOff () {
		return (itemCostType.equalsIgnoreCase("off"));
	}
	
	public boolean isItemCostAdjusted () {
		return (itemCostType.equalsIgnoreCase("adjusted"));
	}
	
	public boolean isItemCostMin () {
		return (itemCostType.equalsIgnoreCase("minimum"));
	}
	
	public boolean isItemCostFull () {
		return (itemCostType.equalsIgnoreCase("full"));
	}
	
	public boolean isItemCostOn () {
		return (!this.isItemCostOff());
	}
	
	public boolean removeEnchantmentsOnRepair()
	{
		return (repairOfEnchantedItems.equalsIgnoreCase("loseEnchantment"));
	}
	
	public boolean allowRepairOfEnchantedItems() 
	{
		return (repairOfEnchantedItems.equalsIgnoreCase("on"));
	}
	
	public boolean disallowRepairOfEnchantedItems() 
	{
		return (repairOfEnchantedItems.equalsIgnoreCase("off"));
	}
	
	public boolean usePermissions() 
	{
		return usePermissions;
	}
	
	public boolean automaticRepair()
	{
		return automaticRepair;
	}

}
