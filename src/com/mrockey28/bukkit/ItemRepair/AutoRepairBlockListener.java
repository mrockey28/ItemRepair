package com.mrockey28.bukkit.ItemRepair;



import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;


/**
 * testing block listener
 * @author lostaris, mrockey28
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

/////
//EVENT HANDLERS
/////
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		support.checkForAnvilRepair(event);
	}
	
	@EventHandler
	public void onBlockDamage(BlockDamageEvent event) {		
		eventAffectsItemInHand(event.getPlayer());
	}

	@EventHandler
	public void PlayerItemDamageEvent(PlayerItemDamageEvent event) {
		if (event.getDamage() > 2)
			event.setDamage(2);
	}
	@EventHandler
	//Need to check item in hand for damager when something or someone is damaged
	//as the damager could be using a sword or other breakable item
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		
		//Make sure damager is a player
		if (event.getDamager().getType() != EntityType.PLAYER) {
			return;
		}
		
		//This is safe because we already ejected if EntityType is not player
		Player player = (Player) (event.getDamager());
		
		eventAffectsItemInHand(player);
	}
	
	@EventHandler
	//This is only needed so bows autorepair
	public void onEntityShootBow(EntityShootBowEvent event) {
		
		//If something else shoots a bow besides a player
		if (event.getEntityType() != EntityType.PLAYER) {
			return;
		}
		//We already know the entityType is a player, so this is a safe conversion
		Player player = (Player) event.getEntity();
		
		eventAffectsItemInHand(player);
		
	}
	
	@EventHandler
	//This is only needed so bows autorepair
	public void onPlayerFishEvent(PlayerFishEvent event) {

		eventAffectsItemInHand(event.getPlayer());
		
	}
	
	@EventHandler
	//If something damages the player, we need to check his armor
	//TODO: Need to replace this overall check with check for armor-damage-only events
	//These are as follows (from minecraft wiki):
	//Direct attacks from mobs
	//Direct attacks from other players
	//Getting hit with an arrow
	//Getting hit with a fireball from a Ghast or Blaze
	//Touching a block of fire or lava
	//Touching a cactus
	//Explosions
	public void onEntityDamage(EntityDamageEvent event) {
		
		//Make sure this is a player
		if (event.getEntity().getType() != EntityType.PLAYER) {
			return;
		}
		//We already know the entityType is a player, so this is a safe conversion
		Player player = (Player) event.getEntity();
		
		eventAffectsArmor(player);
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	//If player changes held item, clear out the warning flags
	public void onPlayerChangeHeldItem(PlayerItemHeldEvent event) {
		Player player = event.getPlayer();
		this.support.setPlayer(player);
		support.setWarning(false);
		support.setLastWarning(false);
	}
	
//////
//NON-EVENT HANDLER FUNCTIONS
/////
	
	//If we need to check the item in hand for breakage when
	//certain events occur
	private void eventAffectsItemInHand (Player player)
	{
		if (!AutoRepairPlugin.isAllowed(player, "access")) {
			return;
		}
		this.support.setPlayer(player);

		ItemStackPlus toolHand = ItemStackPlus.convert(player.getInventory());
		
		//ItemStackPlus toolHand = new ItemStackPlus(player.getItemInHand());
		Short dmg = toolHand.item.getDurability();
		
		//If dmg = 0, it's too late
		if (dmg == 0){
			return;
		} 
		
		if (dmg ==1) {
			support.setWarning(false);
			support.setLastWarning(false);
		}

		int itemMaxDurability = toolHand.getMaxDurability();

		if (dmg > (itemMaxDurability -3) && AutoRepairPlugin.config.automaticRepair_allow)
		{
			repair.autoRepairTool(toolHand);
		}	
		//If the item is not enchanted, warn at some level
		else if (!toolHand.isEnchanted() && (dmg > (itemMaxDurability - 10))) 
		{
			support.doWarnOperation(toolHand, false);
		}
		//If the item IS enchanted, warn at a different level
		else if (toolHand.isEnchanted() && 
				((dmg > (itemMaxDurability - 30)) && (dmg > 60) ||
				(dmg > (itemMaxDurability - 15)))) 
		{
			support.doWarnOperation(toolHand, false);
		}
	}
	
	//If we need to check the item in hand for breakage when
	//certain events occur
	private void eventAffectsArmor (Player player)
	{
		if (!AutoRepairPlugin.isAllowed(player, "access")) {
			return;
		}
		this.support.setPlayer(player);
	
		for (ItemStack pieceIndex : player.getInventory().getArmorContents()) {
			if (!armorDamage(pieceIndex))
				return;
		}
	}
	public boolean armorDamage(ItemStack item) {
		ItemStackPlus piece = new ItemStackPlus(item);
			if (piece.item.getType() == Material.AIR) {
			return true;
			}

			Short dmg = piece.item.getDurability();
			if (dmg == 0){
			return false;
			} else if (dmg ==1) {
				support.setWarning(false);
				support.setLastWarning(false);
			}
			
			int itemMaxDurability = piece.getMaxDurability();
			
			if (dmg > (itemMaxDurability -3) && AutoRepairPlugin.config.automaticRepair_allow) {
				repair.autoRepairTool(piece);
			} else if (itemMaxDurability <= 100 && dmg > (itemMaxDurability - 20)) {
				support.doWarnOperation(piece, false);
			} else if (itemMaxDurability > 100 && dmg > (itemMaxDurability - 100)) {
				support.doWarnOperation(piece, false);
		}
		return true;
	}


	public AutoRepairPlugin getPlugin() {
		return plugin;
	}
}

