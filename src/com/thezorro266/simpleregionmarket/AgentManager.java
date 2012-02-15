package com.thezorro266.simpleregionmarket;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class AgentManager {
	private ArrayList<SignAgent> agents = new ArrayList<SignAgent>();

	public SignAgent addAgent(int mode, Location location, ProtectedRegion region, double price, String account, long renttime) {
		if (mode == SignAgent.MODE_SELL_REGION || mode == SignAgent.MODE_RENT_HOTEL) {
			if(location != null) {
				if(location.getWorld() != null) {
					if(region != null) {
						if(price >= 0) {
							if(account == null) {
								account = "";
							}
							if((renttime == 0 && mode == SignAgent.MODE_SELL_REGION) || (renttime > 0 && mode == SignAgent.MODE_RENT_HOTEL)) {
								SignAgent newagent = new SignAgent(mode, location, region.getId(), price, account, renttime);
								getAgentList().add(newagent);
								return newagent;
							}
						}
					}
				}
			}
		}
		return null;
	}

	public SignAgent addAgent(int mode, Location location, ProtectedRegion region, double price, String account, long renttime, String rentby, Date expiredate) {
		if(expiredate != null) {
			SignAgent newagent = addAgent(mode, location, region, price, account, renttime);
			if(newagent != null) {
				newagent.rentTo(rentby, expiredate);
				return newagent;
			}
		}
		return null;
	}

	public void checkAgents() {
		ArrayList<String> worlds_called = new ArrayList<String>();
		ArrayList<String> regions_called = new ArrayList<String>();
		ArrayList<String> player_hotel_expired = new ArrayList<String>();
		Iterator<SignAgent> itr = getAgentList().iterator();
		while(itr.hasNext()) {
			SignAgent obj = itr.next();
			if (obj.getWorldWorld() == null) { // world removed - remove agent
				if(!worlds_called.contains(obj.getWorld())) { // not already called that world
					ArrayList<String> list = new ArrayList<String>();
					list.add(obj.getWorld());
					LanguageHandler.langOutputConsole("AGENT_WORLD_REMOVED", Level.WARNING, list);
					worlds_called.add(obj.getWorld());
				}
				itr.remove();
			} else if(obj.getProtectedRegion() == null) { // region removed - remove agent
				if(!regions_called.contains(obj.getRegion())) { // not already called that region
					ArrayList<String> list = new ArrayList<String>();
					list.add(obj.getRegion());
					LanguageHandler.langOutputConsole("AGENT_REGION_REMOVED", Level.WARNING, list);
					regions_called.add(obj.getRegion());
				}
				obj.destroyAgent(false);
				itr.remove();
			} else if(obj.getLocation().getBlock() == null ||
					(obj.getLocation().getBlock().getTypeId() != 63 &&
					obj.getLocation().getBlock().getTypeId() != 68)) { // block is not a sign - remove agent
				LanguageHandler.langOutputConsole("AGENT_BLOCK_REMOVED", Level.WARNING, null);
				itr.remove();
			} else {
				if(obj.isRent()) {
					if(obj.getExpireDate().getTime() < System.currentTimeMillis()) {
						Player p = Bukkit.getPlayerExact(obj.getRent());
						obj.getProtectedRegion().setMembers(new DefaultDomain());
						obj.getProtectedRegion().setOwners(new DefaultDomain());
						if(SimpleRegionMarket.logging) {
							ArrayList<String> list = new ArrayList<String>();
							list.add(obj.getRegion());
							list.add(obj.getRent());
							LanguageHandler.langOutputConsole("LOG_EXPIRED_HOTEL", Level.INFO, list);
						}
						obj.rentTo("");
						if(p != null) {
							if(!player_hotel_expired.contains(p.getName())) {
								LanguageHandler.outputDebug(p, "HOTEL_EXPIRED", null);
								player_hotel_expired.add(p.getName());
							}
						}
					}
				}
				actAgent(obj, null);
			}
		}
	}

	public int countAgents(ProtectedRegion region) {
		int count = 0;
		if (region != null) {
			for (SignAgent obj : getAgentList()) {
				if (obj.getProtectedRegion() == region) {
					count++;
				}
			}
		}
		return count;
	}

	public SignAgent getAgent(Location loc) {
		if (loc != null) {
			for (SignAgent obj : getAgentList()) {
				if (loc.equals(obj.getLocation()))
					return obj;
			}
		}
		return null;
	}

	public boolean removeAgent(SignAgent agent) {
		boolean ret = false;
		if(agent != null) {
			agent.destroyAgent(false);
			getAgentList().remove(agent);
			ret = true;
		}
		return ret;
	}

	public void rentRegionForPlayer(ProtectedRegion region, Player p, long renttime) {
		if(region != null) {
			Iterator<SignAgent> itr = getAgentList().iterator();
			while(itr.hasNext()) {
				SignAgent obj = itr.next();
				if(obj.getProtectedRegion() == region) {
					obj.rentTo(p.getName(), new Date(System.currentTimeMillis()+renttime));
					actAgent(obj, null);
				}
			}
		}
	}

	public void actAgent(SignAgent agent, SignChangeEvent event) {
		if(agent != null) {
			Sign agentsign = (Sign)agent.getLocation().getBlock().getState();
			if(agent.getMode() == SignAgent.MODE_SELL_REGION) {
				if(event != null) {
					event.setLine(0, SimpleRegionMarket.agentName);
					event.setLine(1, agent.getRegion());
					if(!SimpleRegionMarket.isEconomy() || agent.getPrice() == 0) {
						event.setLine(2, "FREE");
					} else {
						event.setLine(2, SimpleRegionMarket.econFormat(agent.getPrice()));
					}
				} else {
					agentsign.setLine(0, SimpleRegionMarket.agentName);
					agentsign.setLine(1, agent.getRegion());
					if(!SimpleRegionMarket.isEconomy() || agent.getPrice() == 0) {
						agentsign.setLine(2, "FREE");
					} else {
						agentsign.setLine(2, SimpleRegionMarket.econFormat(agent.getPrice()));
					}
				}
			} else if(agent.getMode() == SignAgent.MODE_RENT_HOTEL) {
				if(event != null) {
					event.setLine(0, SimpleRegionMarket.hotelName);
				} else {
					agentsign.setLine(0, SimpleRegionMarket.hotelName);
				}

				if(agent.isRent()) {
					if(event != null) {
						event.setLine(1, agent.getRent());
						event.setLine(2, getSignTime(agent.getExpireDate().getTime()-System.currentTimeMillis()) + " left");
					} else {
						agentsign.setLine(1, agent.getRent());
						agentsign.setLine(2, getSignTime(agent.getExpireDate().getTime()-System.currentTimeMillis()) + " left");
					}
				} else {
					if(event != null) {
						if(!SimpleRegionMarket.isEconomy() || agent.getPrice() == 0) {
							event.setLine(1, "FREE");
						} else {
							event.setLine(1, SimpleRegionMarket.econFormat(agent.getPrice()));
						}
						event.setLine(2, getSignTime(agent.getRentTime()));
					} else {
						if(!SimpleRegionMarket.isEconomy() || agent.getPrice() == 0) {
							agentsign.setLine(1, "FREE");
						} else {
							agentsign.setLine(1, SimpleRegionMarket.econFormat(agent.getPrice()));
						}
						agentsign.setLine(2, getSignTime(agent.getRentTime()));
					}
				}
			}

			ProtectedRegion region = agent.getProtectedRegion();

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

			if(event != null) {
				event.setLine(3, Integer.toString(rightX) + " x " + Integer.toString(rightY) + " x " + Integer.toString(rightZ));
			} else {
				agentsign.setLine(3, Integer.toString(rightX) + " x " + Integer.toString(rightY) + " x " + Integer.toString(rightZ));
			}


			agentsign.update();
		}
	}

	public ArrayList<SignAgent> getAgentList() {
		return agents;
	}

	public boolean isOwner(Player player, ProtectedRegion region) {
		if (region != null) {
			if (player != null)
				return region.getOwners().contains(SimpleRegionMarket.getWorldGuard().wrapPlayer(player));
		}
		return false;
	}

	public ProtectedRegion getRegion(Location loc) {
		if (loc != null) {
			Vector vec = new Vector(loc.getX(), loc.getY(), loc.getZ());
			ArrayList<ProtectedRegion> regions = new ArrayList<ProtectedRegion>();
			int highestPrior = 0;
			for(ProtectedRegion region: SimpleRegionMarket.getWorldGuard().getRegionManager(loc.getWorld()).getApplicableRegions(vec)) {
				regions.add(region);
				if(region.getPriority() > highestPrior) {
					highestPrior = region.getPriority();
				}
			}

			if(regions.size() == 1)
				return regions.get(0);
		}
		return null;
	}

	public double getRegionPrice(ProtectedRegion region, Player p) {
		if (region != null) {
			ArrayList<Double> prices = new ArrayList<Double>();
			for (SignAgent obj : getAgentList()) {
				if (obj.getProtectedRegion() == region) {
					prices.add(obj.getPrice());
				}
			}
			if (prices.size() > 0) {
				double old = prices.get(0);
				for (int i = 0; i < prices.size(); i++) {
					if (prices.get(i) != old) {
						if(p != null) {
							LanguageHandler.outputError(p, "ERR_REGION_PRICE", null);
							ArrayList<String> list = new ArrayList<String>();
							list.add(region.getId());
							list.add(SimpleRegionMarket.econFormat(old));
							list.add(SimpleRegionMarket.econFormat(prices.get(i)));
							LanguageHandler.outputError(p, "ERR_REGION_PRICE_SHOW", list);
						}
						return -1;
					}
				}
				return old;
			}
		}
		return -1;
	}

	public long getRegionRentTime(ProtectedRegion region) {
		if (region != null) {
			ArrayList<Long> renttimes = new ArrayList<Long>();
			for (SignAgent obj : getAgentList()) {
				if (obj.getProtectedRegion() == region) {
					renttimes.add(obj.getRentTime());
				}
			}
			if (renttimes.size() > 0) {
				long old = renttimes.get(0);
				for (int i = 0; i < renttimes.size(); i++) {
					if (renttimes.get(i) != old)
						return -1;
				}
				return old;
			}
		}
		return -1;
	}

	public static long parseSignTime(String timestring) {
		long time = 0;
		int i, u;

		i = timestring.indexOf("d");
		if(i > 0) {
			if(timestring.charAt(i-1) == ' ' && i > 1) {
				i--;
			}
			u = i-1;
			while(u > 0 && Character.isDigit(timestring.charAt(u-1))) {
				u--;
			}
			time += (Long.parseLong(timestring.substring(u, i)) * 24*60*60*1000);
		}

		i = timestring.indexOf("h");
		if(i > 0) {
			if(timestring.charAt(i-1) == ' ' && i > 1) {
				i--;
			}
			u = i-1;
			while(u > 0 && Character.isDigit(timestring.charAt(u-1))) {
				u--;
			}
			time += (Long.parseLong(timestring.substring(u, i)) * 60*60*1000);
		}

		i = timestring.indexOf("m");
		if(i > 0) {
			if(timestring.charAt(i-1) == ' ' && i > 1) {
				i--;
			}
			u = i-1;
			while(u > 0 && Character.isDigit(timestring.charAt(u-1))) {
				u--;
			}
			time += (Long.parseLong(timestring.substring(u, i)) * 60*1000);
		}

		return time;
	}

	public static String getSignTime(long time) {
		time = time/1000; // From ms to sec
		int days = (int) (time / (24*60*60));
		int hours = (int) (time / (60*60));
		int minutes = (int) (time / (60));
		if(days > 0) {
			if(days == 1)
				return days + " day";
			else
				return days + " days";
		} else if(hours > 0) {
			if(hours == 1)
				return hours + " hour";
			else
				return hours + " hours";
		} else if(minutes > 0) {
			if(minutes == 1)
				return minutes + " min";
			else
				return minutes + " mins";
		}
		return "< 1 min";
	}
}