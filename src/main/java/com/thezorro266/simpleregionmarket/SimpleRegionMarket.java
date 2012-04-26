package com.thezorro266.simpleregionmarket;

import java.io.File;
import java.util.logging.Level;

import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

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

	// Public classes:
	public static ConfigHandler configurationHandler = null;
	public static WorldGuardManager wgManager = null;
	public static PermissionManager permManager = null;
	public static EconomyManager econManager = null;
	public static LimitHandler limitHandler = null;

	// Private classes:
	private CommandHandler commandHandler;
	private LanguageHandler langHandler;
	private TokenManager tokenManager;

	public static String getPluginDir() {
		return pluginDir;
	}

	@Override
	public void onDisable() {
		unloading = true;
		saveAll();
	}

	@Override
	public void onLoad() {
		SimpleRegionMarket.pluginDir = getDataFolder() + File.separator;

		configurationHandler = new ConfigHandler(this);

		langHandler = new LanguageHandler(this);

		wgManager = new WorldGuardManager(langHandler);

		permManager = new PermissionManager();

		econManager = new EconomyManager(this, langHandler);
	}

	@Override
	public void onEnable() {
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
		if (!unloading) {
			if (wgManager.getWorldGuard() != null) {
				for (final World w : getServer().getWorlds()) {
					final RegionManager mgr = wgManager.getWorldGuard().getRegionManager(w);

					try {
						mgr.save();
					} catch (final ProtectionDatabaseException e) {
						langHandler.outputConsole(Level.SEVERE, "WorldGuard >> Failed to write regionsfile: " + e.getMessage());
					}
				}
			} else {
				langHandler.outputConsole(Level.SEVERE, "Saving WorldGuard failed, because it is not loaded.");
			}
		}
		for (final TemplateMain token : TokenManager.tokenList) {
			token.save();
		}
	}
}
