package com.thezorro266.simpleregionmarket.handlers;

/*
 * 
 */

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

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
	 * @param limitHandler
	 *            the limit handler
	 * @param langHandler
	 *            the lang handler
	 */
	public ListenerHandler(SimpleRegionMarket plugin, LimitHandler limitHandler, LanguageHandler langHandler, TokenManager tokenManager) {
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
		/*
		 * final Block b = event.getBlock(); final SignAgent agent = plugin.getAgentManager().getAgent(b.getLocation());
		 * 
		 * if (agent == null) { return; }
		 * 
		 * final Player p = event.getPlayer(); final ProtectedRegion region =
		 * SimpleRegionMarket.getWorldGuard().getRegionManager(b.getLocation().getWorld()).getRegion(agent.getRegion()); if
		 * (!plugin.getAgentManager().isOwner(p, region) && !plugin.isAdmin(p)) { event.setCancelled(true); ((Sign) b.getState()).update(); return; }
		 * 
		 * event.setCancelled(true); if (p != null) { agent.destroyAgent(true); langHandler.outputMessage(p, "AGENT_DELETE", null); }
		 * plugin.getAgentManager().removeAgent(agent); plugin.saveAll();
		 */
	}

	/**
	 * On player interact.
	 * 
	 * @param event
	 *            the event
	 */
	@EventHandler
	public void onPlayerInteract(final PlayerInteractEvent event) {
		/*
		 * if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) { final Block b = event.getClickedBlock(); final SignAgent agent =
		 * plugin.getAgentManager().getAgent(b.getLocation());
		 * 
		 * if (agent == null) { return; }
		 * 
		 * final Player p = event.getPlayer();
		 * 
		 * if (agent.getMode() == SignAgent.MODE_RENT_HOTEL) { if (agent.isRent()) { if (agent.getRent().equals(p.getName())) { final long newRentTime =
		 * agent.getExpireDate().getTime() + agent.getRentTime(); if ((newRentTime - System.currentTimeMillis()) / agent.getRentTime() <
		 * plugin.getConfigurationHandler().getConfig() .getInt("max_rent_multiplier")) { if (Utils.isEconomy()) { final double price = agent.getPrice(); if
		 * (plugin.econHasEnough(p.getName(), price)) { final String account = agent.getAccount(); try { if (account.isEmpty()) {
		 * plugin.econGiveMoney(p.getName(), -price); } else { plugin.econGiveMoney(p.getName(), -price); plugin.econGiveMoney(account, price); } } catch (final
		 * Exception e) { langHandler.outputError(p, "ERR_TRANSACTION", null); return; } agent.setExpireDate(new Date(newRentTime));
		 * plugin.getAgentManager().actAgent(agent, null); langHandler.outputMessage(p, "HOTEL_SUCCESS_RERENT", null); } else { langHandler.outputError(p,
		 * "ERR_NO_MONEY", null); } } else { agent.setExpireDate(new Date(newRentTime)); plugin.getAgentManager().actAgent(agent, null);
		 * langHandler.outputMessage(p, "HOTEL_SUCCESS_RERENT", null); } } else { langHandler.outputError(p, "ERR_RERENT_TOO_LONG", null); } } else {
		 * langHandler.outputError(p, "ERR_ALREADY_RENT", null); } return; } else { if (agent.getProtectedRegion().getParent() != null) { if
		 * (plugin.getAgentManager().isOwner(p, agent.getProtectedRegion().getParent())) { langHandler.outputMessage(p, "HOTEL_YOURS", null); return; } } } }
		 * 
		 * if (!plugin.isAdmin(p)) { if (agent.getMode() == SignAgent.MODE_SELL_REGION) { if (!limitHandler.limitCanBuy(p)) { langHandler.outputError(p,
		 * "ERR_REGION_LIMIT", null); return; } } else if (agent.getMode() == SignAgent.MODE_RENT_HOTEL) { if (!limitHandler.limitCanRent(p)) {
		 * langHandler.outputError(p, "ERR_HOTEL_LIMIT", null); return; } } }
		 * 
		 * final ProtectedRegion region = SimpleRegionMarket.getWorldGuard().getRegionManager(b.getWorld()).getRegion(agent.getRegion());
		 * 
		 * if (agent.getMode() == SignAgent.MODE_SELL_REGION) { if (!plugin.canBuy(p)) { langHandler.outputError(p, "ERR_NO_PERM_BUY", null); } else if
		 * (plugin.getAgentManager().isOwner(p, region)) { if (!agent.getAccount().isEmpty()) { if (p.getName().equals(agent.getAccount())) {
		 * langHandler.outputMessage(p, "AGENT_YOURS", null); } else { langHandler.outputMessage(p, "ERR_REGION_BUY_YOURS", null); } } else {
		 * langHandler.outputMessage(p, "ERR_REGION_BUY_YOURS", null); } } else { if (Utils.isEconomy()) { final String account = agent.getAccount(); final
		 * double price = agent.getPrice(); if (plugin.econHasEnough(p.getName(), price)) { try { if (account.isEmpty()) { plugin.econGiveMoney(p.getName(),
		 * -price); plugin.sellRegion(region, p); final ArrayList<String> list = new ArrayList<String>(); list.add(region.getId()); langHandler.outputMessage(p,
		 * "REGION_BUYED_NONE", list); } else { plugin.econGiveMoney(p.getName(), -price); plugin.econGiveMoney(account, price); plugin.sellRegion(region, p);
		 * final ArrayList<String> list = new ArrayList<String>(); list.add(region.getId()); list.add(account); langHandler.outputMessage(p,
		 * "REGION_BUYED_USER", list); } } catch (final Exception e) { langHandler.outputError(p, "ERR_TRANSACTION", null); return; } } else {
		 * langHandler.outputError(p, "ERR_NO_MONEY", null); } } else { plugin.sellRegion(region, p); final ArrayList<String> list = new ArrayList<String>();
		 * list.add(region.getId()); langHandler.outputMessage(p, "REGION_BUYED_NONE", list); } } } else if (agent.getMode() == SignAgent.MODE_RENT_HOTEL) { if
		 * (!plugin.canRent(p)) { langHandler.outputError(p, "ERR_NO_PERM_RENT", null); } else { if (Utils.isEconomy()) { final String account =
		 * agent.getAccount(); final double price = agent.getPrice(); if (plugin.econHasEnough(p.getName(), price)) { try { if (account.isEmpty()) {
		 * plugin.econGiveMoney(p.getName(), -price); plugin.rentHotel(region, p, agent.getRentTime()); final ArrayList<String> list = new ArrayList<String>();
		 * list.add(region.getId()); langHandler.outputMessage(p, "HOTEL_RENT_NONE", list); } else { plugin.econGiveMoney(p.getName(), -price);
		 * plugin.econGiveMoney(account, price); plugin.rentHotel(region, p, agent.getRentTime()); final ArrayList<String> list = new ArrayList<String>();
		 * list.add(region.getId()); list.add(account); langHandler.outputMessage(p, "HOTEL_RENT_USER", list); } } catch (final Exception e) {
		 * langHandler.outputError(p, "ERR_TRANSACTION", null); return; } } else { langHandler.outputError(p, "ERR_NO_MONEY", null); } } else {
		 * plugin.rentHotel(region, p, agent.getRentTime()); final ArrayList<String> list = new ArrayList<String>(); list.add(region.getId());
		 * langHandler.outputMessage(p, "HOTEL_RENT_NONE", list); } } } }
		 */
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
				final RegionManager worldRegionManager = SimpleRegionMarket.getWorldGuard().getRegionManager(worldWorld);
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

				final String world = worldWorld.getName();
				final String region = protectedRegion.getId();
				if (token.entries.containsKey(world) && token.entries.get(world).containsKey(region)) {
					final ArrayList<Location> signLocations = Utils.getSignLocations(token, world, region);
					signLocations.add(signLocation);
					Utils.setEntry(token, world, region, "signs", signLocations);
				} else {
					double price;
					if (Utils.isEconomy() && !event.getLine(2).isEmpty()) {
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
					if (priceMin > price || price < priceMax) {
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
						if (PLUGIN.isAdmin(p)) {
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

				TOKEN_MANAGER.updateSigns(token.id, world, region);
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
