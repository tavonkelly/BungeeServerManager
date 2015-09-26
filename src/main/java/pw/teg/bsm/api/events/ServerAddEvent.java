package pw.teg.bsm.api.events;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.plugin.Cancellable;

public class ServerAddEvent extends ServerEvent implements Cancellable {

    private CommandSender sender;
    private boolean cancelled;

    public ServerAddEvent(ServerInfo serverModified, CommandSender sender) {
        super(serverModified);
        this.sender = sender;
    }

    public CommandSender getSender() {
        return sender;
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
