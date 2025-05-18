package io.github.lumine1909.registrylib.nms;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Lifecycle;
import io.github.lumine1909.registrylib.api.RegistryType;
import io.github.lumine1909.registrylib.event.RegisterEvent;
import io.github.lumine1909.registrylib.event.RegisterFinishEvent;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import static io.github.lumine1909.registrylib.util.ReflectionUtil.get;
import static io.github.lumine1909.registrylib.util.ReflectionUtil.set;

public class RegistryInjector_1_19_4 implements RegistryInjector {

    private static final MinecraftServer server = MinecraftServer.getServer();
    private static final RegistryAccess.ImmutableRegistryAccess frozen = (RegistryAccess.ImmutableRegistryAccess) server.registryAccess();

    private Map<? extends ResourceKey<? extends Registry<?>>, ? extends Registry<?>> originalRegistries;
    private boolean isInjected;

    @Override
    public void inject() {
        if (isInjected) {
            return;
        }
        originalRegistries = get(RegistryAccess.ImmutableRegistryAccess.class, "c", frozen);
        Map<ResourceKey<? extends Registry<?>>, Registry<?>> registries = new HashMap<>(originalRegistries);
        registries.put(Registries.BIOME, injectSingle(RegistryType.BIOME, Registries.BIOME));
        registries.put(Registries.DIMENSION_TYPE, injectSingle(RegistryType.DIMENSION_TYPE, Registries.DIMENSION_TYPE));
        set(RegistryAccess.ImmutableRegistryAccess.class, "c", frozen, ImmutableMap.copyOf(registries));
        isInjected = true;
    }

    @Override
    public void uninject() {
        if (!isInjected) {
            isInjected = true;
        }
        set(RegistryAccess.ImmutableRegistryAccess.class, "c", frozen, originalRegistries);
        originalRegistries = null;
        isInjected = false;
    }

    @SuppressWarnings("unchecked")
    private <T> Registry<T> injectSingle(RegistryType type, ResourceKey<Registry<T>> resourceKey) {
        MappedRegistry<T> original = (MappedRegistry<T>) frozen.registry(resourceKey).orElseThrow();
        return (Registry<T>) Proxy.newProxyInstance(
            getClass().getClassLoader(),
            new Class[]{WritableRegistry.class},
            (proxy, method, args) -> {
                if (!("a".equals(method.getName()) && method.getParameterCount() == 3 && method.getReturnType() == Holder.Reference.class)) {
                    return method.invoke(original, args);
                }
                ResourceKey<T> key = (ResourceKey<T>) args[0];
                T value = (T) args[1];
                RegisterEvent event = new RegisterEvent(type, key.registry().toString(), value);
                event.callEvent();
                var newKey = ResourceKey.create(resourceKey, new ResourceLocation(event.getKey()));
                var newValue = (T) event.getValue();
                if (!key.equals(newValue) || !value.equals(newValue)) {
                    Lifecycle lifecycle = (Lifecycle) args[2];
                    args = new Object[]{newKey, newValue, lifecycle};
                }
                Holder<T> holder = (Holder<T>) method.invoke(original, args);
                new RegisterFinishEvent(type, new Holder_1_19_4<>(holder));
                return holder;
            }
        );
    }
}
