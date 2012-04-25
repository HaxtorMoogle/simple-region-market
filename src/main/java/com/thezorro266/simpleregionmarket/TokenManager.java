package com.thezorro266.simpleregionmarket;

import java.io.File;
import java.util.ArrayList;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.thezorro266.simpleregionmarket.signs.TemplateHotel;
import com.thezorro266.simpleregionmarket.signs.TemplateLet;
import com.thezorro266.simpleregionmarket.signs.TemplateMain;
import com.thezorro266.simpleregionmarket.signs.TemplateSell;

public class TokenManager {
	/**
	 * Static final attributes
	 */
	public final static String CONFIG_NAME = "templates.yml";
	public final static File CONFIG_FILE = new File(SimpleRegionMarket.getPluginDir() + CONFIG_NAME);

	public static ArrayList<TemplateMain> tokenList = new ArrayList<TemplateMain>();

	private final SimpleRegionMarket PLUGIN;

	public TokenManager(SimpleRegionMarket plugin) {
		PLUGIN = plugin;
	}

	/**
	 * Update signs from tokens
	 * 
	 * @param agent
	 *            the agent
	 * @param event
	 *            the event
	 */
	public void updateSigns(String id, String world, String region) {
		if (id != null && world != null && region != null) {

			final World worldWorld = Bukkit.getWorld(world);
			if (worldWorld == null) {
				return;
			}

			final ProtectedRegion protectedRegion = SimpleRegionMarket.getWorldGuard().getRegionManager(worldWorld).getRegion(region);
			if (protectedRegion == null) {
				return;
			}

			TemplateMain token = null;
			for (final TemplateMain token1 : tokenList) {
				if (token1.id == id) {
					token = token1;
					break;
				}
			}

			if (token == null) {
				return;
			}

			// Parse sign lines
			final String[] lines = new String[Utils.SIGN_LINES];
			for (int i = 0; i < Utils.SIGN_LINES; i++) {
				String line;
				if (Utils.getEntryBoolean(token, world, region, "taken")) {
					line = (String) token.tplOptions.get("taken." + (i + 1));
				} else {
					line = (String) token.tplOptions.get("output." + (i + 1));
				}
				lines[i] = Utils.replaceTokens(line, token.getReplacementMap(world, region));
			}

			// Set sign lines for all signs
			if (Utils.getEntry(token, world, region, "signs") != null) {
				final ArrayList<Location> signLocations = Utils.getSignLocations(token, world, region);
				for (final Location loc : signLocations) {
					if (loc.getBlock().getType() != Material.SIGN_POST && loc.getBlock().getType() != Material.WALL_SIGN) {
						loc.getBlock().setType(Material.SIGN_POST);
					}
					final Sign sign = (Sign) loc.getBlock().getState();
					for (int i = 0; i < Utils.SIGN_LINES; i++) {
						sign.setLine(i, lines[i]);
					}
					sign.update();
				}
			}
		}
	}

	public void initTemplates() {
		if (!CONFIG_FILE.exists()) {
			Bukkit.getLogger().log(Level.INFO, "[SRM] No templates found. Creating standard templates.");
			PLUGIN.saveResource(CONFIG_NAME, false);
		}
		if (CONFIG_FILE.exists()) {
			final YamlConfiguration configHandle = YamlConfiguration.loadConfiguration(CONFIG_FILE);
			for (final String key : configHandle.getKeys(false)) {
				final String type = configHandle.getString(key + ".type");
				if (type.equalsIgnoreCase("sell")) {
					tokenList.add(new TemplateSell(key));
				} else if (type.equalsIgnoreCase("let")) {
					tokenList.add(new TemplateLet(key));
				} else if (type.equalsIgnoreCase("hotel")) {
					tokenList.add(new TemplateHotel(key));
				} else {
					Bukkit.getLogger().log(Level.INFO, "[SRM] I don't know the type " + type + ".");
				}
			}
		} else {
			Bukkit.getLogger().log(Level.SEVERE, "[SRM] Error creating standard templates.");
		}
	}

	public void playerClickedSign(Player player, TemplateMain token, String world, String region) {
		// TODO Handling when player clickes a sign in world "world" and region "region" with the template "token"

	}
}
