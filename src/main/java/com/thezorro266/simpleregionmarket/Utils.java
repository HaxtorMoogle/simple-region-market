package com.thezorro266.simpleregionmarket;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Location;

import com.thezorro266.simpleregionmarket.signs.TemplateMain;

public class Utils {
	public static final int SIGN_LINES = 4;

	public static String replaceTokens(String text, Map<String, String> replacements) {
		final Pattern pattern = Pattern.compile("\\[\\[(.+?)\\]\\]");
		final Matcher matcher = pattern.matcher(text);
		final StringBuffer buffer = new StringBuffer();
		while (matcher.find()) {
			final String replacement = replacements.get(matcher.group(1));
			if (replacement != null) {
				matcher.appendReplacement(buffer, "");
				buffer.append(replacement);
			}
		}
		matcher.appendTail(buffer);
		return buffer.toString();
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

	public static Object getEntry(TemplateMain token, String world, String region, String key) {
		if (token != null && world != null && region != null && key != null) {
			if (token.entries.containsKey(world) && token.entries.get(world).containsKey(region) && token.entries.get(world).get(region).containsKey(key)) {
				return token.entries.get(world).get(region).get(key);
			}
		}
		return null;
	}

	public static String getEntryString(TemplateMain token, String world, String region, String key) {
		return Utils.getEntry(token, world, region, key).toString();
	}

	public static boolean getEntryBoolean(TemplateMain token, String world, String region, String key) {
		return (Boolean) Utils.getEntry(token, world, region, key);
	}

	public static double getEntryDouble(TemplateMain token, String world, String region, String key) {
		return (Double) Utils.getEntry(token, world, region, key);
	}

	public static int getEntryInteger(TemplateMain token, String world, String region, String key) {
		return (Integer) Utils.getEntry(token, world, region, key);
	}

	@SuppressWarnings("unchecked")
	public static ArrayList<Location> getSignLocations(TemplateMain token, String world, String region) {
		return (ArrayList<Location>) Utils.getEntry(token, world, region, "signs");
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
