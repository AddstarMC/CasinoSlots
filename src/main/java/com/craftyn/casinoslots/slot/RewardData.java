package com.craftyn.casinoslots.slot;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.world.registry.LegacyMapper;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import com.craftyn.casinoslots.CasinoSlots;

public class RewardData {
	
	private CasinoSlots plugin;
	private static final Random random = new Random();
	
	public RewardData(CasinoSlots plugin) {
		this.plugin = plugin;
	}
	
	// Sends reward to player
	public void send(Player player, Reward reward, Type type) {
		
		if(reward.message != null) {
			plugin.sendMessage(player, reward.message);
		}
		
		if(reward.money != null) {
			if(reward.money < 0)
				plugin.economy.withdrawPlayer(player, Math.abs(reward.money));
			else
				plugin.economy.depositPlayer(player, reward.money);
		}
		
		if(reward.action != null && !reward.action.isEmpty()) {
			executeAction(reward.action, player, type, reward);
		}
	}
		
	// Parses reward actions
	private void executeAction(List<String> actionList, Player p, Type type, Reward reward) {
		if(plugin.configData.inDebug()) plugin.debug("The size of the actionList is: " + actionList.size());		
		for(String action : actionList) {
			String[] a = action.split(" ");
			
			// Give action
			switch (a[0].toLowerCase()){
				case "GIVE":
					String[] itemData = a[1].split("\\,");
					int amount = Integer.parseInt(a[2]);
					
					int item = Integer.parseInt(itemData[0]);
					
					ItemStack is = null;
					Material mat;
					if(itemData.length == 1 || itemData.length == 2) {
						mat = BukkitAdapter.adapt(LegacyMapper.getInstance().getItemFromLegacy(item));
						is = new ItemStack(mat, amount);
					}else if (itemData.length == 3) {
						mat = BukkitAdapter.adapt(LegacyMapper.getInstance().getItemFromLegacy(item));
						is = new ItemStack(mat, amount);
						
						String enID = itemData[1];
						NamespacedKey key = NamespacedKey.minecraft(enID);
						Enchantment enchantment = Enchantment.getByKey(key);
						
						//check if the enchantment is valid
						if (enchantment == null) {
							plugin.severe("There is an invalid enchantment ID for the type " + type.getName());
							continue;
						}
						
						int enLevel = Integer.parseInt(itemData[2]);
						if (enLevel > 127) enLevel = 127;
						if (enLevel < 1) enLevel = enchantment.getMaxLevel();
						
						try {
							is.addUnsafeEnchantment(enchantment, enLevel);
						} catch (Exception e) {
							plugin.severe("Enchanting one of your rewards wasn't successful.");
						}
					}
					
					p.getInventory().addItem(is);
					break;
				case "KILL":
					p.setHealth(0);
					break;
				case "KICK":
					p.kickPlayer("You cheated the Casino!");
					break;
				case "ADDXP":
					int exp = Integer.parseInt(a[1]);
					p.giveExp(exp);
					break;
				case "ADDXPLVL":
					exp = Integer.parseInt(a[1]);
					int oldLvl = p.getLevel();
					int newLvl = oldLvl+exp;
					p.setLevel(newLvl);
					break;
				case "TPTO":
					String[] xyz = a[1].split("\\,");
					World world = p.getWorld();
					Location loc = new Location(world, Integer.parseInt(xyz[0]), Integer.parseInt(xyz[1]), Integer.parseInt(xyz[2]));
					p.teleport(loc);
					break;
				case "SMITE":
					if(a.length == 2) {
						int times = Integer.parseInt(a[1]);
						for(int i = 1; i < times; i++) {
							p.getWorld().strikeLightning(p.getLocation());
						}
					}else
						p.getWorld().strikeLightning(p.getLocation());
					break;
				case "FIRE":
					if(a.length == 2) {
						int ticks = Integer.parseInt(a[1]);
						p.setFireTicks(ticks);
					}else {
						p.setFireTicks(120);
					}
					break;
				case "GOBLIND":
					if(a.length == 2) {
						int ticks = Integer.parseInt(a[1]);
						p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, ticks, 90));
					}else {
						p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 300, 90));
					}
				case "GOCRAZY":
					if(a.length == 2) {
						int ticks = Integer.parseInt(a[1]);
						p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, ticks, 1000));
					}else {
						p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 160, 1000));
					}
					break;
				case "HIGHJUMP":
					if(a.length == 2) {
						int ticks = Integer.parseInt(a[1]);
						p.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, ticks, 2));
					}else {
						p.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 120, 2));
					}
					break;
				case "DIGFAST":
					if(a.length == 2) {
						int ticks = Integer.parseInt(a[1]);
						p.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, ticks, 2));
					}else {
						p.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 400, 2));
					}
					break;
				case "HULKUP":
					if(a.length == 2) {
						int ticks = Integer.parseInt(a[1]);
						p.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, ticks, 15));
					}else {
						p.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 400, 15));
					}
					break;
				case "DRUUGUP":
					p.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 900, 200));
					p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 400, 10));
					break;
				case "SLAP":
					p.setVelocity(new Vector(random.nextDouble() * 2.0 - 1, random.nextDouble() * 1, random.nextDouble() * 2.0 - 1));
					break;
				case "ROCKET":
					p.setVelocity(new Vector(0, 30, 0));
					break;
				case "COMMAND":
					if (a.length < 2) {
						plugin.error("The command action needs something other than 'command' for it to run.");
						continue;
					}
					
					//Initialize the command
					StringBuilder command = null;
					
					//Set the sender of the command as the console
					CommandSender sender = plugin.server.getConsoleSender();
					
					plugin.debug("Full command: " + Arrays.toString(a));
					
					for(String bit : a) {
						//Strip the "command"
						if (bit.equalsIgnoreCase("command")) {
							continue;
						}
						
						//Replace the "null" with the word after command
						if (bit.equalsIgnoreCase(a[1])) {
							command = new StringBuilder(bit);
							continue;
						}
						
						plugin.debug("Part: " + bit);
						
						//Strip [player] and make it equal the player who played the slot
						if (bit.equalsIgnoreCase("[player]")) {
							bit = p.getName();
							plugin.debug("Found [player] string");
						}
						
						// Add the current bit to the command
						command.append(" ").append(bit);
					}
					
					//Check to make sure the command isn't actually null
					if (command != null) {
						plugin.server.dispatchCommand(sender, command.toString());
					}else {
						// if it is, then return an error in the console and don't do anything.
						plugin.error("Couldn't find a command to do, please check your config.yml file.");
					}
					break;
				case "BROADCAST":
					//Check to make sure that there is actually something to broadcast
					if (a.length < 2) {
						plugin.error("The broadcast action needs something other than 'broadcast' for it to run.");
						continue;
					}
					
					//Initiate the message to broadcast
					String message = action.substring("broadcast".length() + 1);
					plugin.debug("Message: " + message);
					
					//Strip [player] and make bit equal to the player who played the slot
					message = message.replace("[player]", p.getName());
					message = message.replace("[cost]", type.getCost().toString());
					message = message.replace("[type]", type.getName());
					message = message.replace("[moneywon]", reward.money.toString());
					
					//Convert all color codes so that Minecraft shows them as color
					message = message.replaceAll("(?i)&([0-9abcdefklmnor])", "\u00A7$1");
					
					//Broadcast the message
					plugin.server.broadcastMessage(message);
					break;
				default:
						return;
			}
			
		}
	}
}