package io.github.lumine1909.registrylib.event;

import io.github.lumine1909.registrylib.api.RegistryType;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class RegisterEvent extends Event implements Cancellable {

    public static final HandlerList HANDLERS = new HandlerList();

    public final RegistryType registryType;
    public String key;
    public Object value;

    public boolean cancelled = false;

    public RegisterEvent(RegistryType registryType, String key, Object value) {
        this.registryType = registryType;
        this.key = key;
        this.value = value;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public RegistryType getRegistryType() {
        return registryType;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
