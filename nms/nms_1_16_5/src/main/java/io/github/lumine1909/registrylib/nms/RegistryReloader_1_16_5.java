package io.github.lumine1909.registrylib.nms;

import io.netty.buffer.Unpooled;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;

import static io.github.lumine1909.registrylib.util.ReflectionUtil.get;
import static io.github.lumine1909.registrylib.util.ReflectionUtil.invoke;

@SuppressWarnings("all")
public class RegistryReloader_1_16_5 implements RegistryReloader {

    private static final Map<String, NetworkManager> reloadingConnections = new HashMap<>();
    private static final Map<String, EntityPlayer> reloadingPlayers = new HashMap<>();
    private static final MinecraftServer server = MinecraftServer.getServer();

    private final Plugin plugin;

    public RegistryReloader_1_16_5(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean preReload(String playerName) {
        EntityPlayer player = server.getPlayerList().getPlayer(playerName);
        if (reloadingPlayers.containsKey(player.getProfile().getName())) {
            return false;
        }
        Bukkit.getScheduler().runTask(plugin, () -> {
            PlayerConnection connection = player.playerConnection;
            WorldServer worldserver1 = player.getWorldServer();

            worldserver1.removePlayer(player);

            PlayerConnection playerconnection = new PlayerConnection(server, connection.networkManager, player);
            WorldData worlddata = worldserver1.getWorldData();

            GameRules gamerules = worldserver1.getGameRules();
            boolean flag = gamerules.getBoolean(GameRules.DO_IMMEDIATE_RESPAWN);
            boolean flag1 = gamerules.getBoolean(GameRules.REDUCED_DEBUG_INFO);
            playerconnection.sendPacket(new PacketPlayOutLogin(
                player.getId(),
                player.playerInteractManager.getGameMode(),
                player.playerInteractManager.c(),
                BiomeManager.a(worldserver1.getSeed()),
                worlddata.isHardcore(),
                server.F(),
                server.customRegistry,
                worldserver1.getDimensionManager(),
                worldserver1.getDimensionKey(),
                server.getPlayerList().getMaxPlayers(),
                worldserver1.spigotConfig.viewDistance,
                flag1,
                !flag,
                worldserver1.isDebugWorld(),
                worldserver1.isFlatWorld()
            ));
            playerconnection.sendPacket(new PacketPlayOutRespawn(worldserver1.getDimensionManager(), worldserver1.getDimensionKey(), BiomeManager.a(worldserver1.getSeed()), player.playerInteractManager.getGameMode(), player.playerInteractManager.c(), worldserver1.isDebugWorld(), worldserver1.isFlatWorld(), flag));
            player.getBukkitEntity().sendSupportedChannels();
            playerconnection.sendPacket(new PacketPlayOutCustomPayload(PacketPlayOutCustomPayload.a, (new PacketDataSerializer(Unpooled.buffer())).a(server.getServerModName())));
            playerconnection.sendPacket(new PacketPlayOutServerDifficulty(worlddata.getDifficulty(), worlddata.isDifficultyLocked()));
            playerconnection.sendPacket(new PacketPlayOutAbilities(player.abilities));
            playerconnection.sendPacket(new PacketPlayOutHeldItemSlot(player.inventory.itemInHandIndex));
            playerconnection.sendPacket(new PacketPlayOutRecipeUpdate(server.getCraftingManager().b()));
            playerconnection.sendPacket(new PacketPlayOutTags(server.getTagRegistry()));
            server.getPlayerList().d(player);
            player.getStatisticManager().c();
            player.getRecipeBook().a(player);
            server.getPlayerList().sendScoreboard(worldserver1.getScoreboard(), player);
            PacketPlayOutPlayerInfo packet = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, new EntityPlayer[]{player});

            for (int i = 0; i < server.getPlayerList().getPlayers().size(); ++i) {
                EntityPlayer entityplayer1 = server.getPlayerList().getPlayers().get(i);
                if (entityplayer1.getBukkitEntity().canSee(player.getBukkitEntity())) {
                    entityplayer1.playerConnection.sendPacket(packet);
                }

                if (player.getBukkitEntity().canSee(entityplayer1.getBukkitEntity())) {
                    player.playerConnection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, new EntityPlayer[]{entityplayer1}));
                }
            }
            player.sentListPacket = true;
            player.updateInventory(player.activeContainer);
            player.playerConnection.sendPacket(new PacketPlayOutEntityMetadata(player.getId(), player.getDataWatcher(), true));
            worldserver1 = player.getWorldServer();
            server.getPlayerList().a(player, worldserver1);
            if (!server.getResourcePack().isEmpty()) {
                player.setResourcePack(server.getResourcePack(), server.getResourcePackHash());
            }
            for (MobEffect mobeffect : player.getEffects()) {
                playerconnection.sendPacket(new PacketPlayOutEntityEffect(player.getId(), mobeffect));
            }
            postReload(playerName);
        });

        return true;
    }

    @Override
    public boolean postReload(String playerName) {
        EntityPlayer player = server.getPlayerList().getPlayer(playerName);
        player.dead = false;
        player.playerConnection.teleport(new Location(null, player.locX(), player.locY(), player.locZ(), player.yaw, player.pitch));
        player.getWorldServer().addPlayerJoin(player);
        player.getWorldServer().getChunkProvider().movePlayer(player);
        reloadingPlayers.remove(playerName);
        reloadingConnections.remove(playerName);
        return true;
    }

    @Override
    public void reloadFailed(String playerName) {
        PlayerList playerList = server.getPlayerList();
        EntityPlayer player = reloadingPlayers.get(playerName);
        if (player == null) {
            return;
        }

        invoke(PlayerList.class, "savePlayerFile", EntityPlayer.class, playerList, player);
        playerList.players.remove(player);
        ((Map<?, ?>) get(PlayerList.class, "playersByName", playerList)).remove(playerName);
        ((Map<?, ?>) get(PlayerList.class, "j", playerList)).remove(player.getUniqueID());

        reloadingConnections.remove(playerName);
        reloadingPlayers.remove(playerName);
    }

    @Override
    public void close() {
        reloadingPlayers.clear();
        reloadingConnections.clear();
    }
}