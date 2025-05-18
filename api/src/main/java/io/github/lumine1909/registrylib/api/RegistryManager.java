package io.github.lumine1909.registrylib.api;

public interface RegistryManager {

    Holder register(Object value, String key);

    boolean contains(Object value);

    boolean contains(String key);

    Object getValue(String key);

    String getKey(Object value);

    interface Biome extends RegistryManager {

    }

    interface DimensionType extends RegistryManager {

    }
}