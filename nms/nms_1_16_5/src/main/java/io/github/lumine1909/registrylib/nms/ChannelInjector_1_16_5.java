package io.github.lumine1909.registrylib.nms;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import io.github.lumine1909.registrylib.event.ConnectionCloseEvent;
import io.netty.channel.*;
import net.minecraft.server.v1_16_R3.MinecraftServer;
import net.minecraft.server.v1_16_R3.PacketLoginInStart;
import net.minecraft.server.v1_16_R3.ServerConnection;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static io.github.lumine1909.registrylib.common.Constants.PACKET_HANDLER_NAME;
import static io.github.lumine1909.registrylib.util.ReflectionUtil.get;

@SuppressWarnings("unchecked")
public class ChannelInjector_1_16_5 implements ChannelInjector {

    private static final BiMap<String, Channel> MODIFIED_CHANNELS = HashBiMap.create();
    private static final Map<Channel, String> CHANNEL_CACHE = new HashMap<>();

    private ChannelFuture connectionListener;
    private boolean isInjected;

    public ChannelInjector_1_16_5(RegistryReloader reloader) {
    }

    @Override
    public void inject() {
        if (isInjected) {
            return;
        }
        connectionListener = ((List<ChannelFuture>) get(ServerConnection.class, "listeningChannels", MinecraftServer.getServer().getServerConnection())).get(0);
        connectionListener.channel().pipeline().addFirst("inject_handler", new ChannelInboundHandlerAdapter() {
            @Override
            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                if (msg instanceof Channel) {
                    ((Channel) msg).pipeline().addLast(new ChannelInboundHandlerAdapter() {
                        @Override
                        public void channelActive(ChannelHandlerContext ctx) throws Exception {
                            Channel channel = ctx.channel();
                            channel.closeFuture().addListener(future -> Bukkit.getPluginManager().callEvent(new ConnectionCloseEvent(CHANNEL_CACHE.get(channel))));
                            inject2Channel(ctx.channel(), null);
                            channel.pipeline().remove(this);
                            super.channelActive(ctx);
                        }
                    });
                }
                super.channelRead(ctx, msg);
            }
        });
        isInjected = true;
    }

    @Override
    public void uninject() {
        if (!isInjected) {
            return;
        }
        connectionListener.channel().pipeline().remove("inject_handler");
        isInjected = false;
    }

    @Override
    public void directInject(Player player) {
        Channel channel = ((CraftPlayer) player).getHandle().playerConnection.networkManager.channel;
        if (channel == null) {
            return;
        }
        MODIFIED_CHANNELS.put(player.getName(), channel);
        CHANNEL_CACHE.put(channel, player.getName());
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
        if (player != null) {
            CHANNEL_CACHE.put(channel, player.getName());
        }
        channel.pipeline().addBefore("packet_handler", PACKET_HANDLER_NAME, new PacketInterceptor(channel, player) {
            @Override
            public boolean inbound(Object msg) {
                if (msg instanceof PacketLoginInStart) {
                    this.playerName = ((PacketLoginInStart) msg).b().getName();
                    MODIFIED_CHANNELS.put(playerName, channel);
                    CHANNEL_CACHE.put(channel, playerName);
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
        CHANNEL_CACHE.clear();
    }
}