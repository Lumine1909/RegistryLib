package io.github.lumine1909.registrylib.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

// This is an internal event for handle disconnect in Spigot 1.16.5
// It will not be fired if in Paper server
// It is preserved in "API" since might be useful for Spigot server Registry related dev
public class ConnectionCloseEvent extends Event {

    public static final HandlerList HANDLERS = new HandlerList();

    private final String playerName;

    public ConnectionCloseEvent(@NotNull String playerName) {
        super(true);
        this.playerName = playerName;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public String getPlayerName() {
        return playerName;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }
}
