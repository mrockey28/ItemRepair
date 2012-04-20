package com.mrockey28.bukkit.ItemRepair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;

public class RepairRecipe implements ConfigurationSerializable{

	public recipe normal;
	public recipe enchanted;
	
	public HashMap<String, Object> serialize() {
		
		HashMap<String, Object> serialOutput = new HashMap<String, Object>();
		
		serialOutput.put("normal", normal.serialize());
		serialOutput.put("enchanted", enchanted.serialize());
		
		return serialOutput;
	}
	
	public RepairRecipe () {
		normal = new recipe();
		enchanted = new recipe();
	}
	
	public RepairRecipe(HashMap<String, Object> serialInput) {
		
		normal = new recipe(serialInput.get("normal"));
		enchanted = new recipe(serialInput.get("enchanted"));
		
	}
	
	
	public void setMaterial (Material passedMat) {
		normal.material = passedMat.toString();
	}
	
	public void addItemCost (ItemStack newItem) {
		normal.repairItems.add(newItem);
	}
	
	public void setEconCost(double newCost) {
		normal.econCost = newCost;
	}
	public void setCostType (String newCostType) {
		normal.setCostType(newCostType);
	}
	public String getCostType () {
		return normal.costType;
	}
	
	
	private class recipe {
		private String costType;
		private String material;
		private ArrayList<ItemStack> repairItems;
		private double econCost;
		
		public recipe() {
			costType = "";
			material = "";
			repairItems = new ArrayList<ItemStack>(0);
			econCost = 0;
		}
		
		public HashMap<String, Object> serialize() {
			
			HashMap<String, Object> serialOutput = new HashMap<String, Object>();
			
			serialOutput.put("cost-type", costType);
			for (ItemStack i : repairItems)
			{
				serialOutput.put(i.getType().toString(), i.getAmount());
			}
			serialOutput.put("econ-cost", econCost);
			
			return serialOutput;
		}
		
		public recipe(Object input) {
			HashMap<String, Object> serialInput = (HashMap<String, Object>) input;
			if (serialInput.containsKey("econ-cost")) {
				econCost = (Double) serialInput.get("econ-cost");
				serialInput.remove("econ-cost");
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
		public void setCostType (String newCostType) {
			if (newCostType == "item" || newCostType == "econ" || newCostType == "both")
			{
				costType = newCostType;
			}
		}
		public void setMaterial (Material passedMat) {
			material = passedMat.toString();
		}
		
		public void addItemCost (ItemStack newItem) {
			repairItems.add(newItem);
		}
		
		public void setEconCost(double newCost) {
			econCost = newCost;
		}
		
		public void Clear() {
			material = "";
			repairItems.clear();
			econCost = 0;
		}
	}
	
}
