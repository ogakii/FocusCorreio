package br.com.focuscorreio;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class CorreioMenu {
    public static final int PAGE_SIZE = 45;
    public static final int PREVIOUS_SLOT = 45;
    public static final int CLAIM_ALL_SLOT = 49;
    public static final int REFRESH_SLOT = 50;
    public static final int NEXT_SLOT = 53;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
            .withZone(ZoneId.systemDefault());

    private final FocusCorreioPlugin plugin;

    public CorreioMenu(FocusCorreioPlugin plugin) {
        this.plugin = plugin;
    }

    public void open(Player player, int page) {
        List<Reward> rewards = plugin.mailboxService().rewards(player.getUniqueId());
        int pages = Math.max(1, (int) Math.ceil(rewards.size() / (double) PAGE_SIZE));
        int safePage = Math.max(0, Math.min(page, pages - 1));

        FileConfiguration config = plugin.getConfig();
        String title = MessageUtil.color(config.getString("gui.title", "&6Correio"));
        CorreioMenuHolder holder = new CorreioMenuHolder(player.getUniqueId(), safePage);
        Inventory inventory = Bukkit.createInventory(holder, 54, title);
        holder.setInventory(inventory);

        fillRewards(inventory, holder, rewards, safePage);
        fillControls(inventory, safePage, pages, rewards.isEmpty());

        player.openInventory(inventory);
    }

    private void fillRewards(Inventory inventory, CorreioMenuHolder holder, List<Reward> rewards, int page) {
        int start = page * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, rewards.size());

        if (rewards.isEmpty()) {
            inventory.setItem(22, emptyItem());
            return;
        }

        List<Integer> slots = centeredRewardSlots(end - start);
        for (int i = start; i < end; i++) {
            int slot = slots.get(i - start);
            Reward reward = rewards.get(i);
            holder.setRewardIndex(slot, i);
            inventory.setItem(slot, rewardItem(reward));
        }
    }

    private List<Integer> centeredRewardSlots(int amount) {
        List<Integer> slots = new ArrayList<>();
        if (amount <= 0) {
            return slots;
        }

        int rows = Math.min(5, (int) Math.ceil(amount / 9.0));
        int startRow = (5 - rows) / 2;
        int baseItemsPerRow = amount / rows;
        int rowsWithExtraItem = amount % rows;
        int remaining = amount;

        for (int rowOffset = 0; rowOffset < rows && remaining > 0; rowOffset++) {
            int rowItems = baseItemsPerRow + (rowOffset < rowsWithExtraItem ? 1 : 0);
            rowItems = Math.min(rowItems, remaining);
            int startColumn = (9 - rowItems) / 2;
            int row = startRow + rowOffset;

            for (int column = 0; column < rowItems; column++) {
                slots.add(row * 9 + startColumn + column);
            }

            remaining -= rowItems;
        }

        return slots;
    }

    private void fillControls(Inventory inventory, int page, int pages, boolean empty) {
        ItemStack filler = createItem(Material.GRAY_STAINED_GLASS_PANE, " ", List.of());
        for (int slot = 45; slot < 54; slot++) {
            inventory.setItem(slot, filler);
        }

        FileConfiguration config = plugin.getConfig();
        Map<String, String> placeholders = Map.of(
                "page", String.valueOf(page + 1),
                "pages", String.valueOf(pages)
        );

        if (page > 0) {
            inventory.setItem(PREVIOUS_SLOT, createItem(Material.ARROW, config.getString("gui.previous-page"), List.of()));
        }

        inventory.setItem(CLAIM_ALL_SLOT, createItem(
                empty ? Material.MINECART : Material.CHEST_MINECART,
                config.getString("gui.claim-all"),
                List.of(MessageUtil.color(MessageUtil.placeholders(config.getString("gui.page-info"), placeholders)))
        ));

        inventory.setItem(REFRESH_SLOT, createItem(Material.SUNFLOWER, config.getString("gui.refresh"), List.of()));

        if (page + 1 < pages) {
            inventory.setItem(NEXT_SLOT, createItem(Material.ARROW, config.getString("gui.next-page"), List.of()));
        }
    }

    private ItemStack emptyItem() {
        FileConfiguration config = plugin.getConfig();
        return createItem(
                Material.BARRIER,
                config.getString("gui.empty-name"),
                MessageUtil.colorList(config.getStringList("gui.empty-lore"), Map.of())
        );
    }

    private ItemStack rewardItem(Reward reward) {
        FileConfiguration config = plugin.getConfig();
        ItemStack display = reward.getItem();
        ItemMeta meta = display.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(MessageUtil.color("&a" + reward.getTitle()));
            meta.setLore(MessageUtil.colorList(config.getStringList("gui.reward-lore"), Map.of(
                    "source", reward.getSource(),
                    "date", DATE_FORMAT.format(reward.getCreatedAt())
            )));
            meta.addItemFlags(ItemFlag.values());
            display.setItemMeta(meta);
        }
        return display;
    }

    private ItemStack createItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(MessageUtil.color(name == null ? "" : name));
            meta.setLore(new ArrayList<>(lore));
            meta.addItemFlags(ItemFlag.values());
            item.setItemMeta(meta);
        }
        return item;
    }
}
