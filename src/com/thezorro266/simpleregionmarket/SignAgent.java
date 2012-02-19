package com.thezorro266.simpleregionmarket;

import java.util.Date;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class SignAgent {
	public static int MODE_RENT_HOTEL = 1;
	public static int MODE_SELL_REGION = 0;
	private final String account;
	private Date expiredate;
	private final Location location;
	private final int mode;
	private final double price;
	private final String region;
	private String rentby;

	private final long renttime;
	private final String world;

	public SignAgent(int mode, Location location, String region, double price,
			String account, long renttime) {
		this.mode = mode;
		this.location = location;
		world = location.getWorld().getName();
		this.region = region;
		this.price = price;
		this.account = account;
		this.renttime = renttime;
		rentby = "";
		expiredate = null;
	}

	// --- START Methods About Variables ---

	public void destroyAgent(boolean drop) {
		getLocation().getBlock().setType(Material.AIR);
		if (drop) {
			getWorldWorld().dropItem(getLocation(),
					new ItemStack(Material.SIGN, 1));
		}
	}

	public String getAccount() {
		return account;
	}

	public Date getExpireDate() {
		return expiredate;
	}

	public Location getLocation() {
		return location;
	}

	public int getMode() {
		return mode;
	}

	public double getPrice() {
		return price;
	}

	public ProtectedRegion getProtectedRegion() {
		return SimpleRegionMarket.getWorldGuard()
				.getRegionManager(getWorldWorld()).getRegion(getRegion());
	}

	public String getRegion() {
		return region;
	}

	public String getRent() {
		return rentby;
	}

	public long getRentTime() {
		return renttime;
	}

	public String getWorld() {
		return world;
	}

	public World getWorldWorld() {
		return getLocation().getWorld();
	}

	public boolean isRent() {
		return !getRent().isEmpty();
	}

	public boolean onWall() {
		return getLocation().getBlock().getType() == Material.WALL_SIGN;
	}

	public void rentTo(String playername) {
		if (!playername.isEmpty()) {
			expiredate = new Date(System.currentTimeMillis() + getRentTime());
		} else {
			expiredate = null;
		}
		rentby = playername;
	}

	// --- END Methods About Variables ---

	public void rentTo(String playername, Date expiredate) {
		rentby = playername;
		this.expiredate = expiredate;
	}

	public void setExpireDate(Date expiredate) {
		this.expiredate = expiredate;
	}
}