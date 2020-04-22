package com.craftyn.casinoslots.slot.game;

import java.util.ArrayList;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;

public class RotateTask implements Runnable {
	
	private Game game;
	private Integer i;
	
	// Task for rotating one column
	public RotateTask(Game game, Integer i) {
	
		this.game = game;
		this.i = i;
		
	}
	
	// The task itself
	public void run() {
		rotateColumn(i);	
	}
	//todo 1.13 FIX
	// Rotates one column one block
	private void rotateColumn(Integer column) {
		
		ArrayList<Block> blocks = game.getSlot().getBlocks();
		
		ArrayList<Material> last = new ArrayList<>();
		last.add(blocks.get(column+6).getType());
		last.add(blocks.get(column+3).getType());
		
		Material id = getNext();
		
		// Prevent silly-looking duplicate blocks
		while(id.equals(last.get(0))) {
			id = getNext();
		}
		
		// First column
		blocks.get(column+6).setType(id);
		
		// Second Column
		blocks.get(column+3).setType(last.get(0));
		
		// Third Column
		blocks.get(column).setType(last.get(1));
		
	}
	
	// Gets the next block in the reel
	private Material getNext() {
		ArrayList<Material> reel = game.getType().getReel();

		Random generator = new Random();
		int id = generator.nextInt(reel.size());
		
		Material next = reel.get(id);
		return next;
	}
}