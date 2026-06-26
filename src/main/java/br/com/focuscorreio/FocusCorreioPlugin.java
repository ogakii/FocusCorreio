package br.com.focuscorreio;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class FocusCorreioPlugin extends JavaPlugin {
    private MailboxService mailboxService;
    private CorreioMenu menu;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        mailboxService = new MailboxService(this);
        mailboxService.load();
        menu = new CorreioMenu(this);

        CorreioCommand command = new CorreioCommand(this);
        PluginCommand pluginCommand = getCommand("correio");
        if (pluginCommand != null) {
            pluginCommand.setExecutor(command);
            pluginCommand.setTabCompleter(command);
        }

        getServer().getPluginManager().registerEvents(new CorreioListener(this), this);
        getLogger().info("FocusCorreio ativado.");
    }

    @Override
    public void onDisable() {
        if (mailboxService != null) {
            mailboxService.save();
        }
    }

    public MailboxService mailboxService() {
        return mailboxService;
    }

    public CorreioMenu menu() {
        return menu;
    }
}

