package net.zalith.plugin.claims;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public final class Claim {
    private final String worldName;
    private final int minX;
    private final int minZ;
    private final int maxX;
    private final int maxZ;
    private final UUID ownerId;
    private final Set<UUID> trustedPlayerIds = new HashSet<>();

    public Claim(String worldName, int x1, int z1, int x2, int z2, UUID ownerId) {
        this.worldName = worldName;
        this.minX = Math.min(x1, x2);
        this.minZ = Math.min(z1, z2);
        this.maxX = Math.max(x1, x2);
        this.maxZ = Math.max(z1, z2);
        this.ownerId = ownerId;
    }

    public String getWorldName() {
        return worldName;
    }

    public int getMinX() {
        return minX;
    }

    public int getMinZ() {
        return minZ;
    }

    public int getMaxX() {
        return maxX;
    }

    public int getMaxZ() {
        return maxZ;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public Set<UUID> getTrustedPlayerIds() {
        return trustedPlayerIds;
    }

    public boolean contains(int x, int z) {
        return x >= minX && x <= maxX && z >= minZ && z <= maxZ;
    }

    public boolean intersects(Claim other) {
        if (!Objects.equals(this.worldName, other.worldName)) return false;
        return this.minX <= other.maxX && this.maxX >= other.minX
                && this.minZ <= other.maxZ && this.maxZ >= other.minZ;
    }
}
