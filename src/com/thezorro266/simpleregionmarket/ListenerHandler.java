package com.thezorro266.simpleregionmarket;

import java.util.ArrayList;
import java.util.Date;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class ListenerHandler implements Listener {

	public ListenerHandler(Plugin plugin) {
		Bukkit.getPluginManager().registerEvents(this, plugin);
	}

	@EventHandler
	public void onPlayerInteract(final PlayerInteractEvent event) {
		if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
			Block b = event.getClickedBlock();
			SignAgent agent = SimpleRegionMarket.getAgentManager().getAgent(b.getLocation());

			if (agent == null)
				return;

			Player p = event.getPlayer();

			if(agent.getMode() == SignAgent.MODE_RENT_HOTEL) {
				if(agent.isRent()) {
					if (agent.getRent().equals(p.getName())) {
						long newRentTime = agent.getExpireDate().getTime() + agent.getRentTime();
						if(((newRentTime-System.currentTimeMillis()) / agent.getRentTime()) < SimpleRegionMarket.maxRentMultiplier) {
							if(SimpleRegionMarket.isEconomy()) {
								double price = agent.getPrice();
								if (SimpleRegionMarket.econHasEnough(p.getName(), price)) {
									String account = agent.getAccount();
									try {
										if(account.isEmpty()) {
											SimpleRegionMarket.econGiveMoney(p.getName(), -price);
										} else {
											SimpleRegionMarket.econGiveMoney(p.getName(), -price);
											SimpleRegionMarket.econGiveMoney(account, price);
										}
									} catch(Exception e) {
										LanguageHandler.outputError(p, "ERR_TRANSACTION", null);
										return;
									}
									agent.setExpireDate(new Date(newRentTime));
									SimpleRegionMarket.getAgentManager().actAgent(agent, null);
									LanguageHandler.outputDebug(p, "HOTEL_SUCCESS_RERENT", null);
								} else {
									LanguageHandler.outputError(p, "ERR_NO_MONEY", null);
								}
							} else {
								agent.setExpireDate(new Date(newRentTime));
								SimpleRegionMarket.getAgentManager().actAgent(agent, null);
								LanguageHandler.outputDebug(p, "HOTEL_SUCCESS_RERENT", null);
							}
						} else {
							LanguageHandler.outputError(p, "ERR_RERENT_TOO_LONG", null);
						}
					} else {
						LanguageHandler.outputError(p, "ERR_ALREADY_RENT", null);
					}
					return;
				} else {
					if(agent.getProtectedRegion().getParent() != null) {
						if(SimpleRegionMarket.getAgentManager().isOwner(p, agent.getProtectedRegion().getParent())) {
							LanguageHandler.outputDebug(p, "HOTEL_YOURS", null);
							return;
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
					if(SimpleRegionMarket.isEconomy()) {
						String account = agent.getAccount();
						double price = agent.getPrice();
						if (SimpleRegionMarket.econHasEnough(p.getName(), price)) {
							try {
								if(account.isEmpty()) {
									SimpleRegionMarket.econGiveMoney(p.getName(), -price);
									SimpleRegionMarket.sellRegion(region, p);
									ArrayList<String> list = new ArrayList<String>();
									list.add(region.getId());
									LanguageHandler.outputDebug(p, "REGION_BUYED_NONE", list);
								} else {
									SimpleRegionMarket.econGiveMoney(p.getName(), -price);
									SimpleRegionMarket.econGiveMoney(account, price);
									SimpleRegionMarket.sellRegion(region, p);
									ArrayList<String> list = new ArrayList<String>();
									list.add(region.getId());
									list.add(account);
									LanguageHandler.outputDebug(p, "REGION_BUYED_USER", list);
								}
							} catch(Exception e) {
								LanguageHandler.outputError(p, "ERR_TRANSACTION", null);
								return;
							}
						} else {
							LanguageHandler.outputError(p, "ERR_NO_MONEY", null);
						}
					} else {
						SimpleRegionMarket.sellRegion(region, p);
						ArrayList<String> list = new ArrayList<String>();
						list.add(region.getId());
						LanguageHandler.outputDebug(p, "REGION_BUYED_NONE", list);
					}
				}
			} else if(agent.getMode() == SignAgent.MODE_RENT_HOTEL) {
				if(!SimpleRegionMarket.canRent(p)) {
					LanguageHandler.outputError(p, "ERR_NO_PERM_RENT", null);
				} else {
					if(SimpleRegionMarket.isEconomy()) {
						String account = agent.getAccount();
						double price = agent.getPrice();
						if (SimpleRegionMarket.econHasEnough(p.getName(), price)) {
							try {
								if(account.isEmpty()) {
									SimpleRegionMarket.econGiveMoney(p.getName(), -price);
									SimpleRegionMarket.rentHotel(region, p, agent.getRentTime());
									ArrayList<String> list = new ArrayList<String>();
									list.add(region.getId());
									LanguageHandler.outputDebug(p, "HOTEL_RENT_NONE", list);
								} else {
									SimpleRegionMarket.econGiveMoney(p.getName(), -price);
									SimpleRegionMarket.econGiveMoney(account, price);
									SimpleRegionMarket.rentHotel(region, p, agent.getRentTime());
									ArrayList<String> list = new ArrayList<String>();
									list.add(region.getId());
									list.add(account);
									LanguageHandler.outputDebug(p, "HOTEL_RENT_USER", list);
								}
							} catch(Exception e) {
								LanguageHandler.outputError(p, "ERR_TRANSACTION", null);
								return;
							}
						} else {
							LanguageHandler.outputError(p, "ERR_NO_MONEY", null);
						}
					} else {
						SimpleRegionMarket.rentHotel(region, p, agent.getRentTime());
						ArrayList<String> list = new ArrayList<String>();
						list.add(region.getId());
						LanguageHandler.outputDebug(p, "HOTEL_RENT_NONE", list);
					}
				}
			}
		}
	}

	@EventHandler
	public void onBlockBreak(final BlockBreakEvent event) {
		Block b = event.getBlock();
		SignAgent agent = SimpleRegionMarket.getAgentManager().getAgent(b.getLocation());

		if (agent == null)
			return;

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
	}

	@EventHandler
	public void onSignChange(final SignChangeEvent event) {
		if (event.getLine(0).equalsIgnoreCase("[AGENT]") || event.getLine(0).equalsIgnoreCase("[HOTEL]")) {
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

			// If economy is disabled, do not search for a price, but the renttime
			if(SimpleRegionMarket.isEconomy()) {
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
			} else { // ..., but the renttime
				if (event.getLine(2).isEmpty()) {
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

					if(mode == SignAgent.MODE_SELL_REGION && SimpleRegionMarket.isEconomy()) {
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
		}
	}
}
