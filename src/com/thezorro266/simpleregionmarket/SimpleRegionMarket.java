package com.thezorro266.simpleregionmarket;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.nijikokun.register.payment.Method;
import com.nijikokun.register.payment.Methods;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class SimpleRegionMarket extends JavaPlugin {
	private static Server server;
	private static ConfigHandler configuration;
	private static AgentManager agentmanager;

	// public static Permission permission = null;
	public static Economy economy = null;

	private boolean error = false;

	public static int maxRentMultiplier = 2;
	public static int enableEconomy = 1;
	public static String plugin_dir = null;
	public static String language = "en";
	public static String agentName = "[AGENT]";
	public static String hotelName = "[HOTEL]";
	public static boolean removeBuyedSigns = true;
	public static boolean logging = true;
	public static boolean unloading = false;

	public static void saveAll() {
		if(getWorldGuard() != null
				&& getWorldGuard().isEnabled()) {
			for(World w: server.getWorlds()) {
				RegionManager mgr = getWorldGuard().getGlobalRegionManager().get(w);

				try {
					mgr.save();
				} catch (IOException e) {
					LanguageHandler.outputConsole(Level.SEVERE, "WorldGuard >> Failed to write regionsfile: " + e.getMessage());
				}
			}
		} else {
			if(!unloading) {
				LanguageHandler.outputConsole(Level.SEVERE, "Saving WorldGuard failed, because it is not loaded.");
			}
		}
		LimitHandler.saveLimits();
		configuration.save();
	}

	public static AgentManager getAgentManager() {
		return agentmanager;
	}

	public static WorldGuardPlugin getWorldGuard() {
		Plugin plugin = server.getPluginManager().getPlugin("WorldGuard");

		if (plugin == null || !(plugin instanceof WorldGuardPlugin))
			return null;

		return (WorldGuardPlugin) plugin;
	}

	public static Method getEconomicManager() {
		if(Methods.hasMethod())
			return Methods.getMethod();
		else {
			LanguageHandler.langOutputConsole("ERR_NO_ECO", Level.SEVERE, null);
			enableEconomy = 0;
			return null;
		}
	}

	public static boolean isEconomy() {
		return (enableEconomy > 0 &&
				(enableEconomy != 1 || getEconomicManager() != null));
	}
	
	public static String econFormat(double price) {
		String ret = String.valueOf(price);
		if(enableEconomy == 1) {
			if(getEconomicManager() != null) {
				ret = getEconomicManager().format(price);
			}
		} else if(enableEconomy == 2) {
			ret = economy.format(price);
		}
		return ret;
	}

	public static boolean econHasEnough(String account, double money) {
		boolean ret = false;
		if(enableEconomy == 1) {
			if(getEconomicManager() != null) {
				ret = getEconomicManager().getAccount(account).hasEnough(money);
			}
		} else if(enableEconomy == 2) {
			ret = economy.has(account, money);
		}
		return ret;
	}

	public static boolean econGiveMoney(String account, double money) throws Exception {
		boolean ret = true;
		if(enableEconomy == 1) {
			if(getEconomicManager() != null) {
				if(money > 0)
					getEconomicManager().getAccount(account).add(money);
				else
					getEconomicManager().getAccount(account).subtract(-money);
			}
		} else if(enableEconomy == 2) {
			try {
				if(money > 0)
					economy.depositPlayer(account, money);
				else
					economy.withdrawPlayer(account, -money);
			} catch(Exception e) {
				throw e;
			}
		}
		return ret;
	}

	public static boolean canBuy(Player player) {
		return (player.hasPermission("simpleregionmarket.buy") || canSell(player) || isAdmin(player));
	}

	public static boolean canSell(Player player) {
		return (player.hasPermission("simpleregionmarket.sell") || isAdmin(player));
	}

	public static boolean canRent(Player player) {
		return (player.hasPermission("simpleregionmarket.rent") || canCreate(player) || isAdmin(player));
	}

	public static boolean canCreate(Player player) {
		return (player.hasPermission("simpleregionmarket.create") || isAdmin(player));
	}

	public static boolean isAdmin(Player player) {
		return (player.hasPermission("simpleregionmarket.admin") || player.isOp());
	}

	private Boolean setupEconomy()
	{
		RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
		if (economyProvider != null) {
			economy = economyProvider.getProvider();
		}

		return (economy != null);
	}

	/*
	private Boolean setupPermissions()
    {
        RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
        if (permissionProvider != null) {
            permission = permissionProvider.getProvider();
        }
        return (permission != null);
    }
	 */

	public static void sellRegion(ProtectedRegion region, Player p) {
		for (String player : region.getOwners().getPlayers()) {
			Player powner;
			powner = Bukkit.getPlayerExact(player);
			if (powner != null) {
				ArrayList<String> list = new ArrayList<String>();
				list.add(region.getId());
				list.add(p.getName());
				LanguageHandler.outputDebug(powner, "REGION_SOLD", list);
			}
		}
		region.setMembers(new DefaultDomain());
		region.setOwners(new DefaultDomain());
		region.getOwners().addPlayer(getWorldGuard().wrapPlayer(p));

		Iterator<SignAgent> itr = getAgentManager().getAgentList().iterator();
		if(removeBuyedSigns) {
			while(itr.hasNext()) {
				SignAgent obj = itr.next();
				if(obj.getProtectedRegion() == region) {
					obj.destroyAgent(false);
					itr.remove();
				}
			}
		} else {
			while(itr.hasNext()) {
				SignAgent obj = itr.next();
				if(obj.getProtectedRegion() == region) {
					Sign agentsign = (Sign)obj.getLocation().getBlock().getState();
					agentsign.setLine(2, p.getName());
					agentsign.update();
					itr.remove();
				}
			}
		}

		saveAll();
		if(logging) {
			ArrayList<String> list = new ArrayList<String>();
			list.add(region.getId());
			list.add(p.getName());
			LanguageHandler.langOutputConsole("LOG_SOLD_REGION", Level.INFO, list);
		}
	}

	public static void rentHotel(ProtectedRegion region, Player p, long renttime) {
		if(region.getParent() != null) {
			for (String player : region.getParent().getOwners().getPlayers()) {
				Player powner;
				powner = Bukkit.getPlayerExact(player);
				if (powner != null) {
					ArrayList<String> list = new ArrayList<String>();
					list.add(region.getId());
					list.add(p.getName());
					LanguageHandler.outputDebug(powner, "HOTEL_RENT", list);
				}
			}
		}
		region.setMembers(new DefaultDomain());
		region.setOwners(new DefaultDomain());
		region.getMembers().addPlayer(getWorldGuard().wrapPlayer(p));
		getAgentManager().rentRegionForPlayer(region, p, renttime);
		saveAll();
		if(logging) {
			ArrayList<String> list = new ArrayList<String>();
			list.add(region.getId());
			list.add(p.getName());
			LanguageHandler.langOutputConsole("LOG_RENT_HOTEL", Level.INFO, list);
		}
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {

		if (!(sender instanceof Player))
			return false;

		Player p = (Player) sender;

		if (args.length < 1)
			return false;

		if (args[0].equalsIgnoreCase("?") || args[0].equalsIgnoreCase("help")) {
			LanguageHandler.outputDebug(p, "HELP", null);
		} else if (args[0].equalsIgnoreCase("version") || args[0].equalsIgnoreCase("v")) {
			LanguageHandler.outputString(p, "loaded version " + getDescription().getVersion() + ",  Copyright (C) 2011  theZorro266");
		} else if (args[0].equalsIgnoreCase("list") || args[0].equalsIgnoreCase("l")) {
			if(getAgentManager().getAgentList().size() > 200) {
				LanguageHandler.outputError(p, "CMD_LIST_TOO_MANY_REGIONS", null);
			} else {
				ArrayList<SignAgent> list = new ArrayList<SignAgent>();
				for (SignAgent agent : getAgentManager().getAgentList()) {
					if (agent.getWorldWorld() == p.getWorld() && agent.getMode() == SignAgent.MODE_SELL_REGION) {
						boolean add = true;
						for (SignAgent tmp : list) {
							if (tmp.getProtectedRegion() == agent.getProtectedRegion()) {
								add = false;
								break;
							}
						}
						if (add) {
							list.add(agent);
						}
					}
				}

				if(list.size() < 1) {
					LanguageHandler.outputDebug(p, "CMD_LIST_NO_REGIONS", null);
					return true;
				}

				int initsite = 1;
				if(args.length > 1) {
					try {
						initsite = Integer.parseInt(args[1]);
					} catch (Exception e) {
						LanguageHandler.outputError(p, "CMD_LIST_WRONG_ARG", null);
						return true;
					}

					if(initsite < 1) {
						LanguageHandler.outputError(p, "CMD_LIST_WRONG_ARG", null);
						return true;
					}
				}

				int site = list.size()/8;
				if(list.size() % 8 > 0) {
					site++;
				}

				int showsite = 0;
				if(site > 0) {
					showsite = initsite;
				}

				LanguageHandler.outputString(p, "Site " + showsite + " of " + site);
				initsite--;
				initsite *= 8;
				for(int i=initsite; i < initsite+8; i++) {
					if(i >= list.size()) {
						break;
					}

					SignAgent agent = list.get(i);
					if(isEconomy()) {
						LanguageHandler.outputString(p, "Region: " + agent.getRegion() + " - " + econFormat(agent.getPrice()));
					} else {
						LanguageHandler.outputString(p, "Region: " + agent.getRegion());
					}
				}
			}
		} else if (args[0].equalsIgnoreCase("limits") || args[0].equalsIgnoreCase("limit")) {
			if(isAdmin(p)) {
				if(args.length < 2) { // limits
					LanguageHandler.outputDebug(p, "CMD_LIMITS_NO_ARG", null);
					return true;
				} else {
					int mode;
					if(args[1].equalsIgnoreCase("buy") || args[1].equalsIgnoreCase("regions")) {
						mode = 0;
					} else if(args[1].equalsIgnoreCase("rent") || args[1].equalsIgnoreCase("rooms")) {
						mode = 1;
					} else {
						LanguageHandler.outputError(p, "CMD_LIMITS_WRONG_ARG", null);
						return true;
					}
					int limit;
					if(args.length < 3) { // limits buy|rent - Will show global limits of buying regions
						ArrayList<String> list = new ArrayList<String>();
						if(mode == 0) {
							list.add(Integer.toString(LimitHandler.getGlobalBuyLimit()));
						} else if(mode == 1) {
							list.add(Integer.toString(LimitHandler.getGlobalRentLimit()));
						}
						LanguageHandler.outputDebug(p, "CMD_LIMITS_OUTPUT_LIMIT", list);
					} else { // limits buy|rent <limit>|<...>
						try {
							limit = Integer.parseInt(args[2]);
							if(mode == 0) {
								LimitHandler.setGlobalBuyLimit(limit);
							} else if(mode == 1) {
								LimitHandler.setGlobalRentLimit(limit);
							}
							ArrayList<String> list = new ArrayList<String>();
							list.add(Integer.toString(limit));
							LanguageHandler.outputDebug(p, "CMD_LIMITS_SET_LIMIT", list);
						} catch(Exception e) {
							if(args[2].equalsIgnoreCase("world")) {
								World w = Bukkit.getWorld(args[3]);
								if(w != null) {
									if(args.length < 4) { // limits buy|rent world
										LanguageHandler.outputError(p, "CMD_LIMITS_NO_WORLD", null);
									} else if(args.length < 5) { // limits buy|rent world <name>
										ArrayList<String> list = new ArrayList<String>();
										if(mode == 0) {
											list.add(Integer.toString(LimitHandler.getBuyWorldLimit(w)));
										} else if(mode == 1) {
											list.add(Integer.toString(LimitHandler.getRentWorldLimit(w)));
										}
										LanguageHandler.outputDebug(p, "CMD_LIMITS_OUTPUT_LIMIT", list);
									} else { // limits buy|rent world <name> <limit>
										try {
											limit = Integer.parseInt(args[4]);
											if(mode == 0) {
												LimitHandler.setBuyWorldLimit(w, limit);
											} else if(mode == 1) {
												LimitHandler.setRentWorldLimit(w, limit);
											}
											ArrayList<String> list = new ArrayList<String>();
											list.add(Integer.toString(limit));
											LanguageHandler.outputDebug(p, "CMD_LIMITS_SET_LIMIT", list);
										} catch(Exception e2) {
											LanguageHandler.outputError(p, "CMD_LIMITS_WRONG_ARG", null);
										}
									}
								} else {
									LanguageHandler.outputError(p, "CMD_LIMITS_NO_WORLD", null);
								}
							} else if(args[2].equalsIgnoreCase("player")) {
								Player p2 = Bukkit.getPlayer(args[3]);
								if(p2 != null) {
									if(args.length < 4) { // limits buy|rent player
										LanguageHandler.outputError(p, "CMD_LIMITS_NO_PLAYER", null);
									} else if(args.length < 5) { // limits buy|rent player <name>
										ArrayList<String> list = new ArrayList<String>();
										if(mode == 0) {
											list.add(Integer.toString(LimitHandler.getBuyPlayerLimit(p2)));
										} else if(mode == 1) {
											list.add(Integer.toString(LimitHandler.getRentPlayerLimit(p2)));
										}
										LanguageHandler.outputDebug(p, "CMD_LIMITS_OUTPUT_LIMIT", list);
									} else { // limits buy|rent player <name> <limit>
										try {
											limit = Integer.parseInt(args[4]);
											if(mode == 0) {
												LimitHandler.setBuyPlayerLimit(p2, limit);
											} else if(mode == 1) {
												LimitHandler.setRentPlayerLimit(p2, limit);
											}
											ArrayList<String> list = new ArrayList<String>();
											list.add(Integer.toString(limit));
											LanguageHandler.outputDebug(p, "CMD_LIMITS_SET_LIMIT", list);
										} catch(Exception e2) {
											LanguageHandler.outputError(p, "CMD_LIMITS_WRONG_ARG", null);
										}
									}
								} else {
									LanguageHandler.outputError(p, "CMD_LIMITS_NO_PLAYER", null);
								}
							} else {
								LanguageHandler.outputError(p, "CMD_LIMITS_WRONG_ARG", null);
							}
						}
					}
				}

				LimitHandler.saveLimits();
			} else {
				LanguageHandler.outputError(p, "ERR_NO_PERM", null);
			}
		} else if (args[0].equalsIgnoreCase("lang")) {
			if(isAdmin(p)) {
				if(args.length < 2) {
					ArrayList<String> list = new ArrayList<String>();
					list.add(language);
					LanguageHandler.outputDebug(p, "CMD_LANG_NO_ARG", list);
				} else {
					if(LanguageHandler.setLang(args[1])) {
						language = args[1];
						ArrayList<String> list = new ArrayList<String>();
						list.add(language);
						LanguageHandler.outputDebug(p, "CMD_LANG_SWITCHED", list);
						configuration.save();
					} else {
						LanguageHandler.outputError(p, "CMD_LANG_NO_LANG", null);
					}
				}
			} else {
				LanguageHandler.outputError(p, "ERR_NO_PERM", null);
			}
		} else if (args[0].equalsIgnoreCase("logging")) {
			if(isAdmin(p)) {
				ArrayList<String> list = new ArrayList<String>();
				if(args.length < 2) {
					list.add(Boolean.toString(logging));
					LanguageHandler.outputDebug(p, "CMD_LOGGING_NO_ARG", list);
				} else {
					boolean log = false;
					log = Boolean.parseBoolean(args[1]);
					if(logging != log) {
						logging = log;
						list.add(Boolean.toString(log));
						LanguageHandler.outputDebug(p, "CMD_LOGGING_SET", list);
						LanguageHandler.langOutputConsole("LOG_SWITCHED", Level.INFO, list);
					} else {
						list.add(Boolean.toString(logging));
						LanguageHandler.outputDebug(p, "CMD_LOGGING_ALREADY_SET", list);
					}
					configuration.save();
				}
			}
		} else
			return false;
		return true;
	}

	@Override
	public void onDisable() {
		unloading = true;
		if(error) {
			LanguageHandler.langOutputConsole("ERR_PLUGIN_UNLOAD", Level.SEVERE, null);
		} else {
			saveAll();
			LanguageHandler.langOutputConsole("PLUGIN_UNLOAD", Level.INFO, null);
		}
	}

	@Override
	public void onEnable() {
		server = getServer();
		agentmanager = new AgentManager();
		plugin_dir = getDataFolder() + File.separator;

		configuration = new ConfigHandler();
		configuration.load();

		LanguageHandler.setLang(language);

		if (getWorldGuard() == null) {
			error = true;
			LanguageHandler.langOutputConsole("ERR_NO_WORLDGUARD", Level.SEVERE, null);
			server.getPluginManager().disablePlugin(this);
			return;
		}

		if(enableEconomy > 0) {
			if(server.getPluginManager().getPlugin("Register") == null
					&& server.getPluginManager().getPlugin("Vault") == null) {
				LanguageHandler.langOutputConsole("NO_REGISTER_VAULT", Level.WARNING, null);
				enableEconomy = 0;
			} else if(server.getPluginManager().getPlugin("Register") != null
					&& server.getPluginManager().getPlugin("Vault") == null) {
				enableEconomy = 1;
			} else {
				enableEconomy = 2;
				/*
				if(!setupPermissions()) {
					LanguageHandler.langOutputConsole("ERR_VAULT_PERMISSIONS", Level.WARNING, null);
				}
				 */
				if(!setupEconomy()) {
					LanguageHandler.langOutputConsole("ERR_VAULT_ECONOMY", Level.WARNING, null);
					enableEconomy = 0;
				}
			}
		}

		new ListenerHandler(this);

		server.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			@Override
			public void run() {
				getAgentManager().checkAgents();
			}
		}, 20L, 1200L);

		LimitHandler.loadLimits();

		LanguageHandler.outputConsole(Level.INFO, "loaded version " + getDescription().getVersion() + ",  Copyright (C) 2011-2012  Benedikt Ziemons aka theZorro266 - All rights reserved.");
	}
}