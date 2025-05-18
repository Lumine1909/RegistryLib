package io.github.lumine1909.registrylib.nms.manager;

import com.mojang.serialization.Lifecycle;
import io.github.lumine1909.registrylib.api.Holder;
import io.github.lumine1909.registrylib.api.RegistryManager;
import io.github.lumine1909.registrylib.nms.Holder_1_19_4;
import io.github.lumine1909.registrylib.util.Validator;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;

import java.util.IdentityHashMap;
import java.util.Objects;

import static io.github.lumine1909.registrylib.util.ReflectionUtil.set;

@SuppressWarnings("unchecked")
public abstract class RegistryManager_1_19_4<T> implements RegistryManager, Validator {

    protected abstract MappedRegistry<T> registry();

    protected abstract ResourceKey<Registry<T>> rootKey();

    protected abstract Class<T> clazz();

    @Override
    public Holder register(Object value, String key) {
        validateKey(key);
        validateValue(value);
        ResourceLocation location = new ResourceLocation(key);
        ResourceKey<T> resourceKey = ResourceKey.create(rootKey(), location);
        set(MappedRegistry.class, "l", registry(), false);
        set(MappedRegistry.class, "m", registry(), new IdentityHashMap<>());
        Holder holder = new Holder_1_19_4<>(registry().register(resourceKey, (T) value, Lifecycle.stable()));
        set(MappedRegistry.class, "l", registry(), true);
        set(MappedRegistry.class, "m", registry(), null);
        return holder;
    }

    @Override
    public boolean contains(Object value) {
        validateValue(value);
        return registry().getKey((T) value) != null;
    }

    @Override
    public boolean contains(String key) {
        validateKey(key);
        return registry().getOptional(ResourceLocation.tryParse(key)).isPresent();
    }

    @Override
    public T getValue(String key) {
        validateKey(key);
        return registry().getOptional(ResourceLocation.tryParse(key)).orElseThrow();
    }

    @Override
    public String getKey(Object value) {
        validateValue(value);
        return Objects.requireNonNull(registry().getKey((T) value)).toString();
    }

    @Override
    public void validateKey(String key) {
        ResourceLocation location = ResourceLocation.tryParse(key);
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

    public static class Biome extends RegistryManager_1_19_4<net.minecraft.world.level.biome.Biome> implements RegistryManager.Biome {

        private static final MappedRegistry<net.minecraft.world.level.biome.Biome> REGISTRY = (MappedRegistry<net.minecraft.world.level.biome.Biome>) MinecraftServer.getServer().registryAccess().registry(Registries.BIOME).orElseThrow();

        @Override
        protected MappedRegistry<net.minecraft.world.level.biome.Biome> registry() {
            return REGISTRY;
        }

        @Override
        protected ResourceKey<Registry<net.minecraft.world.level.biome.Biome>> rootKey() {
            return Registries.BIOME;
        }

        @Override
        protected Class<net.minecraft.world.level.biome.Biome> clazz() {
            return net.minecraft.world.level.biome.Biome.class;
        }
    }

    public static class DimensionType extends RegistryManager_1_19_4<net.minecraft.world.level.dimension.DimensionType> implements RegistryManager.DimensionType {

        private static final MappedRegistry<net.minecraft.world.level.dimension.DimensionType> REGISTRY = (MappedRegistry<net.minecraft.world.level.dimension.DimensionType>) MinecraftServer.getServer().registryAccess().registry(Registries.DIMENSION_TYPE).orElseThrow();

        @Override
        protected MappedRegistry<net.minecraft.world.level.dimension.DimensionType> registry() {
            return REGISTRY;
        }

        @Override
        protected ResourceKey<Registry<net.minecraft.world.level.dimension.DimensionType>> rootKey() {
            return Registries.DIMENSION_TYPE;
        }

        @Override
        protected Class<net.minecraft.world.level.dimension.DimensionType> clazz() {
            return net.minecraft.world.level.dimension.DimensionType.class;
        }
    }
}