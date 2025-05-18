package io.github.lumine1909.registrylib.nms;

import io.github.lumine1909.registrylib.api.RegistryManager;

public class ManagerHolder {

    private final RegistryManager.Biome biome;
    private final RegistryManager.DimensionType dimensionType;

    public ManagerHolder(final RegistryManager.Biome biome, final RegistryManager.DimensionType dimensionType) {
        this.biome = biome;
        this.dimensionType = dimensionType;
    }

    public RegistryManager.Biome biome() {
        return biome;
    }

    public RegistryManager.DimensionType dimensionType() {
        return dimensionType;
    }
}
