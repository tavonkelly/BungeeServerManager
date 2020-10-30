package pw.teg.bsm.api;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import pw.teg.bsm.api.events.ServerAddEvent;
import pw.teg.bsm.api.events.ServerRemoveEvent;
import pw.teg.bsm.util.ServerHelper;

import java.net.InetSocketAddress;
import java.util.Collection;

public class ServerManagerAPI {

    public boolean addServer(String name, InetSocketAddress ipAddress, String motd, boolean restricted) {
        if (ServerHelper.getServerInfo(name) != null) {
            return false;
        }

        ServerInfo serverInfo = ProxyServer.getInstance().constructServerInfo(name, ipAddress, motd, restricted);
        ServerAddEvent addEvent = new ServerAddEvent(serverInfo, null);

        if (addEvent.isCancelled()) {
            return false;
        }

        ServerHelper.addServer(addEvent.getServerModified());
        return true;
    }

    public boolean removeServer(String name) {
        ServerInfo serverInfo = ServerHelper.getServerInfo(name);

        if (serverInfo == null) {
            return false;
        }

        ServerRemoveEvent addEvent = new ServerRemoveEvent(serverInfo, null);

        if (addEvent.isCancelled()) {
            return false;
        }

        ServerHelper.removeServer(addEvent.getServerModified().getName());
        return true;
    }

    public boolean serverExists(String name) {
        return ServerHelper.serverExists(name);
    }

    public Collection<ServerInfo> listServers() {
        return ServerHelper.getServers().values();
    }
}
