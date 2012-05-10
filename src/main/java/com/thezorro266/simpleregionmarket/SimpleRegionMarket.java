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
	public static PermissionsManager permManager = null;
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

		permManager = new PermissionsManager();

		econManager = new EconomyManager(this, langHandler);
	}

	@Override
	public void onEnable() {
		econManager.setupEconomy();

		tokenManager = new TokenManager(this, langHandler);
		tokenManager.initTemplates();

		limitHandler = new LimitHandler(this, tokenManager);

		new ListenerHandler(this, langHandler, tokenManager);

		commandHandler = new CommandHandler(this, langHandler);
		getCommand("regionmarket").setExecutor(commandHandler);

		final File agents = new File(SimpleRegionMarket.getPluginDir() + "agents.yml");
		if (agents.exists()) {
			if (configurationHandler.loadOld()) {
				langHandler.consoleDirectOut(Level.INFO, "Imported successfully the old agents.yml");
				agents.delete();
			} else {
				langHandler.consoleDirectOut(Level.INFO, "Importing was not successful. Do you have SELL and HOTEL templates?");
			}
		}

		// Check all signs and output stats
		long ms = System.currentTimeMillis();
		final int[] count = tokenManager.checkRegions();
		ms = System.currentTimeMillis() - ms;
		langHandler.consoleDirectOut(Level.INFO, "Loaded " + TokenManager.tokenList.size() + " template(s), " + count[0] + " world(s) and " + count[1]
				+ " region(s).");
		langHandler.consoleDirectOut(Level.INFO, "The check took " + ms + "ms");

		getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			@Override
			public void run() {
				tokenManager.checkRegions();
			}
		}, 1200L, 1200L);
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
						langHandler.consoleDirectOut(Level.SEVERE, "WorldGuard >> Failed to write regionsfile: " + e.getMessage());
					}
				}
			} else {
				langHandler.consoleDirectOut(Level.SEVERE, "Saving WorldGuard failed, because it is not loaded.");
			}
		}
		for (final TemplateMain token : TokenManager.tokenList) {
			token.save();
		}
	}
}
