package io.github.lumine1909.registrylib.nms;

import io.github.lumine1909.registrylib.api.Holder;

public class Holder_1_21<T> implements Holder {

    private final net.minecraft.core.Holder<T> holder;

    public Holder_1_21(net.minecraft.core.Holder<T> holder) {
        this.holder = holder;
    }

    @Override
    public T value() {
        return holder.value();
    }

    @Override
    public String key() {
        return holder.getRegisteredName();
    }

    @Override
    public Object handle() {
        return holder;
    }
}
