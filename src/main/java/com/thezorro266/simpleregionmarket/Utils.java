package com.thezorro266.simpleregionmarket;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.thezorro266.simpleregionmarket.signs.TemplateMain;

public class Utils {
	static final int SIGN_LINES = 4;

	public static String replaceTokens(String text, Map<String, String> replacements) {
		final Pattern pattern = Pattern.compile("\\[\\[(.+?)\\]\\]");
		final Matcher matcher = pattern.matcher(text);
		final StringBuffer buffer = new StringBuffer();
		while (matcher.find()) {
			try {
				final String replacement = replacements.get(matcher.group(1));
				if (replacement != null) {
					matcher.appendReplacement(buffer, "");
					buffer.append(replacement);
				}
			} catch (final Exception e) {
				Bukkit.getLogger().log(Level.INFO, "Replacement map has a misconfiguration at " + matcher.group(1));
			}
		}
		matcher.appendTail(buffer);
		return buffer.toString();
	}

	public static HashMap<String, String> getSignInput(TemplateMain token, String[] lines) {
		final HashMap<String, String> hashMap = new HashMap<String, String>();
		for (int i = 0; i < lines.length; i++) {
			final String inputLine = Utils.getOptionString(token, "input." + (i + 1));
			if (inputLine != null) {
				final Pattern pattern = Pattern.compile("\\[\\[(.+?)\\]\\]");
				final Matcher matcher = pattern.matcher(inputLine);
				while (matcher.find()) {
					hashMap.put(matcher.group(1), lines[i]);
				}
			}
		}
		return hashMap;
	}

	public static void setEntry(TemplateMain token, String world, String region, String key, Object value) {
		if (token != null && world != null && region != null && key != null) {
			if (!token.entries.containsKey(world)) {
				token.entries.put(world, new HashMap<String, HashMap<String, Object>>());
			}
			if (!token.entries.get(world).containsKey(region)) {
				token.entries.get(world).put(region, new HashMap<String, Object>());
			}
			token.entries.get(world).get(region).put(key, value);
		}
	}

	public static void removeEntry(TemplateMain token, String world, String region, String key) {
		if (token != null && world != null && region != null && key != null) {
			if (!token.entries.containsKey(world)) {
				token.entries.put(world, new HashMap<String, HashMap<String, Object>>());
			}
			if (!token.entries.get(world).containsKey(region)) {
				token.entries.get(world).put(region, new HashMap<String, Object>());
			}
			token.entries.get(world).get(region).remove(key);
		}
	}
	
	public static void removeRegion(TemplateMain token, String world, String region) {
		if(token != null && world != null && region != null) {
			if(token.entries.containsKey(world)) {
				if(token.entries.get(world).containsKey(region)) {
					token.entries.get(world).remove(region);
				}
			}
		}
	}
	
	public static void removeWorld(TemplateMain token, String world) {
		if(token != null && world != null) {
			if(token.entries.containsKey(world)) {
				token.entries.remove(world);
			}
		}
	}

	public static Object getEntry(TemplateMain token, String world, String region, String key) {
		if (token != null && world != null && region != null && key != null) {
			if (token.entries.containsKey(world) && token.entries.get(world).containsKey(region) && token.entries.get(world).get(region).containsKey(key)) {
				return token.entries.get(world).get(region).get(key);
			}
		}
		return null;
	}

	public static String getEntryString(TemplateMain token, String world, String region, String key) {
		Object entry = Utils.getEntry(token, world, region, key);
		if (entry != null) {
			entry = entry.toString();
		}
		return (String) entry;
	}

	public static boolean getEntryBoolean(TemplateMain token, String world, String region, String key) {
		final String strEntry = Utils.getEntryString(token, world, region, key);
		if (strEntry != null) {
			return Boolean.parseBoolean(strEntry);
		}
		return false;
	}

	public static double getEntryDouble(TemplateMain token, String world, String region, String key) {
		final String strEntry = Utils.getEntryString(token, world, region, key);
		if (strEntry != null) {
			return Double.parseDouble(strEntry);
		}
		return 0;
	}

	public static int getEntryInteger(TemplateMain token, String world, String region, String key) {
		final String strEntry = Utils.getEntryString(token, world, region, key);
		if (strEntry != null) {
			return Integer.parseInt(strEntry);
		}
		return 0;
	}

	public static long getEntryLong(TemplateMain token, String world, String region, String key) {
		final String strEntry = Utils.getEntryString(token, world, region, key);
		if (strEntry != null) {
			return Long.parseLong(strEntry);
		}
		return 0;
	}

	@SuppressWarnings("unchecked")
	public static ArrayList<Location> getSignLocations(TemplateMain token, String world, String region) {
		final ArrayList<Location> signLocations = (ArrayList<Location>) Utils.getEntry(token, world, region, "signs");
		if (signLocations == null) {
			return new ArrayList<Location>();
		} else {
			return signLocations;
		}
	}

	public static Object getOption(TemplateMain token, String key) {
		if (token != null && key != null) {
			if (token.tplOptions.containsKey(key)) {
				return token.tplOptions.get(key);
			}
		}
		return null;
	}

	public static String getOptionString(TemplateMain token, String key) {
		Object entry = Utils.getOption(token, key);
		if (entry != null) {
			entry = entry.toString();
		}
		return (String) entry;
	}

	public static boolean getOptionBoolean(TemplateMain token, String key) {
		final String strEntry = Utils.getOptionString(token, key);
		if (strEntry != null) {
			return Boolean.parseBoolean(strEntry);
		}
		return false;
	}

	public static double getOptionDouble(TemplateMain token, String key) {
		final String strEntry = Utils.getOptionString(token, key);
		if (strEntry != null) {
			return Double.parseDouble(strEntry);
		}
		return 0;
	}

	public static int getOptionInteger(TemplateMain token, String key) {
		final String strEntry = Utils.getOptionString(token, key);
		if (strEntry != null) {
			return Integer.parseInt(strEntry);
		}
		return 0;
	}

	public static long getOptionLong(TemplateMain token, String key) {
		final String strEntry = Utils.getOptionString(token, key);
		if (strEntry != null) {
			return Long.parseLong(strEntry);
		}
		return 0;
	}

	public static ProtectedRegion getProtectedRegion(String region, Location signLocation) {
		ProtectedRegion protectedRegion = null;
		final RegionManager worldRegionManager = SimpleRegionMarket.wgManager.getWorldGuard().getRegionManager(signLocation.getWorld());
		if (region == null || region.isEmpty()) {
			if (worldRegionManager.getApplicableRegions(signLocation).size() == 1) {
				protectedRegion = worldRegionManager.getApplicableRegions(signLocation).iterator().next();
			}
		} else {
			protectedRegion = worldRegionManager.getRegion(region);
		} // TODO Take child region or region with highest priority
		return protectedRegion;
	}

	/**
	 * Gets the sign time.
	 * 
	 * @param time
	 *            the time
	 * @return the sign time
	 */
	public static String getSignTime(long time) {
		time = time / 1000; // From ms to sec
		final int days = (int) (time / (24 * 60 * 60));
		time = time % (24 * 60 * 60);
		final int hours = (int) (time / (60 * 60));
		time = time % (60 * 60);
		final int minutes = (int) (time / 60);
		String strReturn = "< 1 min";
		if (days > 0) {
			strReturn = Integer.toString(days);
			if (hours > 0) {
				strReturn += "+";
			}
			if (days == 1) {
				strReturn += " day";
			} else {
				strReturn += " days";
			}
		} else if (hours > 0) {
			strReturn = Integer.toString(hours);
			if (minutes > 0) {
				strReturn += "+";
			}
			if (hours == 1) {
				strReturn += " hour";
			} else {
				strReturn += " hours";
			}
		} else if (minutes > 0) {
			strReturn = Integer.toString(minutes);
			if (minutes == 1) {
				strReturn += " min";
			} else {
				strReturn += " mins";
			}
		}
		return strReturn;
	}

	/**
	 * Parses the sign time.
	 * 
	 * @param timestring
	 *            the timestring
	 * @return the long
	 */
	public static long parseSignTime(String timestring) {
		long time = 0;
		int i, u;

		i = timestring.indexOf("d");
		if (i > 0) {
			if (timestring.charAt(i - 1) == ' ' && i > 1) {
				i--;
			}
			u = i - 1;
			while (u > 0 && Character.isDigit(timestring.charAt(u - 1))) {
				u--;
			}
			time += Long.parseLong(timestring.substring(u, i)) * 24 * 60 * 60 * 1000;
		}

		i = timestring.indexOf("h");
		if (i > 0) {
			if (timestring.charAt(i - 1) == ' ' && i > 1) {
				i--;
			}
			u = i - 1;
			while (u > 0 && Character.isDigit(timestring.charAt(u - 1))) {
				u--;
			}
			time += Long.parseLong(timestring.substring(u, i)) * 60 * 60 * 1000;
		}

		i = timestring.indexOf("m");
		if (i > 0) {
			if (timestring.charAt(i - 1) == ' ' && i > 1) {
				i--;
			}
			u = i - 1;
			while (u > 0 && Character.isDigit(timestring.charAt(u - 1))) {
				u--;
			}
			time += Long.parseLong(timestring.substring(u, i)) * 60 * 1000;
		}

		return time;
	}

	/**
	 * Returns the Copyright.
	 * 
	 * @return the copyright
	 */
	public static String getCopyright() {
		return "Copyright (C) 2011-2012  theZorro266  -  GPLv3";
	}
}
