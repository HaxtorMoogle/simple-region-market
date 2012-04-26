package com.thezorro266.simpleregionmarket.handlers;

/*
 * 
 */

import java.util.ArrayList;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.thezorro266.simpleregionmarket.SimpleRegionMarket;
import com.thezorro266.simpleregionmarket.TokenManager;
import com.thezorro266.simpleregionmarket.Utils;
import com.thezorro266.simpleregionmarket.signs.TemplateMain;

public class ListenerHandler implements Listener {
	private final LanguageHandler LANG_HANDLER;
	private final SimpleRegionMarket PLUGIN;
	private final TokenManager TOKEN_MANAGER;

	/**
	 * Instantiates a new listener handler.
	 * 
	 * @param plugin
	 *            the plugin
	 * @param langHandler
	 *            the lang handler
	 */
	public ListenerHandler(SimpleRegionMarket plugin, LanguageHandler langHandler, TokenManager tokenManager) {
		Bukkit.getPluginManager().registerEvents(this, plugin);
		PLUGIN = plugin;
		LANG_HANDLER = langHandler;
		TOKEN_MANAGER = tokenManager;
	}

	/**
	 * On block break.
	 * 
	 * @param event
	 *            the BlockBreakEvent
	 */
	@EventHandler
	public void onBlockBreak(final BlockBreakEvent event) {
		final Material type = event.getBlock().getType();
		if (type == Material.SIGN_POST || type == Material.WALL_SIGN) {
			final Location blockLocation = event.getBlock().getLocation();
			final World worldWorld = blockLocation.getWorld();
			final String world = worldWorld.getName();
			final ApplicableRegionSet regions = SimpleRegionMarket.wgManager.getWorldGuard().getRegionManager(worldWorld).getApplicableRegions(blockLocation);
			for (final TemplateMain token : TokenManager.tokenList) {
				for (final ProtectedRegion protectedRegion : regions) {
					final String region = protectedRegion.getId();

					final Player player = event.getPlayer();
					if (!SimpleRegionMarket.permManager.isAdmin(player) && !TOKEN_MANAGER.playerIsOwner(player, token, world, protectedRegion)) {
						LANG_HANDLER.outputError(player, "ERR_REGION_NO_OWNER", null);
						event.setCancelled(true);
						return;
					}

					final ArrayList<Location> signLocations = Utils.getSignLocations(token, world, region);
					if (!signLocations.isEmpty()) {
						try {
							signLocations.remove(blockLocation);
						} catch (final Exception e) {
						}
						Utils.setEntry(token, world, region, "signs", signLocations);
					}
				}
			}
		}
		PLUGIN.saveAll();
	}

	/**
	 * On player interact.
	 * 
	 * @param event
	 *            the event
	 */
	@EventHandler
	public void onPlayerInteract(final PlayerInteractEvent event) {
		if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
			final Material type = event.getClickedBlock().getType();
			if (type == Material.SIGN_POST || type == Material.WALL_SIGN) {
				final Location blockLocation = event.getClickedBlock().getLocation();
				final World worldWorld = blockLocation.getWorld();
				final String world = worldWorld.getName();
				final Player player = event.getPlayer();
				final ApplicableRegionSet regions = SimpleRegionMarket.wgManager.getWorldGuard().getRegionManager(worldWorld)
						.getApplicableRegions(blockLocation);
				for (final TemplateMain token : TokenManager.tokenList) {
					for (final ProtectedRegion protectedRegion : regions) {
						final String region = protectedRegion.getId();

						final ArrayList<Location> signLocations = Utils.getSignLocations(token, world, region);
						if (signLocations != null) {
							for (final Location signLoc : signLocations) {
								if (signLoc.equals(blockLocation)) {
									LANG_HANDLER.outputConsole(Level.INFO,
											player.getName() + " has " + SimpleRegionMarket.limitHandler.countPlayerRegions(player) + " global regions.");
									LANG_HANDLER.outputConsole(Level.INFO,
											player.getName() + " has " + SimpleRegionMarket.limitHandler.countPlayerTokenRegions(token, player)
													+ " regions from " + token.id);
									TOKEN_MANAGER.playerClickedSign(player, token, world, region);
									return;
								}
							}
						}
					}
				}
			}
		}
	}

	/**
	 * On sign change.
	 * 
	 * @param event
	 *            the event
	 */
	@EventHandler
	public void onSignChange(final SignChangeEvent event) {
		for (final TemplateMain token : TokenManager.tokenList) {
			if (event.getLine(0).equalsIgnoreCase((String) token.tplOptions.get("input.id"))) {
				final Location signLocation = event.getBlock().getLocation();
				final World worldWorld = signLocation.getWorld();
				final RegionManager worldRegionManager = SimpleRegionMarket.wgManager.getWorldGuard().getRegionManager(worldWorld);
				final Player p = event.getPlayer();

				ProtectedRegion protectedRegion = null;

				if (event.getLine(1).isEmpty()) {
					if (worldRegionManager.getApplicableRegions(signLocation).size() == 1) {
						protectedRegion = worldRegionManager.getApplicableRegions(signLocation).iterator().next();
					}
				} else {
					protectedRegion = worldRegionManager.getRegion(event.getLine(1));
				}

				if (protectedRegion == null) {
					LANG_HANDLER.outputError(p, "ERR_REGION_NAME", null);
					event.setCancelled(true);
					event.getBlock().setType(Material.AIR);
					worldWorld.dropItem(signLocation, new ItemStack(Material.SIGN, 1));
					return;
				}

				// TOKEN_MANAGER.addSign() - Sinnvoll?
				final String world = worldWorld.getName();
				final String region = protectedRegion.getId();
				if (token.entries.containsKey(world) && token.entries.get(world).containsKey(region)) {
					final ArrayList<Location> signLocations = Utils.getSignLocations(token, world, region);
					signLocations.add(signLocation);
					Utils.setEntry(token, world, region, "signs", signLocations);
				} else {
					double price;
					if (SimpleRegionMarket.econManager.isEconomy() && !event.getLine(2).isEmpty()) {
						try {
							price = Double.parseDouble(event.getLine(2));
						} catch (final Exception e) {
							LANG_HANDLER.outputError(p, "ERR_NO_PRICE", null);
							event.setCancelled(true);
							event.getBlock().setType(Material.AIR);
							worldWorld.dropItem(signLocation, new ItemStack(Material.SIGN, 1));
							return;
						}
					} else {
						price = 0;
					}

					final double priceMin = Double.parseDouble(token.tplOptions.get("price.min").toString());
					final double priceMax = Double.parseDouble(token.tplOptions.get("price.max").toString());
					if (priceMin > price || price < priceMax) { // TODO existing signs?
						final ArrayList<String> lang = new ArrayList<String>();
						lang.add(String.valueOf(priceMin));
						lang.add(String.valueOf(priceMax));
						LANG_HANDLER.outputError(p, "ERR_PRICE_LIMIT", lang);
						event.setCancelled(true);
						event.getBlock().setType(Material.AIR);
						worldWorld.dropItem(signLocation, new ItemStack(Material.SIGN, 1));
						return;
					}

					String account = p.getName();
					if (!event.getLine(3).isEmpty()) {
						if (SimpleRegionMarket.permManager.isAdmin(p)) {
							if (event.getLine(3).equalsIgnoreCase("none")) {
								account = "";
							} else {
								account = event.getLine(3);
							}
						}
					}

					Utils.setEntry(token, world, region, "price", price);
					Utils.setEntry(token, world, region, "account", account);
					Utils.setEntry(token, world, region, "taken", false);
					Utils.setEntry(token, world, region, "owner", "");

					final ArrayList<Location> signLocations = new ArrayList<Location>();
					signLocations.add(signLocation);
					Utils.setEntry(token, world, region, "signs", signLocations);
				}

				TOKEN_MANAGER.updateSigns(token, world, region);
				break;
			}
		}

		if (!event.isCancelled()) {
			PLUGIN.saveAll();
			for (int i = 0; i < Utils.SIGN_LINES; i++) {
				event.setLine(i, ((Sign) event.getBlock().getState()).getLine(i));
			}
		}
	}
}
