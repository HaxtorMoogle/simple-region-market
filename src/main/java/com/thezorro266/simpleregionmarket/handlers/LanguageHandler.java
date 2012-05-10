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
import com.thezorro266.simpleregionmarket.Utils;

public class LanguageHandler {
	private final FileConfiguration languageFile = new YamlConfiguration();
	private final SimpleRegionMarket plugin;

	public LanguageHandler(SimpleRegionMarket plugin) {
		this.plugin = plugin;

		try {
			languageFile.load(SimpleRegionMarket.getPluginDir() + "en.yml");
		} catch (final FileNotFoundException e) {
			plugin.saveResource("en.yml", false);
			return;
		} catch (final IOException e) {
			e.printStackTrace();
			return;
		} catch (final InvalidConfigurationException e) {
			e.printStackTrace();
			return;
		}
		final String languageVersion = languageFile.getString("version");
		try {
			languageFile.load(plugin.getResource("en.yml"));
		} catch (final IOException e) {
			e.printStackTrace();
			return;
		} catch (final InvalidConfigurationException e) {
			e.printStackTrace();
			return;
		}
		if (languageFile.getString("version") == null) {
			return;
		}
		if (languageVersion == null || Utils.compareVersions(languageFile.getString("version"), languageVersion) == 1) {
			plugin.saveResource("en.yml", true);
		}
	}

	public void consoleDirectOut(Level level, String string) {
		Bukkit.getLogger().log(level, "[SimpleRegionMarket] " + string);
	}

	public void consoleOut(String id, Level level, ArrayList<String> args) {
		consoleDirectOut(level, parseLanguageString(id, args));
	}

	public void consoleOut(String id) {
		final String[] split = id.split("\\.");
		if (split.length < 3) {
			consoleDirectOut(Level.WARNING, "Could not resolve language ID '" + id + "'.");
			return;
		}
		final String levelID = split[1];
		String prefix = "";
		Level msgLevel = Level.INFO;

		if (levelID.equalsIgnoreCase("norm")) {
			msgLevel = Level.INFO;
		} else if (levelID.equalsIgnoreCase("error")) {
			msgLevel = Level.SEVERE;
		} else if (levelID.equalsIgnoreCase("warn")) {
			msgLevel = Level.WARNING;
		} else if (levelID.equalsIgnoreCase("log")) {
			msgLevel = Level.INFO;
		} else if (levelID.equalsIgnoreCase("debug")) {
			msgLevel = Level.INFO;
			prefix = "Debug: ";
		}
		if (msgLevel != null) {
			consoleDirectOut(msgLevel, prefix + parseLanguageString(id, null));
		}
	}

	public void playerDirectOut(Player p, ChatColor color, String string) {
		p.sendMessage(ChatColor.WHITE + "[" + ChatColor.DARK_BLUE + "SRM" + ChatColor.WHITE + "] " + color + string);
	}

	public void playerNormalOut(Player p, String id, ArrayList<String> args) {
		playerDirectOut(p, ChatColor.YELLOW, parseLanguageString(id, args));
	}

	public void playerListOut(Player p, String id, ArrayList<String> args) {
		playerDirectOut(p, ChatColor.BLUE, parseLanguageString(id, args));
	}

	public void playerErrorOut(Player p, String id, ArrayList<String> args) {
		playerDirectOut(p, ChatColor.RED, parseLanguageString(id, args));
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
			consoleDirectOut(Level.WARNING, "Language '" + lang + "' was not found.");
			lang = "en";
			SimpleRegionMarket.configurationHandler.getConfig().set("language", lang);
			plugin.saveConfig();
		}

		try {
			languageFile.load(SimpleRegionMarket.getPluginDir() + lang + ".yml");
			string = languageFile.getString(id);
		} catch (final FileNotFoundException e1) {
			consoleDirectOut(Level.SEVERE, "No write permissions on '" + SimpleRegionMarket.getPluginDir() + "'.");
			e1.printStackTrace();
		} catch (final IOException e1) {
			consoleDirectOut(Level.SEVERE, "IO Exception in language system.");
			e1.printStackTrace();
		} catch (final InvalidConfigurationException e1) {
			consoleDirectOut(Level.SEVERE, "Language file corrupt (Invalid YAML).");
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
