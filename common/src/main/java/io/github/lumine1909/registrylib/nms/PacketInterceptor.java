package io.github.lumine1909.registrylib.nms;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import org.bukkit.entity.Player;

public abstract class PacketInterceptor extends ChannelDuplexHandler {

    protected String playerName;

    public PacketInterceptor(Channel channel, Player player) {
        if (player != null) {
            playerName = player.getName();
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!inbound(msg)) {
            return;
        }
        super.channelRead(ctx, msg);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (!outbound(msg)) {
            return;
        }
        super.write(ctx, msg, promise);
    }

    public boolean inbound(Object msg) {
        return true;
    }

    public boolean outbound(Object msg) {
        return true;
    }
}
