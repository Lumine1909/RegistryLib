package io.github.lumine1909.example;

import io.github.lumine1909.example.listener.ExampleListener;
import io.github.lumine1909.registrylib.api.RegistryAPI;
import net.minecraft.world.level.biome.Biome;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class ExamplePlugin extends JavaPlugin {

    public static ExamplePlugin plugin;

    @Override
    public void onEnable() {
        plugin = this;
        new ExampleListener();
        // Register dynamically
        Biome biome = null;
        RegistryAPI.INSTANCE.biomeManager().register(
            biome,
            "example:examplebiome"
        );
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            return true;
        }
        // Reload registry for player (Requires enable that feature from config)
        RegistryAPI.INSTANCE.reloadPlayer(player);
        return true;
    }
}
