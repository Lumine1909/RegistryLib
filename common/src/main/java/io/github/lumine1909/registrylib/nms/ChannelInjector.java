package io.github.lumine1909.registrylib.nms;

import io.github.lumine1909.registrylib.common.Closable;
import io.netty.channel.Channel;
import org.bukkit.entity.Player;

public interface ChannelInjector extends Closable {

    ChannelInjector EMPTY = new ChannelInjector() {

        @Override
        public void inject() {

        }

        @Override
        public void uninject() {

        }

        @Override
        public void directInject(Player player) {

        }

        @Override
        public void directUninject(String playerName) {

        }

        @Override
        public void inject2Channel(Channel channel, Player player) {

        }

        @Override
        public void uninject2Channel(Channel channel) {

        }
    };

    void inject();

    void uninject();

    void directInject(Player player);

    void directUninject(String playerName);

    void inject2Channel(Channel channel, Player player);

    void uninject2Channel(Channel channel);

    @Override
    default void close() {
        uninject();
    }
}
