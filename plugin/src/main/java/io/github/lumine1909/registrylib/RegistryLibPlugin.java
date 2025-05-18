package io.github.lumine1909.registrylib;

import io.github.lumine1909.registrylib.api.RegistryAPI;
import io.github.lumine1909.registrylib.api.RegistryManager;
import io.github.lumine1909.registrylib.command.RegistryLibCommand;
import io.github.lumine1909.registrylib.command.ReloadRegistryCommand;
import io.github.lumine1909.registrylib.event.RegistryReloadEvent;
import io.github.lumine1909.registrylib.listener.ConnectionCloseListener;
import io.github.lumine1909.registrylib.nms.*;
import io.github.lumine1909.registrylib.nms.manager.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import static io.github.lumine1909.registrylib.common.Constants.MC_VER;

public class RegistryLibPlugin extends JavaPlugin implements RegistryAPI {

    public static RegistryLibPlugin plugin;

    public boolean ENABLE_EVENT;
    public boolean ENABLE_RELOAD;

    private ChannelInjector channelInjector = ChannelInjector.EMPTY;
    private RegistryInjector registryInjector = RegistryInjector.EMPTY;
    private RegistryReloader registryReloader = RegistryReloader.EMPTY;
    private ManagerHolder managerHolder;

    @Override
    public void onLoad() {
        plugin = this;
        Bukkit.getServicesManager().register(RegistryAPI.class, this, this, ServicePriority.Highest);
        saveDefaultConfig();
    }

    @Override
    public void onEnable() {
        callReload();
        registerCommand();
        registerListeners();
    }

    public void registerCommand() {
        new RegistryLibCommand();
        new ReloadRegistryCommand();
    }

    public void registerListeners() {
        new ConnectionCloseListener();
    }

    public void callReload() {
        registryInjector.close();
        channelInjector.close();
        registryReloader.close();
        reloadConfig();
        ENABLE_EVENT = getConfig().getBoolean("enable-registry-change-event");
        ENABLE_RELOAD = getConfig().getBoolean("enable-registry-reload");
        if (MC_VER >= 2105) {
            registryInjector = new RegistryInjector_1_21_5();
            registryReloader = new RegistryReloader_1_21_5(this);
            channelInjector = new ChannelInjector_1_21_5(registryReloader);
            managerHolder = new ManagerHolder(
                new RegistryManager_1_21_5.Biome(),
                new RegistryManager_1_21_5.DimensionType()
            );
        }
        if (MC_VER >= 2103) {
            registryInjector = new RegistryInjector_1_21_3();
            registryReloader = new RegistryReloader_1_21_3(this);
            channelInjector = new ChannelInjector_1_21_3(registryReloader);
            managerHolder = new ManagerHolder(
                new RegistryManager_1_21_3.Biome(),
                new RegistryManager_1_21_3.DimensionType()
            );
        } else if (MC_VER >= 2100) {
            registryInjector = new RegistryInjector_1_21();
            registryReloader = new RegistryReloader_1_21(this);
            channelInjector = new ChannelInjector_1_21(registryReloader);
            managerHolder = new ManagerHolder(
                new RegistryManager_1_21.Biome(),
                new RegistryManager_1_21.DimensionType()
            );
        } else if (MC_VER >= 1903) {
            registryInjector = new RegistryInjector_1_19_4();
            registryReloader = new RegistryReloader_1_19_4(this);
            channelInjector = new ChannelInjector_1_19_4(registryReloader);
            managerHolder = new ManagerHolder(
                new RegistryManager_1_19_4.Biome(),
                new RegistryManager_1_19_4.DimensionType()
            );
        } else if (MC_VER >= 1603) {
            registryInjector = new RegistryInjector_1_16_5();
            registryReloader = new RegistryReloader_1_16_5(this);
            channelInjector = new ChannelInjector_1_16_5(registryReloader);
            managerHolder = new ManagerHolder(
                new RegistryManager_1_16_5.Biome(),
                new RegistryManager_1_16_5.DimensionType()
            );
        } else {
            throw new IllegalStateException("RegistryLibPlugin doesn't support current version, please use: 1.16.5, 1.19.4, or 1.21-1.21.5");
        }
        if (ENABLE_EVENT) {
            registryInjector.inject();
        }
        channelInjector.inject();
        for (Player player : Bukkit.getOnlinePlayers()) {
            channelInjector.directInject(player);
        }
    }

    @Override
    public void onDisable() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            channelInjector.directUninject(player.getName());
        }
        registryInjector.close();
        channelInjector.close();
        registryReloader.close();
    }

    @Override
    public RegistryManager.Biome biomeManager() {
        return managerHolder.biome();
    }

    @Override
    public RegistryManager.DimensionType dimensionTypeManager() {
        return managerHolder.dimensionType();
    }

    @Override
    public void reloadPlayer(Player player) {
        if (!ENABLE_RELOAD || !new RegistryReloadEvent(player).callEvent()) {
            return;
        }
        registryReloader.preReload(player.getName());
    }

    public RegistryReloader getRegistryReloader() {
        return registryReloader;
    }

    public ChannelInjector getChannelInjector() {
        return channelInjector;
    }
}
