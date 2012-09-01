package com.mrockey28.bukkit.ItemRepair;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

import org.bukkit.entity.Player;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import net.milkbowl.vault.economy.Economy;

import com.mrockey28.bukkit.ItemRepair.RepairRecipe;

/**
 * testing for Bukkit
 *
 * @author lostaris, mrockey28
 */

public class AutoRepairPlugin extends JavaPlugin {
	private final AutoRepairBlockListener blockListener = new AutoRepairBlockListener(this);
	private static HashMap<String, Integer> durabilityCosts;
	private static HashMap<String, ArrayList<ItemStack>> repairRecipies;
	private HashMap<String, Object> settings;
	private static HashMap<String, Double> iConCosts;
	private static String useEcon = "false"; //are we using econ, not using econ, using econ and items, or letting the AutoRepair.properties file decide?
	private static String allowEnchanted = "true";
	private static boolean allowAnvils = true;
	private static boolean economyFound = false;
	private static String autoRepair = "true";
	private static boolean repairCosts;
	static boolean issueRepairedNotificationWhenNoRepairCost = true;
	public static boolean isPermissions = false;
	
	public static RepairRecipe recipe;
	public static HashMap<String, RepairRecipe> recipes = new HashMap<String, RepairRecipe>(0);
	public static globalConfig config;
	public static Economy econ = null;
	public static String item_rounding = "flat";
	public static String econ_fractioning = "off";
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
			String fileName = "plugins/AutoRepair/RepairCosts.properties";
			f = new File(fileName);
			if (f.exists()) {
				convertOldRepairCosts();	
			}
			fileName = "plugins/AutoRepair/Config.properties";
			f = new File(fileName);
			if (f.exists()) {
				convertOldConfig();	
			}
		}
		getConfig().options().copyDefaults(true);
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
			ItemStack tool;
			int itemSlot = 0;
			if (split.length == 0) {
				if (isAllowed(player, "repair") && isAllowed(player, "repcommands")) {
					tool = player.getItemInHand();
					repair.manualRepair(tool);
				} else {
					player.sendMessage("§cYou dont have permission to do the repair command.");
				}
			} else if (split.length == 1) {
				try {
					char repairList = split[0].charAt(0);					
					if (repairList == '?') {
						support.toolReq(player.getItemInHand());
					} else if (split[0].equalsIgnoreCase("dmg")) {						
						support.durabilityLeft(inven.getItem(inven.getHeldItemSlot()));
					} else if (split[0].equalsIgnoreCase("arm")) {						
						repair.repairArmor(player);
					} else if (split[0].equalsIgnoreCase("all")) {						
						repair.repairAll(player);
					} else if(split[0].equalsIgnoreCase("reload")) {
						if (isAllowed(player, "reload")){ 
							refreshConfig();
							player.sendMessage("§3Re-loaded AutoRepair config files");
						} else {
							player.sendMessage("§cYou dont have permission to do the reload command.");
						}
					}else {
						if (isAllowed(player, "repair") && isAllowed(player, "repcommands")) {
							itemSlot = Integer.parseInt(split[0]);
							if (itemSlot >0 && itemSlot <=9) {
								tool = inven.getItem(itemSlot -1);
								repair.manualRepair(tool);
							} else {
								player.sendMessage("§6ERROR: Slot must be a quick bar slot between 1 and 9");
							}	
						} else {
							player.sendMessage("§cYou dont have permission to do the repair command.");
						}
					}
				} catch (Exception e) {
					return false;
				}
			}else if (split.length == 2 && split[0].equalsIgnoreCase("arm") && split[1].length() ==1) {
				if (isAllowed(player, "info")) { 
					support.repArmourInfo(split[1]);
				} else {
					player.sendMessage("§cYou dont have permission to do the ? or dmg commands.");
				}
			}else if ((split.length == 2 && split[1].length() ==1)) {
				try {
					char getRecipe = split[1].charAt(0);
					itemSlot = Integer.parseInt(split[0]);
					if (getRecipe == '?' && itemSlot >0 && itemSlot <=9) {
						if (isAllowed(player, "info")) {
							support.toolReq(inven.getItem(itemSlot-1));
						} else {
							player.sendMessage("§cYou dont have permission to do the ? or dmg commands.");
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
							support.durabilityLeft(inven.getItem(itemSlot -1));
						} else {
							player.sendMessage("§6ERROR: Slot must be a quick bar slot between 1 and 9");
						}
					} else {
						player.sendMessage("§cYou dont have permission to do the ? or dmg commands.");
					}
				} catch (Exception e) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	public static boolean isOpAllowed (Player player, operationType op, boolean enchanted)
	{
		switch(op)
		{
			case QUERY:
				if (!isAllowed(player, "info"))
				{
					player.sendMessage("§cYou dont have permission to do repair query commands.");
					return false;
				}
				else return true;
			case WARN:
				if (!isAllowed(player, "warn")) return false;
				else return true;
			case MANUAL_REPAIR:
			case SIGN_REPAIR:
			case FULL_REPAIR:
				if (!isAllowed(player, "repair"))
				{
					if (op != operationType.FULL_REPAIR) player.sendMessage("§cYou dont have permission to do the repair command.");
					return false;
				} 
				if (enchanted) {
					if (allowEnchanted == "false"){
						if (op != operationType.FULL_REPAIR) player.sendMessage("§cEnchanted items can't be repaired.");
						return false;
					} else if (allowEnchanted == "permissions" && !isAllowed(player, "repair.enchanted")){
						if (op != operationType.FULL_REPAIR) player.sendMessage("§cYou dont have permission to repair enchanted items.");
						return false;
					}
				}
				return true;
			case AUTO_REPAIR:
				if (!AutoRepairPlugin.isAutoRepair() || !isAllowed(player, "auto") || !isAllowed(player, "repair")) return false;
				if (enchanted) {
					if (allowEnchanted == "false"){
						player.sendMessage("§cEnchanted items can't be repaired.");
						return false;
					} else if (allowEnchanted == "permissions" && !isAllowed(player, "repair.enchanted")){
						player.sendMessage("§cYou dont have permission to repair enchanted items.");
						return false;
					}
				}
				return true;	
		}
		return false;
	}
	
	public static boolean isAllowed(Player player, String com) {		
		boolean allowed = false;
		if(isPermissions == true) {
			if(player.hasPermission("AutoRepair.access") && player.hasPermission("AutoRepair."+com)) {
				allowed = true;
			} else {
				allowed = false;
			}
		}else if(isPermissions == false && !com.equalsIgnoreCase("none")) {
			allowed = true;
		}
		
		return allowed;
	}

	public HashMap<String, Object> readConfig() {
		String fileName = "plugins/AutoRepair/Config.properties";
		HashMap<String, Object> settings = new HashMap<String, Object>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(fileName));
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
		}catch (Exception e) {
			log.info("Error reading AutoRepair config");
			//If we do fail to read the config files, this means that autorepair will fail without notification,
			//unless we do something about it. 
			//Graceful failure dictates that we do something about it. Therefore, we will turn auto-repair off
			//until the config is able to be read again.
			setAutoRepair("false");
		}		
		return settings;

	}
	public void refreshConfig() {
		refreshSettings();
		refreshRecipes();
	}
	public void refreshSettings() {
		config = new globalConfig(getConfig());
		if (config.isEconCostOn() && !economyFound)
		{
			config.turnEconCostOff();
		}
	}
	
	public void refreshRecipes() {

		for(String key : getConfig().getConfigurationSection("recipes").getKeys(false)){			
			RepairRecipe newRecipe = new RepairRecipe(getConfig(), key);
			recipes.put(key, newRecipe);
		}	
	}
	
	public void convertOldConfig() {
		try {
			
			setSettings(readConfig());
			if (getSettings().containsKey("allow_enchanted"))
			{
				getSettings().put("allowRepairOfEnchantedItems", (String)getSettings().get("allow_enchanted"));
				getSettings().remove("allow_enchanted");
			}
			if (getSettings().containsKey("allow_anvils"))
			{
				getSettings().put("allowAnvilUse", Boolean.parseBoolean((String) getSettings().get("allow_anvils")));
				getSettings().remove("allow_anvils");
			}
			if (getSettings().containsKey("permissions"))
			{
				getSettings().put("usePermissions", Boolean.parseBoolean((String) getSettings().get("permissions")));
				getSettings().remove("permissions");
			}
			if (getSettings().containsKey("auto-repair"))
			{
				getSettings().put("automaticRepair", Boolean.parseBoolean((String) getSettings().get("auto-repair")));
				getSettings().remove("auto-repair");
			}
			if (getSettings().containsKey("economy"))
			{
				if (getSettings().containsKey("repair-costs"))
				{
					if (getSettings().get("repair-costs").toString().equalsIgnoreCase("false"))
					{
						getSettings().put("econCost", "off");
						getSettings().put("itemCost", "off");
					}
					else
					{
						if (getSettings().get("economy").toString().equalsIgnoreCase("true") || getSettings().get("economy").toString().equalsIgnoreCase("both"))
						{
							if (getSettings().get("economy").toString().equalsIgnoreCase("true")) getSettings().put("itemCost", "off");
							if (getSettings().containsKey("econ_fractioning") && getSettings().get("econ_fractioning").toString().equalsIgnoreCase("on"))
								getSettings().put("econCost", "adjusted");
							else
								getSettings().put("econCost", "full");
						}
						if (getSettings().get("economy").toString().equalsIgnoreCase("false") || getSettings().get("economy").toString().equalsIgnoreCase("both"))
						{
							if (getSettings().get("economy").toString().equalsIgnoreCase("false")) getSettings().put("econCost", "off");
							if (getSettings().containsKey("item_rounding") && (getSettings().get("item_rounding").toString().equalsIgnoreCase("min") || getSettings().get("item_rounding").toString().equalsIgnoreCase("round")))
								getSettings().put("itemCost", "adjusted");
							else
								getSettings().put("itemCost", "full");
						}
						if (getSettings().get("economy").toString().equalsIgnoreCase("config"))
						{
							getSettings().put("econCost", "full");
							getSettings().put("itemCost", "full");
						}
					}
				}

				getSettings().remove("economy");
				getSettings().remove("econ_fractioning");
				getSettings().remove("item_rounding");
				getSettings().remove("repair-costs");
			}
			getConfig().createSection("config", getSettings());
			
			
		} catch (Exception e){
			log.info("Error reading AutoRepair config file");
		}
	}

	public void convertOldRepairCosts() {
		try {
			HashMap<String, ArrayList<ItemStack> > map = new HashMap<String, ArrayList<ItemStack> >();
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
						recipe.getNormalCost().setCostType("econ");
					} catch (Exception e) {
					}
				} else {
					recipiesString = line.substring(keyPosition+1, line.length()).trim();
				}
	
				String[] allReqs = recipiesString.split(":");
				int durability = 0;
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
						if (recipe.getNormalCost().getCostType() == "econ")
						{
							recipe.getNormalCost().setCostType("both");
						}
						else
						{
							recipe.getNormalCost().setCostType("item");
						}
					}
				}
				
				//put the recipe into the hashmap, if the recipe exists
				if (!itemReqs.isEmpty()) {
					map.put(item, itemReqs);
				}
				//If there is no recipe and no econ cost, and repair costs are enabled, throw an error
				else if (!iConomy.containsKey(item) && isRepairCosts())
				{
					log.info("[AutoRepair][ERROR] No cost given for item " + item + "!");
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
			setiConCosts(iConomy);
			setRepairRecipies(map);
			setDurabilityCosts(durab);
		}
		catch (Exception e)
		{
			log.info("Error reading AutoRepair recipe file");
		}
	}

	public void setUseEcon(String b) {
		AutoRepairPlugin.useEcon = b;		
	}

	public static String getUseEcon() {
		return AutoRepairPlugin.useEcon;
	}

	public static void setRepairRecipies(HashMap<String, ArrayList<ItemStack>> hashMap) {
		AutoRepairPlugin.repairRecipies = hashMap;
	}

	public static HashMap<String, ArrayList<ItemStack>> getRepairRecipies() {
		return repairRecipies;
	}

	public void setSettings(HashMap<String, Object> settings) {
		this.settings = settings;
	}

	public HashMap<String, Object> getSettings() {
		return settings;
	}

	public void setAutoRepair(String autoRepair) {
		AutoRepairPlugin.autoRepair = autoRepair;
	}

	public static boolean isAutoRepair() {
		if (autoRepair.equalsIgnoreCase("false-nowarnings") || autoRepair.equalsIgnoreCase("false")) {
			return false;
		}
		else
		{
			return true;
		}
		
	}
	
	public static boolean suppressWarningsWithNoAutoRepair() {
		if (autoRepair.equalsIgnoreCase("false-nowarnings")) {
			return true;
		}
		return false;
	}

	public void setRepairCosts(boolean repairCosts) {
		AutoRepairPlugin.repairCosts = repairCosts;
	}

	public static boolean isRepairCosts() {
		return repairCosts;
	}

	public static void setiConCosts(HashMap<String, Double> iConomy) {
		AutoRepairPlugin.iConCosts = iConomy;
	}

	public static HashMap<String, Double> getiConCosts() {
		return iConCosts;
	}
	
	public static void setDurabilityCosts(HashMap<String, Integer> durab) {
		AutoRepairPlugin.durabilityCosts = durab;
	}
	
	public static HashMap<String, Integer> getDurabilityCosts() {
		return durabilityCosts;
	}
	
	public boolean anvilsAllowed() {
		return allowAnvils;
	}
	
	public ItemStack checkForRemoveEnchantment(ItemStack item)
	{
		if (allowEnchanted.equalsIgnoreCase("lose_enchantment"))
		{
			//note that we can put 0 here because ItemStackRevised just becomes a convenient way to
			//wrap the "delete enchantment" function
			ItemStackPlus itemExtended = new ItemStackPlus(item);
			itemExtended.deleteAllEnchantments();
		}
		return item;
	}
}