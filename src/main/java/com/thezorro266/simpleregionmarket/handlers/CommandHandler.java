package com.thezorro266.simpleregionmarket.handlers;

import java.util.logging.Level;

import javax.annotation.Untainted;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
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
			Boolean IsConsole = false;
			if (player == null)
			{
				IsConsole = true;
			}
			if (args.length < 2) {
				if (IsConsole) {
				} else {
					langHandler.playerListOut(player, "CMD.UNTAKE.NO_ARG", null);
					return true;
				}
				if(args[1].isEmpty())
				{
					langHandler.playerDirectOut(player, ChatColor.DARK_RED, "You haven't specified region");
					return true;
				}
				String region = args[1];
				String world = "";
				if(args[2].isEmpty())
				{
					if (IsConsole) {
						langHandler.consoleDirectOut(Level.INFO, "You must specify world");
						return true;
					}
					world = player.getWorld().getName();
				}
				else
				{
					world = args[2];
				}
				if(!SimpleRegionMarket.permManager.isAdmin(player))
				{
					langHandler.playerDirectOut(player, ChatColor.DARK_RED, "You do not have permission to do that");
					return true;
				}
				Boolean found = false;
				for(final TemplateMain token : TokenManager.tokenList)
				{
					if(Utils.getEntry(token, world, region, "taken") != null)
					{
						if(Utils.getEntryBoolean(token, world, region, "taken"))
						{
							found = true;
							token.untakeRegion(world, region);
						}
					}
				}
				if(found)
				{
					if(IsConsole)
					{
						langHandler.consoleDirectOut(Level.INFO, "Region " + region + " has been successfully untaken");
					}
					else
					{
						langHandler.playerDirectOut(player, ChatColor.YELLOW, "Region " + region + " has been successfully untaken");
					}
					return true;
				}
				else
				{
					if(IsConsole)
					{
						langHandler.consoleDirectOut(Level.INFO, "Region hasn't been found in this world");
					}
					else
					{
						langHandler.playerDirectOut(player, ChatColor.DARK_RED, "Region hasn't been found in this world");
					}
					return true;
				}
			}
		} else if (args[0].equalsIgnoreCase("remove")) {
			if (args.length < 2) {
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
			final ProtectedRegion protectedRegion = SimpleRegionMarket.wgManager.getProtectedRegion(Bukkit.getWorld(world), region);
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
