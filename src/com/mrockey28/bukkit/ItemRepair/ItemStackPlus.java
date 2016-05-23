package com.mrockey28.bukkit.ItemRepair;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

/**This is a composition class made to extend the functionality of the Bukkit API implementation of ItemStack.
 * It's a composition of {@link ItemStack} and {@link RepairRecipe} (my own custom class).
 * It's probably not good practice to have made it directly reliant on my plugin itsself, but whatever.
 * It could be fairly easily ported, if you removed those specific things.
 * @author Matt Rockey
 * 
 */
public class ItemStackPlus{
	
	//Does the file appear in the repair recipes file?
	public boolean isRepairable;
	
	//As I stated, this is a composition class. Here are the composing classses.
	public RepairRecipe repairCosts; 
	public ItemStack item;

	/**As it stands now, this constructor relies on AutoRepairPlugin. Probably bad practice.
	 * It would be fairly easy to make this not the case.
	 * @param Resulting {@link ItemStackPlus}
	 */
	public ItemStackPlus(ItemStack stack) {
		if (stack == null)
			stack = new ItemStack(Material.AIR);
		
		item = stack;
		isRepairable = AutoRepairPlugin.recipes.containsKey(stack.getType().getId());

		if (isRepairable)
		{
			repairCosts = AutoRepairPlugin.recipes.get(stack.getType().getId()).clone();

		}
		else
			repairCosts = new RepairRecipe();
	}
	
	public void setAdjustedCosts(globalConfig config)
	{
		//Percent of the item used is calculated like so:
		float percentUsed = (float)item.getDurability() / (float)this.getMaxDurability();
		
		if (config.econCostAdjust)
			repairCosts.setEconAdjustedCosts(percentUsed);
		if (config.xpCostAdjust)
			repairCosts.setXpAdjustedCosts(percentUsed);
		if (config.itemCostAdjust)
			repairCosts.setItemAdjustedCosts(percentUsed);
	}
	
	/**Static method allow conversion to {@link ItemStackPlus}.
	 * convert
	 * ItemStackPlus ItemStackPlus convert
	 * @param {@link ItemStack} to serve as part of composition
	 * @return Resulting {@link ItemStackPlus}
	 */
	public static ItemStackPlus convert(ItemStack stack) {
		ItemStackPlus newitem = new ItemStackPlus(stack);
		return newitem;
	}
	public static ItemStackPlus convert(PlayerInventory inv) {
		ItemStack item = inv.getItemInMainHand();
		if (item == null)
			item = inv.getItemInOffHand();
		return convert(item);
	}
	/**Gets the specific human-readable name of the item.
	 * getName
	 * String ItemStackPlus getName
	 * @return Human-readable item name
	 */
	public String getName()
	{
		return item.getType().toString();
	}
	
	/**Just a reflector for ItemStack.getType()
	 * getType
	 * Material ItemStackPlus getType
	 * @return Material of item
	 */
	public Material getType()
	{
		return item.getType();
	}
	
	/**Just a reflector for ItemStack.getDurability()
	 * getDurability
	 * int ItemStackPlus getDurability
	 * @return Item durability (amount of times it's been used)
	 */
	public int getDurability()
	{
		return item.getDurability();
	}
	
	/**Gets the maximum durability value this particular item can have.
	 * Basically, I wanted to expose this at a higher layer rather than through the type,
	 * because that's a royal pain.
	 * getMaxDurability
	 * int ItemStackPlus getMaxDurability
	 * @return Max Durability (times item can be used before breaking)
	 */
	public int getMaxDurability()
	{
		return item.getType().getMaxDurability();
	}
	
	/**Gets the permission group this item is associated with. This is a property of
	 * the underlying RepairRecipe.
	 * getPermGroup
	 * int ItemStackPlus getPermGroup
	 * @return Permission group of item.
	 */
	public int getPermGroup()
	{
		return repairCosts.getPermGroup();
	}
	
	/**Returns true if repairs for this item should be free.
	 * boolean ItemStackPlus freeRepairs
	 * @return true if repairs are free, false otherwise
	 */
	public boolean freeRepairs()
	{
		if (repairCosts.enchanted.valid == true && this.isEnchanted())
			return false;
		else if (repairCosts.normal.valid == true)
			return false;

		return true;
	}
	
	/**Gets the correct recipe for repair, depending on whether the item is enchanted or not.
	 * getRepairCosts
	 * RepairRecipe.recipe ItemStackPlus getRepairCosts
	 * @return recipe used for calculating repair costs
	 */
	public RepairRecipe.recipe getRepairCosts()
	{
		if (repairCosts.enchanted.valid == true && this.isEnchanted())
		{
			return repairCosts.getEnchantedCost();
		}
		return repairCosts.getNormalCost();
	}
	
	/**Gets the number of times this item can be used without breaking. This is not accurate for items enchanted with "unbreaking".
	 * getUsesLeft
	 * int ItemStackPlus getUsesLeft
	 * @return Uses left in the item before breaking.
	 */
	public int getUsesLeft()
	{
		if (isRepairable) return (this.getMaxDurability() - item.getDurability());
		else return -1;

	}
	
	/**Removes all the enchantments from this particular item.
	 * deleteAllEnchantments
	 * void ItemStackPlus deleteAllEnchantments
	 */
	public void deleteAllEnchantments()
	{
		Set<?> set = item.getEnchantments().entrySet();
		Iterator<?> i = set.iterator();
		
		//This is a really ugly method of stepping through the enchantments.
		//This is where i show my relative inexperience with java.
		while (i.hasNext())
		{
			@SuppressWarnings("rawtypes")
			Map.Entry me = (Map.Entry)i.next();
			Enchantment ench = (Enchantment) me.getKey();
			item.removeEnchantment(ench);
		}
	}
	
	/**Completely repairs the item. If this is not a repairable item, this could have unintended consequences.
	 * For example, if this function is used on some color dye, it turns that dye into an ink sack.
	 * repair
	 * void ItemStackPlus repair
	 */
	public void repair()
	{
		item.setDurability((short)0);
	}
	
	/**Returns true if the item is enchanted.
	 * isEnchanted
	 * boolean ItemStackPlus isEnchanted
	 * @return true if item is enchanted, otherwise false
	 */
	public boolean isEnchanted()
	{
		return (!item.getEnchantments().isEmpty());
	}

}
