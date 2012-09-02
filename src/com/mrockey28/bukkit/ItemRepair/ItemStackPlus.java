package com.mrockey28.bukkit.ItemRepair;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

public class ItemStackPlus extends ItemStack{

	int maxDurability;
	public boolean isRepairable;
	public RepairRecipe repairCosts; 
	
	public ItemStackPlus(ItemStack stack) {
		super(stack);
		maxDurability = (int)stack.getType().getMaxDurability();
		isRepairable = AutoRepairPlugin.recipes.containsKey(stack.getType().toString());
		if (isRepairable)
		{
			repairCosts = AutoRepairPlugin.recipes.get(stack.getType().toString()).clone();

		}
	}
	
	public void setAdjustedCosts(globalConfig config)
	{
		float percentUsed = (float)super.getDurability() / (float)maxDurability;
		if (config.isEconCostAdjusted())
			repairCosts.setEconAdjustedCosts(percentUsed);
		if (config.isXpCostAdjusted())
			repairCosts.setXpAdjustedCosts(percentUsed);
		if (config.isItemCostAdjusted())
			repairCosts.setItemAdjustedCosts(percentUsed);
	}
	
	public static ItemStackPlus convert(ItemStack stack) {
		ItemStackPlus newitem = new ItemStackPlus(stack);
		return newitem;
	}
	
	public String getName()
	{
		return super.getType().toString();
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
		if (isRepairable) return maxDurability - super.getDurability();
		else return -1;

	}
	
	public void deleteAllEnchantments()
	{
		Set<?> set = super.getEnchantments().entrySet();
		Iterator<?> i = set.iterator();
		
		while (i.hasNext())
		{
			@SuppressWarnings("rawtypes")
			Map.Entry me = (Map.Entry)i.next();
			Enchantment ench = (Enchantment) me.getKey();
			super.removeEnchantment(ench);
		}
	}
	
	public boolean isEnchanted()
	{
		return (!super.getEnchantments().isEmpty());
	}

}
