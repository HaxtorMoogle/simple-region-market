package com.thezorro266.simpleregionmarket;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
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

	private final SimpleRegionMarket plugin;
	private final LanguageHandler langHandler;

	public TokenManager(SimpleRegionMarket plugin, LanguageHandler langHandler) {
		this.plugin = plugin;
		this.langHandler = langHandler;
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
					line = Utils.getOptionString(token, "taken."+(i+1));
				} else {
					line = Utils.getOptionString(token, "output."+(i+1));
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
	
	public int[] checkRegions() {
		int[] count = new int[2];
		ArrayList<String> worldsHad = new ArrayList<String>();
		ArrayList<String> regionsHad = new ArrayList<String>();
		for (final TemplateMain token : TokenManager.tokenList) {
			for(String world : token.entries.keySet()) {
				World worldWorld = Bukkit.getWorld(world);
				if(worldWorld == null) {
					if(!worldsHad.contains(world)) {
						langHandler.outputConsole(Level.WARNING, "World "+world+" is not loaded!");
					}
				} else {
					if(!worldsHad.contains(world)) {
						count[0]++;
					}
					for(String region : token.entries.get(world).keySet()) {
						ProtectedRegion protectedRegion = SimpleRegionMarket.wgManager.getProtectedRegion(worldWorld, region);
						if(protectedRegion == null) {
							if(!regionsHad.contains(region)) {
								langHandler.outputConsole(Level.WARNING, "WorldGuard region "+region+" was not found!");
							}
						} else {
							if(!regionsHad.contains(region)) {
								count[1]++;
							}
							token.schedule(world, region);
							updateSigns(token, world, region);
						}
						if(!regionsHad.contains(region)) {
							regionsHad.add(region);
						}
					}
				}
				if(!worldsHad.contains(world)) {
					worldsHad.add(world);
				}
			}
		}
		return count;
	}

	public void initTemplates() {
		if (!CONFIG_FILE.exists()) {
			langHandler.outputConsole(Level.INFO, "No templates found. Creating standard templates.");
			plugin.saveResource(CONFIG_NAME, false);
		}
		if (CONFIG_FILE.exists()) {
			final YamlConfiguration configHandle = YamlConfiguration.loadConfiguration(CONFIG_FILE);
			for (final String key : configHandle.getKeys(false)) {
				final String type = configHandle.getString(key + ".type");
				if (type.equalsIgnoreCase("sell")) {
					tokenList.add(new TemplateSell(plugin, langHandler, this, key));
				} else if (type.equalsIgnoreCase("let")) {
					tokenList.add(new TemplateLet(plugin, langHandler, this, key));
				} else if (type.equalsIgnoreCase("hotel")) {
					tokenList.add(new TemplateHotel(plugin, langHandler, this, key));
				} else {
					langHandler.outputConsole(Level.INFO, "I don't know the type " + type + ".");
				}
			}
		} else {
			langHandler.outputConsole(Level.SEVERE, "Error creating standard templates.");
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
	
	public boolean playerCreatedSign(Player player, TemplateMain token, Location signLocation, String[] lines) {
		String world = signLocation.getWorld().getName();
		final HashMap<String, String> input = Utils.getSignInput(token, lines);
		ProtectedRegion protectedRegion = Utils.getProtectedRegion(input.get("region").toString(), signLocation);

		if (protectedRegion == null) {
			langHandler.outputError(player, "ERR_REGION_NAME", null);
			return false;
		}

		final String region = protectedRegion.getId();
		
		for (final TemplateMain otherToken : TokenManager.tokenList) {
			if(!otherToken.equals(token)) {
				if(!Utils.getSignLocations(otherToken, world, region).isEmpty() || Utils.getEntryBoolean(otherToken, world, region, "taken")) {
					langHandler.outputError(player, "ERR_OTHER_TOKEN", null);
					return false;
				}
			}
		}
		
		// TODO Permissions!
		// TODO Limits!
		return token.signCreated(player, world, protectedRegion, signLocation, input, lines);
	}
	
	public boolean playerSignBreak(Player player, TemplateMain token, String world, ProtectedRegion protectedRegion, Location signLocation) {
		final String region = protectedRegion.getId();
		
		// Permissions
		if (!SimpleRegionMarket.permManager.isAdmin(player) && !playerIsOwner(player, token, world, protectedRegion)) {
			langHandler.outputError(player, "ERR_REGION_NO_OWNER", null);
			return false;
		}

		// TODO Remove whole region
		Utils.setEntry(token, world, region, "signs", Utils.getSignLocations(token, world, region).remove(signLocation));
		langHandler.outputMessage(player, "REGION_DELETE_SIGN", null);
		return true;
	}
}
