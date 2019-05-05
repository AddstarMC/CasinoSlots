package com.craftyn.casinoslots.slot;

import java.util.*;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.world.registry.LegacyMapper;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import com.craftyn.casinoslots.CasinoSlots;

public class SlotData {
	
	private CasinoSlots plugin;
	private HashMap<String, SlotMachine> slots = new HashMap<>();
	
	public HashMap<Player, SlotMachine> creatingSlots = new HashMap<>();
	public HashMap<Player, SlotMachine> placingController = new HashMap<>();
	public HashMap<Player, SlotMachine> punchingSign = new HashMap<>();
	
	
	// Initialize SlotData
	public SlotData(CasinoSlots plugin) {
		this.plugin = plugin;
	}
	
	// Returns a slot machine
	public SlotMachine getSlot(String name) {
		return this.slots.get(name);
	}
	
	// Returns collection of all slot machines
	public Collection<SlotMachine> getSlots() {
		return this.slots.values();
	}
	
	/**
	 * Returns the amount of slots there are.
	 * 
	 * @return The number of slots.
	 */
	public int getAmountofSlots() {
		return this.slots.size();
	}
	
	// Registers a new slot machine
	public void addSlot(SlotMachine slot) {
		
		String name = slot.getName();
		this.slots.put(name, slot);
		
	}
	
	// Returns true if slot machine exists
	public Boolean isSlot(String name) {
		return this.slots.containsKey(name);
	}
	
	// Removes a slot machine
	public void removeSlot(SlotMachine slot) {
		
		this.slots.remove(slot.getName());
		for(Block b : slot.getBlocks()) {
			b.setType(Material.AIR);
		}
		slot.getController().setType(Material.AIR);
		plugin.configData.slots.set("slots." + slot.getName(), null);
		plugin.configData.saveSlots();
	}
	
	/**
	 * Provides a way to clear the loaded slots and then load them again from the config.
	 */
	public void reloadSlots() {
		slots.clear();
		loadSlots();
	}
	
	
	// Loads all slot machines into memory
	public void loadSlots() {
		
		int i = 0;
		this.slots = new HashMap<>();
		if(plugin.configData.slots.isConfigurationSection("slots")) {
			Set<String> slots = plugin.configData.slots.getConfigurationSection("slots").getKeys(false);
			if(!slots.isEmpty()) {
				for(String name : slots) {
					loadSlot(name);
					i++;
				}
			}
		}
		
		plugin.log("Loaded " + i + " slot machines.");
	}
	
	// Writes slot machine data to disk
	public void saveSlot(SlotMachine slot) {
		
		String path = "slots." + slot.getName() + ".";
		ArrayList<String> xyz = new ArrayList<>();
		
		for(Block b : slot.getBlocks()) {
			xyz.add(b.getX() + "," + b.getY() + "," + b.getZ());
		}
		
		Block con = slot.getController();
		String cXyz = con.getX() + "," + con.getY() + "," + con.getZ();
		
		Block sign = slot.getSign();
		String sXyz;
		if (sign == null) {
			sXyz = null;
		}else {
			sXyz = sign.getX() + "," + sign.getY() + "," + sign.getZ();
		}
		
		plugin.configData.slots.set(path + "name", slot.getName());
		plugin.configData.slots.set(path + "type", slot.getType());
		plugin.configData.slots.set(path + "owner", slot.getOwner());
		plugin.configData.slots.set(path + "world", slot.getWorld());
		plugin.configData.slots.set(path + "sign", sXyz);
		plugin.configData.slots.set(path + "managed", slot.isManaged());
		plugin.configData.slots.set(path + "funds", slot.getFunds());
		plugin.configData.slots.set(path + "item", slot.isItem());
		plugin.configData.slots.set(path + "itemID", slot.getItem());
		plugin.configData.slots.set(path + "itemAmt", slot.getItemAmount());
		plugin.configData.slots.set(path + "controller", cXyz);
		plugin.configData.slots.set(path + "location", xyz);
		
		
		plugin.configData.saveSlots();
	}
	
	// Loads a slot machine into memory
	private void loadSlot(String name) {
		
		String path = "slots." + name + ".";
		
		String type = plugin.configData.slots.getString(path + "type");
		String ownerUUID = plugin.configData.slots.getString(path + "owner");
		String world = plugin.configData.slots.getString(path + "world");
		Boolean managed = plugin.configData.slots.getBoolean(path + "managed");
		Double funds = plugin.configData.slots.getDouble(path + "funds");
		Boolean item = plugin.configData.slots.getBoolean(path + "item", false);
		Material mat = null;
		try {
			String in = plugin.configData.slots.getString(path + "itemType","UNKNOWN");
		if(in !=null){
			Material itemType = Material.matchMaterial(in);
			mat = itemType;
		}
		if(mat == null) {
			int itemID = plugin.configData.slots.getInt(path + "itemID", 0);
			mat = BukkitAdapter.adapt(LegacyMapper.getInstance().getBlockFromLegacy(itemID).getBlockType());
			plugin.log("Configuration is using Item ID please update to use " +path + "itemType");
		}
		}catch (Exception e){
			e.printStackTrace();
		}
		int itemAmt = plugin.configData.slots.getInt(path + "itemAmt", 0);
		ArrayList<Block> blocks = getBlocks(name);
		Block controller = getController(name);
		Block sign = getSign(name);
		UUID uuid = UUID.fromString(ownerUUID);
		OfflinePlayer owner = Bukkit.getOfflinePlayer(uuid);
		//Get the chunks
		String rChunk = getRchunk(blocks);
		String cChunk = getCchunk(controller);
		
		SlotMachine slot = new SlotMachine(plugin, name, type, owner, world, rChunk, cChunk, sign, managed, blocks, controller, funds, item, mat, itemAmt);
		addSlot(slot);
	}

	// Gets reel blocks location from disk
	private ArrayList<Block> getBlocks(String name) {
		
		List<String> xyz = plugin.configData.slots.getStringList("slots." + name + ".location");
		ArrayList<Block> blocks = new ArrayList<>();
		World world = Bukkit.getWorld(plugin.configData.slots.getString("slots." + name + ".world", "world"));
				
		if (world == null) {
			plugin.error("The world for the slot '" + name + "' was null, please fix this and restart the server.");
			plugin.disablePlugin();
			return null;
		}
		
		for(String coord : xyz) {
			String[] b = coord.split("\\,");
			Location loc = new Location(world, Integer.parseInt(b[0]), Integer.parseInt(b[1]), Integer.parseInt(b[2]));
		
				blocks.add(loc.getBlock());
				
				loc.getChunk().load();
		}
		
		return blocks;
	}
	
	// Gets controller block from disk
	private Block getController(String name) {
		
		String location = plugin.configData.slots.getString("slots." + name + ".controller");
		World world = Bukkit.getWorld(plugin.configData.slots.getString("slots." + name + ".world"));
		String[] b = location.split("\\,");
		Location loc = new Location(world, Integer.parseInt(b[0]), Integer.parseInt(b[1]), Integer.parseInt(b[2]));
		
		Block controller = loc.getBlock();

		return controller;
		
	}
	
	private Block getSign(String name) {
		String location = plugin.configData.slots.getString("slots." + name + ".sign");
		
		if(location == null) {
			return null;
		}
		
		World world = Bukkit.getWorld(plugin.configData.slots.getString("slots." + name + ".world"));
		String[] b = location.split("\\,");
		Location loc = new Location(world, Integer.parseInt(b[0]), Integer.parseInt(b[1]), Integer.parseInt(b[2]));
		
		Block sign = loc.getBlock();

		return sign;
	}
	
	private String getRchunk(ArrayList<Block> blocks) {
		Block b = blocks.get(1);
		String c = b.getChunk().getX() + "," + b.getChunk().getZ();
		
		return c;
	}
	
	private String getCchunk (Block c) {
		String chunk = c.getChunk().getX() + "," + c.getChunk().getZ();
		
		return chunk;
	}
	
	// Creates the slot machine in the world
	public void createReel(Player player, BlockFace face, SlotMachine slot) {
		
		Block center = player.getTargetBlock(null, 0);
		ArrayList<Block> blocks = new ArrayList<>();
		
		slot.setReelChunk(center.getChunk().getX() + "," + center.getChunk().getZ());
		
		for(int i = 0; i < 3; i++) {
			blocks.add(center.getRelative(getDirection(face, "left"), 2));
			blocks.add(center);
			blocks.add(center.getRelative(getDirection(face, "right"), 2));
			center = center.getRelative(BlockFace.UP, 1);
		}
		
		for(Block b : blocks) {
			if(plugin.configData.inDebug()) {
				if(blocks.get(0) == b || blocks.get(1) == b || blocks.get(2) == b) {
					b.setType(BukkitAdapter.adapt(LegacyMapper.getInstance().getBlockFromLegacy(57).getBlockType()));
				}else if(blocks.get(3) == b || blocks.get(4) == b || blocks.get(5) == b) {
					b.setType(BukkitAdapter.adapt(LegacyMapper.getInstance().getBlockFromLegacy(42).getBlockType()));
				}else if(blocks.get(6) == b || blocks.get(7) == b || blocks.get(8) == b) {
					b.setType(BukkitAdapter.adapt(LegacyMapper.getInstance().getBlockFromLegacy(41).getBlockType()));
				}
					
			}else
				b.setType(BukkitAdapter.adapt(LegacyMapper.getInstance().getBlockFromLegacy(57).getBlockType()));
		}
		
		slot.setBlocks(blocks);
		
	}
	
	// Used for orienting the slot machine correctly
	public BlockFace getDirection(BlockFace face, String direction) {

		switch (face) {
			case NORTH:
				if (direction.equalsIgnoreCase("left")) {
					return BlockFace.EAST;
				} else if (direction.equalsIgnoreCase("right")) {
					return BlockFace.WEST;
				}
				break;
			case SOUTH:
				if (direction.equalsIgnoreCase("left")) {
					return BlockFace.WEST;
				} else if (direction.equalsIgnoreCase("right")) {
					return BlockFace.EAST;
				}
				break;
			case WEST:
				if (direction.equalsIgnoreCase("left")) {
					return BlockFace.SOUTH;
				} else if (direction.equalsIgnoreCase("right")) {
					return BlockFace.NORTH;
				}
				break;
			case EAST:
				if (direction.equalsIgnoreCase("left")) {
					return BlockFace.NORTH;
				} else if (direction.equalsIgnoreCase("right")) {
					return BlockFace.SOUTH;
				}
				break;
		}
		return BlockFace.SELF;
		
	}
	
	// If a player is creating slot machine
	public Boolean isCreatingSlots(Player player) {

		return creatingSlots.containsKey(player);

	}
	
	// If a player is placing controller
	public Boolean isPlacingController(Player player) {

		return placingController.containsKey(player);

	}
	
	// If a player is placing controller
	public Boolean isPunchingSign(Player player) {

		return punchingSign.containsKey(player);

	}
	
	// Toggles creating slots
	public void toggleCreatingSlots(Player player, SlotMachine slot) {
		
		if(this.creatingSlots.containsKey(player)) {
			this.creatingSlots.remove(player);
		}
		else {
			this.creatingSlots.put(player, slot);
		}
	}
	
	// Toggles placing controller
	public void togglePlacingController(Player player, SlotMachine slot) {
		
		if(this.placingController.containsKey(player)) {
			this.placingController.remove(player);
		}
		else {
			this.placingController.put(player, slot);
		}
	}
	
	// Toggles creating a sign for the slot
	public void togglePunchingSign(Player player, SlotMachine slot) {
		
		if(this.punchingSign.containsKey(player)) {
			this.punchingSign.remove(player);
		}else {
			this.punchingSign.put(player, slot);
		}
	}

}