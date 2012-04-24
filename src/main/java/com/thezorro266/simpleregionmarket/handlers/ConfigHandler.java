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
		if (config.get("remove_buyed_signs") == null) {
			config.set("remove_buyed_signs", true);
		}
		if (config.get("enable_economy") == null) {
			config.set("enable_economy", true);
		}
		if (config.get("max_rent_multiplier") == null) {
			config.set("max_rent_multiplier", 2);
		}
		if (config.get("buyer_get_owner") == null) {
			config.set("buyer_get_owner", true);
		}
		if (config.get("renter_get_owner") == null) {
			config.set("renter_get_owner", false);
		}
		if (config.get("defp_player_buy") == null) {
			config.set("defp_player_buy", false);
		}
		if (config.get("defp_player_rent") == null) {
			config.set("defp_player_rent", false);
		}
		if (config.get("defp_player_sell") == null) {
			config.set("defp_player_sell", false);
		}
		if (config.get("defp_player_let") == null) {
			config.set("defp_player_let", false);
		}
		if (config.get("defp_player_addowner") == null) {
			config.set("defp_player_addowner", false);
		}
		if (config.get("defp_player_addmember") == null) {
			config.set("defp_player_addmember", false);
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
