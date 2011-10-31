package com.thezorro266.simpleregionmarket;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.nijikokun.register.payment.Method;
import com.nijikokun.register.payment.Methods;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class SimpleRegionMarket extends JavaPlugin {
	private static Server server;
	private static ConfigHandler configuration;
	private static AgentManager agentmanager;

	private boolean error = false;

	public static String plugin_dir = null;
	public static String language = "en";
	public static boolean logging = true;

	public static void saveAll() {
		ArrayList<World> done = new ArrayList<World>();
		for(int i=0; i < getAgentManager().getAgentList().size(); i++) {
			World iw = getAgentManager().getAgentList().get(i).getWorldWorld();
			if(!done.contains(iw)) {
		        try {
		        	getWorldGuard().getGlobalRegionManager().get(iw).save();
		        } catch (IOException e) {
		        	LanguageHandler.outputConsole(Level.SEVERE, "WorldGuard >> Failed to write regions file: " + e.getMessage());
		        }
				done.add(iw);
			}
		}
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
			return null;
		}
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
		getAgentManager().removeAgentsFromRegion(region);
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

	private BListener blockListener = new BListener();
	private PListener playerListener = new PListener();

	@Override
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {

		if (!(sender instanceof Player))
			return false;

		Player p = (Player) sender;

		if (args.length < 1)
			return false;

		if (args[0].equalsIgnoreCase("?") || args[0].equalsIgnoreCase("help")) {
			if(canBuy(p)) {
				if(canSell(p)) {
					LanguageHandler.outputDebug(p, "HELP_01", null);
					LanguageHandler.outputDebug(p, "HELP_02", null);
					LanguageHandler.outputDebug(p, "HELP_03", null);
					if(isAdmin(p)) {
						LanguageHandler.outputDebug(p, "HELP_ADM_01", null);
						LanguageHandler.outputDebug(p, "HELP_ADM_02", null);
					} else {
						LanguageHandler.outputDebug(p, "HELP_04", null);
					}
					LanguageHandler.outputDebug(p, "HELP_05", null);
				}
				LanguageHandler.outputDebug(p, "HELP_BUY", null);
			} else {
				LanguageHandler.outputError(p, "ERR_NO_PERM_BUY_SELL", null);
			}
		} else if (args[0].equalsIgnoreCase("version") || args[0].equalsIgnoreCase("v")) {
			LanguageHandler.outputConsole(Level.INFO, "Version " + getDescription().getVersion() + ", updated by theZorro266");
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
				if(list.size() % 8 > 0)
					site++;
				
				int showsite = 0;
				if(site > 0)
					showsite = initsite;
				
				LanguageHandler.outputString(p, "Site " + showsite + " of " + site);
				initsite--;
				initsite *= 8;
				for(int i=initsite; i < initsite+8; i++) {
					if(i >= list.size())
						break;
					
					SignAgent agent = list.get(i);
					LanguageHandler.outputString(p, "Region: " + agent.getRegion() + " - " + getEconomicManager().format(agent.getPrice()));
				}
			}
		} else if (args[0].equalsIgnoreCase("limits")) {
			if(isAdmin(p)) {
				if(args.length < 2) {
					LanguageHandler.outputDebug(p, "CMD_LIMITS_NO_ARG", null);
					return true;
				}
				// TODO /rm limits command
				// /rm limits <buy/rent> <world/(group)/player> <name (of world/(group)/player)> (<new limit>)
				configuration.save();
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

		if(server.getPluginManager().getPlugin("Register") == null) {
			error = true;
			LanguageHandler.langOutputConsole("ERR_NO_REGISTER", Level.SEVERE, null);
			server.getPluginManager().disablePlugin(this);
			return;
		}

		server.getPluginManager().registerEvent(Event.Type.BLOCK_BREAK, blockListener, Event.Priority.Normal, this);
		server.getPluginManager().registerEvent(Event.Type.SIGN_CHANGE, blockListener, Event.Priority.Normal, this);
		server.getPluginManager().registerEvent(Event.Type.PLAYER_INTERACT, playerListener, Event.Priority.Normal, this);
		
		server.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			public void run() {
			getAgentManager().checkAgents();
		}
		}, 20L, 1200L);
		
		LanguageHandler.outputConsole(Level.INFO, "Version " + getDescription().getVersion() + " loaded, updated by theZorro266");
	}
}