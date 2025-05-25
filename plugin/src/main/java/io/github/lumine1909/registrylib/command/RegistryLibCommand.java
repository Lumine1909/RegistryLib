package io.github.lumine1909.registrylib.command;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static io.github.lumine1909.registrylib.RegistryLibPlugin.plugin;

public class RegistryLibCommand implements TabExecutor {

    public RegistryLibCommand() {
        Objects.requireNonNull(Bukkit.getPluginCommand("registrylib")).setExecutor(this);
        Objects.requireNonNull(Bukkit.getPluginCommand("registrylib")).setTabCompleter(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!sender.hasPermission("registrylib.admin")) {
            return true;
        }
        plugin.callReload();
        sender.sendMessage(ChatColor.AQUA + "[RegistryLib] Plugin reloaded, please report and restart server if encounter any issues");
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        return Collections.singletonList("reload");
    }
}
