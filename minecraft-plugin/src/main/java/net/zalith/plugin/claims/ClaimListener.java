package net.zalith.plugin.claims;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.EnumSet;
import java.util.Set;

public final class ClaimListener implements Listener {
    private final ClaimManager claimManager;

    private static final Set<Material> CONTAINERS = EnumSet.of(
            Material.CHEST,
            Material.TRAPPED_CHEST,
            Material.BARREL,
            Material.HOPPER,
            Material.DROPPER,
            Material.DISPENSER,
            Material.FURNACE,
            Material.BLAST_FURNACE,
            Material.SMOKER,
            Material.BREWING_STAND,
            Material.SHULKER_BOX,
            Material.WHITE_SHULKER_BOX,
            Material.ORANGE_SHULKER_BOX,
            Material.MAGENTA_SHULKER_BOX,
            Material.LIGHT_BLUE_SHULKER_BOX,
            Material.YELLOW_SHULKER_BOX,
            Material.LIME_SHULKER_BOX,
            Material.PINK_SHULKER_BOX,
            Material.GRAY_SHULKER_BOX,
            Material.LIGHT_GRAY_SHULKER_BOX,
            Material.CYAN_SHULKER_BOX,
            Material.PURPLE_SHULKER_BOX,
            Material.BLUE_SHULKER_BOX,
            Material.BROWN_SHULKER_BOX,
            Material.GREEN_SHULKER_BOX,
            Material.RED_SHULKER_BOX,
            Material.BLACK_SHULKER_BOX
    );

    public ClaimListener(ClaimManager claimManager) {
        this.claimManager = claimManager;
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        boolean allowed = claimManager.canBuild(player.getUniqueId(), event.getBlock().getLocation(), player.hasPermission("claims.bypass"));
        if (!allowed) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You cannot build in this claim.");
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        boolean allowed = claimManager.canBuild(player.getUniqueId(), event.getBlock().getLocation(), player.hasPermission("claims.bypass"));
        if (!allowed) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You cannot break blocks in this claim.");
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block block = event.getClickedBlock();
        if (block == null) return;
        if (!CONTAINERS.contains(block.getType())) return;
        Player player = event.getPlayer();
        boolean allowed = claimManager.canBuild(player.getUniqueId(), block.getLocation(), player.hasPermission("claims.bypass"));
        if (!allowed) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You cannot access containers in this claim.");
        }
    }
}
