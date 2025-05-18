package io.github.lumine1909.registrylib.nms;

import com.google.common.collect.Lists;
import net.minecraft.network.Connection;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.configuration.ConfigurationProtocols;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ConfigurationTask;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.network.config.JoinWorldTask;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.storage.LevelData;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.LockSupport;

import static io.github.lumine1909.registrylib.util.ReflectionUtil.*;

@SuppressWarnings("all")
public class RegistryReloader_1_21_3 implements RegistryReloader {

    private static final Map<String, Connection> reloadingConnections = new HashMap<>();
    private static final Map<String, ServerPlayer> reloadingPlayers = new HashMap<>();
    private static final Map<String, List<Packet<?>>> pendingMessages = new HashMap<>();
    private static final Executor executor = Executors.newVirtualThreadPerTaskExecutor();
    private static final MinecraftServer server = MinecraftServer.getServer();

    private final Plugin plugin;

    public RegistryReloader_1_21_3(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean preReload(String playerName) {
        ServerPlayer player = server.getPlayerList().getPlayerByName(playerName);
        if (reloadingPlayers.containsKey(player.getGameProfile().getName())) {
            return false;
        }
        Bukkit.getScheduler().runTask(plugin, () -> {
            ServerGamePacketListenerImpl connection = player.connection;
            player.serverLevel().removePlayerImmediately(player, Entity.RemovalReason.CHANGED_DIMENSION);
            set(ServerGamePacketListenerImpl.class, "waitingForSwitchToConfig", connection, true);
            connection.send(ClientboundStartConfigurationPacket.INSTANCE);
            connection.connection.setupOutboundProtocol(ConfigurationProtocols.CLIENTBOUND);
            reloadingPlayers.put(playerName, player);
            reloadingConnections.put(playerName, connection.connection);
            pendingMessages.put(playerName, new ArrayList<>());
            executor.execute(() -> {
                while (!(connection.connection.getPacketListener() instanceof ServerConfigurationPacketListenerImpl configurationPacketListener)) {
                    LockSupport.parkNanos(1000);
                }
                Bukkit.getScheduler().runTask(plugin, () -> {
                    configurationPacketListener.startConfiguration();
                    player.unsetRemoved();
                });
            });
        });
        return true;
    }

    @Override
    public boolean postReload(String playerName) {
        if (!reloadingPlayers.containsKey(playerName)) {
            return false;
        }
        Bukkit.getScheduler().runTask(plugin, () -> {
            Connection connection = reloadingConnections.get(playerName);
            ServerPlayer player = reloadingPlayers.get(playerName);
            invoke(
                ServerConfigurationPacketListenerImpl.class,
                "finishCurrentTask",
                ConfigurationTask.Type.class,
                connection.getPacketListener(),
                JoinWorldTask.TYPE
            );

            ServerGamePacketListenerImpl serverGamePacketListenerImpl = new ServerGamePacketListenerImpl(
                MinecraftServer.getServer(),
                connection,
                player,
                invoke(ServerCommonPacketListenerImpl.class, "createCookie", ClientInformation.class, player.connection, player.clientInformation())
            );
            player.connection = serverGamePacketListenerImpl;
            connection.setupOutboundProtocol(GameProtocols.CLIENTBOUND_TEMPLATE.bind(RegistryFriendlyByteBuf.decorator(MinecraftServer.getServer().registryAccess())));
            connection.setupInboundProtocol(
                GameProtocols.SERVERBOUND_TEMPLATE.bind(RegistryFriendlyByteBuf.decorator(MinecraftServer.getServer().registryAccess())),
                player.connection
            );
            reloadingPlayers.remove(playerName);
            reloadingConnections.remove(playerName);
            LevelData levelData = player.serverLevel().levelData;
            GameRules gameRules = player.serverLevel().getGameRules();
            boolean _boolean = gameRules.getBoolean(GameRules.RULE_DO_IMMEDIATE_RESPAWN);
            boolean _boolean1 = gameRules.getBoolean(GameRules.RULE_REDUCEDDEBUGINFO);
            boolean _boolean2 = gameRules.getBoolean(GameRules.RULE_LIMITED_CRAFTING);
            player.connection.send(new ClientboundLoginPacket(
                player.getId(),
                levelData.isHardcore(),
                MinecraftServer.getServer().levelKeys(),
                MinecraftServer.getServer().getPlayerList().getMaxPlayers(),
                player.serverLevel().spigotConfig.viewDistance,// Spigot - view distance
                player.serverLevel().spigotConfig.simulationDistance,
                _boolean1,
                !_boolean,
                _boolean2,
                player.createCommonSpawnInfo(player.serverLevel()),
                MinecraftServer.getServer().enforceSecureProfile()
            ));
            PlayerList playerList = MinecraftServer.getServer().getPlayerList();
            playerList.sendPlayerPermissionLevel(player);
            player.getStats().markAllDirty();
            final List<ServerPlayer> onlinePlayers = Lists.newArrayListWithExpectedSize(playerList.players.size() - 1);
            onlinePlayers.addAll(playerList.players);
            if (!onlinePlayers.isEmpty()) {
                player.connection.send(ClientboundPlayerInfoUpdatePacket.createPlayerInitializing(onlinePlayers, player));
            }
            playerList.sendLevelInfo(player, player.serverLevel());
            playerList.sendAllPlayerInfo(player);
            player.onUpdateAbilities();
            playerList.sendActivePlayerEffects(player);
            playerList.updateEntireScoreboard(player.serverLevel().getScoreboard(), player);
            player.connection.send(ClientboundPlayerPositionPacket.of(player.getId(), PositionMoveRotation.of(player), Collections.emptySet()));

            player.serverLevel().addNewPlayer(player);
            MinecraftServer.getServer().getCustomBossEvents().onPlayerConnect(player);
            for (Packet<?> packet : pendingMessages.get(playerName)) {
                player.connection.send(packet);
            }
            pendingMessages.remove(playerName);
        });
        return true;
    }

    @Override
    public boolean onPacketSend(String playerName, Object packet) {
        if (reloadingPlayers.containsKey(playerName) && packet instanceof Packet<?> && !(packet.getClass().getName().contains(".common.") || packet.getClass().getName().contains(".configuration."))) {
            pendingMessages.get(playerName).add((Packet<?>) packet);
            return false;
        }
        return true;
    }

    @Override
    public void reloadFailed(String playerName) {
        PlayerList playerList = server.getPlayerList();
        ServerPlayer player = reloadingPlayers.get(playerName);
        if (player == null) {
            return;
        }

        invoke(PlayerList.class, "save", ServerPlayer.class, playerList, player);
        playerList.players.remove(player);
        ((Map<?, ?>) get(PlayerList.class, "playersByName", playerList)).remove(playerName);
        ((Map<?, ?>) get(PlayerList.class, "playersByUUID", playerList)).remove(player.getUUID());

        reloadingConnections.remove(playerName);
        reloadingPlayers.remove(playerName);
        pendingMessages.remove(playerName);
    }

    @Override
    public void close() {
        reloadingPlayers.clear();
        reloadingConnections.clear();
        pendingMessages.clear();
    }
}