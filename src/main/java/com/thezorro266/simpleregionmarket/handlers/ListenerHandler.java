package com.thezorro266.simpleregionmarket.handlers;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import com.thezorro266.simpleregionmarket.SimpleRegionMarket;
import com.thezorro266.simpleregionmarket.TokenManager;
import com.thezorro266.simpleregionmarket.Utils;
import com.thezorro266.simpleregionmarket.signs.TemplateMain;

public class ListenerHandler implements Listener {
	private final LanguageHandler langHandler;
	private final SimpleRegionMarket plugin;
	private final TokenManager tokenManager;

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
		this.plugin = plugin;
		this.langHandler = langHandler;
		this.tokenManager = tokenManager;
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
			final String world = blockLocation.getWorld().getName();
			for (final TemplateMain token : TokenManager.tokenList) {
				if (token.entries.containsKey(world)) {
					for (final String region : token.entries.get(world).keySet()) {
						final ArrayList<Location> signLocations = Utils.getSignLocations(token, world, region);
						if (!signLocations.isEmpty()) {
							for (final Location signLoc : signLocations) {
								if (signLoc.equals(blockLocation)) {
									if (!tokenManager.playerSignBreak(event.getPlayer(), token, world, region, blockLocation)) {
										event.setCancelled(true);
										tokenManager.updateSigns(token, world, region);
									} else {
										plugin.saveAll();
									}
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
				final String world = blockLocation.getWorld().getName();
				final Player player = event.getPlayer();
				for (final TemplateMain token : TokenManager.tokenList) {
					if (token.entries.containsKey(world)) {
						for (final String region : token.entries.get(world).keySet()) {
							final ArrayList<Location> signLocations = Utils.getSignLocations(token, world, region);
							if (!signLocations.isEmpty()) {
								for (final Location signLoc : signLocations) {
									if (signLoc.equals(blockLocation)) {
										tokenManager.playerClickedSign(player, token, world, region);
										plugin.saveAll();
										return;
									}
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
			if (event.getLine(0).equalsIgnoreCase(Utils.getOptionString(token, "input.id"))) {
				final Location signLocation = event.getBlock().getLocation();

				final String lines[] = new String[4];
				for (int i = 0; i < 4; i++) {
					lines[i] = event.getLine(i);
				}

				if (!tokenManager.playerCreatedSign(event.getPlayer(), token, signLocation, lines)) {
					event.getBlock().breakNaturally();
					event.setCancelled(true);
					return;
				}

				for (int i = 0; i < 4; i++) {
					event.setLine(i, ((Sign) event.getBlock().getState()).getLine(i));
				}

				langHandler.playerNormalOut(event.getPlayer(), "PLAYER.REGION.ADDED_SIGN", null); // TODO region did not exist before -> Other msg
				plugin.saveAll();
				break;
			}
		}
	}
}
