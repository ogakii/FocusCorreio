package br.com.focuscorreio;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class CorreioMenuHolder implements InventoryHolder {
    private final UUID owner;
    private final int page;
    private final Map<Integer, Integer> rewardIndexes = new HashMap<>();
    private Inventory inventory;

    public CorreioMenuHolder(UUID owner, int page) {
        this.owner = owner;
        this.page = page;
    }

    public UUID owner() {
        return owner;
    }

    public int page() {
        return page;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    public void setRewardIndex(int slot, int rewardIndex) {
        rewardIndexes.put(slot, rewardIndex);
    }

    public Integer rewardIndex(int slot) {
        return rewardIndexes.get(slot);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
