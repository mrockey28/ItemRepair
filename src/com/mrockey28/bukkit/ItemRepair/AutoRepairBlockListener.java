package com.mrockey28.bukkit.ItemRepair;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;


/**
 * testing block listener
 * @author lostaris
 */
public class AutoRepairBlockListener implements Listener {
	private final AutoRepairPlugin plugin;
	public AutoRepairSupport support;
	public Repair repair;

	public AutoRepairBlockListener(final AutoRepairPlugin plugin) {
		this.plugin = plugin;
		this.support = new AutoRepairSupport(plugin, null);
		this.repair = new Repair(plugin);
	}	

	//put all Block related code here
	@EventHandler
	public void onBlockDamage(BlockDamageEvent event) {
		Player player = event.getPlayer();
		if (!AutoRepairPlugin.isAllowed(player, "access")) {
			return;
		}
		this.support.setPlayer(player);

		ItemStack toolHand = player.getItemInHand();
		PlayerInventory inven = player.getInventory();
		int toolSlot = inven.getHeldItemSlot();
		Short dmg = toolHand.getDurability();
		if (dmg ==1) {
			support.setWarning(false);
			support.setLastWarning(false);
		}
		HashMap<String, Integer> durabilities = AutoRepairPlugin.getDurabilityCosts();
		String itemName = Material.getMaterial(toolHand.getTypeId()).toString();
		int durability = durabilities.get(itemName);
		
		if (dmg > (durability -5)) {
			repair.autoRepairTool(toolHand, toolSlot);
		} else if (durability <= 100 && dmg > (durability - 20)) {
			support.repairWarn(toolHand, toolSlot);
		} else if (durability > 100 && dmg > (durability - 100)) {
			support.repairWarn(toolHand, toolSlot);
		} 
	}



	public AutoRepairPlugin getPlugin() {
		return plugin;
	}
}

