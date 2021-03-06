package com.craftyn.casinoslots.slot;

import java.util.*;

import com.craftyn.casinoslots.CasinoSlots;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.registry.LegacyMapper;
import org.bukkit.Material;

public class TypeData {
	
	protected CasinoSlots plugin;
	private HashMap<String, Type> types;
	
	// Initialize TypeData
	public TypeData(CasinoSlots plugin) {
		this.plugin = plugin;
	}
	
	// Returns a type
	public Type getType(String name) {
		return types.get(name);
	}
	
	// Returns collection of all types
	public Collection<Type> getTypes() {
		return this.types.values();
	}
	
	// Registers a new type
	public void addType(Type type) {
		String name = type.getName();
		types.put(name, type);
		
		plugin.configData.config.set("types." + type.getName() +".cost", type.getCost());
		plugin.configData.config.set("types." + type.getName() +".create-cost", type.getCreateCost());
		plugin.saveConfig();
	}
	
	public void removeType(String type) {		
		types.remove(type);
		plugin.configData.config.set("types." + type, null);
		plugin.saveConfig();		
	}
	
	/**
	 * Provides a way to refresh the types information.
	 */
	public void reloadTypes() {
		types.clear();
		loadTypes();
	}
	
	// Check if a type exists
	public Boolean isType(String type) {
        return types.containsKey(type);
    }
	
	// Load all types into memory
	public void loadTypes() {		
		this.types = new HashMap<>();
		Integer i = 0;
		
		if(plugin.configData.config.isConfigurationSection("types")) {
			Set<String> types = plugin.configData.config.getConfigurationSection("types").getKeys(false);
			if(!types.isEmpty()) {
				for(String name : types) {
					if (!plugin.configData.config.contains("types." + name + ".messages")) {
						plugin.log("Please make sure your slots in the config file contains 'messages:'.");
						//If there are no "messages", disables the plugin and forces them to check their config
						plugin.error("[CasinoSlots]" + " PLEASE CHECK ONE OF YOUR CONFIG FILES");
						plugin.disablePlugin();
						return;
					}else {
						loadType(name);
						i++;
					}
				}
			}
		}
		plugin.log("Loaded " + i + " types.");
	}
	
	// Load type into memory
	private void loadType(String name) {		
		String path = "types." + name +".";
		
		Double cost = plugin.configData.config.getDouble(path + "cost");
		String itemCost = plugin.configData.config.getString(path + "itemCost", "0");
		Double createCost = plugin.configData.config.getDouble(path + "create-cost");
		ArrayList<Material> reel = getReel(name);
		
		Map<String, String> messages = getMessages(name);
		List<String> helpMessages = plugin.configData.config.getStringList(path + "messages.help");
		Map<String, Reward> rewards = getRewards(name);
		
		Type type = new Type(name, cost, itemCost, createCost, reel, messages, helpMessages, rewards);
		this.types.put(name, type);
	}
	
	// Returns the parsed reel of a type
	private ArrayList<Material> getReel(String type) {
		List<String> reel = plugin.configData.config.getStringList("types." + type + ".reel");
		
		ArrayList<Material> parsedReel = new ArrayList<>();
		for(String m : reel) {
			String[] item = m.split("\\,");
			Material mat = Material.getMaterial(item[0].toUpperCase());
			Integer qty = Integer.parseInt(item[1]);
			if (mat == null) {
				plugin.log("  Reel " + type + " has invalid material " + m + "!");
			} else {
				while (qty > 0) {
					parsedReel.add(mat);
					qty--;
				}
			}
		}
		return parsedReel;
	}
	
	// Returns reward of id
	public Reward getReward(String type, String id) {
		// Split the id so that a damage value is optional
		String materialName;
		String[] idSplit = id.split("\\,");
		Integer dam = null;
			try {
				dam = Integer.parseInt(idSplit[1]);
			}catch (IndexOutOfBoundsException e){}
			if (dam == null) {
				materialName = idSplit[0];
			}else {
				materialName = idSplit[0];
				plugin.log(id +" is not a reward type we no longer use ID's and data..update.");
			}
			
		
		String path = "types." + type + ".rewards." + materialName + ".";
		if(!plugin.configData.config.contains(path)) {
			plugin.log("Could not retrieve a reward for :" + path +" Constructed from: " +type +" + " +id);
			return null;
		}
		String message = plugin.configData.config.getString(path + "message", "Award given!");
		Double money = plugin.configData.config.getDouble(path + "money", 0.0);
		List<String> action = null;
		
		if(plugin.configData.config.isSet(path + "action")) {
			if(plugin.configData.config.isList(path + "action")) {
				if(plugin.configData.inDebug()) plugin.debug("The reward does have the 'action' as a list, so store and get it.");
				action = plugin.configData.config.getStringList(path + "action");
			}else {
				if(plugin.configData.inDebug()) plugin.debug("The reward does have the 'action' but it is only a string, so we get it as a string and store it as a list");
				String a = plugin.configData.config.getString(path + "action");
				action = Collections.singletonList(a);
			}
		}
		
		Reward reward = new Reward(message, money, action);
		return reward;		
	}
	
	// Returns Map of all rewards for this type
	public Map<String, Reward> getRewards(String type) {
		plugin.log("Getting reward entries for: types." + type + ".rewards");
		Set<String> ids = plugin.configData.config.getConfigurationSection("types." + type +".rewards").getKeys(false);
		Map<String, Reward> rewards = new HashMap<>();
		
		for(String itemId : ids) {
			plugin.log("  Processing " + itemId + "...");
			Reward reward = getReward(type, itemId.toLowerCase());
			rewards.put(itemId.toLowerCase(), reward);
		}		
		return rewards;
	}
	
	// Returns map of messages
	private HashMap<String, String> getMessages(String type) {		
		HashMap<String, String> messages = new HashMap<>();
		Double cost = plugin.configData.config.getDouble("types." + type +".cost");

		messages.put("noPermission", plugin.configData.config.getString("types." + type +".messages.insufficient-permission", "You don't have permission to use this slot."));
		messages.put("noFunds", plugin.configData.config.getString("types." + type +".messages.insufficient-funds", "You can't afford to use this."));
		messages.put("inUse", plugin.configData.config.getString("types." + type +".messages.in-use", "This slot machine is already in use."));
		messages.put("noWin", plugin.configData.config.getString("types." + type +".messages.no-win", "No luck this time."));
		messages.put("start", plugin.configData.config.getString("types." + type +".messages.start", "[cost] removed from your account. Lets roll!"));
		
		// Parse shortcodes
		for(Map.Entry<String, String> entry : messages.entrySet()) {
			String message = entry.getValue();
			String key = entry.getKey();
			message = message.replaceAll("\\[cost\\]", "" + cost);
			messages.put(key, message);
		}		
		return messages;
	}
	
	// Returns value of the highest money reward
	public Double getMaxPrize(String type) {		
		Map<String, Reward> rewards = getRewards(type);
		Double max = 0.0;
		
		for(Map.Entry<String, Reward> entry : rewards.entrySet()) {
			Reward reward = entry.getValue();
			Double money = reward.getMoney();
			if(money > max) {
				max = money;
			}
		}
		return max;
	}
	
	/** Sets the item cost that this type also costs, in addition to economy money and saves the config. */
	public void setItemCost(Type type, String itemCost) {
		String path = "types." + type.getName() + ".itemCost";
		plugin.configData.config.set(path, itemCost);
		plugin.saveConfig();
	}
	
	public void newType(String name) {
		String path = "types." + name + ".";
		List<String> reel = Arrays.asList("42,10", "41,5", "57,2");
		List<String> help = Arrays.asList("Instructions:", "Get 3 in a row to win.", "3 iron blocks: $250", "3 gold blocks: $500", "3 diamond blocks: $1200");
		
		plugin.configData.config.set(path + "cost", 100);
		plugin.configData.config.set(path + "create-cost", 1000);
		plugin.configData.config.set(path + "reel", reel);
		
		path = path + "rewards.";
		
		plugin.configData.config.set(path + "42.message", "Winner!");
		plugin.configData.config.set(path + "42.money", 250);
		plugin.configData.config.set(path + "41.message", "Winner!");
		plugin.configData.config.set(path + "41.money", 500);
		plugin.configData.config.set(path + "57.message", "Winner!");
		plugin.configData.config.set(path + "57.money", 1200);
		
		path = "types." + name + ".messages.";
		
		plugin.configData.config.set(path + "insufficient-funds", "You can't afford to use this.");
		plugin.configData.config.set(path + "in-use", "This slot machine is already in use.");
		plugin.configData.config.set(path + "no-win", "No luck this time.");
		plugin.configData.config.set(path + "start", "[cost] removed from your account. Let's roll!");
		plugin.configData.config.set(path + "help", help);
		
		plugin.saveConfig();
		loadType(name);
	}
}