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
	private final LanguageHandler langHandler;
	private final SimpleRegionMarket plugin;

	private int enableEconomy;
	private Economy economy;

	public EconomyManager(SimpleRegionMarket plugin, LanguageHandler langHandler) {
		this.plugin = plugin;
		this.langHandler = langHandler;
	}

	public void setupEconomy() {
		final Server server = plugin.getServer();
		enableEconomy = SimpleRegionMarket.configurationHandler.getConfig().getBoolean("enable_economy") ? 1 : 0;
		if (enableEconomy > 0) {
			if (server.getPluginManager().getPlugin("Register") == null && server.getPluginManager().getPlugin("Vault") == null) {
				langHandler.consoleOut("MAIN.WARN.NO_ECO_API");
				enableEconomy = 0;
			} else if (server.getPluginManager().getPlugin("Register") != null && server.getPluginManager().getPlugin("Vault") == null) {
				enableEconomy = 1;
			} else {
				enableEconomy = 2;
				if (!setupVaultEconomy()) {
					langHandler.consoleOut("MAIN.WARN.VAULT_NO_ECO");
					enableEconomy = 0;
				}
			}
		}
	}

	private Boolean setupVaultEconomy() {
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
			langHandler.consoleOut("MAIN.WARN.REGISTER_NO_ECO");
			enableEconomy = 0;
			return null;
		}
	}

	public boolean isEconomy() {
		return enableEconomy > 1 || (enableEconomy == 1 && getEconomicManager() != null);
	}

	public boolean econGiveMoney(String account, double money) {
		if (money == 0) {
			langHandler.consoleDirectOut(Level.FINEST, "[EconomyManager] Money is zero");
			return true;
		}
		try {
			if (enableEconomy == 1) {
				if (getEconomicManager() != null) {
					if (money > 0) {
						langHandler.consoleDirectOut(Level.FINEST, "[EconomyManager - Register] Adding " + String.valueOf(money) + " to Account " + account);
						getEconomicManager().getAccount(account).add(money);
					} else {
						langHandler.consoleDirectOut(Level.FINEST, "[EconomyManager - Register] Subtracting " + String.valueOf(money) + " from Account "
								+ account);
						getEconomicManager().getAccount(account).subtract(-money);
					}
				}
			} else if (enableEconomy == 2) {
				if (money > 0) {
					langHandler.consoleDirectOut(Level.FINEST, "[EconomyManager - Register] Adding " + String.valueOf(money) + " to Account " + account);
					economy.depositPlayer(account, money);
				} else {
					langHandler.consoleDirectOut(Level.FINEST, "[EconomyManager] Money is zero");
					economy.withdrawPlayer(account, -money);
				}
			}
		} catch (final Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public boolean econHasEnough(String account, double money) {
		boolean ret = false;
		if (money == 0) {
			return true;
		}
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
					return true;
				} else {
					langHandler.playerErrorOut(Bukkit.getPlayer(from), "PLAYER.ERROR.NO_MONEY", null);
					return false;
				}
			} else if (from == null) {
				econGiveMoney(to, money);
				return true;
			} else {
				if (econHasEnough(from, money)) {
					econGiveMoney(from, -money);
					econGiveMoney(to, money);
					return true;
				} else {
					langHandler.playerErrorOut(Bukkit.getPlayer(from), "PLAYER.ERROR.NO_MONEY", null);
					return false;
				}
			}
		} catch (final Exception e) {
			e.printStackTrace();
		}
		langHandler.playerErrorOut(Bukkit.getPlayer(from), "PLAYER.ERROR.ECO_PROBLEM", null);
		return false;
	}
}
