package io.github.lumine1909.registrylib.nms;

import io.github.lumine1909.registrylib.common.Closable;

@SuppressWarnings("all")
public interface RegistryReloader extends Closable {

    RegistryReloader EMPTY = new RegistryReloader() {

        @Override
        public void close() {

        }

        @Override
        public boolean preReload(String playerName) {
            return false;
        }

        @Override
        public boolean postReload(String playerName) {
            return false;
        }

        @Override
        public void reloadFailed(String playerName) {

        }
    };

    boolean preReload(String playerName);

    boolean postReload(String playerName);

    void reloadFailed(String playerName);

    default boolean onPacketSend(String playerName, Object packet) {
        return true;
    }

    default boolean onPacketReceive(String playerName, Object packet) {
        return true;
    }
}
