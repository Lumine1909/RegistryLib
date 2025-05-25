package io.github.lumine1909.registrylib.api;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Optional;

public interface RegistryAPI {

    RegistryAPI INSTANCE = Optional.ofNullable(Bukkit.getServicesManager().load(RegistryAPI.class)).orElseThrow(() -> new IllegalStateException("Could not load RegistryLib plugin for API usage"));

    RegistryManager.Biome biomeManager();

    RegistryManager.DimensionType dimensionTypeManager();

    void reloadPlayer(Player player);
}