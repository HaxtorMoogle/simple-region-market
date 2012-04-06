/*
 * 
 */
package com.thezorro266.simpleregionmarket;

import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

// TODO: Auto-generated Javadoc
/**
 * The Class CommandHandler.
 */
public class CommandHandler implements CommandExecutor {
	
	/** The lang handler. */
	private final LanguageHandler langHandler;
	
	/** The limit handler. */
	private final LimitHandler limitHandler;
	
	/** The plugin. */
	private final SimpleRegionMarket plugin;

	/**
	 * Instantiates a new command handler.
	 *
	 * @param plugin the plugin
	 * @param limitHandler the limit handler
	 * @param langHandler the lang handler
	 */
	public CommandHandler(SimpleRegionMarket plugin, LimitHandler limitHandler, LanguageHandler langHandler) {
		this.plugin = plugin;
		this.limitHandler = limitHandler;
		this.langHandler = langHandler;
	}

	/* (non-Javadoc)
	 * @see org.bukkit.command.CommandExecutor#onCommand(org.bukkit.command.CommandSender, org.bukkit.command.Command, java.lang.String, java.lang.String[])
	 */
	@Override
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {

		Player p = null;
		if (sender instanceof Player) {
			p = (Player) sender;
		}

		if (args.length < 1) {
			return false;
		}

		if (args[0].equalsIgnoreCase("version") || args[0].equalsIgnoreCase("v")) {
			if (p != null) {
				langHandler.outputString(p, "loaded version " + plugin.getDescription().getVersion() + ",  " + plugin.getCopyright());
			} else {
				langHandler.outputConsole(Level.INFO, "loaded version " + plugin.getDescription().getVersion() + ",  " + plugin.getCopyright());
			}
		} else if (args[0].equalsIgnoreCase("list")) {
			if (p == null) {
				langHandler.outputConsole(Level.INFO, "The console is not allowed to use this command");
				return true;
			}
			if (plugin.getAgentManager().getAgentList().size() > 200) {
				langHandler.outputError(p, "CMD_LIST_TOO_MANY_REGIONS", null);
			} else {
				final ArrayList<SignAgent> list = new ArrayList<SignAgent>();
				for (final SignAgent agent : plugin.getAgentManager().getAgentList()) {
					if (agent.getWorldWorld() == p.getWorld() && agent.getMode() == SignAgent.MODE_SELL_REGION) {
						boolean add = true;
						for (final SignAgent tmp : list) {
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

				if (list.size() < 1) {
					langHandler.outputMessage(p, "CMD_LIST_NO_REGIONS", null);
					return true;
				}

				int initsite = 1;
				if (args.length > 1) {
					try {
						initsite = Integer.parseInt(args[1]);
					} catch (final Exception e) {
						langHandler.outputError(p, "CMD_LIST_WRONG_ARG", null);
						return true;
					}

					if (initsite < 1) {
						langHandler.outputError(p, "CMD_LIST_WRONG_ARG", null);
						return true;
					}
				}

				int site = list.size() / 8;
				if (list.size() % 8 > 0) {
					site++;
				}

				int showsite = 0;
				if (site > 0) {
					showsite = initsite;
				}

				langHandler.outputString(p, "Site " + showsite + " of " + site);
				initsite--;
				initsite *= 8;
				for (int i = initsite; i < initsite + 8; i++) {
					if (i >= list.size()) {
						break;
					}

					final SignAgent agent = list.get(i);
					if (plugin.isEconomy()) {
						langHandler.outputString(p, "Region: " + agent.getRegion() + " - " + plugin.econFormat(agent.getPrice()));
					} else {
						langHandler.outputString(p, "Region: " + agent.getRegion());
					}
				}
			}
		} else if (args[0].equalsIgnoreCase("limits") || args[0].equalsIgnoreCase("limit")) {
			if (p == null || plugin.isAdmin(p)) {
				if (args.length < 2) { // limits
					if (p != null) {
						langHandler.outputMessage(p, "CMD_LIMITS_NO_ARG", null);
					}
					return true;
				} else {
					int mode;
					if (args[1].equalsIgnoreCase("buy") || args[1].equalsIgnoreCase("regions")) {
						mode = 0;
					} else if (args[1].equalsIgnoreCase("rent") || args[1].equalsIgnoreCase("rooms")) {
						mode = 1;
					} else {
						if (p != null) {
							langHandler.outputError(p, "CMD_LIMITS_WRONG_ARG", null);
						}
						return true;
					}
					int limit;
					if (args.length < 3) { // limits buy|rent - Will show global
						// limits of buying regions
						if (p != null) {
							final ArrayList<String> list = new ArrayList<String>();
							if (mode == 0) {
								list.add(Integer.toString(limitHandler.getGlobalBuyLimit()));
							} else if (mode == 1) {
								list.add(Integer.toString(limitHandler.getGlobalRentLimit()));
							}
							langHandler.outputMessage(p, "CMD_LIMITS_OUTPUT_LIMIT", list);
						}
					} else { // limits buy|rent <limit>|<...>
						try {
							limit = Integer.parseInt(args[2]);
							if (mode == 0) {
								limitHandler.setGlobalBuyLimit(limit);
							} else if (mode == 1) {
								limitHandler.setGlobalRentLimit(limit);
							}
							final ArrayList<String> list = new ArrayList<String>();
							list.add(Integer.toString(limit));
							if (p != null) {
								langHandler.outputMessage(p, "CMD_LIMITS_SET_LIMIT", list);
							}
						} catch (final Exception e) {
							if (args[2].equalsIgnoreCase("world")) {
								final World w = Bukkit.getWorld(args[3]);
								if (w != null) {
									if (args.length < 4) { // limits buy|rent
										// world
										if (p != null) {
											langHandler.outputError(p, "CMD_LIMITS_NO_WORLD", null);
										}
									} else if (args.length < 5) { // limits
										// buy|rent
										// world
										// <name>
										final ArrayList<String> list = new ArrayList<String>();
										if (mode == 0) {
											list.add(Integer.toString(limitHandler.getBuyWorldLimit(w)));
										} else if (mode == 1) {
											list.add(Integer.toString(limitHandler.getRentWorldLimit(w)));
										}
										if (p != null) {
											langHandler.outputMessage(p, "CMD_LIMITS_OUTPUT_LIMIT", list);
										}
									} else { // limits buy|rent world <name>
										// <limit>
										try {
											limit = Integer.parseInt(args[4]);
											if (mode == 0) {
												limitHandler.setBuyWorldLimit(w, limit);
											} else if (mode == 1) {
												limitHandler.setRentWorldLimit(w, limit);
											}
											final ArrayList<String> list = new ArrayList<String>();
											list.add(Integer.toString(limit));
											if (p != null) {
												langHandler.outputMessage(p, "CMD_LIMITS_SET_LIMIT", list);
											}
										} catch (final Exception e2) {
											if (p != null) {
												langHandler.outputError(p, "CMD_LIMITS_WRONG_ARG", null);
											}
										}
									}
								} else {
									if (p != null) {
										langHandler.outputError(p, "CMD_LIMITS_NO_WORLD", null);
									}
								}
							} else if (args[2].equalsIgnoreCase("player")) {
								final Player p2 = Bukkit.getPlayer(args[3]);
								if (p2 != null) {
									if (args.length < 4) { // limits buy|rent
										// player
										if (p != null) {
											langHandler.outputError(p, "CMD_LIMITS_NO_PLAYER", null);
										}
									} else if (args.length < 5) { // limits
										// buy|rent
										// player
										// <name>
										final ArrayList<String> list = new ArrayList<String>();
										if (mode == 0) {
											list.add(Integer.toString(limitHandler.getBuyPlayerLimit(p2)));
										} else if (mode == 1) {
											list.add(Integer.toString(limitHandler.getRentPlayerLimit(p2)));
										}
										if (p != null) {
											langHandler.outputMessage(p, "CMD_LIMITS_OUTPUT_LIMIT", list);
										}
									} else { // limits buy|rent player <name>
										// <limit>
										try {
											limit = Integer.parseInt(args[4]);
											if (mode == 0) {
												limitHandler.setBuyPlayerLimit(p2, limit);
											} else if (mode == 1) {
												limitHandler.setRentPlayerLimit(p2, limit);
											}
											final ArrayList<String> list = new ArrayList<String>();
											list.add(Integer.toString(limit));
											if (p != null) {
												langHandler.outputMessage(p, "CMD_LIMITS_SET_LIMIT", list);
											}
										} catch (final Exception e2) {
											if (p != null) {
												langHandler.outputError(p, "CMD_LIMITS_WRONG_ARG", null);
											}
										}
									}
								} else {
									if (p != null) {
										langHandler.outputError(p, "CMD_LIMITS_NO_PLAYER", null);
									}
								}
							} else {
								if (p != null) {
									langHandler.outputError(p, "CMD_LIMITS_WRONG_ARG", null);
								}
							}
						}
					}
				}

				limitHandler.saveLimits();
			} else {
				if (p != null) {
					langHandler.outputError(p, "ERR_NO_PERM", null);
				}
			}
		} else if (args[0].equalsIgnoreCase("addmember")) {
			if (p == null || plugin.canAddMember(p)) {
				if (args.length < 2) {
					langHandler.outputMessage(p, "CMD_ADDMEMBER_NO_ARG", null);
					return true;
				}
				final Player p2add = Bukkit.getPlayer(args[1]);
				if (p2add == p) {
					langHandler.outputError(p, "CMD_ERR_ADD_YOURSELF", null);
				} else {
					if (p2add == null) {
						final ArrayList<String> list = new ArrayList<String>();
						list.add(args[1]);
						langHandler.outputError(p, "ERR_NO_PLAYER", list);
					} else {
						final int countRegions = limitHandler.countPlayerOwnRegion(p) + limitHandler.countPlayerRentRoom(p);
						if (countRegions < 1) {
							langHandler.outputError(p, "ERR_NO_REGION", null);
						} else {
							if (args.length < 3 && countRegions > 1) {
								langHandler.outputError(p, "ERR_SPECIFY_REGION", null);
								langHandler.outputMessage(p, "CMD_ADDMEMBER_NO_ARG", null);
								return true;
							}

							final LocalPlayer p2addLocal = SimpleRegionMarket.getWorldGuard().wrapPlayer(p2add);
							final LocalPlayer pLocal = SimpleRegionMarket.getWorldGuard().wrapPlayer(p);
							ProtectedRegion foundRegion = null;

							for (final SignAgent a : plugin.getAgentManager().getAgentList()) {
								if (a.getMode() == SignAgent.MODE_RENT_HOTEL && a.getRent().equalsIgnoreCase(p.getName())
										&& (args.length < 3 ? true : a.getRegion().equalsIgnoreCase(args[2]))) {
									if (foundRegion != null) {
										langHandler.outputError(p, "ERR_SPECIFY_REGION", null);
										return true;
									}
									foundRegion = a.getProtectedRegion();
								}
							}

							for (final World w : Bukkit.getWorlds()) {
								if (args.length < 3) {
									final Map<String, ProtectedRegion> m = SimpleRegionMarket.getWorldGuard().getRegionManager(w).getRegions();
									for (final String key : m.keySet()) {
										if (m.get(key).getOwners().contains(pLocal)) {
											if (foundRegion != null) {
												langHandler.outputError(p, "ERR_SPECIFY_REGION", null);
												return true;
											}
											foundRegion = m.get(key);
										}
									}
								} else {
									final ProtectedRegion r2add = SimpleRegionMarket.getWorldGuard().getRegionManager(w).getRegion(args[2]);
									if (r2add != null) {
										if (r2add.getOwners().contains(pLocal)) {
											if (foundRegion != null) {
												langHandler.outputError(p, "ERR_SPECIFY_REGION", null);
												return true;
											}
											foundRegion = r2add;
										}
									}
								}
							}

							if (foundRegion != null) {
								foundRegion.getMembers().addPlayer(p2addLocal);
								final ArrayList<String> list = new ArrayList<String>();
								list.add(p2add.getName());
								list.add(foundRegion.getId());
								langHandler.outputMessage(p, "CMD_ADDMEMBER_SUCCESS", list);
								final ArrayList<String> list2 = new ArrayList<String>();
								list2.add(p.getName());
								list2.add(foundRegion.getId());
								langHandler.outputMessage(p2add, "CMD_ADDMEMBER_NEWMEMBER", list2);
								final ArrayList<String> list3 = new ArrayList<String>();
								list3.add(p.getName());
								list3.add(p2add.getName());
								list3.add(foundRegion.getId());
								langHandler.langOutputConsole("LOG_ADDMEMBER", Level.INFO, list3);
							} else {
								langHandler.outputError(p, "ERR_REGION_NAME", null);
								langHandler.outputMessage(p, "CMD_ADDMEMBER_NO_ARG", null);
							}
						}
					}
				}
			} else {
				langHandler.outputError(p, "ERR_NO_PERM", null);
			}
		} else if (args[0].equalsIgnoreCase("remmember") || args[0].equalsIgnoreCase("removemember")) {
			if (p == null || plugin.canAddMember(p)) {
				if (args.length < 2) {
					langHandler.outputMessage(p, "CMD_REMMEMBER_NO_ARG", null);
					return true;
				}
				final OfflinePlayer p2rem = Bukkit.getOfflinePlayer(args[1]);
				if (p2rem.getPlayer() == p) {
					langHandler.outputError(p, "CMD_ERR_REM_YOURSELF", null);
				} else {
					final int countRegions = limitHandler.countPlayerOwnRegion(p) + limitHandler.countPlayerRentRoom(p);
					if (countRegions < 1) {
						langHandler.outputError(p, "ERR_NO_REGION", null);
					} else {
						if (args.length < 3 && countRegions > 1) {
							langHandler.outputError(p, "ERR_SPECIFY_REGION", null);
							langHandler.outputMessage(p, "CMD_REMMEMBER_NO_ARG", null);
							return true;
						}

						final LocalPlayer pLocal = SimpleRegionMarket.getWorldGuard().wrapPlayer(p);
						ProtectedRegion foundRegion = null;

						for (final SignAgent a : plugin.getAgentManager().getAgentList()) {
							if (a.getMode() == SignAgent.MODE_RENT_HOTEL && a.getRent().equalsIgnoreCase(p.getName())
									&& (args.length < 3 ? true : a.getRegion().equalsIgnoreCase(args[2]))) {
								if (foundRegion != null) {
									langHandler.outputError(p, "ERR_SPECIFY_REGION", null);
									return true;
								}
								foundRegion = a.getProtectedRegion();
							}
						}

						for (final World w : Bukkit.getWorlds()) {
							if (args.length < 3) {
								final Map<String, ProtectedRegion> m = SimpleRegionMarket.getWorldGuard().getRegionManager(w).getRegions();
								for (final String key : m.keySet()) {
									if (m.get(key).getOwners().contains(pLocal)) {
										if (foundRegion != null) {
											langHandler.outputError(p, "ERR_SPECIFY_REGION", null);
											return true;
										}
										foundRegion = m.get(key);
									}
								}
							} else {
								final ProtectedRegion r2rem = SimpleRegionMarket.getWorldGuard().getRegionManager(w).getRegion(args[2]);
								if (r2rem != null) {
									if (r2rem.getOwners().contains(pLocal)) {
										if (foundRegion != null) {
											langHandler.outputError(p, "ERR_SPECIFY_REGION", null);
											return true;
										}
										foundRegion = r2rem;
									}
								}
							}
						}

						if (foundRegion != null) {
							boolean bThere = false;
							for (final String s : foundRegion.getMembers().getPlayers()) {
								if (s.equalsIgnoreCase(p2rem.getName())) {
									bThere = true;
									break;
								}
							}
							if (bThere) {
								foundRegion.getMembers().removePlayer(p2rem.getName());
								final ArrayList<String> list = new ArrayList<String>();
								list.add(p2rem.getName());
								list.add(foundRegion.getId());
								langHandler.outputMessage(p, "CMD_REMMEMBER_SUCCESS", list);
								if (p2rem.isOnline()) {
									final ArrayList<String> list2 = new ArrayList<String>();
									list2.add(p.getName());
									list2.add(foundRegion.getId());
									langHandler.outputMessage(p2rem.getPlayer(), "CMD_REMMEMBER_OLDMEMBER", list2);
								}
								final ArrayList<String> list3 = new ArrayList<String>();
								list3.add(p.getName());
								list3.add(p2rem.getName());
								list3.add(foundRegion.getId());
								langHandler.langOutputConsole("LOG_REMMEMBER", Level.INFO, list3);
							} else {
								final ArrayList<String> list = new ArrayList<String>();
								list.add(p2rem.getName());
								langHandler.outputError(p, "ERR_NO_PLAYER", list);
							}
						} else {
							langHandler.outputError(p, "ERR_REGION_NAME", null);
							langHandler.outputMessage(p, "CMD_REMMEMBER_NO_ARG", null);
						}
					}
				}
			} else {
				langHandler.outputError(p, "ERR_NO_PERM", null);
			}
		} else if (args[0].equalsIgnoreCase("addowner")) {
			if (p == null || plugin.canAddOwner(p)) {
				if (args.length < 2) {
					langHandler.outputMessage(p, "CMD_ADDOWNER_NO_ARG", null);
					return true;
				}
				final Player p2add = Bukkit.getPlayer(args[1]);
				if (p2add == p) {
					langHandler.outputError(p, "CMD_ERR_ADD_YOURSELF", null);
				} else {
					if (p2add == null) {
						final ArrayList<String> list = new ArrayList<String>();
						list.add(args[1]);
						langHandler.outputError(p, "ERR_NO_PLAYER", list);
					} else {
						final int countRegions = limitHandler.countPlayerOwnRegion(p) + limitHandler.countPlayerRentRoom(p);
						if (countRegions < 1) {
							langHandler.outputError(p, "ERR_NO_REGION", null);
						} else {
							if (args.length < 3 && countRegions > 1) {
								langHandler.outputError(p, "ERR_SPECIFY_REGION", null);
								langHandler.outputMessage(p, "CMD_ADDMEMBER_NO_ARG", null);
								return true;
							}

							final LocalPlayer p2addLocal = SimpleRegionMarket.getWorldGuard().wrapPlayer(p2add);
							final LocalPlayer pLocal = SimpleRegionMarket.getWorldGuard().wrapPlayer(p);
							ProtectedRegion foundRegion = null;

							for (final SignAgent a : plugin.getAgentManager().getAgentList()) {
								if (a.getMode() == SignAgent.MODE_RENT_HOTEL && a.getRent().equalsIgnoreCase(p.getName())
										&& (args.length < 3 ? true : a.getRegion().equalsIgnoreCase(args[2]))) {
									if (foundRegion != null) {
										langHandler.outputError(p, "ERR_SPECIFY_REGION", null);
										return true;
									}
									foundRegion = a.getProtectedRegion();
								}
							}

							for (final World w : Bukkit.getWorlds()) {
								if (args.length < 3) {
									final Map<String, ProtectedRegion> m = SimpleRegionMarket.getWorldGuard().getRegionManager(w).getRegions();
									for (final String key : m.keySet()) {
										if (m.get(key).getOwners().contains(pLocal)) {
											if (foundRegion != null) {
												langHandler.outputError(p, "ERR_SPECIFY_REGION", null);
												return true;
											}
											foundRegion = m.get(key);
										}
									}
								} else {
									final ProtectedRegion r2add = SimpleRegionMarket.getWorldGuard().getRegionManager(w).getRegion(args[2]);
									if (r2add != null) {
										if (r2add.getOwners().contains(pLocal)) {
											if (foundRegion != null) {
												langHandler.outputError(p, "ERR_SPECIFY_REGION", null);
												return true;
											}
											foundRegion = r2add;
										}
									}
								}
							}

							if (foundRegion != null) {
								foundRegion.getOwners().addPlayer(p2addLocal);
								final ArrayList<String> list = new ArrayList<String>();
								list.add(p2add.getName());
								list.add(foundRegion.getId());
								langHandler.outputMessage(p, "CMD_ADDOWNER_SUCCESS", list);
								final ArrayList<String> list2 = new ArrayList<String>();
								list2.add(p.getName());
								list2.add(foundRegion.getId());
								langHandler.outputMessage(p2add, "CMD_ADDOWNER_NEWOWNER", list2);
								final ArrayList<String> list3 = new ArrayList<String>();
								list3.add(p.getName());
								list3.add(p2add.getName());
								list3.add(foundRegion.getId());
								langHandler.langOutputConsole("LOG_ADDOWNER", Level.INFO, list3);
							} else {
								langHandler.outputError(p, "ERR_REGION_NAME", null);
								langHandler.outputMessage(p, "CMD_ADDMEMBER_NO_ARG", null);
							}
						}
					}
				}
			} else {
				langHandler.outputError(p, "ERR_NO_PERM", null);
			}
		} else if (args[0].equalsIgnoreCase("remowner") || args[0].equalsIgnoreCase("removeowner")) {
			if (p == null || plugin.canAddMember(p)) {
				if (args.length < 2) {
					langHandler.outputMessage(p, "CMD_REMOWNER_NO_ARG", null);
					return true;
				}
				final OfflinePlayer p2rem = Bukkit.getOfflinePlayer(args[1]);
				if (p2rem.getPlayer() == p) {
					langHandler.outputError(p, "CMD_ERR_REM_YOURSELF", null);
				} else {
					final int countRegions = limitHandler.countPlayerOwnRegion(p) + limitHandler.countPlayerRentRoom(p);
					if (countRegions < 1) {
						langHandler.outputError(p, "ERR_NO_REGION", null);
					} else {
						if (args.length < 3 && countRegions > 1) {
							langHandler.outputError(p, "ERR_SPECIFY_REGION", null);
							langHandler.outputMessage(p, "CMD_REMOWNER_NO_ARG", null);
							return true;
						}

						final LocalPlayer pLocal = SimpleRegionMarket.getWorldGuard().wrapPlayer(p);
						ProtectedRegion foundRegion = null;

						for (final SignAgent a : plugin.getAgentManager().getAgentList()) {
							if (a.getMode() == SignAgent.MODE_RENT_HOTEL && a.getRent().equalsIgnoreCase(p.getName())
									&& (args.length < 3 ? true : a.getRegion().equalsIgnoreCase(args[2]))) {
								if (foundRegion != null) {
									langHandler.outputError(p, "ERR_SPECIFY_REGION", null);
									return true;
								}
								foundRegion = a.getProtectedRegion();
							}
						}

						for (final World w : Bukkit.getWorlds()) {
							if (args.length < 3) {
								final Map<String, ProtectedRegion> m = SimpleRegionMarket.getWorldGuard().getRegionManager(w).getRegions();
								for (final String key : m.keySet()) {
									if (m.get(key).getOwners().contains(pLocal)) {
										if (foundRegion != null) {
											langHandler.outputError(p, "ERR_SPECIFY_REGION", null);
											return true;
										}
										foundRegion = m.get(key);
									}
								}
							} else {
								final ProtectedRegion r2rem = SimpleRegionMarket.getWorldGuard().getRegionManager(w).getRegion(args[2]);
								if (r2rem != null) {
									if (r2rem.getOwners().contains(pLocal)) {
										if (foundRegion != null) {
											langHandler.outputError(p, "ERR_SPECIFY_REGION", null);
											return true;
										}
										foundRegion = r2rem;
									}
								}
							}
						}

						if (foundRegion != null) {
							boolean bThere = false;
							for (final String s : foundRegion.getOwners().getPlayers()) {
								if (s.equalsIgnoreCase(p2rem.getName())) {
									bThere = true;
									break;
								}
							}
							if (bThere) {
								foundRegion.getOwners().removePlayer(p2rem.getName());
								final ArrayList<String> list = new ArrayList<String>();
								list.add(p2rem.getName());
								list.add(foundRegion.getId());
								langHandler.outputMessage(p, "CMD_REMOWNER_SUCCESS", list);
								if (p2rem.isOnline()) {
									final ArrayList<String> list2 = new ArrayList<String>();
									list2.add(p.getName());
									list2.add(foundRegion.getId());
									langHandler.outputMessage(p2rem.getPlayer(), "CMD_REMOWNER_OLDOWNER", list2);
								}
								final ArrayList<String> list3 = new ArrayList<String>();
								list3.add(p.getName());
								list3.add(p2rem.getName());
								list3.add(foundRegion.getId());
								langHandler.langOutputConsole("LOG_REMOWNER", Level.INFO, list3);
							} else {
								final ArrayList<String> list = new ArrayList<String>();
								list.add(p2rem.getName());
								langHandler.outputError(p, "ERR_NO_PLAYER", list);
							}
						} else {
							langHandler.outputError(p, "ERR_REGION_NAME", null);
							langHandler.outputMessage(p, "CMD_REMOWNER_NO_ARG", null);
						}
					}
				}
			} else {
				langHandler.outputError(p, "ERR_NO_PERM", null);
			}
		} else {
			return false;
		}
		return true;
	}
}
