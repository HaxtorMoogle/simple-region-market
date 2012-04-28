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
import com.thezorro266.simpleregionmarket.handlers.LanguageHandler;
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
	private final LanguageHandler LANG_HANDLER;

	public TokenManager(SimpleRegionMarket plugin, LanguageHandler langHandler) {
		PLUGIN = plugin;
		LANG_HANDLER = langHandler;
	}

	/**
	 * Update signs from tokens
	 * 
	 * @param agent
	 *            the agent
	 * @param event
	 *            the event
	 */
	public void updateSigns(TemplateMain token, String world, String region) {
		if (token != null && world != null && region != null) {

			final World worldWorld = Bukkit.getWorld(world);
			if (worldWorld == null) {
				return;
			}

			final ProtectedRegion protectedRegion = SimpleRegionMarket.wgManager.getProtectedRegion(worldWorld, region);
			if (protectedRegion == null) {
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
			LANG_HANDLER.outputConsole(Level.INFO, "No templates found. Creating standard templates.");
			PLUGIN.saveResource(CONFIG_NAME, false);
		}
		if (CONFIG_FILE.exists()) {
			final YamlConfiguration configHandle = YamlConfiguration.loadConfiguration(CONFIG_FILE);
			for (final String key : configHandle.getKeys(false)) {
				final String type = configHandle.getString(key + ".type");
				if (type.equalsIgnoreCase("sell")) {
					tokenList.add(new TemplateSell(PLUGIN, LANG_HANDLER, this, key));
				} else if (type.equalsIgnoreCase("let")) {
					tokenList.add(new TemplateLet(PLUGIN, LANG_HANDLER, this, key));
				} else if (type.equalsIgnoreCase("hotel")) {
					tokenList.add(new TemplateHotel(PLUGIN, LANG_HANDLER, this, key));
				} else {
					LANG_HANDLER.outputConsole(Level.INFO, "I don't know the type " + type + ".");
				}
			}
		} else {
			LANG_HANDLER.outputConsole(Level.SEVERE, "Error creating standard templates.");
		}
	}

	public boolean playerIsOwner(Player player, TemplateMain token, String world, ProtectedRegion protectedRegion) {
		if (player != null && token != null && world != null && protectedRegion != null) {
			final String region = protectedRegion.getId();
			if (!Utils.getEntryBoolean(token, world, region, "taken")) {
				if (protectedRegion.isOwner(player.getName())) { // TODO Player Member when bought?
					return true;
				}
			} else {
				if (Utils.getEntryString(token, world, region, "owner").equalsIgnoreCase(player.getName())) {
					return true;
				}
			}
		}
		return false;
	}

	public void playerClickedSign(Player player, TemplateMain token, String world, String region) {
		if (Utils.getEntryBoolean(token, world, region, "taken")) {
			if (player.getName().equalsIgnoreCase(Utils.getEntryString(token, world, region, "owner"))) {
				token.ownerClicksTakenSign(world, region);
			} else {
				token.otherClicksTakenSign(player, world, region);
			}
		} else {
			if (playerIsOwner(player, token, world, SimpleRegionMarket.wgManager.getProtectedRegion(Bukkit.getWorld(world), region))) {
				token.ownerClicksSign(player, world, region);
			} else {
				// TODO Permissions!
				// TODO Limits!
				token.otherClicksSign(player, world, region);
			}
		}
	}
}
