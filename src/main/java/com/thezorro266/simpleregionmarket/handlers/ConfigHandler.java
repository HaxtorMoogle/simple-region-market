package com.thezorro266.simpleregionmarket.handlers;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.thezorro266.simpleregionmarket.SimpleRegionMarket;
import com.thezorro266.simpleregionmarket.TokenManager;
import com.thezorro266.simpleregionmarket.Utils;
import com.thezorro266.simpleregionmarket.signs.TemplateHotel;
import com.thezorro266.simpleregionmarket.signs.TemplateMain;
import com.thezorro266.simpleregionmarket.signs.TemplateSell;

public class ConfigHandler {
	private FileConfiguration config;

	private final SimpleRegionMarket plugin;

	/**
	 * Instantiates a new config handler.
	 * 
	 * @param plugin
	 *            the plugin
	 */
	public ConfigHandler(SimpleRegionMarket plugin) {
		this.plugin = plugin;
		config = plugin.getConfig();
		if (config.get("language") == null) {
			config.set("language", "en");
		}
		if (config.get("logging") == null) {
			config.set("logging", true);
		}
		if (config.get("enable_economy") == null) {
			config.set("enable_economy", true);
		}

		plugin.saveConfig();
	}

	/**
	 * Gets the config.
	 * 
	 * @return the config
	 */
	public FileConfiguration getConfig() {
		plugin.reloadConfig();
		config = plugin.getConfig();
		return config;
	}

	public boolean loadOld() {
		final YamlConfiguration confighandle = YamlConfiguration.loadConfiguration(new File(SimpleRegionMarket.getPluginDir() + "agents.yml"));

		TemplateHotel tokenHotel = null;
		TemplateSell tokenAgent = null;
		for (final TemplateMain token : TokenManager.tokenList) {
			if (token.id.equalsIgnoreCase("SELL")) {
				tokenAgent = (TemplateSell) token;
			}
			if (token.id.equalsIgnoreCase("HOTEL")) {
				tokenHotel = (TemplateHotel) token;
			}
		}
		if (tokenHotel == null || tokenAgent == null) {
			return false;
		}

		ConfigurationSection path;
		for (final String world : confighandle.getKeys(false)) {
			final World worldWorld = Bukkit.getWorld(world);
			if (worldWorld == null) {
				continue;
			}
			path = confighandle.getConfigurationSection(world);
			for (final String region : path.getKeys(false)) {
				final ProtectedRegion protectedRegion = SimpleRegionMarket.wgManager.getProtectedRegion(worldWorld, region);
				if (protectedRegion == null) {
					continue;
				}
				path = confighandle.getConfigurationSection(world).getConfigurationSection(region);
				for (final String signnr : path.getKeys(false)) {
					path = confighandle.getConfigurationSection(world).getConfigurationSection(region).getConfigurationSection(signnr);
					if (path == null) {
						continue;
					}

					if (path.getInt("Mode") == 1) { // HOTEL
						if (!tokenHotel.entries.containsKey(world)) {
							tokenHotel.entries.put(world, new HashMap<String, HashMap<String, Object>>());
						}
						if (!tokenHotel.entries.get(world).containsKey(region)) {
							tokenHotel.entries.get(world).put(region, new HashMap<String, Object>());
							Utils.setEntry(tokenHotel, world, region, "price", path.getInt("Price"));
							Utils.setEntry(tokenHotel, world, region, "account", path.getInt("Account"));
							Utils.setEntry(tokenHotel, world, region, "renttime", path.getLong("RentTime"));
							if (path.isSet("ExpireDate")) {
								Utils.setEntry(tokenHotel, world, region, "taken", true);
								Utils.setEntry(tokenHotel, world, region, "owner", path.getString("RentBy"));
								Utils.setEntry(tokenHotel, world, region, "expiredate", path.getLong("ExpireDate"));
							} else {
								Utils.setEntry(tokenHotel, world, region, "taken", false);
							}
						}

						final ArrayList<Location> signLocations = Utils.getSignLocations(tokenHotel, world, region);
						signLocations.add(new Location(worldWorld, path.getDouble("X"), path.getDouble("Y"), path.getDouble("Z")));
						if (signLocations.size() == 1) {
							Utils.setEntry(tokenHotel, world, region, "signs", signLocations);
						}
					} else { // SELL
						if (!tokenAgent.entries.containsKey(world)) {
							tokenAgent.entries.put(world, new HashMap<String, HashMap<String, Object>>());
						}
						if (!tokenAgent.entries.get(world).containsKey(region)) {
							tokenAgent.entries.get(world).put(region, new HashMap<String, Object>());
							Utils.setEntry(tokenAgent, world, region, "price", path.getInt("Price"));
							Utils.setEntry(tokenAgent, world, region, "account", path.getInt("Account"));
							Utils.setEntry(tokenAgent, world, region, "renttime", path.getLong("RentTime"));
							Utils.setEntry(tokenAgent, world, region, "taken", false);
						}

						final ArrayList<Location> signLocations = Utils.getSignLocations(tokenAgent, world, region);
						signLocations.add(new Location(worldWorld, path.getDouble("X"), path.getDouble("Y"), path.getDouble("Z")));
						if (signLocations.size() == 1) {
							Utils.setEntry(tokenAgent, world, region, "signs", signLocations);
						}
					}
				}
			}
		}
		return true;
	}
}
