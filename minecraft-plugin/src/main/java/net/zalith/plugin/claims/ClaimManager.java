package net.zalith.plugin.claims;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

public final class ClaimManager {
    private final Plugin plugin;
    private final List<Claim> claims = new ArrayList<>();
    private final File file;
    private FileConfiguration config;

    public ClaimManager(Plugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "claims.yml");
        load();
    }

    public synchronized void load() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to create claims.yml: " + e.getMessage());
            }
        }
        config = YamlConfiguration.loadConfiguration(file);
        claims.clear();
        List<Map<?, ?>> list = config.getMapList("claims");
        for (Map<?, ?> raw : list) {
            try {
                String world = Objects.toString(raw.get("world"));
                int x1 = ((Number) raw.get("x1")).intValue();
                int z1 = ((Number) raw.get("z1")).intValue();
                int x2 = ((Number) raw.get("x2")).intValue();
                int z2 = ((Number) raw.get("z2")).intValue();
                UUID owner = UUID.fromString(Objects.toString(raw.get("owner")));
                Claim claim = new Claim(world, x1, z1, x2, z2, owner);
                Object trustedObj = raw.get("trusted");
                if (trustedObj instanceof List) {
                    for (Object o : (List<?>) trustedObj) {
                        try {
                            UUID id = UUID.fromString(Objects.toString(o));
                            claim.getTrustedPlayerIds().add(id);
                        } catch (Exception ignored) {}
                    }
                }
                claims.add(claim);
            } catch (Exception ex) {
                plugin.getLogger().warning("Skipping malformed claim entry: " + raw);
            }
        }
        plugin.getLogger().info("Loaded " + claims.size() + " claims.");
    }

    public synchronized void save() {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Claim c : claims) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("world", c.getWorldName());
            m.put("x1", c.getMinX());
            m.put("z1", c.getMinZ());
            m.put("x2", c.getMaxX());
            m.put("z2", c.getMaxZ());
            m.put("owner", c.getOwnerId().toString());
            List<String> trusted = new ArrayList<>();
            for (UUID id : c.getTrustedPlayerIds()) trusted.add(id.toString());
            m.put("trusted", trusted);
            list.add(m);
        }
        config.set("claims", list);
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save claims.yml: " + e.getMessage());
        }
    }

    public synchronized Optional<Claim> getClaimAt(Location location) {
        String world = location.getWorld().getName();
        int x = location.getBlockX();
        int z = location.getBlockZ();
        for (Claim c : claims) {
            if (c.getWorldName().equals(world) && c.contains(x, z)) {
                return Optional.of(c);
            }
        }
        return Optional.empty();
    }

    public synchronized boolean canBuild(UUID playerId, Location location, boolean hasBypass) {
        if (hasBypass) return true;
        Optional<Claim> claimOpt = getClaimAt(location);
        if (claimOpt.isEmpty()) return true; // wilderness
        Claim claim = claimOpt.get();
        return claim.getOwnerId().equals(playerId) || claim.getTrustedPlayerIds().contains(playerId);
    }

    public synchronized boolean overlaps(String world, int x1, int z1, int x2, int z2) {
        Claim candidate = new Claim(world, x1, z1, x2, z2, UUID.randomUUID());
        for (Claim c : claims) {
            if (c.intersects(candidate)) return true;
        }
        return false;
    }

    public synchronized Claim createClaim(String world, int x1, int z1, int x2, int z2, UUID owner) {
        Claim claim = new Claim(world, x1, z1, x2, z2, owner);
        claims.add(claim);
        save();
        return claim;
    }

    public synchronized boolean deleteClaimAt(Location location, UUID requester) {
        Iterator<Claim> it = claims.iterator();
        String world = location.getWorld().getName();
        int x = location.getBlockX();
        int z = location.getBlockZ();
        while (it.hasNext()) {
            Claim c = it.next();
            if (c.getWorldName().equals(world) && c.contains(x, z)) {
                if (!c.getOwnerId().equals(requester)) return false;
                it.remove();
                save();
                return true;
            }
        }
        return false;
    }

    public synchronized boolean trustAt(Location location, UUID owner, UUID target, boolean trust) {
        Optional<Claim> claimOpt = getClaimAt(location);
        if (claimOpt.isEmpty()) return false;
        Claim c = claimOpt.get();
        if (!c.getOwnerId().equals(owner)) return false;
        if (trust) {
            boolean added = c.getTrustedPlayerIds().add(target);
            if (added) save();
            return added;
        } else {
            boolean removed = c.getTrustedPlayerIds().remove(target);
            if (removed) save();
            return removed;
        }
    }
}
