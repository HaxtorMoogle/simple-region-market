/*
 * 
 */
package com.thezorro266.simpleregionmarket;

import java.util.Date;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;

// TODO: Auto-generated Javadoc
/**
 * The Class SignAgent.
 */
public class SignAgent {
	
	/** The MOD e_ ren t_ hotel. */
	public static int MODE_RENT_HOTEL = 1;
	
	/** The MOD e_ sel l_ region. */
	public static int MODE_SELL_REGION = 0;
	
	/** The account. */
	private final String account;
	
	/** The expiredate. */
	private Date expiredate;
	
	/** The location. */
	private final Location location;
	
	/** The mode. */
	private final int mode;
	
	/** The price. */
	private final double price;
	
	/** The region. */
	private final String region;
	
	/** The rentby. */
	private String rentby;

	/** The renttime. */
	private final long renttime;
	
	/** The world. */
	private final String world;

	/**
	 * Instantiates a new sign agent.
	 *
	 * @param mode the mode
	 * @param location the location
	 * @param region the region
	 * @param price the price
	 * @param account the account
	 * @param renttime the renttime
	 */
	public SignAgent(int mode, Location location, String region, double price, String account, long renttime) {
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

	/**
	 * Destroy agent.
	 *
	 * @param drop the drop
	 */
	public void destroyAgent(boolean drop) {
		getLocation().getBlock().setType(Material.AIR);
		if (drop) {
			getWorldWorld().dropItem(getLocation(), new ItemStack(Material.SIGN, 1));
		}
	}

	/**
	 * Gets the account.
	 *
	 * @return the account
	 */
	public String getAccount() {
		return account;
	}

	/**
	 * Gets the expire date.
	 *
	 * @return the expire date
	 */
	public Date getExpireDate() {
		return expiredate;
	}

	/**
	 * Gets the location.
	 *
	 * @return the location
	 */
	public Location getLocation() {
		return location;
	}

	/**
	 * Gets the mode.
	 *
	 * @return the mode
	 */
	public int getMode() {
		return mode;
	}

	/**
	 * Gets the price.
	 *
	 * @return the price
	 */
	public double getPrice() {
		return price;
	}

	/**
	 * Gets the protected region.
	 *
	 * @return the protected region
	 */
	public ProtectedRegion getProtectedRegion() {
		return SimpleRegionMarket.getWorldGuard().getRegionManager(getWorldWorld()).getRegion(getRegion());
	}

	/**
	 * Gets the region.
	 *
	 * @return the region
	 */
	public String getRegion() {
		return region;
	}

	/**
	 * Gets the rent.
	 *
	 * @return the rent
	 */
	public String getRent() {
		return rentby;
	}

	/**
	 * Gets the rent time.
	 *
	 * @return the rent time
	 */
	public long getRentTime() {
		return renttime;
	}

	/**
	 * Gets the world.
	 *
	 * @return the world
	 */
	public String getWorld() {
		return world;
	}

	/**
	 * Gets the world world.
	 *
	 * @return the world world
	 */
	public World getWorldWorld() {
		return getLocation().getWorld();
	}

	/**
	 * Checks if is rent.
	 *
	 * @return true, if is rent
	 */
	public boolean isRent() {
		return !getRent().isEmpty();
	}

	/**
	 * On wall.
	 *
	 * @return true, if successful
	 */
	public boolean onWall() {
		return getLocation().getBlock().getType() == Material.WALL_SIGN;
	}

	/**
	 * Rent to.
	 *
	 * @param playername the playername
	 */
	public void rentTo(String playername) {
		if (!playername.isEmpty()) {
			expiredate = new Date(System.currentTimeMillis() + getRentTime());
		} else {
			expiredate = null;
		}
		rentby = playername;
	}

	/**
	 * Rent to.
	 *
	 * @param playername the playername
	 * @param expiredate the expiredate
	 */
	public void rentTo(String playername, Date expiredate) {
		rentby = playername;
		this.expiredate = expiredate;
	}

	/**
	 * Sets the expire date.
	 *
	 * @param expiredate the new expire date
	 */
	public void setExpireDate(Date expiredate) {
		this.expiredate = expiredate;
	}
}
