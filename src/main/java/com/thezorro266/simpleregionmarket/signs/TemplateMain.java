package com.thezorro266.simpleregionmarket.signs;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.thezorro266.simpleregionmarket.SimpleRegionMarket;
import com.thezorro266.simpleregionmarket.TokenManager;
import com.thezorro266.simpleregionmarket.Utils;

/**
 * @author theZorro266
 * 
 */
public abstract class TemplateMain {
	/**
	 * Standard template attributes
	 */
	public String id = null;

	/**
	 * HashMap<Key:String, Value:Object>
	 */
	public HashMap<String, Object> tplOptions = new HashMap<String, Object>();

	/**
	 * HashMap<World:String, HashMap<Region:String, HashMap<Key:String, Value:Object>>>
	 */
	public HashMap<String, HashMap<String, HashMap<String, Object>>> entries = new HashMap<String, HashMap<String, HashMap<String, Object>>>();

	public void load() {
		if (checkTemplate()) {
			final YamlConfiguration configHandle = YamlConfiguration.loadConfiguration(TokenManager.CONFIG_FILE);

			for (final String key : configHandle.getConfigurationSection(id).getKeys(true)) {
				tplOptions.put(key, configHandle.getConfigurationSection(id).get(key));
			}

			final YamlConfiguration tokenLoad = YamlConfiguration.loadConfiguration(new File(SimpleRegionMarket.getPluginDir() + "signs/" + id.toLowerCase()
					+ ".yml"));

			ConfigurationSection path;
			for (final String lWorld : tokenLoad.getKeys(false)) {
				path = tokenLoad.getConfigurationSection(lWorld);
				for (final String lRegion : path.getKeys(false)) {
					path = tokenLoad.getConfigurationSection(lWorld).getConfigurationSection(lRegion);
					for (final String lKey : path.getKeys(false)) {
						if (lKey.equals("signs")) {
							final ConfigurationSection pathSigns = path.getConfigurationSection("signs");
							final ArrayList<Location> signLocations = new ArrayList<Location>();
							for (final String lNr : pathSigns.getKeys(false)) {
								signLocations.add(new Location(Bukkit.getWorld(lWorld), pathSigns.getDouble(lNr + ".X"), pathSigns.getDouble(lNr + ".Y"),
										pathSigns.getDouble(lNr + ".Z")));
							}
							Utils.setEntry(this, lWorld, lRegion, lKey, signLocations);
						} else {
							Utils.setEntry(this, lWorld, lRegion, lKey, path.get(lKey));
						}
					}
				}
			}
		} else {
			Bukkit.getLogger().log(Level.SEVERE, "[SRM] Error loading templates.");
		}
	}

	public void save() {
		if (checkTemplate()) {
			final YamlConfiguration tokenSave = new YamlConfiguration();

			for (final String string : entries.keySet()) {
				final String sWorld = string;
				for (final String string2 : entries.get(sWorld).keySet()) {
					final String sRegion = string2;
					for (final String string3 : entries.get(sWorld).get(sRegion).keySet()) {
						final String sKey = string3;
						if (sKey.equals("signs")) {
							final ArrayList<Location> signLocations = Utils.getSignLocations(this, sWorld, sRegion);
							int counter = 0;
							for (final Location signLoc : signLocations) {
								tokenSave.set(sWorld + "." + sRegion + ".signs." + counter + ".X", signLoc.getX());
								tokenSave.set(sWorld + "." + sRegion + ".signs." + counter + ".Y", signLoc.getY());
								tokenSave.set(sWorld + "." + sRegion + ".signs." + counter + ".Z", signLoc.getZ());
								counter++;
							}
						} else {
							tokenSave.set(sWorld + "." + sRegion + "." + sKey, Utils.getEntry(this, sWorld, sRegion, sKey));
						}
					}
				}
			}

			try {
				tokenSave.save(new File(SimpleRegionMarket.getPluginDir() + "signs/" + id.toLowerCase() + ".yml"));
			} catch (final IOException e) {
				Bukkit.getLogger().log(Level.SEVERE, "[SRM] Error saving token " + id + ".");
				e.printStackTrace();
			}
		}
	}

	public boolean checkTemplate() {
		if (id != null) {
			return true;
		} else {
			Bukkit.getLogger().log(Level.SEVERE, "[SRM] Template misconfiguration.");
		}
		return false;
	}

	public Map<String, String> getReplacementMap(String world, String region) {
		if (checkTemplate()) {
			if (world != null && entries.containsKey(world) && region != null && entries.get(world).containsKey(region)) {
				final World worldWorld = Bukkit.getWorld(world);
				if (worldWorld != null) {
					final ProtectedRegion protectedRegion = SimpleRegionMarket.getWorldGuard().getRegionManager(worldWorld).getRegion(region);
					if (protectedRegion != null) {
						final HashMap<String, String> replacementMap = new HashMap<String, String>();
						replacementMap.put("id", id);
						replacementMap.put("id_out", tplOptions.get("output.id").toString());
						replacementMap.put("id_taken", tplOptions.get("taken.id").toString());
						replacementMap.put("world", world.toLowerCase());
						replacementMap.put("region", region.toLowerCase());
						if (Utils.isEconomy() || Utils.getEntryDouble(this, world, region, "price") == 0) {
							replacementMap.put("price", "FREE");
						} else {
							replacementMap.put("price", Utils.econFormat(Utils.getEntryDouble(this, world, region, "price")));
						}
						replacementMap.put("account", Utils.getEntryString(this, world, region, "account"));
						if (Utils.getEntryString(this, world, region, "owner") == null || Utils.getEntryString(this, world, region, "owner").isEmpty()) {
							replacementMap.put("player", "No owner");
						} else {
							replacementMap.put("player", Utils.getEntryString(this, world, region, "owner"));
						}
						replacementMap.put("x", Integer.toString(Math.abs((int) protectedRegion.getMaximumPoint().getX()
								- (int) (protectedRegion.getMinimumPoint().getX() - 1))));
						replacementMap.put("y", Integer.toString(Math.abs((int) protectedRegion.getMaximumPoint().getY()
								- (int) (protectedRegion.getMinimumPoint().getY() - 1))));
						replacementMap.put("z", Integer.toString(Math.abs((int) protectedRegion.getMaximumPoint().getZ()
								- (int) (protectedRegion.getMinimumPoint().getZ() - 1))));

						return replacementMap;
					}
				}
			}
		}
		return null;
	}
}
