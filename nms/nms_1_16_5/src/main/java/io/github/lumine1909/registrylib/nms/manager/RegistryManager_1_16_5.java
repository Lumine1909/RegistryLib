package io.github.lumine1909.registrylib.nms.manager;

import com.mojang.serialization.Lifecycle;
import io.github.lumine1909.registrylib.api.Holder;
import io.github.lumine1909.registrylib.api.RegistryManager;
import io.github.lumine1909.registrylib.nms.Holder_1_16_5;
import io.github.lumine1909.registrylib.util.Validator;
import net.minecraft.server.v1_16_R3.*;

import java.util.Objects;

@SuppressWarnings("all")
public abstract class RegistryManager_1_16_5<T> implements RegistryManager, Validator {

    protected abstract RegistryMaterials<T> registry();

    protected abstract ResourceKey<IRegistry<T>> rootKey();

    protected abstract Class<T> clazz();

    @Override
    public Holder register(Object value, String key) {
        validateKey(key);
        validateValue(value);
        MinecraftKey location = new MinecraftKey(key);
        ResourceKey<T> resourceKey = ResourceKey.a(rootKey(), location);
        return new Holder_1_16_5<>(registry().a(resourceKey, (T) value, Lifecycle.stable()), location);
    }

    @Override
    public boolean contains(Object value) {
        validateValue(value);
        return registry().getKey((T) value) != null;
    }

    @Override
    public boolean contains(String key) {
        validateKey(key);
        return registry().get(MinecraftKey.a(key)) != null;
    }

    @Override
    public T getValue(String key) {
        validateKey(key);
        return Objects.requireNonNull(registry().get(MinecraftKey.a(key)));
    }

    @Override
    public String getKey(Object value) {
        validateValue(value);
        return Objects.requireNonNull(registry().getKey((T) value)).toString();
    }

    @Override
    public void validateKey(String key) {
        MinecraftKey location = MinecraftKey.a(key);
        if (location == null) {
            throw new IllegalArgumentException("Invalid biome key: " + key);
        }
    }

    @Override
    public void validateValue(Object value) {
        if (!(value.getClass().isAssignableFrom(clazz()))) {
            throw new IllegalArgumentException("Not a Minecraft Biome object");
        }
    }

    public static class Biome extends RegistryManager_1_16_5<BiomeBase> implements RegistryManager.Biome {

        private static final RegistryMaterials<BiomeBase> REGISTRY = (RegistryMaterials<BiomeBase>) Objects.requireNonNull(MinecraftServer.getServer().customRegistry.a(IRegistry.ay).get());

        @Override
        protected RegistryMaterials<BiomeBase> registry() {
            return REGISTRY;
        }

        @Override
        protected ResourceKey<IRegistry<BiomeBase>> rootKey() {
            return IRegistry.ay;
        }

        @Override
        protected Class<BiomeBase> clazz() {
            return BiomeBase.class;
        }
    }

    public static class DimensionType extends RegistryManager_1_16_5<DimensionManager> implements RegistryManager.DimensionType {

        private static final RegistryMaterials<DimensionManager> REGISTRY = (RegistryMaterials<DimensionManager>) Objects.requireNonNull(MinecraftServer.getServer().customRegistry.a(IRegistry.K).get());

        @Override
        protected RegistryMaterials<DimensionManager> registry() {
            return REGISTRY;
        }

        @Override
        protected ResourceKey<IRegistry<DimensionManager>> rootKey() {
            return IRegistry.K;
        }

        @Override
        protected Class<DimensionManager> clazz() {
            return DimensionManager.class;
        }
    }
}