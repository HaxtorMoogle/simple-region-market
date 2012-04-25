package com.thezorro266.simpleregionmarket;

import org.bukkit.entity.Player;

public class PermissionManager {

	public PermissionManager() {

	}

	/**
	 * Can buy.
	 * 
	 * @param player
	 *            the player
	 * @return true, if successful
	 */
	public boolean canBuy(Player player) {
		return (SimpleRegionMarket.configurationHandler.getConfig().getBoolean("defp_player_buy") || player.hasPermission("simpleregionmarket.buy"));
	}

	/**
	 * Can let.
	 * 
	 * @param player
	 *            the player
	 * @return true, if successful
	 */
	public boolean canLet(Player player) {
		return (SimpleRegionMarket.configurationHandler.getConfig().getBoolean("defp_player_let") || player.hasPermission("simpleregionmarket.let"));
	}

	/**
	 * Can rent.
	 * 
	 * @param player
	 *            the player
	 * @return true, if successful
	 */
	public boolean canRent(Player player) {
		return (SimpleRegionMarket.configurationHandler.getConfig().getBoolean("defp_player_rent") || player.hasPermission("simpleregionmarket.rent"));
	}

	/**
	 * Can sell.
	 * 
	 * @param player
	 *            the player
	 * @return true, if successful
	 */
	public boolean canSell(Player player) {
		return (SimpleRegionMarket.configurationHandler.getConfig().getBoolean("defp_player_sell") || player.hasPermission("simpleregionmarket.sell"));
	}

	/**
	 * Can add owner.
	 * 
	 * @param player
	 *            the player
	 * @return true, if successful
	 */
	public boolean canAddOwner(Player player) {
		return (SimpleRegionMarket.configurationHandler.getConfig().getBoolean("defp_player_addowner") || player.hasPermission("simpleregionmarket.addowner"));
	}

	/**
	 * Can add member.
	 * 
	 * @param player
	 *            the player
	 * @return true, if successful
	 */
	public boolean canAddMember(Player player) {
		return (SimpleRegionMarket.configurationHandler.getConfig().getBoolean("defp_player_addmember") || player.hasPermission("simpleregionmarket.addmember"));
	}

	/**
	 * Checks if is admin.
	 * 
	 * @param player
	 *            the player
	 * @return true, if is admin
	 */
	public boolean isAdmin(Player player) {
		return (player.isOp() || player.hasPermission("simpleregionmarket.admin"));
	}
}
