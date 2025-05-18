package io.github.lumine1909.registrylib.nms;

import io.github.lumine1909.registrylib.api.Holder;

public class Holder_1_19_4<T> implements Holder {

    private final net.minecraft.core.Holder<T> holder;

    public Holder_1_19_4(net.minecraft.core.Holder<T> holder) {
        this.holder = holder;
    }

    @Override
    public T value() {
        return holder.value();
    }

    @Override
    public String key() {
        return holder.unwrapKey().orElseThrow().registry().toString();
    }

    @Override
    public Object handle() {
        return holder;
    }
}
