package me.raauhh.uhc.listeners;

import me.raauhh.uhc.UHC;
import me.raauhh.uhc.manager.GameManager;
import me.raauhh.uhc.manager.player.UHCPlayer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.material.Gate;
import org.bukkit.material.TrapDoor;

import java.util.Random;

public class SpectatorListener implements Listener {

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        GameManager gameManager = UHC.getInstance().getGameManager();
        UHCPlayer uhcPlayer = UHC.getInstance().getUhcPlayerManager().getPlayer(player.getUniqueId());
        if (uhcPlayer.getPlayerState() == UHCPlayer.State.PLAYING) return;

        if (event.hasItem()) {
            event.setCancelled(true);

            switch (event.getItem().getType()) {
                case DIAMOND:
                    if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                        if (gameManager.getAlivePlayers().isEmpty()) {
                            player.sendMessage("§cThere are not alive players.");
                            break;
                        }

                        UHCPlayer targetPlayer = gameManager.getAlivePlayers().get(new Random().nextInt(gameManager.getAlivePlayers().size()));
                        player.teleport(targetPlayer.getPlayer());
                        player.sendMessage("§fYou've been randomly teleported to: §6" + targetPlayer.getName());
                    }
                    break;
                case ITEM_FRAME:
                    if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                        if (gameManager.getAlivePlayers().isEmpty()) {
                            player.sendMessage("§cThere are not alive players.");
                            break;
                        }
                        UHC.getInstance().getSpectateMenu().open(player);
                    }
                    break;
            }
        }
        player.updateInventory();
    }

    @EventHandler
    public void onEntityTargetLivingEntityEvent(EntityTargetLivingEntityEvent event) {
        if (event.getTarget() instanceof Player) {
            Player player = (Player) event.getTarget();
            UHCPlayer uhcPlayer = UHC.getInstance().getUhcPlayerManager().getPlayer(player.getUniqueId());

            if (uhcPlayer.getPlayerState() == UHCPlayer.State.PLAYING) return;
            event.setTarget(null);
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityTargetEvent(EntityTargetEvent event) {
        if (!(event.getTarget() instanceof Player)) return;

        Player player = (Player) event.getTarget();
        UHCPlayer uhcPlayer = UHC.getInstance().getUhcPlayerManager().getPlayer(player.getUniqueId());

        if (uhcPlayer.getPlayerState() == UHCPlayer.State.PLAYING) return;
        event.setTarget(null);
        event.setCancelled(true);
    }

    @EventHandler
    public void onHangingPlaceEvent(HangingPlaceEvent event) {
        if (!(event.getEntity() instanceof ItemFrame)) return;

        Player player = event.getPlayer();
        UHCPlayer uhcPlayer = UHC.getInstance().getUhcPlayerManager().getPlayer(player.getUniqueId());

        if (uhcPlayer.getPlayerState() == UHCPlayer.State.PLAYING) return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerDropItemEvent(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        UHCPlayer uhcPlayer = UHC.getInstance().getUhcPlayerManager().getPlayer(player.getUniqueId());

        if (uhcPlayer.getPlayerState() == UHCPlayer.State.PLAYING) return;
        event.setCancelled(true);
    }


    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        UHCPlayer uhcPlayer = UHC.getInstance().getUhcPlayerManager().getPlayer(player.getUniqueId());

        if (uhcPlayer.getPlayerState() == UHCPlayer.State.PLAYING) return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onBlockPlaceEvent(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        UHCPlayer uhcPlayer = UHC.getInstance().getUhcPlayerManager().getPlayer(player.getUniqueId());

        if (uhcPlayer.getPlayerState() == UHCPlayer.State.PLAYING) return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        UHCPlayer uhcPlayer = UHC.getInstance().getUhcPlayerManager().getPlayer(player.getUniqueId());

        if (uhcPlayer.getPlayerState() == UHCPlayer.State.PLAYING) return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onFoodLevelChangeEvent(FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();
        UHCPlayer uhcPlayer = UHC.getInstance().getUhcPlayerManager().getPlayer(player.getUniqueId());

        if (uhcPlayer.getPlayerState() == UHCPlayer.State.PLAYING) return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onEntityDamageEvent(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();
        UHCPlayer uhcPlayer = UHC.getInstance().getUhcPlayerManager().getPlayer(player.getUniqueId());

        if (uhcPlayer.getPlayerState() == UHCPlayer.State.PLAYING) return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;

        Player player = (Player) event.getDamager();
        UHCPlayer uhcPlayer = UHC.getInstance().getUhcPlayerManager().getPlayer(player.getUniqueId());

        if (uhcPlayer.getPlayerState() == UHCPlayer.State.PLAYING) return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onVehicleEnterEvent(VehicleEnterEvent event) {
        if (!(event.getEntered() instanceof Player)) return;

        Player player = (Player) event.getEntered();
        UHCPlayer uhcPlayer = UHC.getInstance().getUhcPlayerManager().getPlayer(player.getUniqueId());

        if (uhcPlayer.getPlayerState() != UHCPlayer.State.SPEC) return;
        event.setCancelled(true);
    }

    @EventHandler
    private void onPlayerInteractEvent(PlayerInteractEvent event) {

        Player player = event.getPlayer();
        GameManager gameManager = UHC.getInstance().getGameManager();
        UHCPlayer uhcPlayer = UHC.getInstance().getUhcPlayerManager().getPlayer(player.getUniqueId());

        if (uhcPlayer.getPlayerState() == UHCPlayer.State.PLAYING) return;
        event.setCancelled(true);

        if (!event.hasBlock()) return;

        if ((event.getClickedBlock().getState() instanceof InventoryHolder)) {
            if (gameManager.isInStaffMode(player)) return;

            Inventory chest = ((InventoryHolder) event.getClickedBlock().getState()).getInventory();
            event.getPlayer().openInventory(chest);
        } else if (event.getClickedBlock().getType() == Material.WOODEN_DOOR
                || event.getClickedBlock().getType() == Material.IRON_DOOR_BLOCK
                || event.getClickedBlock().getType() == Material.FENCE_GATE) {
            Location location = event.getClickedBlock().getLocation().setDirection(player.getLocation().getDirection());

            int height = 0;
            if (event.getClickedBlock().getType() == Material.WOODEN_DOOR
                    || event.getClickedBlock().getType() == Material.IRON_DOOR_BLOCK) {
                Material material = event.getClickedBlock().getLocation().add(0.0D, -1.0D, 0.0D).getBlock().getType();
                if (material == Material.WOODEN_DOOR || material == Material.IRON_DOOR_BLOCK) height = -1;
            }

            switch (event.getBlockFace()) {
                case EAST:
                    player.teleport(location.add(-0.5D, height, 0.5D));
                    break;
                case NORTH:
                    player.teleport(location.add(0.5D, height, 1.5D));
                    break;
                case SOUTH:
                    player.teleport(location.add(0.5D, height, -0.5D));
                    break;
                case WEST:
                    player.teleport(location.add(1.5D, height, 0.5D));
                    break;
                case UP:
                    if ((event.getClickedBlock().getState().getData() instanceof Gate)) {
                        Gate gate = (Gate) event.getClickedBlock().getState().getData();
                        switch (gate.getFacing()) {
                            case NORTH:
                            case SOUTH:
                                if (player.getLocation().getX() > location.getX())
                                    player.teleport(location.add(-0.5D, height, 0.5D));
                                else player.teleport(location.add(1.5D, height, 0.5D));
                                break;
                            case EAST:
                            case WEST:
                                if (player.getLocation().getZ() > location.getZ())
                                    player.teleport(location.add(0.5D, height, -0.5D));
                                else player.teleport(location.add(0.5D, height, 1.5D));
                                break;
                        }
                    }
                    break;
            }
        } else if (event.getClickedBlock().getType() == Material.TRAP_DOOR && ((TrapDoor) event.getClickedBlock().getState().getData()).isOpen()) {
            Location location = event.getClickedBlock().getLocation().setDirection(player.getLocation().getDirection());
            switch (event.getBlockFace()) {
                case UP:
                    player.teleport(location.add(0.5D, -1.0D, 0.5D));
                    break;
                case DOWN:
                    player.teleport(location.add(0.5D, 1.0D, 0.5D));
                    break;
            }
        }
    }
}