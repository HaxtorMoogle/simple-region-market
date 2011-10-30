package com.thezorro266.simpleregionmarket;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.World;
import org.bukkit.entity.Player;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

public class LimitHandler {
	private static Map<String, Integer> limitregionworlds = new HashMap<String, Integer>(); // Limits regions per world - Low Priority
	//private static Map<String, Integer> limitregiongroups = new HashMap<String, Integer>(); // Limits regions per groups - Mid Priority
	private static Map<String, Integer> limitregionplayers = new HashMap<String, Integer>(); // Limits regions per player - High Priority

	private static Map<String, Integer> limitroomworlds = new HashMap<String, Integer>(); // Limits rooms per world - Low Priority
	//private static Map<String, Integer> limitroomgroups = new HashMap<String, Integer>(); // Limits rooms per groups - Mid Priority
	private static Map<String, Integer> limitroomplayers = new HashMap<String, Integer>(); // Limits rooms per player - High Priority
	
	// TODO General permission groups / Which permissions plugins?

	private static Map<String, String> regionowners = new HashMap<String, String>();
	
	private static int countPlayerOwnRegion(Player p) {
		// TODO Only regions which were buyed over this plugin
		if(p != null) {
			WorldGuardPlugin tmp = SimpleRegionMarket.getWorldGuard();
			return tmp.getGlobalRegionManager().get(p.getWorld()).getRegionCountOfPlayer(tmp.wrapPlayer(p));
		}
		return 0;
	}

	private static int countPlayerRentRoom(Player p) {
		int count = 0;
		for(int i = 0; i < SimpleRegionMarket.getAgentManager().getAgentList().size(); i++) {
			SignAgent now = SimpleRegionMarket.getAgentManager().getAgentList().get(i);
			if(now != null
					&& now.getMode() == SignAgent.MODE_RENT_HOTEL
					&& now.getRent() == p.getName())
				count++;
		}
		return count;
	}
	
	public static void setBuyWorldLimit(World w, int limit) {
		if(w != null) {
			if(limit < -1)
				limit = -1;
			
			limitregionworlds.put(w.getName(), limit);
		}
	}

	public static void setBuyPlayerLimit(Player p, int limit) {
		if(p != null) {
			if(limit < -1)
				limit = -1;
			
			limitregionplayers.put(p.getName(), limit);
		}
	}
	
	public static void setRentWorldLimit(World w, int limit) {
		if(w != null) {
			if(limit < -1)
				limit = -1;
			
			limitroomworlds.put(w.getName(), limit);
		}
	}

	public static void setRentPlayerLimit(Player p, int limit) {
		if(p != null) {
			if(limit < -1)
				limit = -1;
			
			limitroomplayers.put(p.getName(), limit);
		}
	}
	
	public static boolean limitCanBuy(Player p) {
		if(p != null) {
			String playername = p.getName();
			if(limitregionplayers.containsKey(playername)) {
				return (countPlayerOwnRegion(p) < limitregionplayers.get(playername));
			} else if(p.getWorld() != null
					&& limitregionworlds.containsKey(p.getWorld().getName())
					&& limitregionworlds.get(p.getWorld().getName()) != -1) {
				return (countPlayerOwnRegion(p) < limitregionworlds.get(p.getWorld().getName()));
			}
			return true;
		}
		return false;
	}
	
	public static boolean limitCanRent(Player p) {
		if(p != null) {
			String playername = p.getName();
			if(limitroomplayers.containsKey(playername)) {
				return (countPlayerRentRoom(p) < limitroomplayers.get(playername));
			} else if(p.getWorld() != null
					&& limitroomworlds.containsKey(p.getWorld().getName())
					&& limitroomworlds.get(p.getWorld().getName()) != -1) {
				return (countPlayerRentRoom(p) < limitroomworlds.get(p.getWorld().getName()));
			}
			return true;
		}
		return false;
	}
}
