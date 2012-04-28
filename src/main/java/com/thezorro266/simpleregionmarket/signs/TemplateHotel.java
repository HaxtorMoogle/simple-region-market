package com.thezorro266.simpleregionmarket.signs;

import java.util.Date;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
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
		final long newRentTime = Utils.getEntryDate(this, world, region) + Utils.getEntryLong(this, world, region, "renttime");
		final Player owner = Bukkit.getPlayer(Utils.getEntryString(this, world, region, "owner"));
		if ((Long) tplOptions.get("renttime.max") != -1
				|| (newRentTime - System.currentTimeMillis()) / Utils.getEntryLong(this, world, region, "renttime") < (Long) tplOptions.get("renttime.max")) {
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
	public void takeRegion(Player newOwner, String world, String region) {
		final ProtectedRegion protectedRegion = SimpleRegionMarket.wgManager.getProtectedRegion(Bukkit.getWorld(world), region);

		// Clear Members and Owners
		protectedRegion.setMembers(new DefaultDomain());
		protectedRegion.setOwners(new DefaultDomain());

		protectedRegion.getMembers().addPlayer(SimpleRegionMarket.wgManager.wrapPlayer(newOwner));

		Utils.setEntry(this, world, region, "taken", true);
		Utils.setEntry(this, world, region, "owner", newOwner.getName());
		Utils.setEntry(this, world, region, "expiredate", new Date(Utils.getEntryLong(this, world, region, "renttime")));

		tokenManager.updateSigns(this, world, region);
	}

	@Override
	public void untakeRegion(String world, String region) {
		final ProtectedRegion protectedRegion = SimpleRegionMarket.wgManager.getProtectedRegion(Bukkit.getWorld(world), region);

		// Clear Members and Owners
		protectedRegion.setMembers(new DefaultDomain());
		protectedRegion.setOwners(new DefaultDomain());

		Utils.setEntry(this, world, region, "taken", false);
		Utils.setEntry(this, world, region, "owner", null);
		Utils.setEntry(this, world, region, "expiredate", null);

		tokenManager.updateSigns(this, world, region);
	}
}
