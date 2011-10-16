package com.thezorro266.simpleregionmarket;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import java.util.Date;

public class SignAgent {
	private int mode;
	private Location location;
	private String world;
	private String region;
	private double price;
	private String account;
	private long renttime;
	private String rentby;
	private Date expiredate;
	
	public static int MODE_SELL_REGION = 0;
	public static int MODE_RENT_HOTEL = 1;

	public SignAgent(int mode, Location location, String region, double price, String account, long renttime) {
		this.mode = mode;
		this.location = location;
		this.world = location.getWorld().getName();
		this.region = region;
		this.price = price;
		this.account = account;
		this.renttime = renttime;
		this.rentby = "";
		this.expiredate = null;
	}

	// --- START Methods About Variables ---

	public int getMode() {
		return mode;
	}

	public Location getLocation() {
		return location;
	}

	public String getWorld() {
		return world;
	}

	public World getWorldWorld() {
		return getLocation().getWorld();
	}

	public String getRegion() {
		return region;
	}
	
	public ProtectedRegion getProtectedRegion() {
		return SimpleRegionMarket.getWorldGuard().getRegionManager(getWorldWorld()).getRegion(getRegion());
	}

	public double getPrice() {
		return price;
	}

	public String getAccount() {
		return account;
	}
	
	public long getRentTime() {
		return renttime;
	}
	
	public String getRent() {
		return rentby;
	}
	
	public boolean isRent() {
		return (!getRent().isEmpty());
	}
	
	public void rentTo(String playername) {
		if(!playername.isEmpty()) {
			expiredate = new Date(System.currentTimeMillis()+getRentTime());
		} else {
			expiredate = null;
		}
		rentby = playername;
	}
	
	public void rentTo(String playername, Date expiredate) {
		rentby = playername;
		this.expiredate = expiredate;
	}
	
	public Date getExpireDate() {
		return expiredate;
	}

	public void setExpireDate(Date expiredate) {
		this.expiredate = expiredate;
	}
	
	// --- END Methods About Variables ---
	
	public boolean onWall() {
		return (getLocation().getBlock().getType() == Material.WALL_SIGN);
	}

	public void destroyAgent(boolean drop) {
		getLocation().getBlock().setType(Material.AIR);
		if(drop) {
			getWorldWorld().dropItem(getLocation(), new ItemStack(Material.SIGN, 1));
		}
	}
}