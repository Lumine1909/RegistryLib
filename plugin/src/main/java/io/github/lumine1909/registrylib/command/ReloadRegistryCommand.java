package io.github.lumine1909.registrylib.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static io.github.lumine1909.registrylib.RegistryLibPlugin.plugin;

public class ReloadRegistryCommand implements TabExecutor {

    public ReloadRegistryCommand() {
        Objects.requireNonNull(Bukkit.getPluginCommand("reloadregistry")).setExecutor(this);
        Objects.requireNonNull(Bukkit.getPluginCommand("reloadregistry")).setTabCompleter(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player) || !sender.hasPermission("registrylib.reloadregistry")) {
            return true;
        }
        plugin.reloadPlayer((Player) sender);
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        return Collections.emptyList();
    }
}
