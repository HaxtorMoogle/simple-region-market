package com.thezorro266.simpleregionmarket;

import java.io.File;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.Acrobot.ChestShop.ChestShop;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.databases.ProtectionDatabaseException;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
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

	// Private classes:
	private CommandHandler commandHandler;
	private boolean error = false;
	private LanguageHandler langHandler;
	private LimitHandler limitHandler;
	private ChestShop chestShop;
	private TokenManager tokenManager;

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

		econManager = new EconomyManager(langHandler, this);
	}

	@Override
	public void onEnable() {
		if (getWorldGuard() == null) {
			error = true;
			langHandler.langOutputConsole("ERR_NO_WORLDGUARD", Level.SEVERE, null);
			getServer().getPluginManager().disablePlugin(this);
			return;
		}

		tokenManager = new TokenManager(this);
		tokenManager.initTemplates();

		limitHandler = new LimitHandler(this, langHandler);
		limitHandler.loadLimits();

		new ListenerHandler(this, limitHandler, langHandler, tokenManager);

		commandHandler = new CommandHandler(this, limitHandler, langHandler);
		getCommand("regionmarket").setExecutor(commandHandler);

		chestShop = (ChestShop) Bukkit.getPluginManager().getPlugin("ChestShop");
		if (chestShop != null) {
			langHandler.langOutputConsole("HOOKED_CHESTSHOP", Level.INFO, null);
		}

		// TODO Re-enable scheduler
		/*
		 * server.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() { public void run() { getAgentManager().checkAgents(); } }, 200L, 1200L);
		 */

		langHandler.outputConsole(Level.INFO, "loaded version " + getDescription().getVersion() + ".");
	}

	public boolean playerIsOwner(Player player, TemplateMain token, String world, ProtectedRegion protectedRegion) {
		if (player != null && token != null && world != null && protectedRegion != null) {
			final String region = protectedRegion.getId();
			if (!Utils.getEntryBoolean(token, world, region, "taken")) {
				if (protectedRegion.isOwner(player.getName())) { // TODO Player Member when bought?
					return true;
				}
			} else {
				if (Utils.getEntryString(token, world, region, "owner").equalsIgnoreCase(player.getName())) {
					return true;
				}
			}
		}
		return false;
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
		limitHandler.saveLimits();
		for (final TemplateMain token : TokenManager.tokenList) {
			token.save();
		}
	}

	public static String getPluginDir() {
		return pluginDir;
	}
}
