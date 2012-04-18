package com.mrockey28.bukkit.ItemRepair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;

public class RepairRecipe implements ConfigurationSerializable{

	private String material;
	private ArrayList<ItemStack> repairItems;
	private double econCost;
	
	public RepairRecipe() {
		material = "";
		repairItems = new ArrayList<ItemStack>(0);
		econCost = 0;
	}
	
	public HashMap<String, Object> serialize() {
		
		HashMap<String, Object> serialOutput = new HashMap<String, Object>();
		
		for (ItemStack i : repairItems)
		{
			serialOutput.put(i.getType().toString(), i.getAmount());
		}
		serialOutput.put("econ-cost", econCost);
		
		return serialOutput;
	}
	
	public RepairRecipe deserialize(HashMap<String, Object> serialInput) {
		
		RepairRecipe recipe = new RepairRecipe();
		
		if (serialInput.containsKey("econ-cost")) {
			econCost = (Double) serialInput.get("econ-cost");
			serialInput.remove("econ-cost");
		}
		
		while (!serialInput.isEmpty()) {
			ItemStack item = new ItemStack(0);
			Map.Entry<String, Object> entry = serialInput.entrySet().iterator().next();
			item.setType(Material.getMaterial(entry.getKey()));
			item.setAmount((Integer) entry.getValue());
			serialInput.remove(entry.getKey());
		}
		
		
		return recipe;
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
