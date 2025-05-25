package io.github.lumine1909.registrylib.nms;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import io.netty.channel.Channel;
import io.papermc.paper.network.ChannelInitializeListenerHolder;
import net.minecraft.network.protocol.configuration.ServerboundFinishConfigurationPacket;
import net.minecraft.network.protocol.login.ServerboundHelloPacket;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.UUID;

import static io.github.lumine1909.registrylib.common.Constants.CHANNEL_INIT_KEY;
import static io.github.lumine1909.registrylib.common.Constants.PACKET_HANDLER_NAME;

@SuppressWarnings("all")
public class ChannelInjector_1_21_3 implements ChannelInjector {

    private static final BiMap<String, Channel> MODIFIED_CHANNELS = HashBiMap.create();

    private final RegistryReloader reloader;

    private boolean isInjected;

    public ChannelInjector_1_21_3(RegistryReloader reloader) {
        this.reloader = reloader;
    }

    @Override
    public void inject() {
        if (isInjected) {
            return;
        }
        ChannelInitializeListenerHolder.addListener(CHANNEL_INIT_KEY, channel -> inject2Channel(channel, null));
        isInjected = true;
    }

    @Override
    public void uninject() {
        if (!isInjected) {
            return;
        }
        ChannelInitializeListenerHolder.removeListener(CHANNEL_INIT_KEY);
        isInjected = false;
    }

    @Override
    public void directInject(Player player) {
        Channel channel = ((CraftPlayer) player).getHandle().connection.connection.channel;
        if (channel == null) {
            return;
        }
        MODIFIED_CHANNELS.put(player.getName(), channel);
        inject2Channel(channel, player);
    }

    @Override
    public void directUninject(String playerName) {
        uninject2Channel(MODIFIED_CHANNELS.get(playerName));
    }

    @Override
    public void inject2Channel(Channel channel, Player player) {
        if (channel == null || channel.pipeline().get("packet_handler") == null) {
            return;
        }
        channel.pipeline().addBefore("packet_handler", PACKET_HANDLER_NAME, new PacketInterceptor(channel, player) {
            @Override
            public boolean inbound(Object msg) {
                if (msg instanceof ServerboundHelloPacket(String name, UUID uuid)) {
                    this.playerName = name;
                    MODIFIED_CHANNELS.put(name, channel);
                }
                if (msg instanceof ServerboundFinishConfigurationPacket && reloader.postReload(playerName)) {
                    return false;
                }
                if (!reloader.onPacketReceive(playerName, msg)) {
                    return false;
                }
                return true;
            }

            @Override
            public boolean outbound(Object msg) {
                if (!reloader.onPacketSend(playerName, msg)) {
                    return false;
                }
                return true;
            }
        });
    }

    @Override
    public void uninject2Channel(Channel channel) {
        if (channel == null) {
            return;
        }
        if (channel.pipeline().names().contains(PACKET_HANDLER_NAME)) {
            channel.pipeline().remove(PACKET_HANDLER_NAME);
        }
        MODIFIED_CHANNELS.inverse().remove(channel);
    }

    @Override
    public void close() {
        uninject();
        for (Channel channel : new HashSet<>(MODIFIED_CHANNELS.values())) {
            uninject2Channel(channel);
        }
    }
}
