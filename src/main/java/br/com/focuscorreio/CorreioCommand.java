package br.com.focuscorreio;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public final class CorreioCommand implements CommandExecutor, TabCompleter {
    private final FocusCorreioPlugin plugin;

    public CorreioCommand(FocusCorreioPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            return open(sender);
        }

        String subcommand = args[0].toLowerCase(Locale.ROOT);
        switch (subcommand) {
            case "ajuda":
            case "help":
                return help(sender, label);
            case "resgatar":
            case "claim":
                return claim(sender);
            case "adicionar":
            case "add":
            case "dar":
                return addMaterial(sender, args);
            case "enviar":
            case "send":
                return sendHeldItem(sender, args);
            case "listar":
            case "list":
                return list(sender, args);
            case "limpar":
            case "clear":
                return clear(sender, args);
            case "dragao":
            case "dragon":
                return dragon(sender, args);
            case "reload":
            case "recarregar":
                return reload(sender);
            default:
                return open(sender);
        }
    }

    private boolean open(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            MessageUtil.send(plugin.getConfig(), sender, "only-player");
            return true;
        }

        if (!sender.hasPermission("focuscorreio.usar")) {
            MessageUtil.send(plugin.getConfig(), sender, "no-permission");
            return true;
        }

        plugin.menu().open(player, 0);
        return true;
    }

    private boolean claim(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            MessageUtil.send(plugin.getConfig(), sender, "only-player");
            return true;
        }

        if (!sender.hasPermission("focuscorreio.usar")) {
            MessageUtil.send(plugin.getConfig(), sender, "no-permission");
            return true;
        }

        MailboxService.ClaimResult result = plugin.mailboxService().claimAll(player);
        if (result.isFull()) {
            MessageUtil.send(plugin.getConfig(), sender, "inventory-full");
            return true;
        }

        if (result.claimed() == 0) {
            MessageUtil.send(plugin.getConfig(), sender, "empty");
            return true;
        }

        MessageUtil.send(plugin.getConfig(), sender, "claim-all", Map.of("amount", String.valueOf(result.claimed())));
        return true;
    }

    private boolean addMaterial(CommandSender sender, String[] args) {
        if (!sender.hasPermission("focuscorreio.admin")) {
            MessageUtil.send(plugin.getConfig(), sender, "no-permission");
            return true;
        }

        if (args.length < 5) {
            sender.sendMessage(MessageUtil.color("&cUse: /correio adicionar <jogador> <material> <quantia> <titulo...>"));
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        Material material = Material.matchMaterial(args[2]);
        if (material == null || material.isAir()) {
            MessageUtil.send(plugin.getConfig(), sender, "invalid-material", Map.of("material", args[2]));
            return true;
        }

        int amount = parsePositiveInt(args[3]);
        if (amount <= 0) {
            MessageUtil.send(plugin.getConfig(), sender, "invalid-number");
            return true;
        }

        String title = join(args, 4);
        int maxStack = material.getMaxStackSize();
        int remaining = amount;
        int created = 0;

        while (remaining > 0) {
            int stackAmount = Math.min(remaining, maxStack);
            ItemStack item = new ItemStack(material, stackAmount);
            plugin.mailboxService().addReward(target, Reward.create(title, sender.getName(), item));
            remaining -= stackAmount;
            created++;
        }

        MessageUtil.send(plugin.getConfig(), sender, "reward-added", Map.of("player", displayName(target)));
        if (created > 1) {
            sender.sendMessage(MessageUtil.color("&7Foram criados " + created + " pacotes por limite de stack."));
        }
        return true;
    }

    private boolean sendHeldItem(CommandSender sender, String[] args) {
        if (!sender.hasPermission("focuscorreio.admin")) {
            MessageUtil.send(plugin.getConfig(), sender, "no-permission");
            return true;
        }

        if (!(sender instanceof Player player)) {
            MessageUtil.send(plugin.getConfig(), sender, "only-player");
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage(MessageUtil.color("&cUse: /correio enviar <jogador> <titulo...>"));
            return true;
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType().isAir()) {
            sender.sendMessage(MessageUtil.color("&cSegure um item para enviar."));
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        String title = join(args, 2);
        plugin.mailboxService().addReward(target, Reward.create(title, sender.getName(), item.clone()));
        MessageUtil.send(plugin.getConfig(), sender, "reward-added", Map.of("player", displayName(target)));
        return true;
    }

    private boolean list(CommandSender sender, String[] args) {
        if (!sender.hasPermission("focuscorreio.admin")) {
            MessageUtil.send(plugin.getConfig(), sender, "no-permission");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(MessageUtil.color("&cUse: /correio listar <jogador>"));
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        List<Reward> rewards = plugin.mailboxService().rewards(target.getUniqueId());
        sender.sendMessage(MessageUtil.color("&6Correio de &f" + displayName(target) + "&6: &f" + rewards.size() + " &7pendente(s)."));
        for (Reward reward : rewards.stream().limit(10).collect(Collectors.toList())) {
            sender.sendMessage(MessageUtil.color("&8- &f" + reward.getTitle() + " &7x" + reward.getItem().getAmount() + " &8(" + reward.getItem().getType() + ")"));
        }
        if (rewards.size() > 10) {
            sender.sendMessage(MessageUtil.color("&7...e mais " + (rewards.size() - 10) + "."));
        }
        return true;
    }

    private boolean clear(CommandSender sender, String[] args) {
        if (!sender.hasPermission("focuscorreio.admin")) {
            MessageUtil.send(plugin.getConfig(), sender, "no-permission");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(MessageUtil.color("&cUse: /correio limpar <jogador>"));
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        plugin.mailboxService().clear(target);
        MessageUtil.send(plugin.getConfig(), sender, "cleared", Map.of("player", displayName(target)));
        return true;
    }

    private boolean dragon(CommandSender sender, String[] args) {
        if (!sender.hasPermission("focuscorreio.admin")) {
            MessageUtil.send(plugin.getConfig(), sender, "no-permission");
            return true;
        }

        if (args.length == 1 || List.of("ver", "info", "status").contains(args[1].toLowerCase(Locale.ROOT))) {
            MessageUtil.send(plugin.getConfig(), sender, "dragon-current", dragonPlaceholders());
            return true;
        }

        String action = args[1].toLowerCase(Locale.ROOT);
        switch (action) {
            case "definir":
            case "setar":
            case "set":
                return setDragonReward(sender, args);
            case "ativar":
            case "enable":
                plugin.getConfig().set("automatic-rewards.ender-dragon.enabled", true);
                plugin.saveConfig();
                MessageUtil.send(plugin.getConfig(), sender, "dragon-enabled");
                return true;
            case "desativar":
            case "disable":
                plugin.getConfig().set("automatic-rewards.ender-dragon.enabled", false);
                plugin.saveConfig();
                MessageUtil.send(plugin.getConfig(), sender, "dragon-disabled");
                return true;
            case "dar":
            case "testar":
                return giveDragonReward(sender, args);
            default:
                sender.sendMessage(MessageUtil.color("&cUse: /correio dragao definir <material> <quantia> <titulo...>"));
                sender.sendMessage(MessageUtil.color("&cOu: /correio dragao ver|ativar|desativar|dar <jogador>"));
                return true;
        }
    }

    private boolean setDragonReward(CommandSender sender, String[] args) {
        if (args.length < 5) {
            sender.sendMessage(MessageUtil.color("&cUse: /correio dragao definir <material> <quantia> <titulo...>"));
            return true;
        }

        Material material = Material.matchMaterial(args[2]);
        if (material == null || material.isAir()) {
            MessageUtil.send(plugin.getConfig(), sender, "invalid-material", Map.of("material", args[2]));
            return true;
        }

        int amount = parsePositiveInt(args[3]);
        if (amount <= 0) {
            MessageUtil.send(plugin.getConfig(), sender, "invalid-number");
            return true;
        }

        String title = join(args, 4);
        plugin.getConfig().set("automatic-rewards.ender-dragon.enabled", true);
        plugin.getConfig().set("automatic-rewards.ender-dragon.material", material.name());
        plugin.getConfig().set("automatic-rewards.ender-dragon.amount", amount);
        plugin.getConfig().set("automatic-rewards.ender-dragon.title", title);
        plugin.saveConfig();

        MessageUtil.send(plugin.getConfig(), sender, "dragon-updated", Map.of(
                "material", material.name(),
                "amount", String.valueOf(amount),
                "title", title
        ));
        return true;
    }

    private boolean giveDragonReward(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(MessageUtil.color("&cUse: /correio dragao dar <jogador>"));
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[2]);
        Material material = dragonMaterial();
        int amount = Math.max(1, plugin.getConfig().getInt("automatic-rewards.ender-dragon.amount", 1));
        String title = plugin.getConfig().getString("automatic-rewards.ender-dragon.title", "Recompensa do Dragao");
        String source = plugin.getConfig().getString("automatic-rewards.ender-dragon.source", "Dragao do End");

        addStackedReward(target, title, source, material, amount);
        MessageUtil.send(plugin.getConfig(), sender, "reward-added", Map.of("player", displayName(target)));
        return true;
    }

    private Map<String, String> dragonPlaceholders() {
        return Map.of(
                "enabled", plugin.getConfig().getBoolean("automatic-rewards.ender-dragon.enabled", true) ? "ativado" : "desativado",
                "material", dragonMaterial().name(),
                "amount", String.valueOf(Math.max(1, plugin.getConfig().getInt("automatic-rewards.ender-dragon.amount", 1))),
                "title", plugin.getConfig().getString("automatic-rewards.ender-dragon.title", "Recompensa do Dragao")
        );
    }

    private Material dragonMaterial() {
        String materialName = plugin.getConfig().getString("automatic-rewards.ender-dragon.material", "DRAGON_EGG");
        Material material = Material.matchMaterial(materialName == null ? "DRAGON_EGG" : materialName);
        return material == null || material.isAir() ? Material.DRAGON_EGG : material;
    }

    private void addStackedReward(OfflinePlayer target, String title, String source, Material material, int amount) {
        int maxStack = material.getMaxStackSize();
        int remaining = amount;

        while (remaining > 0) {
            int stackAmount = Math.min(remaining, maxStack);
            plugin.mailboxService().addReward(target, Reward.create(title, source, new ItemStack(material, stackAmount)));
            remaining -= stackAmount;
        }
    }

    private boolean reload(CommandSender sender) {
        if (!sender.hasPermission("focuscorreio.admin")) {
            MessageUtil.send(plugin.getConfig(), sender, "no-permission");
            return true;
        }

        plugin.reloadConfig();
        plugin.mailboxService().load();
        MessageUtil.send(plugin.getConfig(), sender, "reloaded");
        return true;
    }

    private boolean help(CommandSender sender, String label) {
        sender.sendMessage(MessageUtil.color("&6&lFocusCorreio"));
        sender.sendMessage(MessageUtil.color("&e/" + label + " &7- abre o correio."));
        sender.sendMessage(MessageUtil.color("&e/" + label + " resgatar &7- resgata tudo."));
        if (sender.hasPermission("focuscorreio.admin")) {
            sender.sendMessage(MessageUtil.color("&e/" + label + " adicionar <jogador> <material> <quantia> <titulo>"));
            sender.sendMessage(MessageUtil.color("&e/" + label + " enviar <jogador> <titulo> &7- envia item da mao."));
            sender.sendMessage(MessageUtil.color("&e/" + label + " dragao definir <material> <quantia> <titulo>"));
            sender.sendMessage(MessageUtil.color("&e/" + label + " dragao ver|ativar|desativar|dar <jogador>"));
            sender.sendMessage(MessageUtil.color("&e/" + label + " listar|limpar|reload"));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> options = new ArrayList<>(Arrays.asList("ajuda", "resgatar"));
            if (sender.hasPermission("focuscorreio.admin")) {
                options.addAll(Arrays.asList("adicionar", "enviar", "listar", "limpar", "dragao", "reload"));
            }
            return startsWith(options, args[0]);
        }

        if (!sender.hasPermission("focuscorreio.admin")) {
            return Collections.emptyList();
        }

        String subcommand = args[0].toLowerCase(Locale.ROOT);
        if (List.of("dragao", "dragon").contains(subcommand)) {
            if (args.length == 2) {
                return startsWith(Arrays.asList("ver", "definir", "ativar", "desativar", "dar"), args[1]);
            }

            String action = args[1].toLowerCase(Locale.ROOT);
            if (args.length == 3 && List.of("definir", "setar", "set").contains(action)) {
                return startsWith(Arrays.stream(Material.values()).map(Material::name).collect(Collectors.toList()), args[2]);
            }

            if (args.length == 3 && List.of("dar", "testar").contains(action)) {
                return startsWith(Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList()), args[2]);
            }

            if (args.length == 4 && List.of("definir", "setar", "set").contains(action)) {
                return List.of("1", "3", "16", "64");
            }
        }

        if (args.length == 2 && List.of("adicionar", "add", "dar", "enviar", "send", "listar", "list", "limpar", "clear").contains(subcommand)) {
            return startsWith(Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList()), args[1]);
        }

        if (args.length == 3 && List.of("adicionar", "add", "dar").contains(subcommand)) {
            return startsWith(Arrays.stream(Material.values()).map(Material::name).collect(Collectors.toList()), args[2]);
        }

        if (args.length == 4 && List.of("adicionar", "add", "dar").contains(subcommand)) {
            return List.of("1", "3", "16", "64");
        }

        return Collections.emptyList();
    }

    private List<String> startsWith(List<String> values, String prefix) {
        String lowerPrefix = prefix.toLowerCase(Locale.ROOT);
        return values.stream()
                .filter(value -> value.toLowerCase(Locale.ROOT).startsWith(lowerPrefix))
                .limit(60)
                .collect(Collectors.toList());
    }

    private int parsePositiveInt(String text) {
        try {
            return Integer.parseInt(text);
        }
        catch (NumberFormatException exception) {
            return -1;
        }
    }

    private String join(String[] args, int start) {
        return String.join(" ", Arrays.copyOfRange(args, start, args.length));
    }

    private String displayName(OfflinePlayer player) {
        return player.getName() == null ? player.getUniqueId().toString() : player.getName();
    }
}
