package com.thezorro266.simpleregionmarket;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

public class LanguageHandler {
	private static YamlConfiguration languagefile;

	public static boolean setLang(String lang) {
		if(lang == null || lang.isEmpty()) {
			lang = "en";
		}
		File default_lang = new File(SimpleRegionMarket.plugin_dir + "en.yml");
		if(!default_lang.exists()) {
			languagefile = new YamlConfiguration();
			try {
				languagefile.loadFromString("ERR_NO_WORLDGUARD: 'Error: WorldGuard was not found.'\n" +
						"ERR_NO_REGISTER: 'Error: Register was not found.'\n" +
						"ERR_NO_ECO: 'Error: Economic System was not found.'\n" +
						"ERR_ECO_TRANSFER: 'There was a problem with transfering the money.'\n" +
						"ERR_NO_ECO_USER: 'The economic system was not found, please tell the server owner.'\n" +
						"ERR_CREATE_ECO_ACCOUNT: 'Error: Could not create economy account \"$0\".'\n" +
						"CONFIG_SAVED: 'Config saved successfully.'\n" +
						"ERR_CONFIG_NOT_SAVED: 'Error: Config was not saved successfully.'\n" +
						"PLUGIN_UNLOAD: 'Plugin successfully unloaded.'\n" +
						"ERR_PLUGIN_UNLOAD: 'Error: Plugin unloaded. There was an error with the other plugins.'\n" +
						"ERR_NO_MONEY: 'You do not have enough money.'\n" +
						"ERR_NO_PRICE: 'Price not found.'\n" +
						"ERR_PRICE_UNDER_ZERO: 'The price cannot be lower than zero.'\n" +
						"ERR_NO_RENTTIME: 'Renttime not found.'\n" +
						"ERR_NO_PRICE_RENTTIME: 'Price and renttime not found.'\n" +
						"ERR_NO_PERM: 'You are not allowed to do that.'\n" +
						"ERR_NO_PERM_BUY: 'You do not have the permission to buy a region.'\n" +
						"ERR_NO_PERM_BUY_SELL: 'You cannot buy or sell a region.'\n" +
						"ERR_NO_PERM_RENT: 'You do not have the permission to rent a room.'\n" +
						"ERR_NO_PERM_RENT_CREATE: 'You cannot create a hotel or rent a room.'\n" +
						"ERR_PLACE_AGENT: 'The agent could not be created.'\n" +
						"AGENT_WORLD_REMOVED: 'World \"$0\" got removed. Deleting all agents on that world.'\n" +
						"AGENT_REGION_REMOVED: 'Region \"$0\" got removed. Deleting all agents on that region.'\n" +
						"AGENT_BLOCK_REMOVED: 'The sign of the agent got removed somehow. Deleting that agent.'\n" +
						"AGENT_PLACED: 'You have got now $0 agent(s) placed for this region.'\n" +
						"AGENT_DELETE: 'Successfully deleted the agent.'\n" +
						"AGENT_YOURS: 'This is your agent.'\n" +
						"ERR_REGION_NAME: 'There is no region with this name.'\n" +
						"ERR_REGION_NO_OWNER: 'You do not own this region.'\n" +
						"ERR_REGION_OWNER: 'You own this region.'\n" +
						"ERR_REGION_PRICE: 'There were found some signs, which do not have the same price.'\n" +
						"ERR_REGION_PRICE_SHOW: 'Region $0, price $1 and $2.'\n" +
						"ERR_REGION_DELETE: 'Check: Region $0 does not exist anymore. Deleted Sign.'\n" +
						"ERR_REGION_LIMIT: 'You have reached your limit of regions on this world.'\n" +
						"ERR_REGION_BUY_YOURS: 'You cannot buy this region, because its yours.'\n" +
						"REGION_SOLD: 'The region $0 was just sold to $1.'\n" +
						"REGION_BUYED_NONE: 'You successfully bought the region $0.'\n" +
						"REGION_BUYED_USER: 'You successfully bought the region $0 from $1.'\n" +
						"REGION_OFFER_NONE: 'You offer the region for sale by the server.'\n" +
						"REGION_OFFER_USER: 'You offer your region for sale.'\n" +
						"ERR_PARENT_NO_OWNER: 'You do not own the hotel (parent of the region).'\n" +
						"ERR_ALREADY_RENT: 'This room is already rented by someone.'\n" +
						"HOTEL_YOURS: 'You cannot rent a room in your hotel.'\n" +
						"HOTEL_EXPIRED: 'Your room in the hotel expired. It is not anymore yours.'\n" +
						"HOTEL_RENT: 'The room $0 was just rented by $1.'\n" +
						"HOTEL_RENT_NONE: 'You successfully rented the room $0.'\n" +
						"HOTEL_RENT_USER: 'You successfully rented the room $0 from $1.'\n" +
						"HOTEL_OFFER_NONE: 'You offer this room for rent by the server.'\n" +
						"HOTEL_OFFER_USER: 'You offer this room for rent.'\n" +
						"HELP_01: 'To sell a region you need to place a sign on it.'\n" +
						"HELP_02: 'In the first line of the sign should be \"[AGENT]\"'\n" +
						"HELP_03: 'in the second line the name of the region (if there is more than one region at that location)'\n" +
						"HELP_04: 'and in the third line there should be the price (if there is not already a sign with a given price).'\n" +
						"HELP_ADM_01: 'in the third line there should be the price (if there is not already a sign with a given price)'\n" +
						"HELP_ADM_02: 'and in the last line there can be a \"none\" that the money does not go to you, but to the server.'\n" +
						"HELP_05: 'After successfully creating the sign the last line will be filled with the size of the region.'\n" +
						"HELP_BUY: 'If you want to buy a region, just right-click on the sign.'\n" +
						"CMD_MAXREGIONS_NO_ARG: 'Max Regions: $0 - Use /rm maxregions [VALUE] to set Max Regions for all players.'\n" +
						"CMD_MAXREGIONS_WRONG_ARG: 'Use /rm maxregions [VALUE] to set Max Regions for all players.'\n" +
						"CMD_MAXREGIONS: 'Max Regions for all players set to $0.'\n" +
						"CMD_LANG_NO_ARG: 'Language: \"$0\" - Use /rm lang [LANGUAGE] to set the language.'\n" +
						"CMD_LANG_SWITCHED: 'Successfully switched to English translated by <SERVERNAME HERE> :D'\n" +
						"CMD_LANG_NO_LANG: 'Language not found.'\n" +
						"CMD_LOGGING_NO_ARG: 'Logging is set to $0. To enable logging use \"/rm logging true\", otherwise use \"false\" instead.'\n" +
						"CMD_LOGGING_SET: 'Logging was set to $0.'\n" +
						"CMD_LOGGING_ALREADY_SET: 'Logging already set to $0.'\n" +
						"LOG_SWITCHED: 'Logging was switched to $0'\n" +
						"LOG_SOLD_REGION: 'Region $0 was sold to $1'\n" +
						"LOG_RENT_HOTEL: 'Hotel $0 was rent to $1'\n" +
						"LOG_EXPIRED_HOTEL: 'Hotel $0 was rent by $1 and just expired'");
			} catch (InvalidConfigurationException e) {
				outputConsole(Level.SEVERE, "[SimpleRegionMarket] Error: Internal language error!!");
				return false;
			}
			try {
				languagefile.save(default_lang);
			} catch (IOException e) {
				outputConsole(Level.SEVERE, "[SimpleRegionMarket] Could not save default language 'en.yml'.");
			}
		}

		File choosen_lang = new File(SimpleRegionMarket.plugin_dir + lang + ".yml");
		boolean ret = false;
		if(choosen_lang.exists()) {
			ret = true;
			languagefile = YamlConfiguration.loadConfiguration(choosen_lang);
		} else {
			languagefile = YamlConfiguration.loadConfiguration(default_lang);
		}
		return ret;
	}

	private static String parseLanguageString(String id, ArrayList<String> args) {
		String string = id;
		
		if(languagefile != null && languagefile.getString(id) != null) {
			string = languagefile.getString(id);
		}

		for(int i = string.length()-1; i >= 0; i--) {
			if(string.charAt(i) == '$') {
				if(string.charAt(i-1) == '$') {
					string = string.substring(0, i) + string.substring(i+1, string.length());
				} else if(Character.isDigit(string.charAt(i+1))) {
					int argi;
					try {
						argi = Integer.parseInt(Character.toString(string.charAt(i+1)));
					} catch (Exception e) {
						string = string.substring(0, i) + "ERROR ARGUMENT" + string.substring(i+2, string.length());
						continue;
					}

					try {
						string = string.substring(0, i) + args.get(argi) + string.substring(i+2, string.length());
					} catch (Exception e) {
						string = string.substring(0, i) + "ERROR ARGUMENT" + string.substring(i+2, string.length());
						continue;
					}
				}
			}
		}
		return string;
	}
	
	public static void outputConsole(Level level, String string) {
		Bukkit.getLogger().log(level, "[SimpleRegionMarket] " + string);
	}

	public static void langOutputConsole(String id, Level level, ArrayList<String> args) {
		outputConsole(level, parseLanguageString(id, args));
	}

	public static void outputDebug(Player p, String id, ArrayList<String> args) {
		p.sendMessage(ChatColor.WHITE + "[" + ChatColor.DARK_BLUE + "SimpleRegionMarket" + ChatColor.WHITE + "] " + ChatColor.YELLOW + parseLanguageString(id, args));
	}

	public static void outputError(Player p, String id, ArrayList<String> args) {
		p.sendMessage(ChatColor.WHITE + "[" + ChatColor.DARK_BLUE + "SimpleRegionMarket" + ChatColor.WHITE + "] " + ChatColor.RED + parseLanguageString(id, args));
	}

	public static void outputString(Player p, String string) {
		p.sendMessage(ChatColor.WHITE + "[" + ChatColor.DARK_BLUE + "SimpleRegionMarket" + ChatColor.WHITE + "] " + ChatColor.YELLOW + string);
	}
}
