package com.thezorro266.simpleregionmarket.signs;

import java.util.Date;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.thezorro266.simpleregionmarket.SimpleRegionMarket;
import com.thezorro266.simpleregionmarket.TokenManager;
import com.thezorro266.simpleregionmarket.Utils;
import com.thezorro266.simpleregionmarket.handlers.LanguageHandler;

/**
 * @author theZorro266
 * 
 */
public class TemplateHotel extends TemplateLet {
	public TemplateHotel(SimpleRegionMarket plugin, LanguageHandler langHandler, TokenManager tokenManager, String tplId) {
		super(plugin, langHandler, tokenManager, tplId);
	}

	@Override
	public void ownerClicksTakenSign(String world, String region) {
		final long newRentTime = Utils.getEntryLong(this, world, region, "expiredate") + Utils.getEntryLong(this, world, region, "renttime");
		final Player owner = Bukkit.getPlayer(Utils.getEntryString(this, world, region, "owner"));
		if (Utils.getOptionLong(this, "renttime.max") != -1
				|| (newRentTime - System.currentTimeMillis()) / Utils.getEntryLong(this, world, region, "renttime") < Utils.getOptionLong(this, "renttime.max")) {
			if (SimpleRegionMarket.econManager.isEconomy()) {
				String account = Utils.getEntryString(this, world, region, "account");
				if (account.isEmpty()) {
					account = null;
				}
				final double price = Utils.getEntryDouble(this, world, region, "price");
				if (SimpleRegionMarket.econManager.moneyTransaction(Utils.getEntryString(this, world, region, "owner"), account, price)) {
					Utils.setEntry(this, world, region, "expiredate", new Date(newRentTime));
					tokenManager.updateSigns(this, world, region);
					langHandler.outputMessage(owner, "HOTEL_SUCCESS_RERENT", null);
				}
			} else {
				Utils.setEntry(this, world, region, "expiredate", new Date(newRentTime));
				tokenManager.updateSigns(this, world, region);
				langHandler.outputMessage(owner, "HOTEL_SUCCESS_RERENT", null);
			}
		} else {
			langHandler.outputError(owner, "ERR_RERENT_TOO_LONG", null);
		}
	}
	
	@Override
	public void schedule(String world, String region) {
		if(Utils.getEntryBoolean(this, world, region, "taken")) {
			if(Utils.getEntryLong(this, world, region, "expiredate") < System.currentTimeMillis()) {
				untakeRegion(world, region);
				final Player player = Bukkit.getPlayer(Utils.getEntryString(this, world, region, "owner"));
				if(player != null) {
					langHandler.outputMessage(player, "HOTEL_EXPIRED", null);
				}
			}
		}
	}
}
