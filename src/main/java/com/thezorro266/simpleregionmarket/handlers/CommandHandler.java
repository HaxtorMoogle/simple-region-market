package com.thezorro266.simpleregionmarket.handlers;

import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.thezorro266.simpleregionmarket.SimpleRegionMarket;
import com.thezorro266.simpleregionmarket.Utils;

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
		if (sender instanceof Player) {
			player = (Player) sender;
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
			if(args.length < 2) {
				if (player == null) {
					
				} else {
					langHandler.playerListOut(player, "CMD.UNTAKE.NO_ARG", null);
				}
				return true;
			}

		} else if (args[0].equalsIgnoreCase("remove")) {
			if(args.length < 2) {
				if (player == null) {
					
				} else {
					langHandler.playerListOut(player, "CMD.REMOVE.NO_ARG", null);
				}
				return true;
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
