package com.thezorro266.simpleregionmarket.handlers;

import org.bukkit.configuration.file.FileConfiguration;

import com.thezorro266.simpleregionmarket.SimpleRegionMarket;

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
}
