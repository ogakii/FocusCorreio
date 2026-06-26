package br.com.focuscorreio;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;

public final class CorreioListener implements Listener {
    private final FocusCorreioPlugin plugin;

    public CorreioListener(FocusCorreioPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        plugin.mailboxService().mailbox(player.getUniqueId(), player.getName());

        int pending = plugin.mailboxService().rewards(player.getUniqueId()).size();
        if (pending > 0) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> MessageUtil.send(
                    plugin.getConfig(),
                    player,
                    "pending-on-join",
                    Map.of("amount", String.valueOf(pending))
            ), 40L);
        }
    }

    @EventHandler
    public void onDragonDeath(EntityDeathEvent event) {
        if (event.getEntityType() != EntityType.ENDER_DRAGON) {
            return;
        }

        Player killer = event.getEntity().getKiller();
        if (killer == null) {
            return;
        }

        if (!plugin.getConfig().getBoolean("automatic-rewards.ender-dragon.enabled", true)) {
            return;
        }

        String materialName = plugin.getConfig().getString("automatic-rewards.ender-dragon.material", "DRAGON_EGG");
        Material material = Material.matchMaterial(materialName == null ? "DRAGON_EGG" : materialName);
        if (material == null || material.isAir()) {
            plugin.getLogger().warning("Material invalido em automatic-rewards.ender-dragon.material: " + materialName);
            return;
        }

        int amount = Math.max(1, plugin.getConfig().getInt("automatic-rewards.ender-dragon.amount", 1));
        String title = plugin.getConfig().getString("automatic-rewards.ender-dragon.title", "Recompensa do Dragao");
        String source = plugin.getConfig().getString("automatic-rewards.ender-dragon.source", "Dragao do End");

        int remaining = amount;
        while (remaining > 0) {
            int stackAmount = Math.min(remaining, material.getMaxStackSize());
            plugin.mailboxService().addReward(killer, Reward.create(title, source, new ItemStack(material, stackAmount)));
            remaining -= stackAmount;
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof CorreioMenuHolder holder)) {
            return;
        }

        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        if (!player.getUniqueId().equals(holder.owner())) {
            player.closeInventory();
            return;
        }

        int rawSlot = event.getRawSlot();
        if (rawSlot < 0 || rawSlot >= event.getInventory().getSize()) {
            return;
        }

        if (rawSlot == CorreioMenu.PREVIOUS_SLOT) {
            plugin.menu().open(player, holder.page() - 1);
            return;
        }

        if (rawSlot == CorreioMenu.NEXT_SLOT) {
            plugin.menu().open(player, holder.page() + 1);
            return;
        }

        if (rawSlot == CorreioMenu.REFRESH_SLOT) {
            plugin.menu().open(player, holder.page());
            return;
        }

        if (rawSlot == CorreioMenu.CLAIM_ALL_SLOT) {
            claimAll(player);
            return;
        }

        if (rawSlot >= CorreioMenu.PAGE_SIZE) {
            return;
        }

        Integer rewardIndex = holder.rewardIndex(rawSlot);
        if (rewardIndex == null) {
            return;
        }

        List<Reward> rewards = plugin.mailboxService().rewards(player.getUniqueId());
        if (rewardIndex >= rewards.size()) {
            return;
        }

        if (event.getClick() == ClickType.RIGHT) {
            claimAll(player);
            return;
        }

        Reward reward = rewards.get(rewardIndex);
        MailboxService.ClaimResult result = plugin.mailboxService().claimOne(player, reward);
        if (result.isFull()) {
            MessageUtil.send(plugin.getConfig(), player, "inventory-full");
            return;
        }

        MessageUtil.send(plugin.getConfig(), player, "claim-one", Map.of("title", reward.getTitle()));
        plugin.menu().open(player, holder.page());
    }

    private void claimAll(Player player) {
        MailboxService.ClaimResult result = plugin.mailboxService().claimAll(player);
        if (result.isFull()) {
            MessageUtil.send(plugin.getConfig(), player, "inventory-full");
            return;
        }

        if (result.claimed() == 0) {
            MessageUtil.send(plugin.getConfig(), player, "empty");
            return;
        }

        MessageUtil.send(plugin.getConfig(), player, "claim-all", Map.of("amount", String.valueOf(result.claimed())));
        plugin.menu().open(player, 0);
    }
}
