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
					line = Utils.getOptionString(token, "taken." + (i + 1));
				} else {
					line = Utils.getOptionString(token, "output." + (i + 1));
				}
				lines[i] = Utils.replaceTokens(line, token.getReplacementMap(world, region));
			}

			// Set sign lines for all signs
			if (Utils.getEntry(token, world, region, "signs") != null) {
				for (final Location loc : Utils.getSignLocations(token, world, region)) {
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
		final int[] count = new int[2];
		final ArrayList<String> worldsHad = new ArrayList<String>();
		final ArrayList<String> regionsHad = new ArrayList<String>();
		for (final TemplateMain token : TokenManager.tokenList) {
			for (final String world : token.entries.keySet()) {
				final World worldWorld = Bukkit.getWorld(world);
				if (worldWorld == null) {
					if (!worldsHad.contains(world)) {
						ArrayList<String> list = new ArrayList<String>();
						list.add(world);
						langHandler.consoleOut("CHECK.WARN.NO_WORLD", Level.WARNING, list);
					}
					token.entries.remove(world);
				} else {
					if (!worldsHad.contains(world)) {
						count[0]++;
					}
					for (final String region : token.entries.get(world).keySet()) {
						final ProtectedRegion protectedRegion = SimpleRegionMarket.wgManager.getProtectedRegion(worldWorld, region);
						if (protectedRegion == null) {
							if (!regionsHad.contains(region)) {
								ArrayList<String> list = new ArrayList<String>();
								list.add(region);
								langHandler.consoleOut("CHECK.WARN.NO_REGION", Level.WARNING, list);
							}
							token.entries.get(world).remove(region);
						} else {
							if (!regionsHad.contains(region)) {
								count[1]++;
							}
							token.schedule(world, region);
							updateSigns(token, world, region);
						}
						if (!regionsHad.contains(region)) {
							regionsHad.add(region);
						}
					}
				}
				if (!worldsHad.contains(world)) {
					worldsHad.add(world);
				}
			}
		}
		return count;
	}

	public void initTemplates() {
		if (!CONFIG_FILE.exists()) {
			langHandler.consoleOut("TEMPLATES.NORM.NO_TEMPLATES");
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
					ArrayList<String> list = new ArrayList<String>();
					list.add(type);
					langHandler.consoleOut("TEMPLATES.WARN.TYPE_NOT_KOWN", Level.WARNING, list);
				}
			}
		} else {
			langHandler.consoleOut("TEMPLATES.ERROR.NO_STANDARD_TEMPLATES");
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
				// Permissions
				if (SimpleRegionMarket.permManager.canPlayerBuyToken(player, token)) {
					// Limits
					if (SimpleRegionMarket.limitHandler.checkPlayerGlobal(player)) {
						if (SimpleRegionMarket.limitHandler.checkPlayerToken(player, token)) {
							if (SimpleRegionMarket.limitHandler.checkPlayerGlobalWorld(player, world)) {
								if (SimpleRegionMarket.limitHandler.checkPlayerTokenWorld(player, token, world)) {
									final ProtectedRegion protectedRegion = SimpleRegionMarket.wgManager.getProtectedRegion(Bukkit.getWorld(world), region);
									if (protectedRegion == null || protectedRegion.getParent() == null) {
										token.otherClicksSign(player, world, region);
									} else {
										if (SimpleRegionMarket.limitHandler.checkPlayerGlobalRegion(player, protectedRegion.getParent())) {
											if (SimpleRegionMarket.limitHandler.checkPlayerTokenRegion(player, token, protectedRegion.getParent())) {
												token.otherClicksSign(player, world, region);
											} else {
												langHandler.playerErrorOut(player, "PLAYER.LIMITS.TOKEN_PARENTREGION", null);
											}
										} else {
											langHandler.playerErrorOut(player, "PLAYER.LIMITS.GLOBAL_PARENTREGION", null);
										}
									}
								} else {
									langHandler.playerErrorOut(player, "PLAYER.LIMITS.TOKEN_WORLD", null);
								}
							} else {
								langHandler.playerErrorOut(player, "PLAYER.LIMITS.GLOBAL_WORLD", null);
							}
						} else {
							langHandler.playerErrorOut(player, "PLAYER.LIMITS.TOKEN", null);
						}
					} else {
						langHandler.playerErrorOut(player, "PLAYER.LIMITS.GLOBAL", null);
					}
				} else {
					langHandler.playerErrorOut(player, "PLAYER.NO_PERMISSIONS.BUY", null);
				}
			}
		}
	}

	public boolean playerCreatedSign(Player player, TemplateMain token, Location signLocation, String[] lines) {
		final String world = signLocation.getWorld().getName();
		final HashMap<String, String> input = Utils.getSignInput(token, lines);
		final ProtectedRegion protectedRegion = Utils.getProtectedRegion(input.get("region").toString(), signLocation);

		if (protectedRegion == null) {
			langHandler.playerErrorOut(player, "PLAYER.ERROR.NO_REGION", null);
			return false;
		}

		final String region = protectedRegion.getId();

		for (final TemplateMain otherToken : TokenManager.tokenList) {
			if (!otherToken.equals(token) && Utils.getEntry(otherToken, world, region, "taken") != null) {
				if (!Utils.getSignLocations(otherToken, world, region).isEmpty() || Utils.getEntryBoolean(otherToken, world, region, "taken")) {
					langHandler.playerErrorOut(player, "PLAYER.ERROR.OTHER_TOKEN", null);
					return false;
				}
			}
		}

		// Permissions
		if (!SimpleRegionMarket.permManager.canPlayerSellToken(player, token)) {
			langHandler.playerErrorOut(player, "PLAYER.NO_PERMISSIONS.SELL", null);
			return false;
		}

		return token.signCreated(player, world, protectedRegion, signLocation, input, lines);
	}

	public boolean playerSignBreak(Player player, TemplateMain token, String world, String region, Location signLocation) {
		final World worldWorld = Bukkit.getWorld(world);
		final ProtectedRegion protectedRegion = SimpleRegionMarket.wgManager.getProtectedRegion(worldWorld, region);

		// Permissions
		if (!SimpleRegionMarket.permManager.isAdmin(player) && !playerIsOwner(player, token, world, protectedRegion)) {
			langHandler.playerErrorOut(player, "PLAYER.ERROR.NOT_OWNER", null);
			return false;
		}

		final ArrayList<Location> signLocations = Utils.getSignLocations(token, world, region);
		if (signLocations.size() == 1) {
			if (Utils.getEntryBoolean(token, world, region, "taken")) {
				if (token.canLiveWithoutSigns) { // TODO Ask for deletion (region)
					Utils.removeEntry(token, world, region, "signs");
				} else {
					langHandler.playerErrorOut(player, "PLAYER.ERROR.NEED_ONE_SIGN", null);
					return false;
				}
			} else {
				Utils.removeRegion(token, world, region);
				langHandler.playerNormalOut(player, "PLAYER.REGION.DELETED", null);
				return true;
			}
		} else {
			signLocations.remove(signLocation);
		}
		langHandler.playerNormalOut(player, "PLAYER.REGION.DELETED_SIGN", null);
		return true;
	}
}
