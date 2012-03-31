package com.thezorro266.simpleregionmarket;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class AgentManager {
  public static String getSignTime(long time) {
    time = time / 1000; // From ms to sec
    final int days = (int) (time / (24 * 60 * 60));
    final int hours = (int) (time / (60 * 60));
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

  private final ArrayList<SignAgent> agents = new ArrayList<SignAgent>();

  private final LanguageHandler langHandler;
  private final SimpleRegionMarket plugin;

  public AgentManager(SimpleRegionMarket plugin, LanguageHandler langHandler) {
    this.plugin = plugin;
    this.langHandler = langHandler;
  }

  public void actAgent(SignAgent agent, SignChangeEvent event) {
    if (agent != null) {
      final Sign agentsign = (Sign) agent.getLocation().getBlock().getState();
      if (agent.getMode() == SignAgent.MODE_SELL_REGION) {
        if (event != null) {
          event.setLine(0, plugin.getConfigurationHandler().getConfig().getString("agent_name"));
          event.setLine(1, agent.getRegion());
          if (!plugin.isEconomy() || agent.getPrice() == 0) {
            event.setLine(2, "FREE");
          } else {
            event.setLine(2, plugin.econFormat(agent.getPrice()));
          }
        } else {
          agentsign.setLine(0, plugin.getConfigurationHandler().getConfig().getString("agent_name"));
          agentsign.setLine(1, agent.getRegion());
          if (!plugin.isEconomy() || agent.getPrice() == 0) {
            agentsign.setLine(2, "FREE");
          } else {
            agentsign.setLine(2, plugin.econFormat(agent.getPrice()));
          }
        }
      } else if (agent.getMode() == SignAgent.MODE_RENT_HOTEL) {
        if (event != null) {
          event.setLine(0, plugin.getConfigurationHandler().getConfig().getString("hotel_name"));
        } else {
          agentsign.setLine(0, plugin.getConfigurationHandler().getConfig().getString("hotel_name"));
        }

        if (agent.isRent()) {
          if (event != null) {
            event.setLine(1, agent.getRent());
            event.setLine(2, getSignTime(agent.getExpireDate().getTime() - System.currentTimeMillis()) + " left");
          } else {
            agentsign.setLine(1, agent.getRent());
            agentsign.setLine(2, getSignTime(agent.getExpireDate().getTime() - System.currentTimeMillis()) + " left");
          }
        } else {
          if (event != null) {
            if (!plugin.isEconomy() || agent.getPrice() == 0) {
              event.setLine(1, "FREE");
            } else {
              event.setLine(1, plugin.econFormat(agent.getPrice()));
            }
            event.setLine(2, getSignTime(agent.getRentTime()));
          } else {
            if (!plugin.isEconomy() || agent.getPrice() == 0) {
              agentsign.setLine(1, "FREE");
            } else {
              agentsign.setLine(1, plugin.econFormat(agent.getPrice()));
            }
            agentsign.setLine(2, getSignTime(agent.getRentTime()));
          }
        }
      }

      final ProtectedRegion region = agent.getProtectedRegion();

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

      if (event != null) {
        event.setLine(3, Integer.toString(rightX) + " x " + Integer.toString(rightY) + " x " + Integer.toString(rightZ));
      } else {
        agentsign.setLine(3, Integer.toString(rightX) + " x " + Integer.toString(rightY) + " x " + Integer.toString(rightZ));
      }

      agentsign.update();
    }
  }

  public SignAgent addAgent(int mode, Location location, ProtectedRegion region, double price, String account, long renttime) {
    if (mode == SignAgent.MODE_SELL_REGION || mode == SignAgent.MODE_RENT_HOTEL) {
      if (location != null) {
        if (location.getWorld() != null) {
          if (region != null) {
            if (price >= 0) {
              if (account == null) {
                account = "";
              }
              if (renttime < 0) {
                renttime = 86400000; // Reset to 1 day
              }
              final SignAgent newagent = new SignAgent(mode, location, region.getId(), price, account, renttime);
              getAgentList().add(newagent);
              if (location.getBlock().getType() != Material.SIGN_POST && location.getBlock().getType() != Material.WALL_SIGN) {
                location.getBlock().setType(Material.SIGN_POST);
                actAgent(newagent, null);
              }
              return newagent;
            }
          }
        }
      }
    }
    return null;
  }

  public SignAgent addAgent(int mode, Location location, ProtectedRegion region, double price, String account, long renttime, String rentby, Date expiredate) {
    if (expiredate != null) {
      final SignAgent newagent = addAgent(mode, location, region, price, account, renttime);
      if (newagent != null) {
        newagent.rentTo(rentby, expiredate);
        return newagent;
      }
    }
    return null;
  }

  public void checkAgents() {
    final ArrayList<String> worlds_called = new ArrayList<String>();
    final ArrayList<String> regions_called = new ArrayList<String>();
    final ArrayList<String> player_hotel_expired = new ArrayList<String>();
    final Iterator<SignAgent> itr = getAgentList().iterator();
    while (itr.hasNext()) {
      final SignAgent obj = itr.next();
      if (obj.getWorldWorld() == null) {
        if (!worlds_called.contains(obj.getWorld())) {
          final ArrayList<String> list = new ArrayList<String>();
          list.add(obj.getWorld());
          langHandler.langOutputConsole("AGENT_WORLD_REMOVED", Level.WARNING, list);
          worlds_called.add(obj.getWorld());
        }
        itr.remove();
      } else if (obj.getProtectedRegion() == null) {
        if (!regions_called.contains(obj.getRegion())) {
          final ArrayList<String> list = new ArrayList<String>();
          list.add(obj.getRegion());
          langHandler.langOutputConsole("AGENT_REGION_REMOVED", Level.WARNING, list);
          regions_called.add(obj.getRegion());
        }
        obj.destroyAgent(false);
        itr.remove();
      } else if (obj.getLocation().getBlock() == null || obj.getLocation().getBlock().getTypeId() != 63 && obj.getLocation().getBlock().getTypeId() != 68) {
        langHandler.langOutputConsole("AGENT_BLOCK_REMOVED", Level.WARNING, null);
        itr.remove();
      } else {
        if (obj.isRent()) {
          if (obj.getExpireDate().getTime() < System.currentTimeMillis()) {
            plugin.unrentHotel(obj);
            final Player p = Bukkit.getPlayerExact(obj.getRent());
            if (p != null) {
              if (!player_hotel_expired.contains(p.getName())) {
                langHandler.outputMessage(p, "HOTEL_EXPIRED", null);
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
      for (final SignAgent obj : getAgentList()) {
        if (obj.getProtectedRegion() == region) {
          count++;
        }
      }
    }
    return count;
  }

  public SignAgent getAgent(Location loc) {
    if (loc != null) {
      for (final SignAgent obj : getAgentList()) {
        if (loc.equals(obj.getLocation())) {
          return obj;
        }
      }
    }
    return null;
  }

  public ArrayList<SignAgent> getAgentList() {
    return agents;
  }

  public ProtectedRegion getRegion(Location loc) {
    if (loc != null) {
      final Vector vec = new Vector(loc.getX(), loc.getY(), loc.getZ());
      final ArrayList<ProtectedRegion> regions = new ArrayList<ProtectedRegion>();
      int highestPrior = 0;
      for (final ProtectedRegion region : SimpleRegionMarket.getWorldGuard().getRegionManager(loc.getWorld()).getApplicableRegions(vec)) {
        regions.add(region);
        if (region.getPriority() > highestPrior) {
          highestPrior = region.getPriority();
        }
      }

      if (regions.size() == 1) {
        return regions.get(0);
      }
    }
    return null;
  }

  public double getRegionPrice(ProtectedRegion region, Player p) {
    if (region != null) {
      final ArrayList<Double> prices = new ArrayList<Double>();
      for (final SignAgent obj : getAgentList()) {
        if (obj.getProtectedRegion() == region) {
          prices.add(obj.getPrice());
        }
      }
      if (prices.size() > 0) {
        final double old = prices.get(0);
        for (int i = 0; i < prices.size(); i++) {
          if (prices.get(i) != old) {
            if (p != null) {
              langHandler.outputError(p, "ERR_REGION_PRICE", null);
              final ArrayList<String> list = new ArrayList<String>();
              list.add(region.getId());
              list.add(plugin.econFormat(old));
              list.add(plugin.econFormat(prices.get(i)));
              langHandler.outputError(p, "ERR_REGION_PRICE_SHOW", list);
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
      final ArrayList<Long> renttimes = new ArrayList<Long>();
      for (final SignAgent obj : getAgentList()) {
        if (obj.getProtectedRegion() == region) {
          renttimes.add(obj.getRentTime());
        }
      }
      if (renttimes.size() > 0) {
        final long old = renttimes.get(0);
        for (int i = 0; i < renttimes.size(); i++) {
          if (renttimes.get(i) != old) {
            return -1;
          }
        }
        return old;
      }
    }
    return -1;
  }

  public boolean isOwner(Player player, ProtectedRegion region) {
    if (region != null) {
      if (player != null) {
        return region.getOwners().contains(SimpleRegionMarket.getWorldGuard().wrapPlayer(player));
      }
    }
    return false;
  }

  public boolean removeAgent(SignAgent agent) {
    boolean ret = false;
    if (agent != null) {
      agent.destroyAgent(false);
      getAgentList().remove(agent);
      ret = true;
    }
    return ret;
  }

  public void rentRegionForPlayer(ProtectedRegion region, Player p, long renttime) {
    if (region != null) {
      final Iterator<SignAgent> itr = getAgentList().iterator();
      while (itr.hasNext()) {
        final SignAgent obj = itr.next();
        if (obj.getProtectedRegion() == region) {
          obj.rentTo(p.getName(), new Date(System.currentTimeMillis() + renttime));
          actAgent(obj, null);
        }
      }
    }
  }
}
