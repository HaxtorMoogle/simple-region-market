package com.thezorro266.simpleregionmarket;

import java.util.ArrayList;
import java.util.logging.Level;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerListener;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;

class PListener extends PlayerListener {

	@Override
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
			Block b = event.getClickedBlock();
			SignAgent agent = SimpleRegionMarket.getAgentManager().getAgent(b.getLocation());

			if (agent == null)
				return;

			if(SimpleRegionMarket.getEconomicManager() != null) {
				Player p = event.getPlayer();
				
				if(agent.getMode() == SignAgent.MODE_RENT_HOTEL) {
					if(agent.isRent()) {
						if (agent.getRent().equals(p.getName())) {
							// TODO rent erweiterung
						} else {
							LanguageHandler.outputError(p, "ERR_ALREADY_RENT", null);
						}
					} else {
						if(agent.getProtectedRegion().getParent() != null) {
							if(SimpleRegionMarket.getAgentManager().isOwner(p, agent.getProtectedRegion().getParent())) {
								LanguageHandler.outputDebug(p, "HOTEL_YOURS", null);
							}
						}
					}
				}
				
				if(!SimpleRegionMarket.isAdmin(p)) {
					if(agent.getMode() == SignAgent.MODE_SELL_REGION) {
						if(!LimitHandler.limitCanBuy(p)) {
							LanguageHandler.outputError(p, "ERR_REGION_LIMIT", null);
							return;
						}
					} else if(agent.getMode() == SignAgent.MODE_RENT_HOTEL) {
						if(!LimitHandler.limitCanRent(p)) {
							LanguageHandler.outputError(p, "ERR_HOTEL_LIMIT", null);
							return;
						}
					}
				}

				ProtectedRegion region = SimpleRegionMarket.getWorldGuard().getRegionManager(b.getWorld()).getRegion(agent.getRegion());

				if(agent.getMode() == SignAgent.MODE_SELL_REGION) {
					if(!SimpleRegionMarket.canBuy(p)) {
						LanguageHandler.outputError(p, "ERR_NO_PERM_BUY", null);
					} else if (SimpleRegionMarket.getAgentManager().isOwner(p, region)) {
						if(!agent.getAccount().isEmpty()) {
							if(p.getName().equals(agent.getAccount())) {
								LanguageHandler.outputDebug(p, "AGENT_YOURS", null);
							} else {
								LanguageHandler.outputDebug(p, "ERR_REGION_BUY_YOURS", null);
							}
						} else {
							LanguageHandler.outputDebug(p, "ERR_REGION_BUY_YOURS", null);
						}
					} else {
						String account = agent.getAccount();
						if (!SimpleRegionMarket.getEconomicManager().hasAccount(p.getName())) {
							SimpleRegionMarket.getEconomicManager().createAccount(p.getName());
						}
						if (SimpleRegionMarket.getEconomicManager().hasAccount(p.getName())) {
							double price = agent.getPrice();
							if (SimpleRegionMarket.getEconomicManager().getAccount(p.getName()).hasEnough(price)) {
								if(account.isEmpty()) {
									SimpleRegionMarket.getEconomicManager().getAccount(p.getName()).subtract(price);
									SimpleRegionMarket.sellRegion(region, p);
									ArrayList<String> list = new ArrayList<String>();
									list.add(region.getId());
									LanguageHandler.outputDebug(p, "REGION_BUYED_NONE", list);
								} else {
									if (!SimpleRegionMarket.getEconomicManager().hasAccount(account)) {
										SimpleRegionMarket.getEconomicManager().createAccount(account);
									}
									if (SimpleRegionMarket.getEconomicManager().hasAccount(account)) {
										SimpleRegionMarket.getEconomicManager().getAccount(p.getName()).subtract(price);
										SimpleRegionMarket.getEconomicManager().getAccount(account).add(price);
										SimpleRegionMarket.sellRegion(region, p);
										ArrayList<String> list = new ArrayList<String>();
										list.add(region.getId());
										list.add(account);
										LanguageHandler.outputDebug(p, "REGION_BUYED_USER", list);
									} else {
										LanguageHandler.outputError(p, "ERR_ECO_TRANSFER", null);
										ArrayList<String> list = new ArrayList<String>();
										list.add(account);
										LanguageHandler.langOutputConsole("ERR_CREATE_ECO_ACCOUNT", Level.SEVERE, list);
									}
								}
							} else {
								LanguageHandler.outputError(p, "ERR_NO_MONEY", null);
							}
						} else {
							LanguageHandler.outputError(p, "ERR_ECO_TRANSFER", null);
							ArrayList<String> list = new ArrayList<String>();
							list.add(p.getName());
							LanguageHandler.langOutputConsole("ERR_CREATE_ECO_ACCOUNT", Level.SEVERE, list);
						}
					}
				} else if(agent.getMode() == SignAgent.MODE_RENT_HOTEL) {
					if(!SimpleRegionMarket.canRent(p)) {
						LanguageHandler.outputError(p, "ERR_NO_PERM_RENT", null);
					} else {
						String account = agent.getAccount();
						if (!SimpleRegionMarket.getEconomicManager().hasAccount(p.getName())) {
							SimpleRegionMarket.getEconomicManager().createAccount(p.getName());
						}
						if (SimpleRegionMarket.getEconomicManager().hasAccount(p.getName())) {
							double price = agent.getPrice();
							if (SimpleRegionMarket.getEconomicManager().getAccount(p.getName()).hasEnough(price)) {
								if(account.isEmpty()) {
									SimpleRegionMarket.getEconomicManager().getAccount(p.getName()).subtract(price);
									SimpleRegionMarket.rentHotel(region, p, agent.getRentTime());
									ArrayList<String> list = new ArrayList<String>();
									list.add(region.getId());
									LanguageHandler.outputDebug(p, "HOTEL_RENT_NONE", list);
								} else {
									if (!SimpleRegionMarket.getEconomicManager().hasAccount(account)) {
										SimpleRegionMarket.getEconomicManager().createAccount(account);
									}
									if (SimpleRegionMarket.getEconomicManager().hasAccount(account)) {
										SimpleRegionMarket.getEconomicManager().getAccount(p.getName()).subtract(price);
										SimpleRegionMarket.getEconomicManager().getAccount(account).add(price);
										SimpleRegionMarket.rentHotel(region, p, agent.getRentTime());
										ArrayList<String> list = new ArrayList<String>();
										list.add(region.getId());
										list.add(account);
										LanguageHandler.outputDebug(p, "HOTEL_RENT_USER", list);
									} else {
										LanguageHandler.outputError(p, "ERR_ECO_TRANSFER", null);
										ArrayList<String> list = new ArrayList<String>();
										list.add(account);
										LanguageHandler.langOutputConsole("ERR_CREATE_ECO_ACCOUNT", Level.SEVERE, list);
									}
								}
							} else {
								LanguageHandler.outputError(p, "ERR_NO_MONEY", null);
							}
						} else {
							LanguageHandler.outputError(p, "ERR_ECO_TRANSFER", null);
							ArrayList<String> list = new ArrayList<String>();
							list.add(p.getName());
							LanguageHandler.langOutputConsole("ERR_CREATE_ECO_ACCOUNT", Level.SEVERE, list);
						}
					}
				}
			} else {
				if(event.getPlayer() != null) {
					LanguageHandler.outputError(event.getPlayer(), "ERR_NO_ECO_USER", null);
				}
			}
		}
	}
}