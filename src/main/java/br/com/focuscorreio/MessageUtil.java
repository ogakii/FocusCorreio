package br.com.focuscorreio;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class MessageUtil {
    private MessageUtil() {
    }

    public static String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text == null ? "" : text);
    }

    public static List<String> colorList(List<String> lines, Map<String, String> placeholders) {
        List<String> colored = new ArrayList<>();
        for (String line : lines) {
            colored.add(color(placeholders(line, placeholders)));
        }
        return colored;
    }

    public static String placeholders(String text, Map<String, String> placeholders) {
        String result = text == null ? "" : text;
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            result = result.replace("%" + entry.getKey() + "%", entry.getValue());
        }
        return result;
    }

    public static void send(FileConfiguration config, CommandSender sender, String key) {
        send(config, sender, key, Map.of());
    }

    public static void send(FileConfiguration config, CommandSender sender, String key, Map<String, String> placeholders) {
        String prefix = config.getString("messages.prefix", "");
        String message = config.getString("messages." + key, key);
        sender.sendMessage(color(prefix + placeholders(message, placeholders)));
    }

    public static ItemStack named(ItemStack item, String name, List<String> lore) {
        ItemStack clone = item.clone();
        ItemMeta meta = clone.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(color(name));
            meta.setLore(lore);
            clone.setItemMeta(meta);
        }
        return clone;
    }
}

