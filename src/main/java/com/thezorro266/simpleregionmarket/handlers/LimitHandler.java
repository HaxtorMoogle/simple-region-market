package com.thezorro266.simpleregionmarket.handlers;

/*
 * 
 */

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.thezorro266.simpleregionmarket.SimpleRegionMarket;

public class LimitHandler {
	private static YamlConfiguration limitConfig;

	// private static Map<String, Integer> limitregiongroups = new
	// HashMap<String, Integer>(); // Limits regions per groups - Mid Priority
	/** The limitregionplayers. */
	private static Map<String, Integer> limitregionplayers = new HashMap<String, Integer>(); // Limits
	// regions
	// per
	// player
	// -
	// High
	// Priority
	/** The limitregions. */
	private static int limitregions = -1;

	/** The limitregionworlds. */
	private static Map<String, Integer> limitregionworlds = new HashMap<String, Integer>(); // Limits
	// regions
	// per
	// world
	// -
	// Low
	// Priority

	// private static Map<String, Integer> limitroomgroups = new HashMap<String,
	// Integer>(); // Limits rooms per groups - Mid Priority
	/** The limitroomplayers. */
	private static Map<String, Integer> limitroomplayers = new HashMap<String, Integer>(); // Limits
	// rooms
	// per
	// player
	// -
	// High
	// Priority
	/** The limitrooms. */
	private static int limitrooms = -1;

	/** The limitroomworlds. */
	private static Map<String, Integer> limitroomworlds = new HashMap<String, Integer>(); // Limits
	// rooms
	// per
	// world
	// -
	// Low
	// Priority

	// TODO Parent region limits

	/** The lang handler. */
	private final LanguageHandler langHandler;

	/**
	 * Instantiates a new limit handler.
	 * 
	 * @param plugin
	 *            the plugin
	 * @param langHandler
	 *            the lang handler
	 */
	public LimitHandler(SimpleRegionMarket plugin, LanguageHandler langHandler) {
		this.langHandler = langHandler;
	}

	/**
	 * Count player own region.
	 * 
	 * @param p
	 *            the p
	 * @return the int
	 */
	// TODO New count system per template
	public int countPlayerOwnRegion(Player p) {
		/*
		 * if (p != null) { final WorldGuardPlugin tmp = SimpleRegionMarket.getWorldGuard(); return
		 * tmp.getGlobalRegionManager().get(p.getWorld()).getRegionCountOfPlayer(tmp.wrapPlayer(p)); }
		 */
		return 0;
	}

	/**
	 * Count player rent room.
	 * 
	 * @param p
	 *            the p
	 * @return the int
	 */
	public int countPlayerRentRoom(Player p) {
		final int count = 0;
		/*
		 * for (int i = 0; i < plugin.getAgentManager().getAgentList().size(); i++) { final SignAgent now = plugin.getAgentManager().getAgentList().get(i); if
		 * (now != null && now.getMode() == SignAgent.MODE_RENT_HOTEL && now.getRent().equalsIgnoreCase(p.getName())) { count++; } }
		 */
		return count;
	}

	/**
	 * Gets the buy player limit.
	 * 
	 * @param p
	 *            the p
	 * @return the buy player limit
	 */
	public int getBuyPlayerLimit(Player p) {
		final String playername = p.getName().toLowerCase();
		if (limitregionplayers.get(playername) != null) {
			return limitregionplayers.get(playername);
		} else {
			return -1;
		}
	}

	/**
	 * Gets the buy world limit.
	 * 
	 * @param w
	 *            the w
	 * @return the buy world limit
	 */
	public int getBuyWorldLimit(World w) {
		final String worldname = w.getName().toLowerCase();
		if (limitregionworlds.get(worldname) != null) {
			return limitregionworlds.get(worldname);
		} else {
			return -1;
		}
	}

	/**
	 * Gets the global buy limit.
	 * 
	 * @return the global buy limit
	 */
	public int getGlobalBuyLimit() {
		return limitregions;
	}

	/**
	 * Gets the global rent limit.
	 * 
	 * @return the global rent limit
	 */
	public int getGlobalRentLimit() {
		return limitrooms;
	}

	/**
	 * Gets the rent player limit.
	 * 
	 * @param p
	 *            the p
	 * @return the rent player limit
	 */
	public int getRentPlayerLimit(Player p) {
		final String playername = p.getName().toLowerCase();
		if (limitroomplayers.get(playername) != null) {
			return limitroomplayers.get(playername);
		} else {
			return -1;
		}
	}

	/**
	 * Gets the rent world limit.
	 * 
	 * @param w
	 *            the w
	 * @return the rent world limit
	 */
	public int getRentWorldLimit(World w) {
		final String worldname = w.getName().toLowerCase();
		if (limitroomworlds.get(worldname) != null) {
			return limitroomworlds.get(worldname);
		} else {
			return -1;
		}
	}

	/**
	 * Limit can buy.
	 * 
	 * @param p
	 *            the p
	 * @return true, if successful
	 */
	public boolean limitCanBuy(Player p) {
		if (p != null) {
			final String playername = p.getName().toLowerCase();
			final String worldname = p.getWorld().getName().toLowerCase();
			if (limitregionplayers.containsKey(playername)) {
				return countPlayerOwnRegion(p) < limitregionplayers.get(playername);
			} else if (p.getWorld() != null && limitregionworlds.containsKey(worldname) && limitregionworlds.get(worldname) != -1) {
				return countPlayerOwnRegion(p) < limitregionworlds.get(worldname);
			} else if (limitregions != -1) {
				return countPlayerOwnRegion(p) < limitregions;
			}
			return true;
		}
		return false;
	}

	/**
	 * Limit can rent.
	 * 
	 * @param p
	 *            the p
	 * @return true, if successful
	 */
	public boolean limitCanRent(Player p) {
		if (p != null) {
			final String playername = p.getName().toLowerCase();
			final String worldname = p.getWorld().getName().toLowerCase();
			if (limitroomplayers.containsKey(playername)) {
				return countPlayerRentRoom(p) < limitroomplayers.get(playername);
			} else if (p.getWorld() != null && limitroomworlds.containsKey(worldname) && limitroomworlds.get(worldname) != -1) {
				return countPlayerRentRoom(p) < limitroomworlds.get(worldname);
			} else if (limitrooms != -1) {
				return countPlayerRentRoom(p) < limitrooms;
			}
			return true;
		}
		return false;
	}

	/**
	 * Load limits.
	 */
	public void loadLimits() {
		limitConfig = YamlConfiguration.loadConfiguration(new File(SimpleRegionMarket.getPluginDir() + "limits.yml"));

		ConfigurationSection path;
		for (final String main : limitConfig.getKeys(false)) {
			path = limitConfig.getConfigurationSection(main);
			for (final String limiter : path.getKeys(false)) {
				if (limiter.equalsIgnoreCase("global")) {
					if (main.equalsIgnoreCase("regions")) {
						limitregions = path.getInt(limiter, -1);
					} else if (main.equalsIgnoreCase("rooms")) {
						limitrooms = path.getInt(limiter, -1);
					}
					continue;
				}
				path = limitConfig.getConfigurationSection(main).getConfigurationSection(limiter);
				for (final String key : path.getKeys(false)) {
					if (main.equalsIgnoreCase("regions")) {
						if (limiter.equalsIgnoreCase("worlds")) {
							limitregionworlds.put(key.toLowerCase(), path.getInt(key));
						} else if (limiter.equalsIgnoreCase("players")) {
							limitregionplayers.put(key.toLowerCase(), path.getInt(key));
						}
					} else if (main.equalsIgnoreCase("rooms")) {
						if (limiter.equalsIgnoreCase("worlds")) {
							limitroomworlds.put(key.toLowerCase(), path.getInt(key));
						} else if (limiter.equalsIgnoreCase("players")) {
							limitroomplayers.put(key.toLowerCase(), path.getInt(key));
						}
					}
				}
			}
		}
	}

	/**
	 * Save limits.
	 */
	public void saveLimits() {
		limitConfig = new YamlConfiguration();

		limitConfig.createSection("regions");
		limitConfig.set("regions.global", limitregions);

		limitConfig.createSection("regions.worlds");
		for (final String key : limitregionworlds.keySet()) {
			limitConfig.set("regions.worlds." + key, limitregionworlds.get(key));
		}

		limitConfig.createSection("regions.players");
		for (final String key : limitregionplayers.keySet()) {
			limitConfig.set("regions.players." + key, limitregionplayers.get(key));
		}

		limitConfig.createSection("rooms");
		limitConfig.set("rooms.global", limitrooms);

		limitConfig.createSection("rooms.worlds");
		for (final String key : limitroomworlds.keySet()) {
			limitConfig.set("rooms.worlds." + key, limitroomworlds.get(key));
		}

		limitConfig.createSection("rooms.players");
		for (final String key : limitroomplayers.keySet()) {
			limitConfig.set("rooms.players." + key, limitroomplayers.get(key));
		}

		try {
			limitConfig.save(SimpleRegionMarket.getPluginDir() + "limits.yml");
		} catch (final IOException e) {
			langHandler.outputConsole(Level.SEVERE, "Could not save limits.");
		}
	}

	/**
	 * Sets the buy player limit.
	 * 
	 * @param p
	 *            the p
	 * @param limit
	 *            the limit
	 */
	public void setBuyPlayerLimit(Player p, int limit) {
		if (p != null) {
			if (limit < -1) {
				limit = -1;
			}

			final String playername = p.getName().toLowerCase();
			limitregionplayers.put(playername, limit);
		}
	}

	/**
	 * Sets the buy world limit.
	 * 
	 * @param w
	 *            the w
	 * @param limit
	 *            the limit
	 */
	public void setBuyWorldLimit(World w, int limit) {
		if (w != null) {
			if (limit < -1) {
				limit = -1;
			}

			final String worldname = w.getName().toLowerCase();
			limitregionworlds.put(worldname, limit);
		}
	}

	/**
	 * Sets the global buy limit.
	 * 
	 * @param limit
	 *            the new global buy limit
	 */
	public void setGlobalBuyLimit(int limit) {
		if (limit < -1) {
			limit = -1;
		}

		limitregions = limit;
	}

	/**
	 * Sets the global rent limit.
	 * 
	 * @param limit
	 *            the new global rent limit
	 */
	public void setGlobalRentLimit(int limit) {
		if (limit < -1) {
			limit = -1;
		}

		limitrooms = limit;
	}

	/**
	 * Sets the rent player limit.
	 * 
	 * @param p
	 *            the p
	 * @param limit
	 *            the limit
	 */
	public void setRentPlayerLimit(Player p, int limit) {
		if (p != null) {
			if (limit < -1) {
				limit = -1;
			}

			final String playername = p.getName().toLowerCase();
			limitroomplayers.put(playername, limit);
		}
	}

	/**
	 * Sets the rent world limit.
	 * 
	 * @param w
	 *            the w
	 * @param limit
	 *            the limit
	 */
	public void setRentWorldLimit(World w, int limit) {
		if (w != null) {
			if (limit < -1) {
				limit = -1;
			}

			final String worldname = w.getName().toLowerCase();
			limitroomworlds.put(worldname, limit);
		}
	}
}
