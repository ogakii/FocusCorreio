package br.com.focuscorreio;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class MailboxService {
    private final FocusCorreioPlugin plugin;
    private final File dataFile;
    private final Map<UUID, PlayerMailbox> mailboxes = new HashMap<>();
    private YamlConfiguration data;

    public MailboxService(FocusCorreioPlugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "data.yml");
    }

    public void load() {
        plugin.getDataFolder().mkdirs();
        data = YamlConfiguration.loadConfiguration(dataFile);
        mailboxes.clear();

        ConfigurationSection players = data.getConfigurationSection("players");
        if (players == null) {
            return;
        }

        for (String uuidText : players.getKeys(false)) {
            UUID uuid = parseUuid(uuidText);
            if (uuid == null) {
                continue;
            }

            ConfigurationSection playerSection = players.getConfigurationSection(uuidText);
            if (playerSection == null) {
                continue;
            }

            PlayerMailbox mailbox = new PlayerMailbox(uuid, playerSection.getString("name", "Desconhecido"));
            ConfigurationSection rewards = playerSection.getConfigurationSection("rewards");
            if (rewards != null) {
                for (String rewardId : rewards.getKeys(false)) {
                    ConfigurationSection rewardSection = rewards.getConfigurationSection(rewardId);
                    if (rewardSection == null) {
                        continue;
                    }

                    ItemStack item = rewardSection.getItemStack("item");
                    if (item == null || item.getType().isAir()) {
                        continue;
                    }

                    String title = rewardSection.getString("title", item.getType().name());
                    String source = rewardSection.getString("source", "Sistema");
                    Instant createdAt = parseInstant(rewardSection.getString("created-at"));
                    mailbox.addReward(new Reward(rewardId, title, source, createdAt, item));
                }
            }

            mailboxes.put(uuid, mailbox);
        }
    }

    public void save() {
        data.set("players", null);

        for (PlayerMailbox mailbox : mailboxes.values()) {
            if (mailbox.size() == 0) {
                continue;
            }

            String base = "players." + mailbox.getOwner();
            data.set(base + ".name", mailbox.getLastKnownName());

            for (Reward reward : mailbox.rewards()) {
                String rewardPath = base + ".rewards." + reward.getId();
                data.set(rewardPath + ".title", reward.getTitle());
                data.set(rewardPath + ".source", reward.getSource());
                data.set(rewardPath + ".created-at", reward.getCreatedAt().toString());
                data.set(rewardPath + ".item", reward.getItem());
            }
        }

        try {
            data.save(dataFile);
        }
        catch (IOException exception) {
            plugin.getLogger().severe("Nao foi possivel salvar data.yml: " + exception.getMessage());
        }
    }

    public PlayerMailbox mailbox(UUID uuid, String playerName) {
        PlayerMailbox mailbox = mailboxes.computeIfAbsent(uuid, key -> new PlayerMailbox(key, playerName));
        mailbox.setLastKnownName(playerName);
        return mailbox;
    }

    public PlayerMailbox mailbox(OfflinePlayer player) {
        return mailbox(player.getUniqueId(), player.getName());
    }

    public List<Reward> rewards(UUID uuid) {
        PlayerMailbox mailbox = mailboxes.get(uuid);
        if (mailbox == null) {
            return List.of();
        }
        return mailbox.rewards();
    }

    public void addReward(OfflinePlayer target, Reward reward) {
        PlayerMailbox mailbox = mailbox(target);
        mailbox.addReward(reward);
        save();

        Player online = Bukkit.getPlayer(target.getUniqueId());
        if (online != null) {
            MessageUtil.send(plugin.getConfig(), online, "reward-added-target", Map.of("title", reward.getTitle()));
        }
    }

    public ClaimResult claimOne(Player player, Reward reward) {
        if (!canFit(player.getInventory(), reward.getItem())) {
            return ClaimResult.full();
        }

        player.getInventory().addItem(reward.getItem());
        mailbox(player.getUniqueId(), player.getName()).removeReward(reward.getId());
        save();
        return ClaimResult.claimed(1);
    }

    public ClaimResult claimAll(Player player) {
        PlayerMailbox mailbox = mailbox(player.getUniqueId(), player.getName());
        List<Reward> snapshot = new ArrayList<>(mailbox.rewards());
        int claimed = 0;

        for (Reward reward : snapshot) {
            if (!canFit(player.getInventory(), reward.getItem())) {
                continue;
            }

            player.getInventory().addItem(reward.getItem());
            mailbox.removeReward(reward.getId());
            claimed++;
        }

        if (claimed > 0) {
            save();
        }

        if (claimed == 0 && !snapshot.isEmpty()) {
            return ClaimResult.full();
        }

        return ClaimResult.claimed(claimed);
    }

    public int clear(OfflinePlayer target) {
        PlayerMailbox mailbox = mailbox(target);
        int amount = mailbox.size();
        mailbox.clear();
        save();
        return amount;
    }

    private boolean canFit(PlayerInventory inventory, ItemStack incoming) {
        int remaining = incoming.getAmount();
        int maxStack = incoming.getMaxStackSize();

        for (ItemStack slot : inventory.getStorageContents()) {
            if (slot == null || slot.getType().isAir()) {
                remaining -= maxStack;
                if (remaining <= 0) {
                    return true;
                }
                continue;
            }

            if (slot.isSimilar(incoming) && slot.getAmount() < maxStack) {
                remaining -= (maxStack - slot.getAmount());
                if (remaining <= 0) {
                    return true;
                }
            }
        }

        return remaining <= 0;
    }

    private UUID parseUuid(String text) {
        try {
            return UUID.fromString(text);
        }
        catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private Instant parseInstant(String text) {
        if (text == null || text.isBlank()) {
            return Instant.now();
        }

        try {
            return Instant.parse(text);
        }
        catch (IllegalArgumentException exception) {
            return Instant.now();
        }
    }

    public static final class ClaimResult {
        private final int claimed;
        private final boolean full;

        private ClaimResult(int claimed, boolean full) {
            this.claimed = claimed;
            this.full = full;
        }

        public static ClaimResult claimed(int amount) {
            return new ClaimResult(amount, false);
        }

        public static ClaimResult full() {
            return new ClaimResult(0, true);
        }

        public int claimed() {
            return claimed;
        }

        public boolean isFull() {
            return full;
        }
    }
}
