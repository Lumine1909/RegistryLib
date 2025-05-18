package io.github.lumine1909.registrylib.nms;

import io.github.lumine1909.registrylib.common.Closable;

public interface RegistryInjector extends Closable {

    RegistryInjector EMPTY = new RegistryInjector() {
        @Override
        public void inject() {

        }

        @Override
        public void uninject() {

        }
    };

    void inject();

    void uninject();

    @Override
    default void close() {
        uninject();
    }
}
