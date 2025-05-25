package io.github.lumine1909.example.listener;

import io.github.lumine1909.registrylib.api.RegistryType;
import io.github.lumine1909.registrylib.event.ConnectionCloseEvent;
import io.github.lumine1909.registrylib.event.RegisterEvent;
import io.github.lumine1909.registrylib.event.RegisterFinishEvent;
import net.minecraft.world.level.biome.Biome;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import static io.github.lumine1909.example.ExamplePlugin.plugin;

public class ExampleListener implements Listener {

    public ExampleListener() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onConnectionClose(ConnectionCloseEvent e) {
        // If you are in Spigot 1.16.5 and you need this event...
    }

    @EventHandler
    public void onRegisterFinish(RegisterFinishEvent e) {
        // Access registered entry (Requires enable that feature from config)
        if (e.getRegistryType() == RegistryType.BIOME) {
            Biome biome = (Biome) e.getHolder().value();
            // Do what you want...
        }
    }

    @EventHandler
    public void onRegister(RegisterEvent e) {
        // Modify registering entry (Requires enable that feature from config)
        if (e.getRegistryType() == RegistryType.BIOME) {
            Biome biome = null;
            // Set register value if you need...
            e.setValue(biome);
        }
    }
}