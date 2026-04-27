package id.ac.ui.cs.advprog.yomu.clan.internal.model;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

public class Clan {
    public static final int MAX_MEMBERS = 5;

    private final UUID id;
    private final String name;
    private UUID ownerUserId;
    private final Set<UUID> memberUserIds;
    private final String tier;
    private final double score;

    public Clan(UUID id, String name, UUID ownerUserId, Set<UUID> memberUserIds, String tier, double score) {
        this.id = id;
        this.name = name;
        this.ownerUserId = ownerUserId;
        this.memberUserIds = new LinkedHashSet<>(memberUserIds);
        this.tier = tier;
        this.score = score;
    }

    public static Clan create(UUID ownerUserId, String name) {
        LinkedHashSet<UUID> members = new LinkedHashSet<>();
        members.add(ownerUserId);
        return new Clan(UUID.randomUUID(), name, ownerUserId, members, "BRONZE", 0.0d);
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public UUID getOwnerUserId() {
        return ownerUserId;
    }

    public Set<UUID> getMemberUserIds() {
        return Collections.unmodifiableSet(memberUserIds);
    }

    public String getTier() {
        return tier;
    }

    public double getScore() {
        return score;
    }

    public boolean isMember(UUID userId) {
        return memberUserIds.contains(userId);
    }

    public void addMember(UUID userId) {
        if (isFull()) {
            throw new IllegalStateException("Clan member limit reached");
        }
        memberUserIds.add(userId);
    }

    public void removeMember(UUID userId) {
        memberUserIds.remove(userId);
    }

    public boolean isFull() {
        return memberUserIds.size() >= MAX_MEMBERS;
    }

    public void transferOwnership(UUID newOwnerUserId) {
        this.ownerUserId = newOwnerUserId;
    }
}
