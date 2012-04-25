package com.thezorro266.simpleregionmarket.handlers;

/*
 * 
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.thezorro266.simpleregionmarket.SimpleRegionMarket;

public class LanguageHandler {
	private final FileConfiguration LANGUAGE_FILE = new YamlConfiguration();
	private final SimpleRegionMarket PLUGIN;

	/**
	 * Instantiates a new language handler.
	 * 
	 * @param plugin
	 *            the plugin
	 */
	public LanguageHandler(SimpleRegionMarket plugin) {
		PLUGIN = plugin;
		if (!new File(SimpleRegionMarket.getPluginDir() + "en.yml").exists()) {
			plugin.saveResource("en.yml", false);
		}
	}

	/**
	 * Lang output console.
	 * 
	 * @param id
	 *            the id
	 * @param level
	 *            the level
	 * @param args
	 *            the args
	 */
	public void langOutputConsole(String id, Level level, ArrayList<String> args) {
		outputConsole(level, parseLanguageString(id, args));
	}

	/**
	 * Output console.
	 * 
	 * @param level
	 *            the level
	 * @param string
	 *            the string
	 */
	public void outputConsole(Level level, String string) {
		Bukkit.getLogger().log(level, "[SimpleRegionMarket] " + string);
	}

	/**
	 * Output message.
	 * 
	 * @param p
	 *            the p
	 * @param id
	 *            the id
	 * @param args
	 *            the args
	 */
	public void outputMessage(Player p, String id, ArrayList<String> args) {
		p.sendMessage(ChatColor.WHITE + "[" + ChatColor.DARK_BLUE + "SRM" + ChatColor.WHITE + "] " + ChatColor.YELLOW + parseLanguageString(id, args));
	}

	/**
	 * Output error.
	 * 
	 * @param p
	 *            the p
	 * @param id
	 *            the id
	 * @param args
	 *            the args
	 */
	public void outputError(Player p, String id, ArrayList<String> args) {
		p.sendMessage(ChatColor.WHITE + "[" + ChatColor.DARK_BLUE + "SRM" + ChatColor.WHITE + "] " + ChatColor.RED + parseLanguageString(id, args));
	}

	/**
	 * Output string.
	 * 
	 * @param p
	 *            the p
	 * @param string
	 *            the string
	 */
	public void outputString(Player p, String string) {
		p.sendMessage(ChatColor.WHITE + "[" + ChatColor.DARK_BLUE + "SRM" + ChatColor.WHITE + "] " + ChatColor.YELLOW + string);
	}

	/**
	 * Parses the language string.
	 * 
	 * @param id
	 *            the id
	 * @param args
	 *            the args
	 * @return the string
	 */
	private String parseLanguageString(String id, ArrayList<String> args) {
		String string = id;

		String lang = SimpleRegionMarket.configurationHandler.getConfig().getString("language");
		if (!new File(SimpleRegionMarket.getPluginDir() + lang + ".yml").exists()) {
			outputConsole(Level.WARNING, "Language '" + lang + "' was not found.");
			lang = "en";
			SimpleRegionMarket.configurationHandler.getConfig().set("language", lang);
			PLUGIN.saveConfig();
		}

		try {
			LANGUAGE_FILE.load(SimpleRegionMarket.getPluginDir() + lang + ".yml");
			string = LANGUAGE_FILE.getString(id);
		} catch (final FileNotFoundException e1) {
			outputConsole(Level.SEVERE, "No write permissions on '" + SimpleRegionMarket.getPluginDir() + "'.");
			e1.printStackTrace();
		} catch (final IOException e1) {
			outputConsole(Level.SEVERE, "IO Exception in language system.");
			e1.printStackTrace();
		} catch (final InvalidConfigurationException e1) {
			outputConsole(Level.SEVERE, "Language file corrupt (Invalid YAML).");
			e1.printStackTrace();
		} catch (final Exception e1) {
			e1.printStackTrace();
		}

		if (string == null || string.isEmpty()) {
			string = id;
		}

		for (int i = string.length() - 1; i > -1; i--) {
			if (string.charAt(i) == '$') {
				if (i != 0 && string.charAt(i - 1) == '$') {
					string = string.substring(0, i) + string.substring(i + 1, string.length());
				} else if (Character.isDigit(string.charAt(i + 1))) {
					int argi;
					try {
						argi = Integer.parseInt(Character.toString(string.charAt(i + 1)));
					} catch (final Exception e) {
						string = string.substring(0, i) + "ERROR ARGUMENT" + string.substring(i + 2, string.length());
						continue;
					}

					try {
						string = string.substring(0, i) + args.get(argi) + string.substring(i + 2, string.length());
					} catch (final Exception e) {
						string = string.substring(0, i) + "ERROR ARGUMENT" + string.substring(i + 2, string.length());
						continue;
					}
				}
			}
		}
		return string;
	}
}
