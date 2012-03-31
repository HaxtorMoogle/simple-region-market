package com.thezorro266.simpleregionmarket;

import java.util.ArrayList;
import java.util.Date;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class ListenerHandler implements Listener {

  private final LanguageHandler langHandler;
  private final LimitHandler limitHandler;
  private final SimpleRegionMarket plugin;

  public ListenerHandler(SimpleRegionMarket plugin, LimitHandler limitHandler, LanguageHandler langHandler) {
    Bukkit.getPluginManager().registerEvents(this, plugin);
    this.plugin = plugin;
    this.limitHandler = limitHandler;
    this.langHandler = langHandler;
  }

  @EventHandler
  public void onBlockBreak(final BlockBreakEvent event) {
    final Block b = event.getBlock();
    final SignAgent agent = plugin.getAgentManager().getAgent(b.getLocation());

    if (agent == null) {
      return;
    }

    final Player p = event.getPlayer();
    final ProtectedRegion region = SimpleRegionMarket.getWorldGuard().getRegionManager(b.getLocation().getWorld()).getRegion(agent.getRegion());
    if (!plugin.getAgentManager().isOwner(p, region) && !plugin.isAdmin(p)) {
      event.setCancelled(true);
      ((Sign) b.getState()).update();
      return;
    }

    event.setCancelled(true);
    if (p != null) {
      agent.destroyAgent(true);
      langHandler.outputMessage(p, "AGENT_DELETE", null);
    }
    plugin.getAgentManager().removeAgent(agent);
    plugin.saveAll();
  }

  @EventHandler
  public void onPlayerInteract(final PlayerInteractEvent event) {
    if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
      final Block b = event.getClickedBlock();
      final SignAgent agent = plugin.getAgentManager().getAgent(b.getLocation());

      if (agent == null) {
        return;
      }

      final Player p = event.getPlayer();

      if (agent.getMode() == SignAgent.MODE_RENT_HOTEL) {
        if (agent.isRent()) {
          if (agent.getRent().equals(p.getName())) {
            final long newRentTime = agent.getExpireDate().getTime() + agent.getRentTime();
            if ((newRentTime - System.currentTimeMillis()) / agent.getRentTime() < plugin.getConfigurationHandler().getConfig().getInt("max_rent_multiplier")) {
              if (plugin.isEconomy()) {
                final double price = agent.getPrice();
                if (plugin.econHasEnough(p.getName(), price)) {
                  final String account = agent.getAccount();
                  try {
                    if (account.isEmpty()) {
                      plugin.econGiveMoney(p.getName(), -price);
                    } else {
                      plugin.econGiveMoney(p.getName(), -price);
                      plugin.econGiveMoney(account, price);
                    }
                  } catch (final Exception e) {
                    langHandler.outputError(p, "ERR_TRANSACTION", null);
                    return;
                  }
                  agent.setExpireDate(new Date(newRentTime));
                  plugin.getAgentManager().actAgent(agent, null);
                  langHandler.outputMessage(p, "HOTEL_SUCCESS_RERENT", null);
                } else {
                  langHandler.outputError(p, "ERR_NO_MONEY", null);
                }
              } else {
                agent.setExpireDate(new Date(newRentTime));
                plugin.getAgentManager().actAgent(agent, null);
                langHandler.outputMessage(p, "HOTEL_SUCCESS_RERENT", null);
              }
            } else {
              langHandler.outputError(p, "ERR_RERENT_TOO_LONG", null);
            }
          } else {
            langHandler.outputError(p, "ERR_ALREADY_RENT", null);
          }
          return;
        } else {
          if (agent.getProtectedRegion().getParent() != null) {
            if (plugin.getAgentManager().isOwner(p, agent.getProtectedRegion().getParent())) {
              langHandler.outputMessage(p, "HOTEL_YOURS", null);
              return;
            }
          }
        }
      }

      if (!plugin.isAdmin(p)) {
        if (agent.getMode() == SignAgent.MODE_SELL_REGION) {
          if (!limitHandler.limitCanBuy(p)) {
            langHandler.outputError(p, "ERR_REGION_LIMIT", null);
            return;
          }
        } else if (agent.getMode() == SignAgent.MODE_RENT_HOTEL) {
          if (!limitHandler.limitCanRent(p)) {
            langHandler.outputError(p, "ERR_HOTEL_LIMIT", null);
            return;
          }
        }
      }

      final ProtectedRegion region = SimpleRegionMarket.getWorldGuard().getRegionManager(b.getWorld()).getRegion(agent.getRegion());

      if (agent.getMode() == SignAgent.MODE_SELL_REGION) {
        if (!plugin.canBuy(p)) {
          langHandler.outputError(p, "ERR_NO_PERM_BUY", null);
        } else if (plugin.getAgentManager().isOwner(p, region)) {
          if (!agent.getAccount().isEmpty()) {
            if (p.getName().equals(agent.getAccount())) {
              langHandler.outputMessage(p, "AGENT_YOURS", null);
            } else {
              langHandler.outputMessage(p, "ERR_REGION_BUY_YOURS", null);
            }
          } else {
            langHandler.outputMessage(p, "ERR_REGION_BUY_YOURS", null);
          }
        } else {
          if (plugin.isEconomy()) {
            final String account = agent.getAccount();
            final double price = agent.getPrice();
            if (plugin.econHasEnough(p.getName(), price)) {
              try {
                if (account.isEmpty()) {
                  plugin.econGiveMoney(p.getName(), -price);
                  plugin.sellRegion(region, p);
                  final ArrayList<String> list = new ArrayList<String>();
                  list.add(region.getId());
                  langHandler.outputMessage(p, "REGION_BUYED_NONE", list);
                } else {
                  plugin.econGiveMoney(p.getName(), -price);
                  plugin.econGiveMoney(account, price);
                  plugin.sellRegion(region, p);
                  final ArrayList<String> list = new ArrayList<String>();
                  list.add(region.getId());
                  list.add(account);
                  langHandler.outputMessage(p, "REGION_BUYED_USER", list);
                }
              } catch (final Exception e) {
                langHandler.outputError(p, "ERR_TRANSACTION", null);
                return;
              }
            } else {
              langHandler.outputError(p, "ERR_NO_MONEY", null);
            }
          } else {
            plugin.sellRegion(region, p);
            final ArrayList<String> list = new ArrayList<String>();
            list.add(region.getId());
            langHandler.outputMessage(p, "REGION_BUYED_NONE", list);
          }
        }
      } else if (agent.getMode() == SignAgent.MODE_RENT_HOTEL) {
        if (!plugin.canRent(p)) {
          langHandler.outputError(p, "ERR_NO_PERM_RENT", null);
        } else {
          if (plugin.isEconomy()) {
            final String account = agent.getAccount();
            final double price = agent.getPrice();
            if (plugin.econHasEnough(p.getName(), price)) {
              try {
                if (account.isEmpty()) {
                  plugin.econGiveMoney(p.getName(), -price);
                  plugin.rentHotel(region, p, agent.getRentTime());
                  final ArrayList<String> list = new ArrayList<String>();
                  list.add(region.getId());
                  langHandler.outputMessage(p, "HOTEL_RENT_NONE", list);
                } else {
                  plugin.econGiveMoney(p.getName(), -price);
                  plugin.econGiveMoney(account, price);
                  plugin.rentHotel(region, p, agent.getRentTime());
                  final ArrayList<String> list = new ArrayList<String>();
                  list.add(region.getId());
                  list.add(account);
                  langHandler.outputMessage(p, "HOTEL_RENT_USER", list);
                }
              } catch (final Exception e) {
                langHandler.outputError(p, "ERR_TRANSACTION", null);
                return;
              }
            } else {
              langHandler.outputError(p, "ERR_NO_MONEY", null);
            }
          } else {
            plugin.rentHotel(region, p, agent.getRentTime());
            final ArrayList<String> list = new ArrayList<String>();
            list.add(region.getId());
            langHandler.outputMessage(p, "HOTEL_RENT_NONE", list);
          }
        }
      }
    }
  }

  @EventHandler
  public void onSignChange(final SignChangeEvent event) {
    if (event.getLine(0).equalsIgnoreCase("[AGENT]") || event.getLine(0).equalsIgnoreCase("[HOTEL]")) {
      ProtectedRegion region;
      final Location signloc = event.getBlock().getLocation();

      // Get Mode from '[AGENT]' or '[HOTEL]'
      int mode = 0;
      if (event.getLine(0).equalsIgnoreCase("[AGENT]")) {
        mode = SignAgent.MODE_SELL_REGION;
      } else if (event.getLine(0).equalsIgnoreCase("[HOTEL]")) {
        mode = SignAgent.MODE_RENT_HOTEL;
      }

      final Player p = event.getPlayer();

      // Get Region from above or line 2 on the sign
      if (event.getLine(1).isEmpty()) {
        region = plugin.getAgentManager().getRegion(signloc);
      } else {
        region = SimpleRegionMarket.getWorldGuard().getRegionManager(signloc.getWorld()).getRegion(event.getLine(1));
      }

      if (region == null) {
        if (p != null) {
          langHandler.outputError(p, "ERR_REGION_NAME", null);
        }
        event.setCancelled(true);
        event.getBlock().setType(Material.AIR);
        signloc.getWorld().dropItem(signloc, new ItemStack(Material.SIGN, 1));
        return;
      }

      // Check player permission
      if (p != null) {
        if (mode == SignAgent.MODE_SELL_REGION) {
          if (!plugin.canSell(p)) {
            event.setCancelled(true);
            langHandler.outputMessage(p, "ERR_NO_PERM_SELL", null);
            event.getBlock().setType(Material.AIR);
            signloc.getWorld().dropItem(signloc, new ItemStack(Material.SIGN, 1));
            return;
          }
          if (!plugin.isAdmin(p) && !plugin.getAgentManager().isOwner(p, region)) {
            langHandler.outputError(p, "ERR_REGION_NO_OWNER", null);
            event.setCancelled(true);
            event.getBlock().setType(Material.AIR);
            signloc.getWorld().dropItem(signloc, new ItemStack(Material.SIGN, 1));
            return;
          }
        } else if (mode == SignAgent.MODE_RENT_HOTEL) {
          if (!plugin.canLet(p)) {
            event.setCancelled(true);
            langHandler.outputMessage(p, "ERR_NO_PERM_RENT_CREATE", null);
            event.getBlock().setType(Material.AIR);
            signloc.getWorld().dropItem(signloc, new ItemStack(Material.SIGN, 1));
            return;
          }
          if (!plugin.isAdmin(p) && !plugin.getAgentManager().isOwner(p, region.getParent())) {
            langHandler.outputError(p, "ERR_PARENT_NO_OWNER", null);
            event.setCancelled(true);
            event.getBlock().setType(Material.AIR);
            signloc.getWorld().dropItem(signloc, new ItemStack(Material.SIGN, 1));
            return;
          }
        }
      }

      // Check renttime and price
      long renttime = 0;
      double price = 0;
      String[] linetwo = null;
      if (mode == SignAgent.MODE_RENT_HOTEL) {
        linetwo = event.getLine(2).split(":");
        if (linetwo.length == 1) {
          try {
            renttime = AgentManager.parseSignTime(linetwo[0]);
          } catch (final Exception e) {
            if (p != null) {
              langHandler.outputError(p, "ERR_NO_RENTTIME", null);
            }
            event.setCancelled(true);
            event.getBlock().setType(Material.AIR);
            signloc.getWorld().dropItem(signloc, new ItemStack(Material.SIGN, 1));
            return;
          }
        } else if (linetwo.length > 2) {
          if (p != null) {
            langHandler.outputError(p, "ERR_NO_PRICE_RENTTIME", null);
          }
          event.setCancelled(true);
          event.getBlock().setType(Material.AIR);
          signloc.getWorld().dropItem(signloc, new ItemStack(Material.SIGN, 1));
          return;
        } else if (linetwo.length == 2) {
          try {
            renttime = AgentManager.parseSignTime(linetwo[1]);
          } catch (final Exception e) {
            if (p != null) {
              langHandler.outputError(p, "ERR_NO_RENTTIME", null);
            }
            event.setCancelled(true);
            event.getBlock().setType(Material.AIR);
            signloc.getWorld().dropItem(signloc, new ItemStack(Material.SIGN, 1));
            return;
          }
        }
      }

      // If economy is disabled, do not search for a price, but the
      // renttime
      if (plugin.isEconomy()) {
        if (event.getLine(2).isEmpty()) {
          if (plugin.getAgentManager().countAgents(region) > 0) {
            price = plugin.getAgentManager().getRegionPrice(region, null);
          } else {
            price = -1;
          }
          if (price < 0) {
            if (p != null) {
              langHandler.outputError(p, "ERR_NO_PRICE", null);
            }
            event.setCancelled(true);
            event.getBlock().setType(Material.AIR);
            signloc.getWorld().dropItem(signloc, new ItemStack(Material.SIGN, 1));
            return;
          }
          if (mode == SignAgent.MODE_RENT_HOTEL) {
            if (plugin.getAgentManager().countAgents(region) > 0) {
              renttime = plugin.getAgentManager().getRegionRentTime(region);
            } else {
              renttime = -1;
            }
            if (renttime <= 0) {
              if (p != null) {
                langHandler.outputError(p, "ERR_NO_RENTTIME", null);
              }
              event.setCancelled(true);
              event.getBlock().setType(Material.AIR);
              signloc.getWorld().dropItem(signloc, new ItemStack(Material.SIGN, 1));
              return;
            }
          }
        } else {
          if (mode == SignAgent.MODE_RENT_HOTEL) {
            if (linetwo == null || linetwo.length == 1) {
              if (p != null) {
                langHandler.outputError(p, "ERR_NO_PRICE", null);
              }
              event.setCancelled(true);
              event.getBlock().setType(Material.AIR);
              signloc.getWorld().dropItem(signloc, new ItemStack(Material.SIGN, 1));
              return;
            }
          }
          try {
            if (mode == SignAgent.MODE_RENT_HOTEL) {
              price = Double.parseDouble(linetwo[0]);
            } else {
              price = Double.parseDouble(event.getLine(2));
            }
          } catch (final Exception e) {
            if (p != null) {
              langHandler.outputError(p, "ERR_NO_PRICE", null);
            }
            event.setCancelled(true);
            event.getBlock().setType(Material.AIR);
            signloc.getWorld().dropItem(signloc, new ItemStack(Material.SIGN, 1));
            return;
          }
          if (price < 0) {
            if (p != null) {
              langHandler.outputError(p, "ERR_PRICE_UNDER_ZERO", null);
            }
            event.setCancelled(true);
            event.getBlock().setType(Material.AIR);
            signloc.getWorld().dropItem(signloc, new ItemStack(Material.SIGN, 1));
            return;
          }
        }
      } else { // ..., but the renttime
        if (event.getLine(2).isEmpty()) {
          if (mode == SignAgent.MODE_RENT_HOTEL) {
            if (plugin.getAgentManager().countAgents(region) > 0) {
              renttime = plugin.getAgentManager().getRegionRentTime(region);
            } else {
              renttime = -1;
            }
            if (renttime <= 0) {
              if (p != null) {
                langHandler.outputError(p, "ERR_NO_RENTTIME", null);
              }
              event.setCancelled(true);
              event.getBlock().setType(Material.AIR);
              signloc.getWorld().dropItem(signloc, new ItemStack(Material.SIGN, 1));
              return;
            }
          }
        }
      }

      // Check account
      String account = p.getName();
      if (!event.getLine(3).isEmpty()) {
        if (plugin.isAdmin(p)) {
          if (event.getLine(3).equalsIgnoreCase("none")) {
            account = "";
          } else {
            account = event.getLine(3);
          }
        }
      }

      // Create
      final SignAgent newagent = plugin.getAgentManager().addAgent(mode, signloc, region, price, account, renttime);

      // Check and finalize
      if (newagent != null) {
        if (p != null) {

          // Successful message
          if (mode == SignAgent.MODE_SELL_REGION) {
            if (account.isEmpty()) {
              langHandler.outputMessage(p, "REGION_OFFER_NONE", null);
            } else {
              langHandler.outputMessage(p, "REGION_OFFER_USER", null);
            }
          } else if (mode == SignAgent.MODE_RENT_HOTEL) {
            if (account.isEmpty()) {
              langHandler.outputMessage(p, "HOTEL_OFFER_NONE", null);
            } else {
              langHandler.outputMessage(p, "HOTEL_OFFER_USER", null);
            }
          }

          if (mode == SignAgent.MODE_SELL_REGION && plugin.isEconomy()) {
            plugin.getAgentManager().getRegionPrice(region, p);
          }

          if (plugin.getAgentManager().countAgents(region) > 1) {
            final ArrayList<String> list = new ArrayList<String>();
            list.add(Integer.toString(plugin.getAgentManager().countAgents(region)));
            langHandler.outputMessage(p, "AGENT_PLACED", list);
          }
        }

        plugin.getAgentManager().actAgent(newagent, event);

        plugin.saveAll();
      } else {
        if (p != null) {
          langHandler.outputError(p, "ERR_PLACE_AGENT", null);
        }
        event.setCancelled(true);
        event.getBlock().setType(Material.AIR);
        signloc.getWorld().dropItem(signloc, new ItemStack(Material.SIGN, 1));
        return;
      }
    }
  }
}
