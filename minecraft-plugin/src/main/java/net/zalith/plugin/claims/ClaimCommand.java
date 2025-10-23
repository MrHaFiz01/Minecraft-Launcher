package net.zalith.plugin.claims;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class ClaimCommand implements TabExecutor {
    private final ClaimManager claimManager;

    public ClaimCommand(ClaimManager claimManager) {
        this.claimManager = claimManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }
        Player player = (Player) sender;

        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sendHelp(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "create":
                int radius = 10;
                if (args.length >= 2) {
                    try { radius = Math.min(64, Math.max(4, Integer.parseInt(args[1]))); } catch (NumberFormatException ignored) {}
                }
                Location loc = player.getLocation();
                int x = loc.getBlockX();
                int z = loc.getBlockZ();
                int x1 = x - radius;
                int z1 = z - radius;
                int x2 = x + radius;
                int z2 = z + radius;
                String world = loc.getWorld().getName();
                if (claimManager.overlaps(world, x1, z1, x2, z2)) {
                    player.sendMessage(ChatColor.RED + "Your claim overlaps with an existing claim.");
                    return true;
                }
                claimManager.createClaim(world, x1, z1, x2, z2, player.getUniqueId());
                player.sendMessage(ChatColor.GREEN + "Created claim from (" + x1 + ", " + z1 + ") to (" + x2 + ", " + z2 + ").");
                return true;
            case "delete":
                boolean deleted = claimManager.deleteClaimAt(player.getLocation(), player.getUniqueId());
                if (deleted) {
                    player.sendMessage(ChatColor.GREEN + "Claim deleted.");
                } else {
                    player.sendMessage(ChatColor.RED + "No claim here or you are not the owner.");
                }
                return true;
            case "trust":
            case "untrust":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /claim " + args[0] + " <player>");
                    return true;
                }
                String targetName = args[1];
                OfflinePlayer offline = Bukkit.getOfflinePlayerIfCached(targetName);
                if (offline == null) offline = Bukkit.getPlayerExact(targetName);
                if (offline == null) {
                    player.sendMessage(ChatColor.RED + "Player not found (must have joined before).");
                    return true;
                }
                UUID targetId = offline.getUniqueId();
                boolean trust = args[0].equalsIgnoreCase("trust");
                boolean ok = claimManager.trustAt(player.getLocation(), player.getUniqueId(), targetId, trust);
                if (ok) {
                    player.sendMessage(ChatColor.GREEN + (trust ? "Trusted " : "Untrusted ") + offline.getName() + ".");
                } else {
                    player.sendMessage(ChatColor.RED + "No claim here or you are not the owner.");
                }
                return true;
            case "info":
                Optional<Claim> c = claimManager.getClaimAt(player.getLocation());
                if (c.isEmpty()) {
                    player.sendMessage(ChatColor.YELLOW + "Wilderness: no claim here.");
                } else {
                    Claim claim = c.get();
                    String ownerName = Optional.ofNullable(Bukkit.getOfflinePlayer(claim.getOwnerId()).getName()).orElse(claim.getOwnerId().toString());
                    player.sendMessage(ChatColor.GOLD + "Claim by " + ownerName + ChatColor.GRAY +
                            " [x:" + claim.getMinX() + ".." + claim.getMaxX() + ", z:" + claim.getMinZ() + ".." + claim.getMaxZ() + "]");
                    if (!claim.getTrustedPlayerIds().isEmpty()) {
                        player.sendMessage(ChatColor.GRAY + "Trusted: " + claim.getTrustedPlayerIds().size() + " players");
                    }
                }
                return true;
            default:
                sendHelp(player);
                return true;
        }
    }

    private void sendHelp(Player player) {
        player.sendMessage(ChatColor.AQUA + "Claims: /claim create [radius], /claim delete, /claim trust <player>, /claim untrust <player>, /claim info");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("create", "delete", "trust", "untrust", "info");
        }
        if (args.length == 2 && ("trust".equalsIgnoreCase(args[0]) || "untrust".equalsIgnoreCase(args[0]))) {
            List<String> names = new ArrayList<>();
            Bukkit.getOnlinePlayers().forEach(p -> names.add(p.getName()));
            return names;
        }
        return new ArrayList<>();
    }
}
