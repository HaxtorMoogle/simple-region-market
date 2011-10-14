package com.thezorro266.simpleregionmarket;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

public class ConfigHandler {
	private File agents = new File(SimpleRegionMarket.plugin_dir + "agents.yml");
	private File config = new File(SimpleRegionMarket.plugin_dir + "config.yml");

	public void load() {
		YamlConfiguration confighandle;
		
		confighandle = YamlConfiguration.loadConfiguration(config);
		AgentManager.max_regions = confighandle.getInt("maxregions", 0);
		SimpleRegionMarket.language = confighandle.getString("language", "en");
		
		confighandle = YamlConfiguration.loadConfiguration(agents);
		
		ConfigurationSection path;
		for (String world: confighandle.getKeys(false)) {
			path = confighandle.getConfigurationSection(world);
			for (String region: path.getKeys(false)) {
				path = confighandle.getConfigurationSection(world).getConfigurationSection(region);
				for (String signnr: path.getKeys(false)) {
					path = confighandle.getConfigurationSection(world).getConfigurationSection(region).getConfigurationSection(signnr);
					String owner = path.getString("Owner");
					double price = path.getDouble("Price", 0);
					if (price >= 0) {
						SimpleRegionMarket.getAgentManager().getAgentList().add(new RegionAgent(region,
							new Location(
								Bukkit.getWorld(world),
								path.getDouble("X", 0),
								path.getDouble("Y", 0),
								path.getDouble("Z", 0)),
							owner, price));
					}
				}
			}
		}
	}

	public void save() {
		YamlConfiguration confighandle;
		
		confighandle = new YamlConfiguration();
		
		confighandle.set("maxregions", AgentManager.max_regions);
		confighandle.set("language", SimpleRegionMarket.language);
		
		try {
			confighandle.save(config);
		} catch (IOException e) {
			Bukkit.getLogger().log(Level.SEVERE, "[SimpleRegionMarket] Could not save configuration.");
		}

		confighandle = new YamlConfiguration();
	
		int i = 0;
		String path;
		for (RegionAgent obj : SimpleRegionMarket.getAgentManager().getAgentList()) {
			Location loc = obj.getLocation();
			path = loc.getWorld().getName() + "." + obj.getRegion() + "." + Integer.toString(i);
			confighandle.set(path + ".X", loc.getX());
			confighandle.set(path + ".Y", loc.getY());
			confighandle.set(path + ".Z", loc.getZ());
			confighandle.set(path + ".Owner", obj.getOwner());
			confighandle.set(path + ".Price", obj.getPrice());
			i++;
		}
		
		try {
			confighandle.save(agents);
		} catch (IOException e) {
			Bukkit.getLogger().log(Level.SEVERE, "[SimpleRegionMarket] Could not save agents.");
		}
	}
}
