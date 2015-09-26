package pw.teg.bsm.api.events;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.plugin.Cancellable;

public class ServerModifiedEvent<T> extends ServerEvent implements Cancellable {

    private CommandSender sender;
    private ServerField modified;
    private T newValue;
    private boolean cancelled;

    public ServerModifiedEvent(ServerInfo serverModified, CommandSender sender, ServerField modified, T newValue) {
        super(serverModified);
        this.sender = sender;
        this.modified = modified;
        this.newValue = newValue;
    }

    public CommandSender getSender() {
        return sender;
    }

    public ServerField getModified() {
        return modified;
    }

    public T getNewValue() {
        return newValue;
    }

    public void setNewValue(T newValue) {
        this.newValue = newValue;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public enum ServerField {
        NAME, IP, MOTD
    }
}
