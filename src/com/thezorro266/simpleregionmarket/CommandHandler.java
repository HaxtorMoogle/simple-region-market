package com.thezorro266.simpleregionmarket;

import java.util.ArrayList;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandHandler implements CommandExecutor {
	private final LanguageHandler langHandler;
	private final LimitHandler limitHandler;
	private final SimpleRegionMarket plugin;

	public CommandHandler(SimpleRegionMarket plugin, LimitHandler limitHandler,
			LanguageHandler langHandler) {
		this.plugin = plugin;
		this.limitHandler = limitHandler;
		this.langHandler = langHandler;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String commandLabel, String[] args) {

		Player p = null;
		if (sender instanceof Player) {
			p = (Player) sender;
		}

		if (args.length < 1)
			return false;

		if (args[0].equalsIgnoreCase("version")
				|| args[0].equalsIgnoreCase("v")) {
			if (p != null) {
				langHandler.outputString(p,
						"loaded version "
								+ plugin.getDescription().getVersion() + ",  "
								+ plugin.getCopyright());
			} else {
				langHandler.outputConsole(Level.INFO,
						"loaded version "
								+ plugin.getDescription().getVersion() + ",  "
								+ plugin.getCopyright());
			}
		} else if (args[0].equalsIgnoreCase("list")) {
			if (p == null) {
				langHandler.outputConsole(Level.INFO,
						"The console is not allowed to use this command");
				return true;
			}
			if (plugin.getAgentManager().getAgentList().size() > 200) {
				langHandler.outputError(p, "CMD_LIST_TOO_MANY_REGIONS", null);
			} else {
				final ArrayList<SignAgent> list = new ArrayList<SignAgent>();
				for (final SignAgent agent : plugin.getAgentManager()
						.getAgentList()) {
					if (agent.getWorldWorld() == p.getWorld()
							&& agent.getMode() == SignAgent.MODE_SELL_REGION) {
						boolean add = true;
						for (final SignAgent tmp : list) {
							if (tmp.getProtectedRegion() == agent
									.getProtectedRegion()) {
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
					langHandler.outputDebug(p, "CMD_LIST_NO_REGIONS", null);
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
						langHandler.outputString(
								p,
								"Region: " + agent.getRegion() + " - "
										+ plugin.econFormat(agent.getPrice()));
					} else {
						langHandler.outputString(p,
								"Region: " + agent.getRegion());
					}
				}
			}
		} else if (args[0].equalsIgnoreCase("limits")
				|| args[0].equalsIgnoreCase("limit")) {
			if (p == null || plugin.isAdmin(p)) {
				if (args.length < 2) { // limits
					if (p != null) {
						langHandler.outputDebug(p, "CMD_LIMITS_NO_ARG", null);
					}
					return true;
				} else {
					int mode;
					if (args[1].equalsIgnoreCase("buy")
							|| args[1].equalsIgnoreCase("regions")) {
						mode = 0;
					} else if (args[1].equalsIgnoreCase("rent")
							|| args[1].equalsIgnoreCase("rooms")) {
						mode = 1;
					} else {
						if (p != null) {
							langHandler.outputError(p, "CMD_LIMITS_WRONG_ARG",
									null);
						}
						return true;
					}
					int limit;
					if (args.length < 3) { // limits buy|rent - Will show global
											// limits of buying regions
						if (p != null) {
							final ArrayList<String> list = new ArrayList<String>();
							if (mode == 0) {
								list.add(Integer.toString(limitHandler
										.getGlobalBuyLimit()));
							} else if (mode == 1) {
								list.add(Integer.toString(limitHandler
										.getGlobalRentLimit()));
							}
							langHandler.outputDebug(p,
									"CMD_LIMITS_OUTPUT_LIMIT", list);
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
								langHandler.outputDebug(p,
										"CMD_LIMITS_SET_LIMIT", list);
							}
						} catch (final Exception e) {
							if (args[2].equalsIgnoreCase("world")) {
								final World w = Bukkit.getWorld(args[3]);
								if (w != null) {
									if (args.length < 4) { // limits buy|rent
															// world
										if (p != null) {
											langHandler
													.outputError(
															p,
															"CMD_LIMITS_NO_WORLD",
															null);
										}
									} else if (args.length < 5) { // limits
																	// buy|rent
																	// world
																	// <name>
										final ArrayList<String> list = new ArrayList<String>();
										if (mode == 0) {
											list.add(Integer.toString(limitHandler
													.getBuyWorldLimit(w)));
										} else if (mode == 1) {
											list.add(Integer.toString(limitHandler
													.getRentWorldLimit(w)));
										}
										if (p != null) {
											langHandler.outputDebug(p,
													"CMD_LIMITS_OUTPUT_LIMIT",
													list);
										}
									} else { // limits buy|rent world <name>
												// <limit>
										try {
											limit = Integer.parseInt(args[4]);
											if (mode == 0) {
												limitHandler.setBuyWorldLimit(
														w, limit);
											} else if (mode == 1) {
												limitHandler.setRentWorldLimit(
														w, limit);
											}
											final ArrayList<String> list = new ArrayList<String>();
											list.add(Integer.toString(limit));
											if (p != null) {
												langHandler.outputDebug(p,
														"CMD_LIMITS_SET_LIMIT",
														list);
											}
										} catch (final Exception e2) {
											if (p != null) {
												langHandler.outputError(p,
														"CMD_LIMITS_WRONG_ARG",
														null);
											}
										}
									}
								} else {
									if (p != null) {
										langHandler.outputError(p,
												"CMD_LIMITS_NO_WORLD", null);
									}
								}
							} else if (args[2].equalsIgnoreCase("player")) {
								final Player p2 = Bukkit.getPlayer(args[3]);
								if (p2 != null) {
									if (args.length < 4) { // limits buy|rent
															// player
										if (p != null) {
											langHandler.outputError(p,
													"CMD_LIMITS_NO_PLAYER",
													null);
										}
									} else if (args.length < 5) { // limits
																	// buy|rent
																	// player
																	// <name>
										final ArrayList<String> list = new ArrayList<String>();
										if (mode == 0) {
											list.add(Integer.toString(limitHandler
													.getBuyPlayerLimit(p2)));
										} else if (mode == 1) {
											list.add(Integer.toString(limitHandler
													.getRentPlayerLimit(p2)));
										}
										if (p != null) {
											langHandler.outputDebug(p,
													"CMD_LIMITS_OUTPUT_LIMIT",
													list);
										}
									} else { // limits buy|rent player <name>
												// <limit>
										try {
											limit = Integer.parseInt(args[4]);
											if (mode == 0) {
												limitHandler.setBuyPlayerLimit(
														p2, limit);
											} else if (mode == 1) {
												limitHandler
														.setRentPlayerLimit(p2,
																limit);
											}
											final ArrayList<String> list = new ArrayList<String>();
											list.add(Integer.toString(limit));
											if (p != null) {
												langHandler.outputDebug(p,
														"CMD_LIMITS_SET_LIMIT",
														list);
											}
										} catch (final Exception e2) {
											if (p != null) {
												langHandler.outputError(p,
														"CMD_LIMITS_WRONG_ARG",
														null);
											}
										}
									}
								} else {
									if (p != null) {
										langHandler.outputError(p,
												"CMD_LIMITS_NO_PLAYER", null);
									}
								}
							} else {
								if (p != null) {
									langHandler.outputError(p,
											"CMD_LIMITS_WRONG_ARG", null);
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
		} else
			return false;
		return true;
	}
}
