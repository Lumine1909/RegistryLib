package io.github.lumine1909.registrylib.event;

import io.github.lumine1909.registrylib.api.Holder;
import io.github.lumine1909.registrylib.api.RegistryType;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class RegisterFinishEvent extends RegisterEvent {

    public static final HandlerList HANDLERS = new HandlerList();

    private final Holder holder;

    public RegisterFinishEvent(RegistryType registryType, Holder holder) {
        super(registryType, holder.key(), holder.value());
        this.holder = holder;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public Holder getHolder() {
        return holder;
    }

    @Override
    public void setKey(String key) {
        throw new UnsupportedOperationException("You can't change the key after register is finished.");
    }

    @Override
    public void setValue(Object value) {
        throw new UnsupportedOperationException("You can't change the value after register is finished.");
    }
}
