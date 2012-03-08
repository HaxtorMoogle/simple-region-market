package com.thezorro266.simpleregionmarket;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class ConfigHandler {
	private final File agents = new File(SimpleRegionMarket.plugin_dir
			+ "agents.yml");
	private final File agents_fail = new File(SimpleRegionMarket.plugin_dir
			+ "agents_fail.yml");
	private FileConfiguration config;
	private final LanguageHandler langHandler;
	private final SimpleRegionMarket plugin;

	public ConfigHandler(SimpleRegionMarket plugin, LanguageHandler langHandler) {
		this.plugin = plugin;
		config = plugin.getConfig();
		this.langHandler = langHandler;

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
		if (config.get("agent_name") == null) {
			config.set("agent_name", "[AGENT]");
		}
		if (config.get("hotel_name") == null) {
			config.set("hotel_name", "[HOTEL]");
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

		plugin.saveConfig();
	}

	public FileConfiguration getConfig() {
		plugin.reloadConfig();
		config = plugin.getConfig();
		return config;
	}

	public void load() {
		final YamlConfiguration confighandle = YamlConfiguration
				.loadConfiguration(agents);

		final ArrayList<String> worlds_called = new ArrayList<String>();
		final ArrayList<String> regions_called = new ArrayList<String>();

		ConfigurationSection path;
		for (final String world : confighandle.getKeys(false)) {
			path = confighandle.getConfigurationSection(world);
			for (final String region : path.getKeys(false)) {
				path = confighandle.getConfigurationSection(world)
						.getConfigurationSection(region);
				for (final String signnr : path.getKeys(false)) {
					path = confighandle.getConfigurationSection(world)
							.getConfigurationSection(region)
							.getConfigurationSection(signnr);
					if (path == null) {
						continue;
					}

					String account = "";
					if (path.isSet("Owner")) {
						account = path.getString("Owner");
					} else if (path.isSet("Account")) {
						account = path.getString("Account");
					}

					Date expiredate = null;
					if (path.isSet("ExpireDate")) {
						expiredate = new Date(path.getLong("ExpireDate"));
					}

					final World world_world = Bukkit.getWorld(world);
					final ProtectedRegion protectedregion_region = SimpleRegionMarket
							.getWorldGuard().getRegionManager(world_world)
							.getRegion(region);

					SignAgent newagent;
					if (expiredate != null) {
						newagent = plugin.getAgentManager().addAgent(
								path.getInt("Mode", 0),
								new Location(world_world,
										path.getDouble("X", 0),
										path.getDouble("Y",	0),
										path.getDouble("Z", 0)),
								protectedregion_region,
								path.getDouble("Price"),
								account,
								path.getLong("RentTime", -1),
								path.getString("RentBy"),
								expiredate);
					} else {
						newagent = plugin.getAgentManager().addAgent(
								path.getInt("Mode", 0),
								new Location(world_world,
										path.getDouble("X", 0),
										path.getDouble("Y", 0),
										path.getDouble("Z", 0)),
								protectedregion_region,
								path.getDouble("Price"),
								account,
								path.getLong("RentTime", -1));
					}

					if (newagent == null) {
						if (world_world == null) {
							if (!worlds_called.contains(world)) {
								langHandler.outputConsole(Level.WARNING, "World '" + world + "' was deleted. All agents on this world got deleted aswell.");
								worlds_called.add(world);
							}
						} else if (protectedregion_region == null) {
							if (!regions_called.contains(region)) {
								langHandler.outputConsole(Level.WARNING, "Region '" + region + "' was deleted. All agents on this region got deleted aswell.");
								regions_called.add(region);
							}
						} else {
							langHandler.outputConsole(Level.WARNING, "Agent '" + path.toString() + "' from 'agents.yml' was not loaded properly. Adding to 'agents_fail.yml'");
							final YamlConfiguration tempconfighandle = YamlConfiguration.loadConfiguration(agents_fail);
							tempconfighandle.set(path.getCurrentPath() + ".Mode", path.getInt("Mode", 0));
							tempconfighandle.set(path.getCurrentPath() + ".X", path.getDouble("X", 0));
							tempconfighandle.set(path.getCurrentPath() + ".Y", path.getDouble("Y", 0));
							tempconfighandle.set(path.getCurrentPath() + ".Z", path.getDouble("Z", 0));
							tempconfighandle.set(path.getCurrentPath() + ".Price", path.getDouble("Price"));
							tempconfighandle.set(path.getCurrentPath() + ".Account", account);
							tempconfighandle.set(path.getCurrentPath() + ".RentTime", path.getLong("RentTime", 0));
							if (path.isSet("RentBy")) {
								tempconfighandle.set(path.getCurrentPath() + ".RentBy", path.getString("RentBy"));
							}

							if (path.isSet("ExpireDate")) {
								tempconfighandle.set(path.getCurrentPath() + ".ExpireDate", path.getLong("ExpireDate"));
							}

							try {
								tempconfighandle.save(agents_fail);
							} catch (final IOException e) {
								langHandler.outputConsole(Level.SEVERE, "Could not backup that agent.");
							}
						}
					}
				}
			}
		}
	}

	public void save() {
		final YamlConfiguration confighandle = new YamlConfiguration();

		int i = 0;
		String path;
		for (final SignAgent obj : plugin.getAgentManager().getAgentList()) {
			final Location loc = obj.getLocation();
			path = loc.getWorld().getName() + "." + obj.getRegion() + "."
					+ Integer.toString(i);

			confighandle.set(path + ".Mode", obj.getMode());
			confighandle.set(path + ".X", loc.getX());
			confighandle.set(path + ".Y", loc.getY());
			confighandle.set(path + ".Z", loc.getZ());
			confighandle.set(path + ".Price", obj.getPrice());
			confighandle.set(path + ".Account", obj.getAccount());
			confighandle.set(path + ".RentTime", obj.getRentTime());
			if (obj.isRent()) {
				confighandle.set(path + ".RentBy", obj.getRent());
				confighandle.set(path + ".ExpireDate", obj.getExpireDate()
						.getTime());
			}
			i++;
		}

		try {
			confighandle.save(agents);
		} catch (final IOException e) {
			langHandler.outputConsole(Level.SEVERE, "Could not save agents.");
		}
	}
}
