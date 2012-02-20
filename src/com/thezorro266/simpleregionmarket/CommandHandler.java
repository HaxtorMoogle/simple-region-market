package com.thezorro266.simpleregionmarket;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandHandler implements CommandExecutor {
	private final LimitHandler limitHandler;
	private final SimpleRegionMarket plugin;

	public CommandHandler(SimpleRegionMarket plugin, LimitHandler limitHandler) {
		this.plugin = plugin;
		this.limitHandler = limitHandler;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String commandLabel, String[] args) {

		if (!(sender instanceof Player))
			return false;

		final Player p = (Player) sender;

		if (args.length < 1)
			return false;

		if (args[0].equalsIgnoreCase("version")
				|| args[0].equalsIgnoreCase("v")) {
			LanguageHandler.outputString(p,
					"loaded version " + plugin.getDescription().getVersion()
							+ ",  " + plugin.getCopyright());
		} else if (args[0].equalsIgnoreCase("list")) {
			if (plugin.getAgentManager().getAgentList().size() > 200) {
				LanguageHandler.outputError(p, "CMD_LIST_TOO_MANY_REGIONS",
						null);
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
					LanguageHandler.outputDebug(p, "CMD_LIST_NO_REGIONS", null);
					return true;
				}

				int initsite = 1;
				if (args.length > 1) {
					try {
						initsite = Integer.parseInt(args[1]);
					} catch (final Exception e) {
						LanguageHandler.outputError(p, "CMD_LIST_WRONG_ARG",
								null);
						return true;
					}

					if (initsite < 1) {
						LanguageHandler.outputError(p, "CMD_LIST_WRONG_ARG",
								null);
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

				LanguageHandler.outputString(p, "Site " + showsite + " of "
						+ site);
				initsite--;
				initsite *= 8;
				for (int i = initsite; i < initsite + 8; i++) {
					if (i >= list.size()) {
						break;
					}

					final SignAgent agent = list.get(i);
					if (plugin.isEconomy()) {
						LanguageHandler.outputString(
								p,
								"Region: " + agent.getRegion() + " - "
										+ plugin.econFormat(agent.getPrice()));
					} else {
						LanguageHandler.outputString(p,
								"Region: " + agent.getRegion());
					}
				}
			}
		} else if (args[0].equalsIgnoreCase("limits")
				|| args[0].equalsIgnoreCase("limit")) {
			if (plugin.isAdmin(p)) {
				if (args.length < 2) { // limits
					LanguageHandler.outputDebug(p, "CMD_LIMITS_NO_ARG", null);
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
						LanguageHandler.outputError(p, "CMD_LIMITS_WRONG_ARG",
								null);
						return true;
					}
					int limit;
					if (args.length < 3) { // limits buy|rent - Will show global
											// limits of buying regions
						final ArrayList<String> list = new ArrayList<String>();
						if (mode == 0) {
							list.add(Integer.toString(limitHandler
									.getGlobalBuyLimit()));
						} else if (mode == 1) {
							list.add(Integer.toString(limitHandler
									.getGlobalRentLimit()));
						}
						LanguageHandler.outputDebug(p,
								"CMD_LIMITS_OUTPUT_LIMIT", list);
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
							LanguageHandler.outputDebug(p,
									"CMD_LIMITS_SET_LIMIT", list);
						} catch (final Exception e) {
							if (args[2].equalsIgnoreCase("world")) {
								final World w = Bukkit.getWorld(args[3]);
								if (w != null) {
									if (args.length < 4) { // limits buy|rent
															// world
										LanguageHandler.outputError(p,
												"CMD_LIMITS_NO_WORLD", null);
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
										LanguageHandler
												.outputDebug(
														p,
														"CMD_LIMITS_OUTPUT_LIMIT",
														list);
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
											LanguageHandler.outputDebug(p,
													"CMD_LIMITS_SET_LIMIT",
													list);
										} catch (final Exception e2) {
											LanguageHandler.outputError(p,
													"CMD_LIMITS_WRONG_ARG",
													null);
										}
									}
								} else {
									LanguageHandler.outputError(p,
											"CMD_LIMITS_NO_WORLD", null);
								}
							} else if (args[2].equalsIgnoreCase("player")) {
								final Player p2 = Bukkit.getPlayer(args[3]);
								if (p2 != null) {
									if (args.length < 4) { // limits buy|rent
															// player
										LanguageHandler.outputError(p,
												"CMD_LIMITS_NO_PLAYER", null);
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
										LanguageHandler
												.outputDebug(
														p,
														"CMD_LIMITS_OUTPUT_LIMIT",
														list);
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
											LanguageHandler.outputDebug(p,
													"CMD_LIMITS_SET_LIMIT",
													list);
										} catch (final Exception e2) {
											LanguageHandler.outputError(p,
													"CMD_LIMITS_WRONG_ARG",
													null);
										}
									}
								} else {
									LanguageHandler.outputError(p,
											"CMD_LIMITS_NO_PLAYER", null);
								}
							} else {
								LanguageHandler.outputError(p,
										"CMD_LIMITS_WRONG_ARG", null);
							}
						}
					}
				}

				limitHandler.saveLimits();
			} else {
				LanguageHandler.outputError(p, "ERR_NO_PERM", null);
			}
		} else
			return false;
		return true;
	}
}
