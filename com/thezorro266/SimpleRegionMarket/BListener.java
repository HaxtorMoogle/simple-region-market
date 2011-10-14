package com.thezorro266.simpleregionmarket;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.ItemStack;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;

class BListener extends BlockListener {

	@Override
	public void onBlockBreak(BlockBreakEvent event) {
		Block b = event.getBlock();
		if (b.getTypeId() == 63 || b.getTypeId() == 68) {
			Sign sign = (Sign) b.getState();
			if (sign.getLine(0).equals("[AGENT]")) {
				if(SimpleRegionMarket.getEconomicManager() != null) {
					RegionAgent agent = SimpleRegionMarket.getAgentManager().getAgent(b.getLocation());

					if (agent == null)
						return;

					Player p = event.getPlayer();
					ProtectedRegion region = SimpleRegionMarket.getWorldGuard().getRegionManager(b.getLocation().getWorld()).getRegion(agent.getRegion());
					if (!SimpleRegionMarket.getAgentManager().isOwner(p, region) && !SimpleRegionMarket.isAdmin(p)) {
						event.setCancelled(true);
						sign.update();
						return;
					}

					event.setCancelled(true);
					if (p != null) {
						agent.destroyAgent(true);
						LanguageHandler.outputDebug(event.getPlayer(), "AGENT_DELETE", null);
					}
					SimpleRegionMarket.getAgentManager().removeAgent(agent);
					SimpleRegionMarket.saveAll();
				} else {
					if(event.getPlayer() != null) {
						LanguageHandler.outputError(event.getPlayer(), "ERR_NO_ECO_USER", null);
					}
				}
			}
		}
	}

	@Override
	public void onSignChange(SignChangeEvent event) {
		if (event.getLine(0).equalsIgnoreCase("[AGENT]")) {
			if(SimpleRegionMarket.getEconomicManager() != null) {
				ProtectedRegion region;
				Location signloc = event.getBlock().getLocation();

				RegionAgent oldagent = SimpleRegionMarket.getAgentManager().getAgent(signloc);
				if(oldagent != null) {
					SimpleRegionMarket.getAgentManager().getAgentList().remove(oldagent);
				}

				if (event.getLine(1).isEmpty()) {
					region = SimpleRegionMarket.getAgentManager().getRegion(signloc);
				} else {
					region = SimpleRegionMarket.getWorldGuard().getRegionManager(signloc.getWorld()).getRegion(event.getLine(1));
				}

				Player p = event.getPlayer();

				if (p != null) {
					if(!SimpleRegionMarket.canSell(p)) {
						event.setCancelled(true);
						event.getBlock().setType(Material.AIR);
						signloc.getWorld().dropItem(signloc, new ItemStack(Material.SIGN, 1));
						return;
					}
				}
				if (region == null) {
					if (p != null) {
						LanguageHandler.outputError(p, "ERR_REGION_NAME", null);
					}
					event.setCancelled(true);
					event.getBlock().setType(Material.AIR);
					signloc.getWorld().dropItem(signloc, new ItemStack(Material.SIGN, 1));
					return;
				}

				if (p != null) {
					if (!SimpleRegionMarket.getAgentManager().isOwner(p, region) && !SimpleRegionMarket.isAdmin(p)) {
						LanguageHandler.outputError(p, "ERR_REGION_NO_OWNER", null);
						event.setCancelled(true);
						event.getBlock().setType(Material.AIR);
						signloc.getWorld().dropItem(signloc, new ItemStack(Material.SIGN, 1));
						return;
					}
				}

				double price = 0;
				if (event.getLine(2).isEmpty()) {
					if (SimpleRegionMarket.getAgentManager().countAgents(region) > 0) {
						price = SimpleRegionMarket.getAgentManager().getRegionPrice(region, null);
					} else {
						price = -1;
					}
					if (price < 0) {
						if (p != null) {
							LanguageHandler.outputError(p, "ERR_NO_PRICE", null);
						}
						event.setCancelled(true);
						event.getBlock().setType(Material.AIR);
						signloc.getWorld().dropItem(signloc, new ItemStack(Material.SIGN, 1));
						return;
					}
				} else {
					try {
						price = Double.parseDouble(event.getLine(2));
					} catch (Exception e) {
						if (p != null) {
							LanguageHandler.outputError(p, "ERR_NO_PRICE", null);
						}
						event.setCancelled(true);
						event.getBlock().setType(Material.AIR);
						signloc.getWorld().dropItem(signloc, new ItemStack(Material.SIGN, 1));
						return;
					}
					if (price < 0) {
						if (p != null) {
							LanguageHandler.outputError(p, "ERR_PRICE_UNDER_ZERO", null);
						}
						event.setCancelled(true);
						event.getBlock().setType(Material.AIR);
						signloc.getWorld().dropItem(signloc, new ItemStack(Material.SIGN, 1));
						return;
					}
				}

				Player lp = p;
				if (p != null) {
					if (!SimpleRegionMarket.getAgentManager().isOwner(p, region) || event.getLine(3).equalsIgnoreCase("none")) {
						if(SimpleRegionMarket.isAdmin(p)) {
							lp = null;
						}
					}
				}

				if (SimpleRegionMarket.getAgentManager().addAgent(region, signloc, lp, price)) {
					if (p != null) {
						if(lp == null) {
							LanguageHandler.outputDebug(p, "REGION_OFFER_NONE", null);
						} else {
							LanguageHandler.outputDebug(p, "REGION_OFFER_USER", null);
						}
						SimpleRegionMarket.getAgentManager().getRegionPrice(region, p);
						if (SimpleRegionMarket.getAgentManager().countAgents(region) > 1) {

							ArrayList<String> list = new ArrayList<String>();
							list.add(Integer.toString(SimpleRegionMarket.getAgentManager().countAgents(region)));
							LanguageHandler.outputDebug(p, "AGENT_PLACED", list);
						}
					}
					event.setLine(0, "[AGENT]");
					event.setLine(1, region.getId());
					event.setLine(2, SimpleRegionMarket.getEconomicManager().format(price));
					int rightX = (int) region.getMaximumPoint().getX() - (int) (region.getMinimumPoint().getX() - 1);
					if (rightX < 0) {
						rightX *= -1;
					}
					int rightY = (int) region.getMaximumPoint().getY() - (int) (region.getMinimumPoint().getY() - 1);
					if (rightY < 0) {
						rightY *= -1;
					}
					int rightZ = (int) region.getMaximumPoint().getZ() - (int) (region.getMinimumPoint().getZ() - 1);
					if (rightZ < 0) {
						rightZ *= -1;
					}
					event.setLine(3, Integer.toString(rightX) + " x " + Integer.toString(rightY) + " x " + Integer.toString(rightZ));
					SimpleRegionMarket.saveAll();
				} else {
					if (p != null) {
						LanguageHandler.outputError(p, "ERR_PLACE_AGENT", null);
					}
					event.setCancelled(true);
					event.getBlock().setType(Material.AIR);
					signloc.getWorld().dropItem(signloc, new ItemStack(Material.SIGN, 1));
					return;
				}
			} else {
				if(event.getPlayer() != null) {
					LanguageHandler.outputError(event.getPlayer(), "ERR_NO_ECO_USER", null);
				}
			}
		}
	}
}