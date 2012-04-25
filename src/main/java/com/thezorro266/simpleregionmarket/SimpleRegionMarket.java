package com.thezorro266.simpleregionmarket;

import java.io.File;
import java.util.logging.Level;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.Acrobot.ChestShop.ChestShop;
import com.nijikokun.register.payment.Method;
import com.nijikokun.register.payment.Methods;
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
	/** The economy. */
	static Economy economy = null;

	/** The enable economy. */
	static int enableEconomy = 1;

	/** The plugin_dir. */
	private static String pluginDir = null;

	/** The server. */
	private static Server server;

	/** The unloading. */
	private static boolean unloading = false;

	/**
	 * Gets the world guard.
	 * 
	 * @return the world guard
	 */
	public static WorldGuardPlugin getWorldGuard() {
		final Plugin plugin = server.getPluginManager().getPlugin("WorldGuard");

		if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
			return null;
		}

		return (WorldGuardPlugin) plugin;
	}

	private CommandHandler commandHandler;
	private ConfigHandler configurationHandler = null;
	private boolean error = false;
	private LanguageHandler langHandler;
	private LimitHandler limitHandler;
	private ChestShop chestShop;
	private TokenManager tokenManager;
	private PermissionManager permManager;

	/**
	 * Econ give money.
	 * 
	 * @param account
	 *            the account
	 * @param money
	 *            the money
	 * @return true, if successful
	 * @throws Exception
	 *             the exception
	 */
	public boolean econGiveMoney(String account, double money) throws Exception {
		final boolean ret = true;
		if (enableEconomy == 1) {
			if (getEconomicManager() != null) {
				if (money > 0) {
					getEconomicManager().getAccount(account).add(money);
				} else {
					getEconomicManager().getAccount(account).subtract(-money);
				}
			}
		} else if (enableEconomy == 2) {
			try {
				if (money > 0) {
					economy.depositPlayer(account, money);
				} else {
					economy.withdrawPlayer(account, -money);
				}
			} catch (final Exception e) {
				throw e;
			}
		}
		return ret;
	}

	/**
	 * Econ has enough.
	 * 
	 * @param account
	 *            the account
	 * @param money
	 *            the money
	 * @return true, if successful
	 */
	public boolean econHasEnough(String account, double money) {
		boolean ret = false;
		if (enableEconomy == 1) {
			if (getEconomicManager() != null) {
				ret = getEconomicManager().getAccount(account).hasEnough(money);
			}
		} else if (enableEconomy == 2) {
			ret = economy.has(account, money);
		}
		return ret;
	}

	/**
	 * Gets the configuration handler.
	 * 
	 * @return the configuration handler
	 */
	public ConfigHandler getConfigurationHandler() {
		return configurationHandler;
	}

	/**
	 * Gets the economic manager.
	 * 
	 * @return the economic manager
	 */
	public static Method getEconomicManager() {
		if (Methods.hasMethod()) {
			return Methods.getMethod();
		} else {
			Bukkit.getLogger().log(Level.SEVERE, "Error: Economic System was not found.");
			enableEconomy = 0;
			return null;
		}
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
		server = getServer();
		SimpleRegionMarket.pluginDir = getDataFolder() + File.separator;

		configurationHandler = new ConfigHandler(this);

		langHandler = new LanguageHandler(this);
	}

	@Override
	public void onEnable() {
		if (getWorldGuard() == null) {
			error = true;
			langHandler.langOutputConsole("ERR_NO_WORLDGUARD", Level.SEVERE, null);
			server.getPluginManager().disablePlugin(this);
			return;
		}

		tokenManager = new TokenManager(this);
		tokenManager.initTemplates();
		
		permManager = new PermissionManager(configurationHandler);

		enableEconomy = configurationHandler.getConfig().getBoolean("enable_economy") ? 1 : 0;
		if (enableEconomy > 0) {
			if (server.getPluginManager().getPlugin("Register") == null && server.getPluginManager().getPlugin("Vault") == null) {
				langHandler.langOutputConsole("NO_REGISTER_VAULT", Level.WARNING, null);
				enableEconomy = 0;
			} else if (server.getPluginManager().getPlugin("Register") != null && server.getPluginManager().getPlugin("Vault") == null) {
				enableEconomy = 1;
			} else {
				enableEconomy = 2;
				/*
				 * if(!setupPermissions()) { langHandler.langOutputConsole("ERR_VAULT_PERMISSIONS", Level.WARNING, null); }
				 */
				if (!setupEconomy()) {
					langHandler.langOutputConsole("ERR_VAULT_ECONOMY", Level.WARNING, null);
					enableEconomy = 0;
				}
			}
		}

		limitHandler = new LimitHandler(this, langHandler);
		limitHandler.loadLimits();

		new ListenerHandler(this, permManager, limitHandler, langHandler, tokenManager);

		commandHandler = new CommandHandler(this, limitHandler, langHandler, permManager);
		getCommand("regionmarket").setExecutor(commandHandler);

		chestShop = (ChestShop) Bukkit.getPluginManager().getPlugin("ChestShop");
		if (chestShop != null) {
			langHandler.langOutputConsole("HOOKED_CHESTSHOP", Level.INFO, null);
		}

		/*
		 * server.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() { public void run() { getAgentManager().checkAgents(); } }, 200L, 1200L);
		 */

		langHandler.outputConsole(Level.INFO, "loaded version " + getDescription().getVersion() + ".");
	}
	
	public boolean playerIsOwner(Player player, TemplateMain token, String world, ProtectedRegion protectedRegion) {
		if(player != null && token != null && world != null && protectedRegion != null) {
			String region = protectedRegion.getId();
			if(!Utils.getEntryBoolean(token, world, region, "taken")) {
				if(protectedRegion.isOwner(player.getName())) { // TODO Player Member when bought?
					return true;
				}
			} else {
				if(Utils.getEntryString(token, world, region, "owner").equalsIgnoreCase(player.getName())) {
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
			for (final World w : server.getWorlds()) {
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

	/**
	 * Setup economy.
	 * 
	 * @return the boolean
	 */
	private Boolean setupEconomy() {
		final RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
		if (economyProvider != null) {
			economy = economyProvider.getProvider();
		}

		return economy != null;
	}

	public static String getPluginDir() {
		return pluginDir;
	}
}
