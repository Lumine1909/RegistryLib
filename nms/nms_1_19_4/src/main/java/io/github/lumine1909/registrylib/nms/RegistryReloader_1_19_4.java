package io.github.lumine1909.registrylib.nms;

import com.google.common.collect.Lists;
import io.netty.buffer.Unpooled;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.protocol.status.ServerStatus;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.players.PlayerList;
import net.minecraft.tags.TagNetworkSerialization;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.storage.LevelData;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftPlayer;
import org.bukkit.plugin.Plugin;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.github.lumine1909.registrylib.util.ReflectionUtil.get;
import static io.github.lumine1909.registrylib.util.ReflectionUtil.invoke;

@SuppressWarnings("all")
public class RegistryReloader_1_19_4 implements RegistryReloader {

    private static final Map<String, Connection> reloadingConnections = new HashMap<>();
    private static final Map<String, ServerPlayer> reloadingPlayers = new HashMap<>();
    private static final MinecraftServer server = MinecraftServer.getServer();

    private final Plugin plugin;

    public RegistryReloader_1_19_4(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean preReload(String playerName) {
        ServerPlayer player = server.getPlayerList().getPlayerByName(playerName);
        if (reloadingPlayers.containsKey(player.getGameProfile().getName())) {
            return false;
        }
        reloadingPlayers.put(playerName, player);
        reloadingConnections.put(playerName, player.connection.connection);
        Bukkit.getScheduler().runTask(plugin, () -> {
            ServerGamePacketListenerImpl connection = player.connection;
            ServerLevel worldserver1 = player.getLevel();

            worldserver1.removePlayerImmediately(player, Entity.RemovalReason.CHANGED_DIMENSION);
            LevelData worlddata = worldserver1.getLevelData();

            ServerGamePacketListenerImpl playerconnection = new ServerGamePacketListenerImpl(server, connection.connection, player);
            GameRules gamerules = worldserver1.getGameRules();
            boolean flag = gamerules.getBoolean(GameRules.RULE_DO_IMMEDIATE_RESPAWN);
            boolean flag1 = gamerules.getBoolean(GameRules.RULE_REDUCEDDEBUGINFO);
            playerconnection.send(new ClientboundLoginPacket(
                player.getId(),
                worlddata.isHardcore(),
                player.gameMode.getGameModeForPlayer(),
                player.gameMode.getPreviousGameModeForPlayer(),
                server.levelKeys(),
                server.registryAccess(),
                worldserver1.dimensionTypeId(),
                worldserver1.dimension(),
                BiomeManager.obfuscateSeed(worldserver1.getSeed()),
                server.getPlayerList().getMaxPlayers(),
                worldserver1.getChunkSource().chunkMap.playerChunkManager.getTargetSendDistance(),
                worldserver1.getChunkSource().chunkMap.playerChunkManager.getTargetTickViewDistance(),
                flag1,
                !flag,
                worldserver1.isDebug(),
                worldserver1.isFlat(),
                player.getLastDeathLocation()
            ));
            player.getBukkitEntity().sendSupportedChannels();
            playerconnection.send(new ClientboundUpdateEnabledFeaturesPacket(FeatureFlags.REGISTRY.toNames(worldserver1.enabledFeatures())));
            playerconnection.send(new ClientboundCustomPayloadPacket(ClientboundCustomPayloadPacket.BRAND, (new FriendlyByteBuf(Unpooled.buffer())).writeUtf(server.getServerModName())));
            playerconnection.send(new ClientboundChangeDifficultyPacket(worlddata.getDifficulty(), worlddata.isDifficultyLocked()));
            playerconnection.send(new ClientboundPlayerAbilitiesPacket(player.getAbilities()));
            playerconnection.send(new ClientboundSetCarriedItemPacket(player.getInventory().selected));
            playerconnection.send(new ClientboundUpdateRecipesPacket(server.getRecipeManager().getRecipes()));
            playerconnection.send(new ClientboundUpdateTagsPacket(TagNetworkSerialization.serializeTagsToNetwork(server.registries())));
            server.getPlayerList().sendPlayerPermissionLevel(player);
            player.getStats().markAllDirty();
            player.getRecipeBook().sendInitialRecipeBook(player);
            server.getPlayerList().updateEntireScoreboard(worldserver1.getScoreboard(), player);
            //player.connection.send(new ClientboundPlayerPositionPacket(player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot(), Collections.emptySet(), get(ServerGamePacketListenerImpl.class, "j", player.connection)));

            ServerStatus serverping = server.getStatus();

            if (serverping != null) {
                player.sendServerStatus(serverping);
            }

            server.getCustomBossEvents().onPlayerConnect(player);
            CraftPlayer bukkitPlayer = player.getBukkitEntity();
            player.containerMenu.transferTo(player.containerMenu, bukkitPlayer);

            ClientboundPlayerInfoUpdatePacket packet = ClientboundPlayerInfoUpdatePacket.createPlayerInitializing(List.of(player));

            final List<ServerPlayer> onlinePlayers = Lists.newArrayListWithExpectedSize(server.getPlayerList().getPlayers().size() - 1); // Paper - use single player info update packet
            for (int i = 0; i < server.getPlayerList().getPlayers().size(); ++i) {
                ServerPlayer entityplayer1 = server.getPlayerList().getPlayers().get(i);

                if (entityplayer1.getBukkitEntity().canSee(bukkitPlayer)) {
                    entityplayer1.connection.send(packet);
                }

                if (entityplayer1 == player || !bukkitPlayer.canSee(entityplayer1.getBukkitEntity())) { // Paper - don't include joining player
                    continue;
                }

                onlinePlayers.add(entityplayer1);
            }
            if (!onlinePlayers.isEmpty()) {
                player.connection.send(ClientboundPlayerInfoUpdatePacket.createPlayerInitializing(onlinePlayers));
            }

            server.getPlayerList().sendLevelInfo(player, worldserver1);

            server.getServerResourcePack().ifPresent((minecraftserver_serverresourcepackinfo) -> {
                player.sendTexturePack(minecraftserver_serverresourcepackinfo.url(), minecraftserver_serverresourcepackinfo.hash(), minecraftserver_serverresourcepackinfo.isRequired(), minecraftserver_serverresourcepackinfo.prompt());
            });
            for (MobEffectInstance mobeffect : player.getActiveEffects()) {
                playerconnection.send(new ClientboundUpdateMobEffectPacket(player.getId(), mobeffect));
            }
            postReload(playerName);
        });
        return true;
    }

    @Override
    public boolean postReload(String playerName) {
        ServerPlayer player = server.getPlayerList().getPlayerByName(playerName);
        player.unsetRemoved();
        player.connection.internalTeleport(player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot(), Collections.emptySet());
        player.getLevel().addNewPlayer(player);
        reloadingPlayers.remove(playerName);
        reloadingConnections.remove(playerName);
        return true;
    }

    @Override
    public void reloadFailed(String playerName) {
        PlayerList playerList = server.getPlayerList();
        ServerPlayer player = playerList.getPlayerByName(playerName);
        if (player == null) {
            return;
        }

        invoke(PlayerList.class, "b", ServerPlayer.class, playerList, player);
        playerList.players.remove(player);
        ((Map<?, ?>) get(PlayerList.class, "playersByName", playerList)).remove(playerName);
        ((Map<?, ?>) get(PlayerList.class, "l", playerList)).remove(player.getUUID());


        reloadingConnections.remove(playerName);
        reloadingPlayers.remove(playerName);
    }

    @Override
    public void close() {
        reloadingPlayers.clear();
        reloadingConnections.clear();
    }
}
