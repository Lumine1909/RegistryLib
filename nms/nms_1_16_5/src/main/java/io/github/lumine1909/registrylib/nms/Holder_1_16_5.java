package io.github.lumine1909.registrylib.nms;

import io.github.lumine1909.registrylib.api.Holder;
import net.minecraft.server.v1_16_R3.MinecraftKey;

public class Holder_1_16_5<T> implements Holder {

    private final T value;
    private final String key;

    public Holder_1_16_5(T handle, MinecraftKey key) {
        this.value = handle;
        this.key = key.toString();
    }

    @Override
    public T value() {
        return value;
    }

    @Override
    public String key() {
        return key;
    }

    @Override
    public Object handle() {
        return value;
    }
}
