package br.com.focuscorreio;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public final class PlayerMailbox {
    private final UUID owner;
    private String lastKnownName;
    private final List<Reward> rewards = new ArrayList<>();

    public PlayerMailbox(UUID owner, String lastKnownName) {
        this.owner = owner;
        this.lastKnownName = lastKnownName;
    }

    public UUID getOwner() {
        return owner;
    }

    public String getLastKnownName() {
        return lastKnownName;
    }

    public void setLastKnownName(String lastKnownName) {
        if (lastKnownName != null && !lastKnownName.isBlank()) {
            this.lastKnownName = lastKnownName;
        }
    }

    public void addReward(Reward reward) {
        rewards.add(reward);
    }

    public boolean removeReward(String rewardId) {
        return rewards.removeIf(reward -> reward.getId().equals(rewardId));
    }

    public void clear() {
        rewards.clear();
    }

    public List<Reward> rewards() {
        rewards.sort(Comparator.comparing(Reward::getCreatedAt));
        return Collections.unmodifiableList(rewards);
    }

    public int size() {
        return rewards.size();
    }
}

