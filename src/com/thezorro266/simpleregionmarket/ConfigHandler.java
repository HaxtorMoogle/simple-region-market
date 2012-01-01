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
import org.bukkit.configuration.file.YamlConfiguration;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class ConfigHandler {
	private File agents = new File(SimpleRegionMarket.plugin_dir + "agents.yml");
	private File agents_fail = new File(SimpleRegionMarket.plugin_dir + "agents_fail.yml");
	private File config = new File(SimpleRegionMarket.plugin_dir + "config.yml");

	public void load() {
		YamlConfiguration confighandle;

		confighandle = YamlConfiguration.loadConfiguration(config);
		SimpleRegionMarket.language = confighandle.getString("language", "en");
		SimpleRegionMarket.logging = confighandle.getBoolean("logging", true);
		SimpleRegionMarket.removeBuyedSigns = confighandle.getBoolean("remove_buyed_signs", true);
		SimpleRegionMarket.enableEconomy = confighandle.getBoolean("enable_economy", true);
		SimpleRegionMarket.maxRentMultiplier = confighandle.getInt("max_rent_multiplier", 2);
		SimpleRegionMarket.agentName = confighandle.getString("agent_name", "[AGENT]");
		SimpleRegionMarket.hotelName = confighandle.getString("hotel_name", "[HOTEL]");

		confighandle = YamlConfiguration.loadConfiguration(agents);

		ArrayList<String> worlds_called = new ArrayList<String>();
		ArrayList<String> regions_called = new ArrayList<String>();

		ConfigurationSection path;
		for (String world: confighandle.getKeys(false)) {
			path = confighandle.getConfigurationSection(world);
			for (String region: path.getKeys(false)) {
				path = confighandle.getConfigurationSection(world).getConfigurationSection(region);
				for (String signnr: path.getKeys(false)) {
					path = confighandle.getConfigurationSection(world).getConfigurationSection(region).getConfigurationSection(signnr);
					if(path == null) {
						continue;
					}

					String account = "";
					if(path.isSet("Owner")) {
						account = path.getString("Owner");
					} else if(path.isSet("Account")) {
						account = path.getString("Account");
					}

					Date expiredate = null;
					if(path.isSet("ExpireDate")) {
						expiredate = new Date(path.getLong("ExpireDate"));
					}

					World world_world = Bukkit.getWorld(world);
					ProtectedRegion protectedregion_region = SimpleRegionMarket.getWorldGuard().getRegionManager(world_world).getRegion(region);

					SignAgent newagent;
					if(expiredate != null) {
						newagent = SimpleRegionMarket.getAgentManager().addAgent(path.getInt("Mode", 0),
								new Location(world_world,
										path.getDouble("X", 0),
										path.getDouble("Y", 0),
										path.getDouble("Z", 0)),
										protectedregion_region, path.getDouble("Price"), account, path.getInt("RentTime", 0), path.getString("RentBy"), expiredate);
					} else {
						newagent = SimpleRegionMarket.getAgentManager().addAgent(path.getInt("Mode", 0),
								new Location(world_world,
										path.getDouble("X", 0),
										path.getDouble("Y", 0),
										path.getDouble("Z", 0)),
										protectedregion_region, path.getDouble("Price"), account, path.getInt("RentTime", 0));
					}

					if(newagent == null) {
						if(world_world == null) {
							if(!worlds_called.contains(world)) {
								LanguageHandler.outputConsole(Level.WARNING, "World '" + world + "' was deleted. All agents on this world got deleted aswell.");
								worlds_called.add(world);
							}
						} else if(protectedregion_region == null) {
							if(!regions_called.contains(region)) {
								LanguageHandler.outputConsole(Level.WARNING, "Region '" + region + "' was deleted. All agents on this world got deleted aswell.");
								regions_called.add(region);
							}
						} else {
							LanguageHandler.outputConsole(Level.WARNING, "Agent '" + path.toString() + "' from 'agents.yml' was not loaded properly. Adding to 'agents_fail.yml'");
							YamlConfiguration tempconfighandle = YamlConfiguration.loadConfiguration(agents_fail);
							tempconfighandle.set(path.getCurrentPath() + ".Mode", path.getInt("Mode", 0));
							tempconfighandle.set(path.getCurrentPath() + ".X", path.getDouble("X", 0));
							tempconfighandle.set(path.getCurrentPath() + ".Y", path.getDouble("Y", 0));
							tempconfighandle.set(path.getCurrentPath() + ".Z", path.getDouble("Z", 0));
							tempconfighandle.set(path.getCurrentPath() + ".Price", path.getDouble("Price"));
							tempconfighandle.set(path.getCurrentPath() + ".Account", account);
							tempconfighandle.set(path.getCurrentPath() + ".RentTime", path.getInt("RentTime", 0));
							if(path.isSet("RentBy")) {
								tempconfighandle.set(path.getCurrentPath() + ".RentBy", path.getString("RentBy"));
							}

							if(path.isSet("ExpireDate")) {
								tempconfighandle.set(path.getCurrentPath() + ".ExpireDate", path.getLong("ExpireDate"));
							}

							try {
								tempconfighandle.save(agents_fail);
							} catch(IOException e) {
								LanguageHandler.outputConsole(Level.SEVERE, "Could not backup that agent.");
							}
						}
					}
				}
			}
		}
	}

	public void save() {
		YamlConfiguration confighandle;

		confighandle = new YamlConfiguration();

		confighandle.set("language", SimpleRegionMarket.language);
		confighandle.set("logging", SimpleRegionMarket.logging);
		confighandle.set("remove_buyed_signs", SimpleRegionMarket.removeBuyedSigns);
		confighandle.set("enable_economy", SimpleRegionMarket.enableEconomy);
		confighandle.set("max_rent_multiplier", SimpleRegionMarket.maxRentMultiplier);
		confighandle.set("agent_name", SimpleRegionMarket.agentName);
		confighandle.set("hotel_name", SimpleRegionMarket.hotelName);

		try {
			confighandle.save(config);
		} catch (IOException e) {
			LanguageHandler.outputConsole(Level.SEVERE, "Could not save configuration.");
		}

		confighandle = new YamlConfiguration();

		int i = 0;
		String path;
		for (SignAgent obj : SimpleRegionMarket.getAgentManager().getAgentList()) {
			Location loc = obj.getLocation();
			path = loc.getWorld().getName() + "." + obj.getRegion() + "." + Integer.toString(i);

			confighandle.set(path + ".Mode", obj.getMode());
			confighandle.set(path + ".X", loc.getX());
			confighandle.set(path + ".Y", loc.getY());
			confighandle.set(path + ".Z", loc.getZ());
			confighandle.set(path + ".Price", obj.getPrice());
			confighandle.set(path + ".Account", obj.getAccount());
			confighandle.set(path + ".RentTime", obj.getRentTime());
			if(obj.isRent()) {
				confighandle.set(path + ".RentBy", obj.getRent());
				confighandle.set(path + ".ExpireDate", obj.getExpireDate().getTime());
			}
			i++;
		}

		try {
			confighandle.save(agents);
		} catch (IOException e) {
			LanguageHandler.outputConsole(Level.SEVERE, "Could not save agents.");
		}
	}
}
