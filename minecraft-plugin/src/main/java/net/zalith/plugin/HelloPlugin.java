package net.zalith.plugin;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class HelloPlugin extends JavaPlugin {
    @Override
    public void onEnable() {
        getLogger().info("HelloPlugin enabled");
    }

    @Override
    public void onDisable() {
        getLogger().info("HelloPlugin disabled");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase("hello")) {
            return false;
        }
        String message = ChatColor.GREEN + "Hello from Zalith plugin!";
        if (sender instanceof Player) {
            sender.sendMessage(message);
        } else {
            sender.sendMessage("Hello from Zalith plugin!");
        }
        return true;
    }
}
