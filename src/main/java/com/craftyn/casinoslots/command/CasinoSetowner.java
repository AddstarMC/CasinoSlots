package com.craftyn.casinoslots.command;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import com.craftyn.casinoslots.CasinoSlots;
import com.craftyn.casinoslots.slot.SlotMachine;

public class CasinoSetowner extends AnCommand {
	
	// Command for setting the owner of a managed slot machine
	public CasinoSetowner(CasinoSlots plugin, String[] args, Player player) {
		super(plugin, args, player);
	}
	
	public Boolean process() {		
		// Correct command format
		if(args.length == 3) {
			
			// Slot exists
			if(plugin.slotData.isSlot(args[1])) {
				SlotMachine slot = plugin.slotData.getSlot(args[1]);
				
				// Can access slot
				if(isOwner(slot)) {
					OfflinePlayer owner = Bukkit.getOfflinePlayer(args[2]);
					if(owner == null) {
						UUID uuid = UUID.fromString(args[2]);
						owner = Bukkit.getOfflinePlayer(uuid);
					}
					if(owner == null){
						sendMessage(args[2] + " is not a valid owner of " + args[1] + " slot machine.");
					}else {
						slot.setOwner(owner.getUniqueId());
						sendMessage(owner + " is now the owner of the " + args[1] + " slot machine.");
					}
			}
				// No access
				else {
					sendMessage("You do not own this slot machine.");
				}
			}
			
			// Slot does not exist
			else {
				sendMessage("Invalid slot machine.");
			}
		}
		
		// Incorrect command format
		else {
			sendMessage("Usage:");
			sendMessage("/casino setowner <slotname> <owner>");
		}
		return true;
	}

}