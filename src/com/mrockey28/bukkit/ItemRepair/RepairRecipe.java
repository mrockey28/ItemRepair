package com.mrockey28.bukkit.ItemRepair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class RepairRecipe implements ConfigurationSerializable{

	public recipe normal;
	public recipe enchanted;

	
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
	
	public RepairRecipe () {
		normal = new recipe();
		enchanted = new recipe();
	}
	
	public RepairRecipe(Object input) {
		@SuppressWarnings("unchecked")
		HashMap<String, Object> serialInput = (HashMap<String, Object>) input;
		
		normal = new recipe(serialInput.get("normal"));
		enchanted = new recipe(serialInput.get("enchanted"));
		
	}
	
	public recipe getNormalCost() {
		return normal;
	}
	
	public recipe getEnchantedCost() {
		return enchanted;
	}	
	
	
	public class recipe implements Cloneable{
		private String costType;
		private String material;
		private ArrayList<ItemStack> repairItems;
		private double econCost;
		private double econCostMin;
		private int xpCost;
		private int xpCostMin;
		
		
		private recipe() {
			costType = "";
			material = "";
			repairItems = new ArrayList<ItemStack>(0);
			econCost = 0;
			econCostMin = 0;
			xpCost = 0;
			xpCostMin = 0;
		}
		public recipe clone()
		{
			recipe result = new recipe();
			result.costType = costType;
			result.material = material;
			result.repairItems = (ArrayList<ItemStack>) repairItems.clone(); 
			result.econCost = econCost;
			result.econCostMin = econCostMin;
			result.xpCost = xpCost;
			result.xpCostMin = xpCostMin; 
			return result;
		}
		private HashMap<String, Object> serialize() {
			
			HashMap<String, Object> serialOutput = new HashMap<String, Object>();
			
			//This will all be empty if there is nothing of relevance to do
			if (costType != "")
			{
				serialOutput.put("cost-type", costType);
			}
			for (ItemStack i : repairItems)
			{
				serialOutput.put(i.getType().toString(), i.getAmount());
			}
			if (econCost > 0) {
				serialOutput.put("econ-cost", econCost);
			}
			
			return serialOutput;
		}
		
		private recipe(Object input) {
			@SuppressWarnings("unchecked")
			HashMap<String, Object> serialInput = (HashMap<String, Object>) input;
			
			//We remove the known possible other fields besides the recipe ingredients from the map
			//so that we can run through all the recipe ingredients at the end
			if (serialInput.containsKey("econ-cost")) {
				econCost = (Double) serialInput.get("econ-cost");
				serialInput.remove("econ-cost");
			}
			if (serialInput.containsKey("econ-cost-min")) {
				econCostMin = (Double) serialInput.get("econ-cost-min");
				serialInput.remove("econ-cost-min");
			}
			if (serialInput.containsKey("cost-type")) {
				costType = (String) serialInput.get("cost-type");
				serialInput.remove("cost-type");
			}
			if (serialInput.containsKey("xp-cost")) {
				xpCost = (Integer) serialInput.get("xp-cost");
				serialInput.remove("xp-cost");
			}
			if (serialInput.containsKey("xp-cost-min")) {
				xpCostMin = (Integer) serialInput.get("xp-cost-min");
				serialInput.remove("xp-cost-min");
			}
			while (!serialInput.isEmpty()) {
				ItemStack item = new ItemStack(0);
				Map.Entry<String, Object> entry = serialInput.entrySet().iterator().next();
				item.setType(Material.getMaterial(entry.getKey()));
				item.setAmount((Integer) entry.getValue());
				repairItems.add(item.clone());
				material = entry.getKey().toString();
				serialInput.remove(entry.getKey());
			}
		}
		
		
		
		
		public void adjustRepairCost(ItemStackRevised item)
		{
			if (AutoRepairPlugin.config.isXpCostOff())
			{
				xpCost = 0;
			}
			else if (AutoRepairPlugin.config.isXpCostAdjusted())
			{
		    	xpCost = (int)adjustCost ((double)xpCost, item);
	    		if (xpCost < xpCostMin)
	    		{
	    			xpCost = xpCostMin;
	    		}
		    }
			
			if (AutoRepairPlugin.config.isEconCostOff())
			{
				econCost = 0;
			}
			else if (AutoRepairPlugin.config.isEconCostAdjusted())
			{
		    	econCost = adjustCost (econCost, item);
	    		if (econCost < econCostMin)
	    		{
	    			econCost = econCostMin;
	    		}
		    }
		    //repeat this pattern for item cost and econ cost.
		}

		private double adjustCost (double cost, ItemStackRevised object)
		{
		    float fraction = object.getItemStack().getDurability() / object.getMaxDurability();
		    return cost * fraction;
		}
		
		//This function HAS to assume that there will be no overflow conditions;
		//that there has already been a check done to make sure the cost CAN be deducted. We just need to do it now.
		public void ApplyCost (ItemStackRevised item, Player player)
		{
			//Deduct XP cost
			player.setExp(player.getExp() - xpCost);
			
			//Deduct Econ cost
			AutoRepairPlugin.econ.withdrawPlayer(player.getName(), econCost);
		    
		    //since we can't do player.item -= itemCost,
		    //the loop below will have to suffice.
		    for (ItemStack i : repairItems)
		    {
		    	
		        int invenIndex = player.getInventory().first(i.getType());
		        while (player.getInventory().getItem(invenIndex).getAmount() < i.getAmount())
		        {
		        	i.setAmount(i.getAmount() - player.getInventory().getItem(invenIndex).getAmount());
		            player.getInventory().clear(invenIndex);
		            invenIndex = player.getInventory().first(i.getType());
		        }    
		        player.getInventory().getItem(invenIndex).setAmount(player.getInventory().getItem(invenIndex).getAmount() - i.getAmount());
		    }

		    if (AutoRepairPlugin.config.removeEnchanmentsOnRepair())
		    {
		        item.deleteAllEnchantments();
		    }

		}
		
		
		void setCostType (String newCostType) {
			if (newCostType == "item" || newCostType == "econ" || newCostType == "both")
			{
				costType = newCostType;
			}
		}
		
		public String getCostType () {
			return costType;
		}
		
		private void setMaterial (Material passedMat) {
			material = passedMat.toString();
		}
		
		public Material getMaterial ()
		{
			return Material.getMaterial(material.toString());
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
