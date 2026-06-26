package br.com.focuscorreio;

import org.bukkit.inventory.ItemStack;

import java.time.Instant;
import java.util.UUID;

public final class Reward {
    private final String id;
    private final String title;
    private final String source;
    private final Instant createdAt;
    private final ItemStack item;

    public Reward(String id, String title, String source, Instant createdAt, ItemStack item) {
        this.id = id;
        this.title = title;
        this.source = source;
        this.createdAt = createdAt;
        this.item = item;
    }

    public static Reward create(String title, String source, ItemStack item) {
        return new Reward(UUID.randomUUID().toString(), title, source, Instant.now(), item);
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getSource() {
        return source;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public ItemStack getItem() {
        return item.clone();
    }
}

