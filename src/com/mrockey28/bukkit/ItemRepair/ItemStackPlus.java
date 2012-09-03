package com.mrockey28.bukkit.ItemRepair;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

public class ItemStackPlus{

	int maxDurability;
	public boolean isRepairable;
	public RepairRecipe repairCosts; 
	public ItemStack item;
	public static final Logger log = Logger.getLogger("Minecraft");
	public ItemStackPlus(ItemStack stack) {
		
		item = stack;
		maxDurability = (int)stack.getType().getMaxDurability();
		isRepairable = AutoRepairPlugin.recipes.containsKey(stack.getType().toString());

		if (isRepairable)
		{
			repairCosts = AutoRepairPlugin.recipes.get(stack.getType().toString()).clone();

		}
		else
			repairCosts = new RepairRecipe();
	}
	
	public void setAdjustedCosts(globalConfig config)
	{
		float percentUsed = (float)item.getDurability() / (float)maxDurability;
		if (config.econCostAdjust)
			repairCosts.setEconAdjustedCosts(percentUsed);
		if (config.xpCostAdjust)
			repairCosts.setXpAdjustedCosts(percentUsed);
		if (config.itemCostAdjust)
			repairCosts.setItemAdjustedCosts(percentUsed);
	}
	
	public static ItemStackPlus convert(ItemStack stack) {
		ItemStackPlus newitem = new ItemStackPlus(stack);
		return newitem;
	}
	
	public String getName()
	{
		return item.getType().toString();
	}
	
	public Material getType()
	{
		return item.getType();
	}
	
	public int getDurability()
	{
		return item.getDurability();
	}
	
	public int getMaxDurability()
	{
		return maxDurability;
	}
	
	public boolean freeRepairs()
	{
		if (repairCosts.enchanted.valid == true && this.isEnchanted())
			return false;
		else if (repairCosts.normal.valid == true)
			return false;

		return true;
	}
	
	public RepairRecipe.recipe getRepairCosts()
	{
		if (repairCosts.enchanted.valid == true && this.isEnchanted())
		{
			return repairCosts.getEnchantedCost();
		}
		return repairCosts.getNormalCost();
	}
	
	public int getUsesLeft()
	{
		if (isRepairable) return maxDurability - item.getDurability();
		else return -1;

	}
	
	public void deleteAllEnchantments()
	{
		Set<?> set = item.getEnchantments().entrySet();
		Iterator<?> i = set.iterator();
		
		while (i.hasNext())
		{
			@SuppressWarnings("rawtypes")
			Map.Entry me = (Map.Entry)i.next();
			Enchantment ench = (Enchantment) me.getKey();
			item.removeEnchantment(ench);
		}
	}
	
	public void repair()
	{
		item.setDurability((short)0);
	}
	
	public boolean isEnchanted()
	{
		return (!item.getEnchantments().isEmpty());
	}

}
