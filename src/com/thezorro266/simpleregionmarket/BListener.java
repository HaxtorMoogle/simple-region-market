package com.thezorro266.simpleregionmarket;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.ItemStack;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;

class BListener extends BlockListener {

	@Override
	public void onBlockBreak(BlockBreakEvent event) {
		Block b = event.getBlock();
		SignAgent agent = SimpleRegionMarket.getAgentManager().getAgent(b.getLocation());

		if (agent == null)
			return;

		if(SimpleRegionMarket.getEconomicManager() != null) {
			Player p = event.getPlayer();
			ProtectedRegion region = SimpleRegionMarket.getWorldGuard().getRegionManager(b.getLocation().getWorld()).getRegion(agent.getRegion());
			if (!SimpleRegionMarket.getAgentManager().isOwner(p, region) && !SimpleRegionMarket.isAdmin(p)) {
				event.setCancelled(true);
				((Sign)b.getState()).update();
				return;
			}

			event.setCancelled(true);
			if (p != null) {
				agent.destroyAgent(true);
				LanguageHandler.outputDebug(p, "AGENT_DELETE", null);
			}
			SimpleRegionMarket.getAgentManager().removeAgent(agent);
			SimpleRegionMarket.saveAll();
		} else {
			if(event.getPlayer() != null) {
				LanguageHandler.outputError(event.getPlayer(), "ERR_NO_ECO_USER", null);
			}
		}
	}

	@Override
	public void onSignChange(SignChangeEvent event) {
		if (event.getLine(0).equalsIgnoreCase("[AGENT]") || event.getLine(0).equalsIgnoreCase("[HOTEL]")) {
			if(SimpleRegionMarket.getEconomicManager() != null) {
				ProtectedRegion region;
				Location signloc = event.getBlock().getLocation();

				// Get Mode from '[AGENT]' or '[HOTEL]'
				int mode = 0;
				if(event.getLine(0).equalsIgnoreCase("[AGENT]")) {
					mode = SignAgent.MODE_SELL_REGION;
				} else if (event.getLine(0).equalsIgnoreCase("[HOTEL]")) {
					mode = SignAgent.MODE_RENT_HOTEL;
				}

				Player p = event.getPlayer();

				// Get Region from above or line 2 on the sign
				if (event.getLine(1).isEmpty()) {
					region = SimpleRegionMarket.getAgentManager().getRegion(signloc);
				} else {
					region = SimpleRegionMarket.getWorldGuard().getRegionManager(signloc.getWorld()).getRegion(event.getLine(1));
				}

				if (region == null) {
					if (p != null) {
						LanguageHandler.outputError(p, "ERR_REGION_NAME", null);
					}
					event.setCancelled(true);
					event.getBlock().setType(Material.AIR);
					signloc.getWorld().dropItem(signloc, new ItemStack(Material.SIGN, 1));
					return;
				}

				// Check player permission
				if (p != null) {
					if(mode == SignAgent.MODE_SELL_REGION) {
						if(!SimpleRegionMarket.canSell(p)) {
							event.setCancelled(true);
							LanguageHandler.outputDebug(p, "ERR_NO_PERM_SELL", null);
							event.getBlock().setType(Material.AIR);
							signloc.getWorld().dropItem(signloc, new ItemStack(Material.SIGN, 1));
							return;
						}
						if (!SimpleRegionMarket.isAdmin(p) && !SimpleRegionMarket.getAgentManager().isOwner(p, region)) {
							LanguageHandler.outputError(p, "ERR_REGION_NO_OWNER", null);
							event.setCancelled(true);
							event.getBlock().setType(Material.AIR);
							signloc.getWorld().dropItem(signloc, new ItemStack(Material.SIGN, 1));
							return;
						}
					} else if(mode == SignAgent.MODE_RENT_HOTEL) {
						if(!SimpleRegionMarket.canCreate(p)) {
							event.setCancelled(true);
							LanguageHandler.outputDebug(p, "ERR_NO_PERM_RENT_CREATE", null);
							event.getBlock().setType(Material.AIR);
							signloc.getWorld().dropItem(signloc, new ItemStack(Material.SIGN, 1));
							return;
						}
						if (!SimpleRegionMarket.isAdmin(p) && !SimpleRegionMarket.getAgentManager().isOwner(p, region.getParent())) {
							LanguageHandler.outputError(p, "ERR_PARENT_NO_OWNER", null);
							event.setCancelled(true);
							event.getBlock().setType(Material.AIR);
							signloc.getWorld().dropItem(signloc, new ItemStack(Material.SIGN, 1));
							return;
						}
					}
				}

				// Check renttime and price
				long renttime = 0;
				double price = 0;
				String[] linetwo = null;
				if(mode == SignAgent.MODE_RENT_HOTEL) {
					linetwo = event.getLine(2).split(":");
					if(linetwo.length == 1) {
						try {
							renttime = AgentManager.parseSignTime(linetwo[0]);
						} catch(Exception e) {
							if (p != null) {
								LanguageHandler.outputError(p, "ERR_NO_RENTTIME", null);
							}
							event.setCancelled(true);
							event.getBlock().setType(Material.AIR);
							signloc.getWorld().dropItem(signloc, new ItemStack(Material.SIGN, 1));
							return;
						}
					} else if(linetwo.length > 2) {
						if (p != null) {
							LanguageHandler.outputError(p, "ERR_NO_PRICE_RENTTIME", null);
						}
						event.setCancelled(true);
						event.getBlock().setType(Material.AIR);
						signloc.getWorld().dropItem(signloc, new ItemStack(Material.SIGN, 1));
						return;
					} else if(linetwo.length == 2) {
						try {
							renttime = AgentManager.parseSignTime(linetwo[1]);
						} catch(Exception e) {
							if (p != null) {
								LanguageHandler.outputError(p, "ERR_NO_RENTTIME", null);
							}
							event.setCancelled(true);
							event.getBlock().setType(Material.AIR);
							signloc.getWorld().dropItem(signloc, new ItemStack(Material.SIGN, 1));
							return;
						}
					}
				}

				if (event.getLine(2).isEmpty()) {
					if (SimpleRegionMarket.getAgentManager().countAgents(region) > 0) {
						price = SimpleRegionMarket.getAgentManager().getRegionPrice(region, null);
					} else {
						price = -1;
					}
					if (price < 0) {
						if (p != null) {
							LanguageHandler.outputError(p, "ERR_NO_PRICE", null);
						}
						event.setCancelled(true);
						event.getBlock().setType(Material.AIR);
						signloc.getWorld().dropItem(signloc, new ItemStack(Material.SIGN, 1));
						return;
					}
					if(mode == SignAgent.MODE_RENT_HOTEL) {
						if (SimpleRegionMarket.getAgentManager().countAgents(region) > 0) {
							renttime = SimpleRegionMarket.getAgentManager().getRegionRentTime(region);
						} else {
							renttime = -1;
						}
						if (renttime <= 0) {
							if (p != null) {
								LanguageHandler.outputError(p, "ERR_NO_RENTTIME", null);
							}
							event.setCancelled(true);
							event.getBlock().setType(Material.AIR);
							signloc.getWorld().dropItem(signloc, new ItemStack(Material.SIGN, 1));
							return;
						}
					}
				} else {
					if(mode == SignAgent.MODE_RENT_HOTEL) {
						if(linetwo == null || linetwo.length == 1) {
							if (p != null) {
								LanguageHandler.outputError(p, "ERR_NO_PRICE", null);
							}
							event.setCancelled(true);
							event.getBlock().setType(Material.AIR);
							signloc.getWorld().dropItem(signloc, new ItemStack(Material.SIGN, 1));
							return;
						}
					}
					try {
						if(mode == SignAgent.MODE_RENT_HOTEL) {
							price = Double.parseDouble(linetwo[0]);
						} else {
							price = Double.parseDouble(event.getLine(2));
						}
					} catch (Exception e) {
						if (p != null) {
							LanguageHandler.outputError(p, "ERR_NO_PRICE", null);
						}
						event.setCancelled(true);
						event.getBlock().setType(Material.AIR);
						signloc.getWorld().dropItem(signloc, new ItemStack(Material.SIGN, 1));
						return;
					}
					if (price < 0) {
						if (p != null) {
							LanguageHandler.outputError(p, "ERR_PRICE_UNDER_ZERO", null);
						}
						event.setCancelled(true);
						event.getBlock().setType(Material.AIR);
						signloc.getWorld().dropItem(signloc, new ItemStack(Material.SIGN, 1));
						return;
					}
				}

				// Check account
				String account = p.getName();
				if(!event.getLine(3).isEmpty()) {
					if(SimpleRegionMarket.isAdmin(p)) {
						if(event.getLine(3).equalsIgnoreCase("none")) {
							account = "";
						} else {
							account = event.getLine(3);
						}
					}
				}

				// Create
				SignAgent newagent = SimpleRegionMarket.getAgentManager().addAgent(mode, signloc, region, price, account, renttime);

				// Check and finalize
				if (newagent != null) {
					if (p != null) {

						// Successful message
						if(mode == SignAgent.MODE_SELL_REGION) {
							if(account.isEmpty()) {
								LanguageHandler.outputDebug(p, "REGION_OFFER_NONE", null);
							} else {
								LanguageHandler.outputDebug(p, "REGION_OFFER_USER", null);
							}
						} else if(mode == SignAgent.MODE_RENT_HOTEL) {
							if(account.isEmpty()) {
								LanguageHandler.outputDebug(p, "HOTEL_OFFER_NONE", null);
							} else {
								LanguageHandler.outputDebug(p, "HOTEL_OFFER_USER", null);
							}
						}

						if(mode == SignAgent.MODE_SELL_REGION) {
							SimpleRegionMarket.getAgentManager().getRegionPrice(region, p);
						}

						if (SimpleRegionMarket.getAgentManager().countAgents(region) > 1) {
							ArrayList<String> list = new ArrayList<String>();
							list.add(Integer.toString(SimpleRegionMarket.getAgentManager().countAgents(region)));
							LanguageHandler.outputDebug(p, "AGENT_PLACED", list);
						}
					}

					SimpleRegionMarket.getAgentManager().actAgent(newagent, event);

					SimpleRegionMarket.saveAll();
				} else {
					if (p != null) {
						LanguageHandler.outputError(p, "ERR_PLACE_AGENT", null);
					}
					event.setCancelled(true);
					event.getBlock().setType(Material.AIR);
					signloc.getWorld().dropItem(signloc, new ItemStack(Material.SIGN, 1));
					return;
				}
			} else {
				if(event.getPlayer() != null) {
					LanguageHandler.outputError(event.getPlayer(), "ERR_NO_ECO_USER", null);
				}
			}
		}
	}
}