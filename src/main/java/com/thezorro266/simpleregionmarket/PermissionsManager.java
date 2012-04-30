package com.thezorro266.simpleregionmarket;

import org.bukkit.entity.Player;

import com.thezorro266.simpleregionmarket.signs.TemplateMain;

public class PermissionsManager {
	public void setupTokenPermissions() {
		// TODO register permissions
	}
	
	public boolean canPlayerBuyToken(Player player, TemplateMain token) {
		return player.hasPermission("simpleregionmarket."+token.id+".buy");
	}
	
	public boolean canPlayerSellToken(Player player, TemplateMain token) {
		return player.hasPermission("simpleregionmarket."+token.id+".sell");
	}

	public boolean isAdmin(Player player) {
		return (player.isOp() || player.hasPermission("simpleregionmarket.admin"));
	}
}
