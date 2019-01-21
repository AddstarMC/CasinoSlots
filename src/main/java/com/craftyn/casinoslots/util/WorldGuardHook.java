package com.craftyn.casinoslots.util;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

/**
 * Created for the AddstarMC Project. Created by Narimm on 21/01/2019.
 */
public class WorldGuardHook {
    private WorldGuard worldguard;
    
    public WorldGuardHook(WorldGuard worldguard) {
        this.worldguard = worldguard;
    }
    
    public boolean canBuild(Player player){
        BukkitPlayer wgPlayer = BukkitAdapter.adapt(player);
        RegionQuery q = worldguard.getPlatform().getRegionContainer().createQuery();
        StateFlag.State state = q.queryValue(wgPlayer.getLocation(), (LocalPlayer) wgPlayer, Flags.BUILD);
        return state == StateFlag.State.ALLOW;
    }
    
    public boolean canBuild(Player player, Block block){
        BukkitPlayer wgPlayer = BukkitAdapter.adapt(player);
        RegionQuery q = worldguard.getPlatform().getRegionContainer().createQuery();
        StateFlag.State state = q.queryValue(BukkitAdapter.adapt(block.getLocation()), (LocalPlayer) wgPlayer, Flags.BUILD);
        return state == StateFlag.State.ALLOW;
        
    }
}
