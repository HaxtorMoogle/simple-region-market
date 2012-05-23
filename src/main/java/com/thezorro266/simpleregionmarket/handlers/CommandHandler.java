package com.thezorro266.simpleregionmarket.handlers;

import java.util.ArrayList;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.thezorro266.simpleregionmarket.SimpleRegionMarket;
import com.thezorro266.simpleregionmarket.TokenManager;
import com.thezorro266.simpleregionmarket.Utils;
import com.thezorro266.simpleregionmarket.signs.TemplateMain;

public class CommandHandler implements CommandExecutor {
	private final LanguageHandler langHandler;
	private final SimpleRegionMarket plugin;

	/**
	 * Instantiates a new command handler.
	 * 
	 * @param plugin
	 *            the plugin
	 * @param langHandler
	 *            the lang handler
	 */
	public CommandHandler(SimpleRegionMarket plugin, LanguageHandler langHandler) {
		this.plugin = plugin;
		this.langHandler = langHandler;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
		Player player = null;
		Boolean isConsole = true;
		if (sender instanceof Player) {
			player = (Player) sender;
			isConsole = false;
		}

		if (args.length < 1) {
			return false;
		}

		if (args[0].equalsIgnoreCase("version") || args[0].equalsIgnoreCase("v")) {
			if (player != null) {
				langHandler.playerDirectOut(player, ChatColor.YELLOW, "loaded version " + plugin.getDescription().getVersion() + ",  " + Utils.getCopyright());
			} else {
				langHandler.consoleDirectOut(Level.INFO, "loaded version " + plugin.getDescription().getVersion() + ",  " + Utils.getCopyright());
			}
		} else if (args[0].equalsIgnoreCase("untake")) {
			if (isConsole || SimpleRegionMarket.permManager.isAdmin(player)) {
				if (args.length < 2) {
					if (isConsole) {
						langHandler.consoleOut("CMD.UNTAKE.NO_ARG", Level.INFO, null);
					} else {
						langHandler.playerListOut(player, "CMD.UNTAKE.NO_ARG", null);
					}
					return true;
				} else {
					final String region = args[1];
					String world;
					if (args.length > 2) {
						world = args[2];
					} else {
						if (isConsole) {
							langHandler.consoleOut("CMD.UNTAKE.CONSOLE_NOWORLD", Level.SEVERE, null);
							return true;
						} else {
							world = player.getWorld().getName();
						}
					}
					Boolean found = false;
					for (final TemplateMain token : TokenManager.tokenList) {
						if (Utils.getEntry(token, world, region, "taken") != null) {
							if (Utils.getEntryBoolean(token, world, region, "taken")) {
								token.untakeRegion(world, region);
								found = true;
								break;
							}
						}
					}
					final ArrayList<String> list = new ArrayList<String>();
					list.add(region);
					list.add(world);
					if (found) {
						if (isConsole) {
							langHandler.consoleOut("CMD.UNTAKE.SUCCESS", Level.INFO, list);
						} else {
							langHandler.playerNormalOut(player, "CMD.UNTAKE.SUCCESS", list);
						}
					} else {
						if (isConsole) {
							langHandler.consoleOut("CMD.UNTAKE.NO_REGION", Level.WARNING, list);
						} else {
							langHandler.playerErrorOut(player, "CMD.UNTAKE.NO_REGION", list);
						}
					}
				}
			} else {
				langHandler.playerErrorOut(player, "PLAYER.NO_PERMISSIONS.NORM", null);
			}
		} else if (args[0].equalsIgnoreCase("remove")) {
			if (args.length < 2) {
				if (player == null) {

				} else {
					langHandler.playerListOut(player, "CMD.REMOVE.NO_ARG", null);
				}
			}
		} else if (args[0].equalsIgnoreCase("list")) { // TODO Can list own and rented regions
			if (player == null) {
				langHandler.consoleDirectOut(Level.INFO, "Not yet implemented");
			} else {
				langHandler.playerDirectOut(player, ChatColor.BLUE, "Not yet implemented");
			}
		} else if (args[0].equalsIgnoreCase("limits") || args[0].equalsIgnoreCase("limit")) { // TODO set/get limits command
			if (player == null) {
				langHandler.consoleDirectOut(Level.INFO, "Not yet implemented");
			} else {
				langHandler.playerDirectOut(player, ChatColor.BLUE, "Not yet implemented");
			}
		} else if (args[0].equalsIgnoreCase("addmember")) { // TODO addmember, removemember, addowner, removeowner
			if (player == null) {
				langHandler.consoleDirectOut(Level.INFO, "Not yet implemented");
			} else {
				langHandler.playerDirectOut(player, ChatColor.BLUE, "Not yet implemented");
			}
		} else if (args[0].equalsIgnoreCase("remmember") || args[0].equalsIgnoreCase("removemember")) {
			if (player == null) {
				langHandler.consoleDirectOut(Level.INFO, "Not yet implemented");
			} else {
				langHandler.playerDirectOut(player, ChatColor.BLUE, "Not yet implemented");
			}
		} else if (args[0].equalsIgnoreCase("addowner")) {
			if (player == null) {
				langHandler.consoleDirectOut(Level.INFO, "Not yet implemented");
			} else {
				langHandler.playerDirectOut(player, ChatColor.BLUE, "Not yet implemented");
			}
		} else if (args[0].equalsIgnoreCase("remowner") || args[0].equalsIgnoreCase("removeowner")) {
			if (player == null) {
				langHandler.consoleDirectOut(Level.INFO, "Not yet implemented");
			} else {
				langHandler.playerDirectOut(player, ChatColor.BLUE, "Not yet implemented");
			}
		} else {
			return false;
		}
		return true;
	}
}
