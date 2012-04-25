package com.thezorro266.simpleregionmarket.handlers;

import java.util.logging.Level;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.thezorro266.simpleregionmarket.SimpleRegionMarket;
import com.thezorro266.simpleregionmarket.Utils;

public class CommandHandler implements CommandExecutor {
	private final LanguageHandler LANG_HANDLER;
	private final LimitHandler LIMIT_HANDLER;
	private final SimpleRegionMarket PLUGIN;

	/**
	 * Instantiates a new command handler.
	 * 
	 * @param plugin
	 *            the plugin
	 * @param limitHandler
	 *            the limit handler
	 * @param langHandler
	 *            the lang handler
	 */
	public CommandHandler(SimpleRegionMarket plugin, LimitHandler limitHandler, LanguageHandler langHandler) {
		PLUGIN = plugin;
		LIMIT_HANDLER = limitHandler;
		LANG_HANDLER = langHandler;
	}

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
				LANG_HANDLER.outputString(p, "loaded version " + PLUGIN.getDescription().getVersion() + ",  " + Utils.getCopyright());
			} else {
				LANG_HANDLER.outputConsole(Level.INFO, "loaded version " + PLUGIN.getDescription().getVersion() + ",  " + Utils.getCopyright());
			}
		} else if (args[0].equalsIgnoreCase("list")) { // TODO Can list own and rented regions
			if (p == null) {
				LANG_HANDLER.outputConsole(Level.INFO, "Not yet implemented");
			} else {
				LANG_HANDLER.outputString(p, "Not yet implemented");
			}
		} else if (args[0].equalsIgnoreCase("limits") || args[0].equalsIgnoreCase("limit")) { // TODO set/get limits command
			if (p == null) {
				LANG_HANDLER.outputConsole(Level.INFO, "Not yet implemented");
			} else {
				LANG_HANDLER.outputString(p, "Not yet implemented");
			}
		} else if (args[0].equalsIgnoreCase("addmember")) { // TODO addmember, removemember, addowner, removeowner
			if (p == null) {
				LANG_HANDLER.outputConsole(Level.INFO, "Not yet implemented");
			} else {
				LANG_HANDLER.outputString(p, "Not yet implemented");
			}
		} else if (args[0].equalsIgnoreCase("remmember") || args[0].equalsIgnoreCase("removemember")) {
			if (p == null) {
				LANG_HANDLER.outputConsole(Level.INFO, "Not yet implemented");
			} else {
				LANG_HANDLER.outputString(p, "Not yet implemented");
			}
		} else if (args[0].equalsIgnoreCase("addowner")) {
			if (p == null) {
				LANG_HANDLER.outputConsole(Level.INFO, "Not yet implemented");
			} else {
				LANG_HANDLER.outputString(p, "Not yet implemented");
			}
		} else if (args[0].equalsIgnoreCase("remowner") || args[0].equalsIgnoreCase("removeowner")) {
			if (p == null) {
				LANG_HANDLER.outputConsole(Level.INFO, "Not yet implemented");
			} else {
				LANG_HANDLER.outputString(p, "Not yet implemented");
			}
		} else {
			return false;
		}
		return true;
	}
}
