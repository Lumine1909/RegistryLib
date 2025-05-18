package io.github.lumine1909.registrylib.api;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public interface RegistryAPI {

    RegistryAPI INSTANCE = Bukkit.getServicesManager().load(RegistryAPI.class);

    RegistryManager.Biome biomeManager();

    RegistryManager.DimensionType dimensionTypeManager();

    void reloadPlayer(Player player);
}