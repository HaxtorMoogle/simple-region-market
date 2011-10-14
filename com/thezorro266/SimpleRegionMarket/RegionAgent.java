package com.thezorro266.simpleregionmarket;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class RegionAgent {
	private Location AgentLocation;
	private String AgentRegion;
	private String AgentOwner;
	private double AgentPrice;

	public RegionAgent(String region, Location loc, String owner, double price) {
		AgentLocation = loc;
		AgentRegion = region;
		if(owner == null) {
			AgentOwner = "";
		} else {
			AgentOwner = owner;
		}
		AgentPrice = price;
	}

	public Location getLocation() {
		return AgentLocation;
	}

	public String getOwner() {
		return AgentOwner;
	}

	public Player getOwnerPlayer() {
		if(!getOwner().isEmpty())
			return Bukkit.getPlayerExact(getOwner());
		return null;
	}

	public double getPrice() {
		return AgentPrice;
	}

	public ProtectedRegion getProtectedRegion() {
		return SimpleRegionMarket.getWorldGuard().getRegionManager(getWorldWorld()).getRegion(getRegion());
	}

	public String getRegion() {
		return AgentRegion;
	}

	public String getWorld() {
		return getLocation().getWorld().getName();
	}

	public World getWorldWorld() {
		return getLocation().getWorld();
	}

	public void destroyAgent(boolean drop) {
		getLocation().getBlock().setType(Material.AIR);
		if(drop) {
			getWorldWorld().dropItem(getLocation(), new ItemStack(Material.SIGN, 1));
		}
	}
}