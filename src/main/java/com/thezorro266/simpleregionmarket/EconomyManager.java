package com.thezorro266.simpleregionmarket;

import java.util.logging.Level;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.plugin.RegisteredServiceProvider;

import com.nijikokun.register.payment.Method;
import com.nijikokun.register.payment.Methods;
import com.thezorro266.simpleregionmarket.handlers.LanguageHandler;

public class EconomyManager {
	private final LanguageHandler LANG_HANDLER;

	private int enableEconomy;
	private Economy economy;

	public EconomyManager(SimpleRegionMarket plugin, LanguageHandler langHandler) {
		LANG_HANDLER = langHandler;
		final Server server = plugin.getServer();
		enableEconomy = SimpleRegionMarket.configurationHandler.getConfig().getBoolean("enable_economy") ? 1 : 0;
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
	}

	private Boolean setupEconomy() {
		final RegisteredServiceProvider<Economy> economyProvider = Bukkit.getServer().getServicesManager()
				.getRegistration(net.milkbowl.vault.economy.Economy.class);
		if (economyProvider != null) {
			economy = economyProvider.getProvider();
		}

		return economy != null;
	}

	public Method getEconomicManager() {
		if (Methods.hasMethod()) {
			return Methods.getMethod();
		} else {
			LANG_HANDLER.outputConsole(Level.SEVERE, "Error: Economic System was not found.");
			enableEconomy = 0;
			return null;
		}
	}

	public boolean isEconomy() {
		return enableEconomy > 0 && (enableEconomy != 1 || getEconomicManager() != null);
	}

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

	public String econFormat(double price) {
		String ret = String.valueOf(price);
		if (enableEconomy == 1) {
			if (getEconomicManager() != null) {
				ret = getEconomicManager().format(price);
			}
		} else if (enableEconomy == 2) {
			ret = economy.format(price);
		}
		return ret;
	}

	public boolean moneyTransaction(String from, String to, double money) {
		try {
			if (to == null) {
				if (econHasEnough(from, money)) {
					econGiveMoney(from, -money);
				} else {
					LANG_HANDLER.outputError(Bukkit.getPlayer(from), "ERR_NO_MONEY", null);
				}
				return true;
			} else if (from == null) {
				econGiveMoney(to, money);
				return true;
			} else {
				if (econHasEnough(from, money)) {
					econGiveMoney(from, -money);
					econGiveMoney(to, money);
				} else {
					LANG_HANDLER.outputError(Bukkit.getPlayer(from), "ERR_NO_MONEY", null);
				}
				return true;
			}
		} catch (final Exception e) {
			e.printStackTrace();
		}
		LANG_HANDLER.outputError(Bukkit.getPlayer(from), "ERR_ECO_TRANSFER", null);
		return false;
	}
}
