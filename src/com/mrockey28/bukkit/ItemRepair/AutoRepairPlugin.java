package com.mrockey28.bukkit.ItemRepair;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;

import com.mrockey28.bukkit.ItemRepair.RepairRecipe;

/**
 * testing for Bukkit
 *
 * @author lostaris, mrockey28
 */

public class AutoRepairPlugin extends JavaPlugin {
	private final AutoRepairBlockListener blockListener = new AutoRepairBlockListener(this);
	private HashMap<String, Object> settings;
	private static boolean economyFound = false;
	
	public static HashMap<Integer, RepairRecipe> recipes = new HashMap<Integer, RepairRecipe>(0);
	public static globalConfig config = new globalConfig();
	public static Economy econ = null;
	public static final Logger log = Logger.getLogger("Minecraft");
	//public AutoRepairSupport support = new AutoRepairSupport(this);
	public Repair repair = new Repair(this);
	
	public enum operationType {
		QUERY,
		WARN,
		MANUAL_REPAIR,
		AUTO_REPAIR,
		FULL_REPAIR,
		SIGN_REPAIR,
	}
	
	static {
		ConfigurationSerialization.registerClass(RepairRecipe.class);
	}

	@Override
	public void onEnable() {
		//  Place any custom enable code here including the registration of any events
		// Register our events
		getServer().getPluginManager().registerEvents(blockListener, this);
		
		// EXAMPLE: Custom code, here we just output some info so we can check all is well
		PluginDescriptionFile pdfFile = this.getDescription();
		log.info( pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled" );
		
		if (!setupEconomy() ) {
	         log.info(String.format("[%s] Economy not linked, Vault or Economy plugin not found", getDescription().getName()));
		 }
		else
		{
			economyFound = true;
		}
		File f = new File("plugins/AutoRepair/config.yml");
		if (!f.exists())
		{
			getConfig().options().copyDefaults(true);
			String fileName = "plugins/AutoRepair/Config.properties";
			f = new File(fileName);
			if (f.exists()) {
				convertOldConfig();	
			}
			fileName = "plugins/AutoRepair/RepairCosts.properties";
			f = new File(fileName);
			if (f.exists()) {
				convertOldRepairCosts();	
			}
		}
		saveConfig();
		refreshConfig();
	}
	 


	private boolean setupEconomy() {
	     if (getServer().getPluginManager().getPlugin("Vault") == null) {
	         return false;
	     }
	     RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
	     if (rsp == null) {
	         return false;
	     }
	     
	     
	     econ = rsp.getProvider();
	     return econ != null;
	}	
	
	@Override
	public void onDisable() {
		//  Place any custom disable code here

		// NOTE: All registered events are automatically unregistered when a plugin is disabled

		// EXAMPLE: Custom code, here we just output some info so we can check all is well
		PluginDescriptionFile pdfFile = this.getDescription();
		log.info( pdfFile.getName() + " version " + pdfFile.getVersion() + " is disabled" );
	}

	@Override
	public boolean onCommand(org.bukkit.command.CommandSender sender,
			org.bukkit.command.Command command, String commandLabel, String[] args) {
		Player player = null;

		String[] split = args;
		
		if(sender instanceof Player) {
			player = (Player) sender;
		}
		else {
			if (split.length == 1 && split[0].equalsIgnoreCase("reload"))
			{
				refreshConfig();
				log.info(String.format("[%s] Re-loaded AutoRepair config files.", getDescription().getName()));
			}
			else
			{
				log.info(String.format("[%s] This command is not supported from the console.", getDescription().getName()));
			}
			return true;
		}
		PlayerInventory inven = player.getInventory();
		
		String commandName = command.getName().toLowerCase();
		AutoRepairSupport support = new AutoRepairSupport(this, player);

		if (commandName.equals("repair")) {
			if (!isAllowed(player, "access")) {
				return true;
			}
			String argsOneString = "";
			for (String i : args)
			{
				argsOneString += (" " + i);
			}

			log.info("[PLAYER_COMMAND] " + player.getName().toString() + ": /" + commandLabel.toString() + argsOneString);
			ItemStackPlus tool;
			int itemSlot = 0;
			if (split.length == 0) {
				if (isAllowed(player, "repair") && isAllowed(player, "repcommands")) {
					tool = ItemStackPlus.convert(inven);
					repair.manualRepair(tool);
				} else {
					player.sendMessage(ChatColor.RED + "You dont have permission to do the repair command.");
				}
			} else if (split.length == 1) {
				try {
					char repairList = split[0].charAt(0);					
					if (repairList == '?') {
						support.doQueryOperation(ItemStackPlus.convert(inven));
					} else if (split[0].equalsIgnoreCase("dmg")) {						
						support.durabilityLeft(ItemStackPlus.convert((inven.getItem(inven.getHeldItemSlot()))));
					} else if (split[0].equalsIgnoreCase("arm")) {
						if (isAllowed(player, "repair") && isAllowed(player, "repcommands")) {
							player.sendMessage(ChatColor.DARK_AQUA + "Attempting to repair all armor.");
							repair.repairArmor(player);
						}
						else {
							player.sendMessage(ChatColor.RED + "You don't have permission to do that.");
						}
					} else if (split[0].equalsIgnoreCase("all")) {
						if (isAllowed(player, "repair") && isAllowed(player, "repcommands")) {
							player.sendMessage(ChatColor.DARK_AQUA + "Attempting to repair all items in inventory.");
							repair.repairAll(player);
						}
						else {
							player.sendMessage(ChatColor.RED + "You don't have permission to do that.");
						}
					} else if(split[0].equalsIgnoreCase("reload")) {
						if (isAllowed(player, "reload")){ 
							refreshConfig();
							player.sendMessage(ChatColor.DARK_AQUA + "Re-loaded AutoRepair config files");
						} else {
							player.sendMessage(ChatColor.RED + "You dont have permission to do the reload command.");
						}
					}else {
						if (isAllowed(player, "repair") && isAllowed(player, "repcommands")) {
							itemSlot = Integer.parseInt(split[0]);
							if (itemSlot >0 && itemSlot <=9) {
								tool = ItemStackPlus.convert(inven.getItem(itemSlot -1));
								repair.manualRepair(tool);
							} else {
								player.sendMessage(ChatColor.GOLD + "ERROR: Slot must be a quick bar slot between 1 and 9");
							}	
						} else {
							player.sendMessage(ChatColor.GOLD + "You dont have permission to do the repair command.");
						}
					}
				}
				catch (Exception e) {
					return false;
				}
			}else if (split.length == 2 && split[0].equalsIgnoreCase("arm") && split[1].length() ==1) {
				if (isAllowed(player, "info")) { 
					support.repArmourInfo(split[1]);
				} else {
					player.sendMessage(ChatColor.RED + "You dont have permission to do the ? or dmg commands.");
				}
			}else if ((split.length == 2 && split[1].length() ==1)) {
				try {
					char getRecipe = split[1].charAt(0);
					itemSlot = Integer.parseInt(split[0]);
					if (getRecipe == '?' && itemSlot >0 && itemSlot <=9) {
						if (isAllowed(player, "info")) {
							support.doQueryOperation(ItemStackPlus.convert(inven.getItem(itemSlot-1)));
						} else {
							player.sendMessage(ChatColor.RED + "You dont have permission to do the ? or dmg commands.");
						}
					}
				} catch (Exception e) {
					return false;
				} 
			} else if (split.length == 2 && split[1].equalsIgnoreCase("dmg")) {
				try {
					if (isAllowed(player, "info")) {
						itemSlot = Integer.parseInt(split[0]);
						if (itemSlot >0 && itemSlot <=9) {
							support.durabilityLeft(ItemStackPlus.convert(inven.getItem(itemSlot -1)));
						} else {
							player.sendMessage(ChatColor.GOLD + "ERROR: Slot must be a quick bar slot between 1 and 9");
						}
					} else {
						player.sendMessage(ChatColor.RED + "You dont have permission to do the ? or dmg commands.");
					}
				} catch (Exception e) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	public static boolean isOpAllowed (Player player, operationType op, boolean enchanted, int itemPermGroup)
	{
		switch(op)
		{
			case WARN:
				if (!isAllowed(player, "warn")) return false;
				else return true;
			case QUERY:
				if (!isAllowed(player, "info"))
				{
					player.sendMessage(ChatColor.RED + "You dont have permission to do repair query commands.");
					return false;
				}
			case MANUAL_REPAIR:
			case SIGN_REPAIR:
			case FULL_REPAIR:
				if (!isAllowed(player, "repair"))
				{
					if (op != operationType.FULL_REPAIR) player.sendMessage(ChatColor.RED + "You dont have permission to do repairs.");
					return false;
				} 
				if (!isInRightPermGroup(player, itemPermGroup)) 
				{
					if (op != operationType.FULL_REPAIR) player.sendMessage(ChatColor.RED + "You don't have permission to repair this particular item.");	
					return false;
				}
				if (enchanted) {
					if (!config.repairOfEnchantedItems_allow){
						if (op != operationType.FULL_REPAIR) player.sendMessage(ChatColor.RED + "Enchanted items can't be repaired.");
						return false;
					} else if (!isAllowed(player, "repair.enchanted")){
						if (op != operationType.FULL_REPAIR) player.sendMessage(ChatColor.RED + "You dont have permission to repair enchanted items.");
						return false;
					}
				}
				return true;
			case AUTO_REPAIR:
				if (!config.automaticRepair_allow || !isAllowed(player, "auto") || !isAllowed(player, "repair")) return false;
				if (!isInRightPermGroup(player, itemPermGroup)) return false;
				if (enchanted) {
					if (!config.repairOfEnchantedItems_allow){
						if (!config.automaticRepair_noWarnings) player.sendMessage(ChatColor.RED + "Enchanted items can't be repaired.");
						return false;
					} else if (!isAllowed(player, "repair.enchanted")){
						if (!config.automaticRepair_noWarnings) player.sendMessage(ChatColor.RED + "You dont have permission to repair enchanted items.");
						return false;
					}
				}
				return true;	
		}
		return false;
	}
	
	public static boolean isInRightPermGroup(Player player, int permGroup)
	{
		if (permGroup == 0)
			return true;
		else
			return isAllowed(player, "itemgroup" + Integer.toString(permGroup));
	}
	
	public static boolean isAllowed(Player player, String com) {		

		if(config.usePermissions) {
			if(player.hasPermission("AutoRepair.access") && player.hasPermission("AutoRepair."+com)) {
				return true;
			} else {
				return false;
			}
		}
		return true;
	}

	public HashMap<String, Object> readConfig() {
		String fileName = "plugins/AutoRepair/Config.properties";
		HashMap<String, Object> settings = new HashMap<String, Object>();
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(fileName));
			String line;
			String setting = null;
			String value = null;
			while ((line = reader.readLine()) != null) {
				if ((line.trim().length() == 0) || 
						(line.charAt(0) == '#')) {
					continue;
				}
				int keyPosition = line.indexOf('=');
				setting = line.substring(0, keyPosition).trim();
				value = line.substring(keyPosition +1, line.length());
				settings.put(setting, value);
			}			
			reader.close();
		}catch (Exception e) {
			log.info("Error reading AutoRepair config");
			//If we do fail to read the config files, this means that autorepair will fail without notification,
			//unless we do something about it. 
			//Graceful failure dictates that we do something about it. Therefore, we will turn auto-repair off
			//until the config is able to be read again.
			config.setAutoRepairOff();
		}		
		return settings;

	}
	public void refreshConfig() {
		this.reloadConfig();
		refreshSettings();
		refreshRecipes();
	}
	public void refreshSettings() {
		config.clear();
		config = new globalConfig(getConfig());

		if (config.econCostUse && !economyFound)
		{
			config.setEconCostOff();
		}
	}
	

	public void refreshRecipes() {
		recipes.clear();
		for(String key : getConfig().getConfigurationSection("recipes").getKeys(false)){			
			RepairRecipe newRecipe = new RepairRecipe(getConfig(), key);
			int keyInt;
			try {
				keyInt = Integer.parseInt(key);
			}
			catch (NumberFormatException e){
				keyInt = Material.getMaterial(key).getId();
			}
			
			//We need to check and make sure they're not putting something 
			//stupidly non-repairable in the recipe db. This should still
			//allow custom weapon mods to be compatible.
			if (Material.getMaterial(keyInt).getMaxDurability() != 0)
				recipes.put(keyInt, newRecipe);
		}	
	}
	
	public void convertOldConfig() {
		try {
			globalConfig config = new globalConfig();
			setSettings(readConfig());
			if (getSettings().containsKey("allow_enchanted"))
			{
				config.repairOfEnchantedItems_allow = true;
				config.repairOfEnchantedItems_loseEnchantment = false;
				if (getSettings().get("allow_enchanted").toString().equalsIgnoreCase("false")) 
					config.repairOfEnchantedItems_allow = false;
				if (getSettings().get("allow_enchanted").toString().equalsIgnoreCase("lose_enchantment")) 
					config.repairOfEnchantedItems_loseEnchantment = true;
			}
			if (getSettings().containsKey("allow_anvils"))
			{
				config.anvilUse_allow = Boolean.parseBoolean((String) getSettings().get("allow_anvils"));
			}
			if (getSettings().containsKey("permissions"))
			{
				config.usePermissions = Boolean.parseBoolean((String) getSettings().get("permissions"));
			}
			if (getSettings().containsKey("auto-repair"))
			{
				config.automaticRepair_allow = false;
				config.automaticRepair_noWarnings = false;
				if (getSettings().get("auto-repair").toString().equalsIgnoreCase("true"))
					config.automaticRepair_allow = true;
				if (getSettings().get("auto-repair").toString().equalsIgnoreCase("false-nowarnings"))
					config.automaticRepair_noWarnings = true;
			}
			
			//WE can assume defaults were initialized correctly (use: true, adjust: false) for each cost type
			if (getSettings().containsKey("economy"))
			{
				if (getSettings().containsKey("repair-costs"))
				{
					if (getSettings().get("repair-costs").toString().equalsIgnoreCase("false"))
					{
						config.econCostUse = false;
						config.xpCostUse = false;
						config.itemCostUse = false;
					} else {
						if (getSettings().get("economy").toString().equalsIgnoreCase("true")
								|| getSettings().get("economy").toString().equalsIgnoreCase("both")) {
							if (getSettings().get("economy").toString().equalsIgnoreCase("true")) 
								config.itemCostUse = false;
							if (getSettings().containsKey("econ_fractioning")
									&& getSettings().get("econ_fractioning").toString().equalsIgnoreCase("on"))
								config.econCostAdjust = true;
						}
						if (getSettings().get("economy").toString().equalsIgnoreCase("false")
								|| getSettings().get("economy").toString().equalsIgnoreCase("both")) {
							if (getSettings().get("economy").toString().equalsIgnoreCase("false")) 
								config.econCostUse = false;
							if (getSettings().containsKey("item_rounding")
									&& (getSettings().get("item_rounding").toString().equalsIgnoreCase("min")
											|| getSettings().get("item_rounding").toString().equalsIgnoreCase("round")))
								config.itemCostAdjust = true;
						}
					}
				}
			}
			getConfig().createSection("config", config.serialize());
			
			
		} catch (Exception e){
			log.info("Error reading AutoRepair config file");
		}
	}

	public void convertOldRepairCosts() {
		try {
			RepairRecipe recipe;
			HashMap<String, ArrayList<ItemStack>> map = new HashMap<String, ArrayList<ItemStack>>();
			HashMap<String, Double> iConomy = new HashMap<String, Double>();
			HashMap<String, Integer> durab = new HashMap<String, Integer>();
			String fileName = "plugins/AutoRepair/RepairCosts.properties";
			BufferedReader reader = new BufferedReader(new FileReader(fileName));
			
			String line;
			while ((line = reader.readLine()) != null) {
				if ((line.trim().length() == 0) || 
						(line.charAt(0) == '#')) {
					continue;
				}
				recipe = new RepairRecipe();
				int keyPosition = line. indexOf('=');
				String[] reqs;
				ArrayList<ItemStack> itemReqs = new ArrayList<ItemStack>();
				String item = line.substring(0, keyPosition).trim();
				String recipiesString;
				if (line.indexOf(' ') != -1) {
					recipiesString = line.substring(keyPosition+1, line.indexOf(' '));
					try {
						double amount = Double.parseDouble(line.substring(line.lastIndexOf("=") +1, line.length()));
						iConomy.put(item, amount);
						recipe.getNormalCost().setEconCost(amount);
					} catch (Exception e) {
					}
				} else {
					recipiesString = line.substring(keyPosition+1, line.length()).trim();
				}
	
				String[] allReqs = recipiesString.split(":");
				int durability = 0;
				if ((allReqs.length > 0) && getSettings().get("item_rounding").toString().equalsIgnoreCase("min"))
					recipe.normal.setItemMinCost(1);
				for (int i =0; i < allReqs.length; i++) {
					if (i==0)
					{
						try {
							durability = Integer.parseInt(allReqs[0]);
						} catch (Exception e)
						{
							log.info("[AutoRepair][ERROR] Bad or no durability given for item " + item + "!");
						}
					}
					else
					{
						reqs = allReqs[i].split(",");
						ItemStack currItem = new ItemStack(Integer.parseInt(reqs[0]), Integer.parseInt(reqs[1]));
						itemReqs.add(currItem);
						recipe.getNormalCost().addItemCost(currItem);
					}
				}
				
				//put the recipe into the hashmap, if the recipe exists
				if (!itemReqs.isEmpty()) {
					map.put(item, itemReqs);
				}
				
				//stick durability in the hashmap. if there is no durability, throw an error
				if (durability != 0){
					durab.put(item, durability);
				}
				getConfig().createSection("recipes."+item, recipe.serialize());
			}
			
			saveConfig();
			log.info("finished loading config");
			reader.close();
		}
		catch (Exception e)
		{
			log.info("Error reading AutoRepair recipe file");
		}
	}

	public void setSettings(HashMap<String, Object> settings) {
		this.settings = settings;
	}

	public HashMap<String, Object> getSettings() {
		return settings;
	}
}