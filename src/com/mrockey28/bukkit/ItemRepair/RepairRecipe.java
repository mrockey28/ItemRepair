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
	public enum RecipeType {
		NORMAL,
		ENCHANTED
	}
	
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
	
	public RepairRecipe(HashMap<String, Object> serialInput) {
		
		normal = new recipe(serialInput.get("normal"));
		enchanted = new recipe(serialInput.get("enchanted"));
		
	}
	
	
	public void setMaterial (Material passedMat, RecipeType type) {
		if (type == RecipeType.NORMAL)
		{
			normal.setMaterial(passedMat);
		}
		else
		{
			enchanted.setMaterial(passedMat);
		}
	}
	
	public Material getMaterial (RecipeType type) {
		if (type == RecipeType.NORMAL)
		{
			return normal.getMaterial();
		}
		else
		{
			return enchanted.getMaterial();
		}
	}
	
	public void addItemCost (ItemStack newItem, RecipeType type) {
		if (type == RecipeType.NORMAL)
		{
			normal.addItemCost(newItem);
		}
		else
		{
			enchanted.addItemCost(newItem);
		}
		
	}
	
	public ArrayList<ItemStack> getItemCost (RecipeType type) {
		if (type == RecipeType.NORMAL)
		{
			return normal.getItemCost();
		}
		else
		{
			return enchanted.getItemCost();
		}
		
	}
	
	public void setEconCost(double newCost, RecipeType type) {
		if (type == RecipeType.NORMAL)
		{
			normal.setEconCost(newCost);
		}
		else
		{
			enchanted.setEconCost(newCost);
		}	
	}
	
	public double getEconCost(RecipeType type) {
		if (type == RecipeType.NORMAL)
		{
			return normal.getEconCost();
		}
		else
		{
			return enchanted.getEconCost();
		}	
	}
	
	public void setCostType (String newCostType, RecipeType type) {
		if (type == RecipeType.NORMAL)
		{
			normal.setCostType(newCostType);
		}
		else
		{
			enchanted.setCostType(newCostType);
		}
		
	}
	public String getCostType (RecipeType type) {
		if (type == RecipeType.NORMAL)
		{
			return normal.getCostType();
		}
		else
		{
			return enchanted.getCostType();
		}
		
	}
	
	
	private class recipe {
		private String costType;
		private String material;
		private ArrayList<ItemStack> repairItems;
		private double econCost;
		
		private recipe() {
			costType = "";
			material = "";
			repairItems = new ArrayList<ItemStack>(0);
			econCost = 0;
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
			if (serialInput.containsKey("cost-type")) {
				costType = (String) serialInput.get("econ-cost");
				serialInput.remove("cost-type");
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
		
		private void setCostType (String newCostType) {
			if (newCostType == "item" || newCostType == "econ" || newCostType == "both")
			{
				costType = newCostType;
			}
		}
		
		private String getCostType () {
			return costType;
		}
		
		private void setMaterial (Material passedMat) {
			material = passedMat.toString();
		}
		
		private Material getMaterial ()
		{
			return Material.getMaterial(material.toString());
		}
		
		private void addItemCost (ItemStack newItem) {
			repairItems.add(newItem);
		}
		
		private ArrayList<ItemStack> getItemCost () {
			return repairItems;
		}
		
		private void setEconCost(double newCost) {
			econCost = newCost;
		}
		
		private double getEconCost()
		{
			return econCost;
		}
	}
	
}
