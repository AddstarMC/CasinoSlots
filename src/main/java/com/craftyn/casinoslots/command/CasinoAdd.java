package com.craftyn.casinoslots.command;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import com.craftyn.casinoslots.CasinoSlots;
import com.craftyn.casinoslots.slot.SlotMachine;

public class CasinoAdd extends AnCommand {
	
	private String name;
	private String type;
	private OfflinePlayer owner;
	private String world;
	
	// Command for adding unmanaged slot machine
	public CasinoAdd(CasinoSlots plugin, String[] args, Player player) {
		super(plugin, args, player);
	}
	
	public Boolean process() {
		
		// Permissions
		if(!plugin.permission.canCreate(player)) {
			noPermission();
			return true;
		}

		// Valid command format
		if(args.length >= 2 && args.length <= 3) {
						
			// Slot does not exist
			if(!plugin.slotData.isSlot(args[1])) {
							
				this.name = args[1];
								
					// Valid type
				if(args.length < 3) {
					
					type = "default";
				}
				
				else if(plugin.typeData.isType(args[2])) {
					String typeName = args[2];
					
					// Has type permission
					if(!plugin.permission.canCreate(player, typeName)) {
						sendMessage("Invalid type " + typeName);
						return true;
					}
					else {
						type = typeName;
					}
				}
				
				// Invalid type
				else {
					sendMessage("Invalid type " + args[2]);
					return true;
				}
				
				owner = player;
				
				// Creation cost
				Double createCost = plugin.typeData.getType(type).getCreateCost();
				if(plugin.economy.has(player, createCost)) {
					plugin.economy.withdrawPlayer(player, createCost);
				}
				else {
					sendMessage(player.getName());
					sendMessage("You can't afford to create this slot machine. Cost: " + createCost);
					return true;
				}
				
				world = player.getWorld().getName();
				
				//Good to start punching the blocks to create the slot.
				SlotMachine slot = new SlotMachine(plugin, name, type, owner, world, false, false, Material.AIR, 0);
				plugin.slotData.toggleCreatingSlots(player, slot);
				sendMessage("Punch a block to serve as the base for this slot machine.");
			}
			
			// Slot exists
			else {
				sendMessage("Slot machine " + args[1] +" already registered.");
			}
		}
		
		// incorrect command format
		else {
			sendMessage("Usage:");
			sendMessage("/casino add <name> (<type>)");
		}
		return true;

	}

}