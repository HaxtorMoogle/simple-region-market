package com.thezorro266.simpleregionmarket.handlers;

import java.io.File;
import java.util.HashMap;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.thezorro266.simpleregionmarket.SimpleRegionMarket;
import com.thezorro266.simpleregionmarket.TokenManager;
import com.thezorro266.simpleregionmarket.signs.TemplateMain;

public class LimitHandler {
	private final static String LIMITS_NAME = "limits.yml";
	private final static File LIMITS_FILE = new File(SimpleRegionMarket.getPluginDir() + LIMITS_NAME);

	private final LanguageHandler langHandler;

	private final HashMap<String, Object> limitEntries = new HashMap<String, Object>();

	public LimitHandler(SimpleRegionMarket plugin, LanguageHandler langHandler, TokenManager tokenManager) {
		this.langHandler = langHandler;
		load();
	}

	public void load() {
		final YamlConfiguration configHandle = YamlConfiguration.loadConfiguration(LIMITS_FILE);

		for (final String key : configHandle.getKeys(true)) {
			limitEntries.put(key, configHandle.get(key));
		}
	}

	public boolean checkPlayerGlobal(Player player) {
		if (getLimitEntry("global.global") != -1) {
			return (countPlayerGlobalRegions(player) < getLimitEntry("global.global"));
		}
		return true;
	}

	public boolean checkPlayerToken(Player player, TemplateMain token) {
		if (getLimitEntry(token.id + ".global") != -1) {
			return (countPlayerTokenRegions(player, token) < getLimitEntry(token.id + ".global"));
		}
		return true;
	}

	public boolean checkPlayerGlobalWorld(Player player, String world) {
		if (getLimitEntry("global.world." + world) != -1) {
			return (countPlayerGlobalWorldRegions(player, world) < getLimitEntry("global.world." + world));
		}
		return true;
	}

	public boolean checkPlayerTokenWorld(Player player, TemplateMain token, String world) {
		if (getLimitEntry(token.id + ".world." + world) != -1) {
			return (countPlayerWorldRegions(player, token, world) < getLimitEntry(token.id + ".world." + world));
		}
		return true;
	}

	public boolean checkPlayerGlobalRegion(Player player, ProtectedRegion parentRegion) {
		if (getLimitEntry("global.parentregion." + parentRegion.getId()) != -1) {
			return (countPlayerGlobalChildRegions(player, parentRegion) < getLimitEntry("global.parentregion." + parentRegion.getId()));
		}
		return true;
	}

	public boolean checkPlayerTokenRegion(Player player, TemplateMain token, ProtectedRegion parentRegion) {
		if (getLimitEntry(token.id + ".parentregion." + parentRegion.getId()) != -1) {
			return (countPlayerChildRegions(player, token, parentRegion) < getLimitEntry(token.id + ".parentregion." + parentRegion.getId()));
		}
		return true;
	}

	/**
	 * Returns a count from template, parentRegion for regions, where player is owner
	 * 
	 * @param player
	 * @param token
	 *            template, where to count regions from
	 * @param parentRegion
	 * @return the count of all regions from the template in the parent region owned by the player
	 */
	public int countPlayerChildRegions(Player player, TemplateMain token, ProtectedRegion parentRegion) {
		int count = 0;
		for (final String world : token.entries.keySet()) {
			for (final String region : token.entries.get(world).keySet()) {
				final ProtectedRegion childRegion = SimpleRegionMarket.wgManager.getProtectedRegion(Bukkit.getWorld(world), region);
				if (childRegion != null && childRegion.getParent().equals(parentRegion)) {
					if (token.isRegionOwner(player, world, region)) {
						count++;
					}
				}
			}
		}
		langHandler.outputConsole(Level.INFO, "Counting " + count + " regions for player " + player.getName() + " for parent region " + parentRegion.getId()
				+ " on template " + token.id + ".");
		return count;
	}

	/**
	 * Returns a global count for regions with the parent region parentRegion, where player is owner
	 * 
	 * @param player
	 * @param parentRegion
	 * @return the count of all regions with the parent region owned by the player
	 */
	public int countPlayerGlobalChildRegions(Player player, ProtectedRegion parentRegion) {
		int count = 0;
		for (final TemplateMain token : TokenManager.tokenList) {
			count += countPlayerChildRegions(player, token, parentRegion);
		}
		langHandler.outputConsole(Level.INFO, "Counting " + count + " regions for player " + player.getName() + " for parent region " + parentRegion.getId()
				+ ".");
		return count;
	}

	/**
	 * Returns a count from template, world for regions, where player is owner
	 * 
	 * @param player
	 * @param token
	 *            template, where to count regions from
	 * @param world
	 * @return the count of all regions from the template in the world owned by the player
	 */
	public int countPlayerWorldRegions(Player player, TemplateMain token, String world) {
		int count = 0;
		for (final String region : token.entries.get(world).keySet()) {
			final ProtectedRegion protectedRegion = SimpleRegionMarket.wgManager.getProtectedRegion(Bukkit.getWorld(world), region);
			if (protectedRegion != null) {
				if (token.isRegionOwner(player, world, region)) {
					count++;
				}
			}
		}
		langHandler.outputConsole(Level.INFO, "Counting " + count + " regions for player " + player.getName() + " in world " + world + " on template "
				+ token.id + ".");
		return count;
	}

	/**
	 * Returns a global count for regions in world, where player is owner
	 * 
	 * @param player
	 * @param world
	 * @return the count of all regions in the world owned by the player
	 */
	public int countPlayerGlobalWorldRegions(Player player, String world) {
		int count = 0;
		for (final TemplateMain token : TokenManager.tokenList) {
			count += countPlayerWorldRegions(player, token, world);
		}
		langHandler.outputConsole(Level.INFO, "Counting " + count + " regions for player " + player.getName() + " in the world " + world + ".");
		return count;
	}

	/**
	 * Returns a count per template for regions, where player is owner
	 * 
	 * @param player
	 * @param token
	 *            template, where to count regions from
	 * @return the count of all regions from the template owned by the player
	 */
	public int countPlayerTokenRegions(Player player, TemplateMain token) {
		int count = 0;
		for (final String world : token.entries.keySet()) {
			for (final String region : token.entries.get(world).keySet()) {
				final ProtectedRegion protectedRegion = SimpleRegionMarket.wgManager.getProtectedRegion(Bukkit.getWorld(world), region);
				if (protectedRegion != null) {
					if (token.isRegionOwner(player, world, region)) {
						count++;
					}
				}
			}
		}
		langHandler.outputConsole(Level.INFO, "Counting " + count + " regions for player " + player.getName() + " on template " + token.id + ".");
		return count;
	}

	/**
	 * Returns a global count for regions, where player is owner
	 * 
	 * @param player
	 * @return the count of all regions owned by the player
	 */
	public int countPlayerGlobalRegions(Player player) {
		int count = 0;
		for (final TemplateMain token : TokenManager.tokenList) {
			count += countPlayerTokenRegions(player, token);
		}
		langHandler.outputConsole(Level.INFO, "Counting " + count + " regions for player " + player.getName() + ".");
		return count;
	}

	private int getLimitEntry(String key) {
		if (limitEntries.containsKey(key)) {
			try {
				return Integer.parseInt(limitEntries.get(key).toString());
			} catch (final Exception e) {
			}
		}
		return -1;
	}
}
