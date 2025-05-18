package io.github.lumine1909.registrylib.listener;

import com.destroystokyo.paper.event.player.PlayerConnectionCloseEvent;
import io.github.lumine1909.registrylib.event.ConnectionCloseEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import static io.github.lumine1909.registrylib.RegistryLibPlugin.plugin;
import static io.github.lumine1909.registrylib.util.ReflectionUtil.hasClass;

public class ConnectionCloseListener {

    public ConnectionCloseListener() {
        if (hasClass("com.destroystokyo.paper.event.player.PlayerConnectionCloseEvent")) {
            Bukkit.getPluginManager().registerEvents(new Paper(), plugin);
        } else {
            Bukkit.getPluginManager().registerEvents(new Spigot(), plugin);
        }
    }

    private static class Paper implements Listener {

        @EventHandler
        public void onConnectionClose(PlayerConnectionCloseEvent e) {
            plugin.getRegistryReloader().reloadFailed(e.getPlayerName());
            plugin.getChannelInjector().directUninject(e.getPlayerName());
        }
    }

    private static class Spigot implements Listener {

        @EventHandler
        public void onConnectionClose(ConnectionCloseEvent e) {
            plugin.getRegistryReloader().reloadFailed(e.getPlayerName());
            plugin.getChannelInjector().directUninject(e.getPlayerName());
        }
    }
}