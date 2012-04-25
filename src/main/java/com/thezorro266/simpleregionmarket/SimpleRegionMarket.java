package com.thezorro266.simpleregionmarket;

import java.io.File;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.databases.ProtectionDatabaseException;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.thezorro266.simpleregionmarket.handlers.CommandHandler;
import com.thezorro266.simpleregionmarket.handlers.ConfigHandler;
import com.thezorro266.simpleregionmarket.handlers.LanguageHandler;
import com.thezorro266.simpleregionmarket.handlers.LimitHandler;
import com.thezorro266.simpleregionmarket.handlers.ListenerHandler;
import com.thezorro266.simpleregionmarket.signs.TemplateMain;

/**
 * The Class SimpleRegionMarket.
 */
public class SimpleRegionMarket extends JavaPlugin {
	private static String pluginDir = null;
	private boolean unloading = false;

	/**
	 * Gets the world guard.
	 * 
	 * @return the world guard
	 */
	public static WorldGuardPlugin getWorldGuard() {
		final Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("WorldGuard");

		if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
			return null;
		}

		return (WorldGuardPlugin) plugin;
	}

	// Public classes:
	public static ConfigHandler configurationHandler = null;
	public static PermissionManager permManager = null;
	public static EconomyManager econManager = null;
	public static LimitHandler limitHandler = null;

	// Private classes:
	private CommandHandler commandHandler;
	private boolean error = false;
	private LanguageHandler langHandler;
	private TokenManager tokenManager;

	public static String getPluginDir() {
		return pluginDir;
	}

	@Override
	public void onDisable() {
		unloading = true;
		if (error) {
			langHandler.langOutputConsole("ERR_PLUGIN_UNLOAD", Level.SEVERE, null);
		} else {
			saveAll();
			langHandler.langOutputConsole("PLUGIN_UNLOAD", Level.INFO, null);
		}
	}

	@Override
	public void onLoad() {
		SimpleRegionMarket.pluginDir = getDataFolder() + File.separator;

		configurationHandler = new ConfigHandler(this);

		langHandler = new LanguageHandler(this);

		permManager = new PermissionManager();

		econManager = new EconomyManager(this, langHandler);
	}

	@Override
	public void onEnable() {
		if (getWorldGuard() == null) {
			error = true;
			langHandler.langOutputConsole("ERR_NO_WORLDGUARD", Level.SEVERE, null);
			getServer().getPluginManager().disablePlugin(this);
			return;
		}

		tokenManager = new TokenManager(this, langHandler);
		tokenManager.initTemplates();

		limitHandler = new LimitHandler(this, langHandler, tokenManager);

		new ListenerHandler(this, langHandler, tokenManager);

		commandHandler = new CommandHandler(this, langHandler);
		getCommand("regionmarket").setExecutor(commandHandler);

		// TODO Re-enable scheduler
		/*
		 * server.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() { public void run() { getAgentManager().checkAgents(); } }, 200L, 1200L);
		 */

		// Update for all signs (will create missing signs)

		for (final TemplateMain token : TokenManager.tokenList) {
			for (final String world : token.entries.keySet()) {
				for (final String region : token.entries.get(world).keySet()) {
					tokenManager.updateSigns(token, world, region);
				}
			}
		}

		langHandler.outputConsole(Level.INFO, "loaded version " + getDescription().getVersion() + ".");
	}

	/**
	 * Save all.
	 */
	public void saveAll() {
		if (getWorldGuard() != null && getWorldGuard().isEnabled()) {
			for (final World w : getServer().getWorlds()) {
				final RegionManager mgr = getWorldGuard().getGlobalRegionManager().get(w);

				try {
					mgr.save();
				} catch (final ProtectionDatabaseException e) {
					langHandler.outputConsole(Level.SEVERE, "WorldGuard >> Failed to write regionsfile: " + e.getMessage());
				}
			}
		} else {
			if (!unloading) {
				langHandler.outputConsole(Level.SEVERE, "Saving WorldGuard failed, because it is not loaded.");
			}
		}
		for (final TemplateMain token : TokenManager.tokenList) {
			token.save();
		}
	}
}
