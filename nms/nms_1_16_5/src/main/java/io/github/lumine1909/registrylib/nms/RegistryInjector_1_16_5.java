package io.github.lumine1909.registrylib.nms;

import com.mojang.serialization.Lifecycle;
import io.github.lumine1909.registrylib.api.RegistryType;
import io.github.lumine1909.registrylib.event.RegisterEvent;
import io.github.lumine1909.registrylib.event.RegisterFinishEvent;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.Bukkit;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static io.github.lumine1909.registrylib.util.ReflectionUtil.get;
import static io.github.lumine1909.registrylib.util.ReflectionUtil.set;

@SuppressWarnings("all")
public class RegistryInjector_1_16_5 implements RegistryInjector {

    private static final MinecraftServer server = MinecraftServer.getServer();
    private static final IRegistryCustom.Dimension frozen = (IRegistryCustom.Dimension) server.customRegistry;
    private Map<? extends ResourceKey<? extends IRegistry<?>>, ? extends IRegistry<?>> originalRegistries;
    private boolean isInjected;

    @Override
    public void inject() {
        if (isInjected) {
            return;
        }
        originalRegistries = get(IRegistryCustom.Dimension.class, "b", frozen);
        Map<ResourceKey<? extends IRegistry<?>>, IRegistry<?>> registries = new HashMap<>(originalRegistries);
        registries.put(IRegistry.ay, injectSingle(RegistryType.BIOME, IRegistry.ay));
        registries.put(IRegistry.K, injectSingle(RegistryType.DIMENSION_TYPE, IRegistry.K));
        set(IRegistryCustom.Dimension.class, "b", frozen, new HashMap<>(registries));
        isInjected = true;
    }

    @Override
    public void uninject() {
        if (!isInjected) {
            return;
        }
        set(IRegistryCustom.Dimension.class, "b", frozen, originalRegistries);
        originalRegistries = null;
        isInjected = false;
    }

    @SuppressWarnings("unchecked")
    private <T> IRegistry<T> injectSingle(RegistryType type, ResourceKey<IRegistry<T>> resourceKey) {
        RegistryMaterials<T> original = (RegistryMaterials<T>) Objects.requireNonNull(frozen.a(resourceKey).get());
        return (IRegistry<T>) Proxy.newProxyInstance(
            getClass().getClassLoader(),
            new Class[]{IRegistryWritable.class},
            (proxy, method, args) -> {
                if (!"a".equals(method.getName()) && method.getParameterCount() == 3 && method.getReturnType() == Object.class) {
                    return method.invoke(original, args);
                }
                ResourceKey<T> key = (ResourceKey<T>) args[0];
                T value = (T) args[1];
                RegisterEvent event = new RegisterEvent(type, key.a().toString(), value);
                Bukkit.getPluginManager().callEvent(event);
                ResourceKey<T> newKey = ResourceKey.a(resourceKey, new MinecraftKey(event.getKey()));
                T newValue = (T) event.getValue();
                if (!key.equals(newValue) || !value.equals(newValue)) {
                    Lifecycle lifecycle = (Lifecycle) args[2];
                    args = new Object[]{newKey, newValue, lifecycle};
                }
                Object object = method.invoke(original, args);
                new RegisterFinishEvent(type, new Holder_1_16_5<>(object, newKey.a()));
                return object;
            }
        );
    }
}
