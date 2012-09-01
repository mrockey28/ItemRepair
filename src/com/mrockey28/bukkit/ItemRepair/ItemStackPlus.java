package com.mrockey28.bukkit.ItemRepair;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

public class ItemStackPlus extends ItemStack{

	int maxDurability;
	
	public ItemStackPlus(ItemStack stack) {
		super(stack);
		maxDurability = (int)stack.getType().getMaxDurability();
	}
	
	public int getMaxDurability()
	{
		return maxDurability;
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
