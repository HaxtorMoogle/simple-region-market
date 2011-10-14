package com.thezorro266.simpleregionmarket;

import java.util.ArrayList;
import java.util.logging.Level;

import org.bukkit.block.Block;
import org.bukkit.block.Sign;
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
			if(b != null) {
				if ((b.getTypeId() == 63) || (b.getTypeId() == 68)) {
					Sign sign = (Sign) b.getState();
					if (sign.getLine(0).equals("[AGENT]")) {
						RegionAgent agent = SimpleRegionMarket.getAgentManager().getAgent(b.getLocation());

						if (agent == null)
							return;

						if(SimpleRegionMarket.getEconomicManager() != null) {
							Player p = event.getPlayer();
							if(AgentManager.max_regions != 0) {
								int count = 0;
								count = SimpleRegionMarket.getWorldGuard().getRegionManager(b.getWorld()).getRegionCountOfPlayer(SimpleRegionMarket.getWorldGuard().wrapPlayer(p));
								if(count >= AgentManager.max_regions && !SimpleRegionMarket.isAdmin(p)) {
									LanguageHandler.outputError(p, "ERR_REGION_LIMIT", null);
									return;
								}
							}

							ProtectedRegion region = SimpleRegionMarket.getWorldGuard().getRegionManager(b.getWorld()).getRegion(agent.getRegion());

							if(!SimpleRegionMarket.canBuy(p)) {
								LanguageHandler.outputError(p, "ERR_NO_PERM_BUY", null);
							} else if (SimpleRegionMarket.getAgentManager().isOwner(p, region)) {
								if(agent.getOwnerPlayer() != null) {
									if(p.equals(agent.getOwnerPlayer())) {
										LanguageHandler.outputDebug(p, "AGENT_YOURS", null);
									} else {
										LanguageHandler.outputDebug(p, "ERR_REGION_BUY_YOURS", null);
									}
								} else {
									LanguageHandler.outputDebug(p, "ERR_REGION_BUY_YOURS", null);
								}
							} else {
								String owner = agent.getOwner();
								if (!SimpleRegionMarket.getEconomicManager().hasAccount(p.getName())) {
									SimpleRegionMarket.getEconomicManager().createAccount(p.getName());
								}
								if (SimpleRegionMarket.getEconomicManager().hasAccount(p.getName())) {
									double price = agent.getPrice();
									if (SimpleRegionMarket.getEconomicManager().getAccount(p.getName()).hasEnough(price)) {
										if(owner.isEmpty()) {
											SimpleRegionMarket.getEconomicManager().getAccount(p.getName()).subtract(price);
											SimpleRegionMarket.sellRegion(region, p);
											ArrayList<String> list = new ArrayList<String>();
											list.add(region.getId());
											LanguageHandler.outputDebug(p, "REGION_BUYED_NONE", list);
										} else {
											if (!SimpleRegionMarket.getEconomicManager().hasAccount(owner)) {
												SimpleRegionMarket.getEconomicManager().createAccount(owner);
											}
											if (SimpleRegionMarket.getEconomicManager().hasAccount(owner)) {
												SimpleRegionMarket.getEconomicManager().getAccount(p.getName()).subtract(price);
												SimpleRegionMarket.getEconomicManager().getAccount(owner).add(price);
												SimpleRegionMarket.sellRegion(region, p);
												ArrayList<String> list = new ArrayList<String>();
												list.add(region.getId());
												list.add(owner);
												LanguageHandler.outputDebug(p, "REGION_BUYED_USER", list);
											} else {
												LanguageHandler.outputError(p, "ERR_ECO_TRANSFER", null);
												ArrayList<String> list = new ArrayList<String>();
												list.add(owner);
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
						} else {
							if(event.getPlayer() != null) {
								LanguageHandler.outputError(event.getPlayer(), "ERR_NO_ECO_USER", null);
							}
						}
					}
				}
			}
		}
	}
}