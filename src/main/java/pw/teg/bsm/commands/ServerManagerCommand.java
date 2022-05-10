package pw.teg.bsm.commands;

import com.google.common.base.Joiner;
import net.md_5.bungee.Util;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.api.config.ListenerInfo;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.plugin.Command;
import pw.teg.bsm.BungeeServerManager;
import pw.teg.bsm.api.events.ServerAddEvent;
import pw.teg.bsm.api.events.ServerModifiedEvent;
import pw.teg.bsm.api.events.ServerRemoveEvent;
import pw.teg.bsm.util.ConfigHelper;
import pw.teg.bsm.util.ServerHelper;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.*;

public class ServerManagerCommand extends Command {

    private final String prefix = ChatColor.GRAY + "[" + ChatColor.GREEN + "ServerManager" + ChatColor.GRAY + "] ";

    public ServerManagerCommand() {
        super("servermanager", "servermanager.use", "svm");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return;
        }

        if (args[0].equalsIgnoreCase("help")) {
            sendHelp(sender);
            return;
        }

        if (args[0].equalsIgnoreCase("list")) {
            if (ServerHelper.getServers().isEmpty()) {
                sender.sendMessage(TextComponent.fromLegacyText(prefix + "No servers."));
                return;
            }

            sender.sendMessage(TextComponent.fromLegacyText(prefix + "Servers: " + ChatColor.GREEN + Joiner.on(ChatColor.GRAY + ", " + ChatColor.GREEN).join(ServerHelper.getServers().keySet())));
            return;
        }

        if (args[0].equalsIgnoreCase("info")) {
            if (args.length < 2) {
                sendUsage(sender, "/svm info <server>");
                return;
            }

            if (!ServerHelper.serverExists(args[1])) {
                sender.sendMessage(TextComponent.fromLegacyText(prefix + "The server " + ChatColor.GREEN + args[1] + ChatColor.GRAY + " does not exist."));
                return;
            }

            ServerInfo info = ServerHelper.getServerInfo(args[1]);

            if (info == null) {
                sender.sendMessage(TextComponent.fromLegacyText(prefix + "The server " + ChatColor.GREEN + args[1] + ChatColor.GRAY + " does not exist."));
                return;
            }

            Set<String> forcedHosts = new HashSet<>();

            for (ListenerInfo listenerInfo : BungeeServerManager.get().getProxy().getConfig().getListeners()) {
                for (Map.Entry<String, String> entry : listenerInfo.getForcedHosts().entrySet()) {
                    if (entry.getValue().equalsIgnoreCase(info.getName())) {
                        forcedHosts.add(entry.getKey());
                    }
                }
            }

            sender.sendMessage(TextComponent.fromLegacyText(ChatColor.GRAY + "--- " + ChatColor.GREEN + info.getName() + " Info" + ChatColor.GRAY + " ---"));
            sender.sendMessage(TextComponent.fromLegacyText(ChatColor.GREEN + "Name: " + ChatColor.GRAY + info.getName()));
            sender.sendMessage(TextComponent.fromLegacyText(ChatColor.GREEN + "Address: " + ChatColor.GRAY + ConfigHelper.socketAddressToString(info.getSocketAddress())));
            sender.sendMessage(TextComponent.fromLegacyText(ChatColor.GREEN + "Motd: " + ChatColor.GRAY + info.getMotd()));
            sender.sendMessage(TextComponent.fromLegacyText(ChatColor.GREEN + "Restricted: " + ChatColor.GRAY + info.isRestricted()));
            sender.sendMessage(TextComponent.fromLegacyText(ChatColor.GREEN + "Player Count: " + ChatColor.GRAY + info.getPlayers().size()));
            if (!forcedHosts.isEmpty()) {
                sender.sendMessage(TextComponent.fromLegacyText(ChatColor.GREEN + "Forced Hosts: " + ChatColor.GRAY + Joiner.on(", ").join(forcedHosts)));
            }
            sender.sendMessage(TextComponent.fromLegacyText(ChatColor.GRAY + "--------"));
            return;
        }

        if (args[0].equalsIgnoreCase("add")) {
            if (args.length < 2) {
                sendUsage(sender, "/svm add <server> [hostname]");
                return;
            }

            if (ServerHelper.serverExists(args[1])) {
                sender.sendMessage(TextComponent.fromLegacyText(prefix + "The server " + ChatColor.GREEN + args[1] + ChatColor.GRAY + " already exists."));
                return;
            }

            SocketAddress address = new InetSocketAddress(22565);
            boolean customAddress = false;

            if (args.length >= 3) {
                address = getIp(args[2]);
                customAddress = true;

                if (address == null) {
                    sender.sendMessage(TextComponent.fromLegacyText(prefix + "Invalid address " + ChatColor.GREEN + args[2] + ChatColor.GRAY + ". Here's an example: " + ChatColor.GREEN + "127.0.0.1:25565"));
                    return;
                }
            }

            ServerInfo info = ProxyServer.getInstance().constructServerInfo(args[1], address, "", false);
            ServerAddEvent addEvent = new ServerAddEvent(info, sender);

            BungeeServerManager.get().getProxy().getPluginManager().callEvent(addEvent);

            if (addEvent.isCancelled()) {
                return;
            }

            ServerHelper.addServer(addEvent.getServerModified());
            sender.sendMessage(TextComponent.fromLegacyText(prefix + "Added a server with the name " +
                    ChatColor.GREEN + addEvent.getServerModified().getName() + (customAddress ? " and address " +
                    ChatColor.GREEN + args[2] + ChatColor.GREEN + "." : "")));
            return;
        }

        if (args[0].equalsIgnoreCase("remove")) {
            if (args.length < 2) {
                sendUsage(sender, "/svm remove <server>");
                return;
            }

            if (!ServerHelper.serverExists(args[1])) {
                sender.sendMessage(TextComponent.fromLegacyText(prefix + "The server " + ChatColor.GREEN + args[1] + ChatColor.GRAY + " does not exist."));
                return;
            }

            ServerRemoveEvent removeEvent = new ServerRemoveEvent(ServerHelper.getServerInfo(args[1]), sender);

            BungeeServerManager.get().getProxy().getPluginManager().callEvent(removeEvent);

            if (removeEvent.isCancelled()) {
                return;
            }

            ServerHelper.removeServer(removeEvent.getServerModified().getName());
            sender.sendMessage(TextComponent.fromLegacyText(prefix + "Removed the server " + ChatColor.GREEN + removeEvent.getServerModified().getName()));
            return;
        }

        if (args[0].equalsIgnoreCase("edit")) {
            if (args.length < 2) {
                sendUsage(sender, "/svm edit <server>");
                return;
            }

            ServerInfo info = ServerHelper.getServerInfo(args[1]);

            if (info == null) {
                sender.sendMessage(TextComponent.fromLegacyText(prefix + "The server " + ChatColor.GREEN + args[1] + ChatColor.GRAY + " does not exist."));
                return;
            }

            if (args.length == 2) {
                sendEditMenu(sender, info.getName());
                return;
            }

            if (args[2].equalsIgnoreCase("name")) {
                if (args.length < 4) {
                    sendUsage(sender, "/svm edit " + info.getName() + " name <name>");
                    return;
                }

                ServerModifiedEvent<String> modifiedEvent = new ServerModifiedEvent<>(info, sender, ServerModifiedEvent.ServerField.NAME, args[3]);

                BungeeServerManager.get().getProxy().getPluginManager().callEvent(modifiedEvent);

                if (modifiedEvent.isCancelled()) {
                    return;
                }

                info = modifiedEvent.getServerModified();

                ServerHelper.removeServer(info.getName());
                ServerHelper.addServer(ProxyServer.getInstance().constructServerInfo(modifiedEvent.getNewValue(), info.getSocketAddress(), info.getMotd(), false));
                sender.sendMessage(TextComponent.fromLegacyText(prefix + "Renamed " + ChatColor.GREEN + info.getName() + ChatColor.GRAY + " to " + ChatColor.GREEN + modifiedEvent.getNewValue() + ChatColor.GRAY + "."));
                return;
            }

            if (args[2].equalsIgnoreCase("ip")) {
                if (args.length < 4) {
                    sendUsage(sender, "/svm edit " + info.getName() + " ip <hostname>");
                    return;
                }

                SocketAddress address = getIp(args[3]);

                if (address == null) {
                    sender.sendMessage(TextComponent.fromLegacyText(prefix + "Invalid address " + ChatColor.GREEN + args[3] + ChatColor.GRAY + ". Here's an example: " + ChatColor.GREEN + "127.0.0.1:25565"));
                    return;
                }

                ServerModifiedEvent<SocketAddress> modifiedEvent = new ServerModifiedEvent<>(ServerHelper.getServerInfo(args[1]), sender, ServerModifiedEvent.ServerField.IP, address);

                BungeeServerManager.get().getProxy().getPluginManager().callEvent(modifiedEvent);

                if (modifiedEvent.isCancelled()) {
                    return;
                }

                info = modifiedEvent.getServerModified();

                ServerHelper.removeServer(info.getName());
                ServerHelper.addServer(ProxyServer.getInstance().constructServerInfo(info.getName(), modifiedEvent.getNewValue(), info.getMotd(), false));
                sender.sendMessage(TextComponent.fromLegacyText(prefix + "Set the address of " + ChatColor.GREEN + info.getName() + ChatColor.GRAY + " to " + ChatColor.GREEN + args[3] + ChatColor.GRAY + "."));
                return;
            }

            if (args[2].equalsIgnoreCase("motd")) {
                if (args.length < 4) {
                    sendUsage(sender, "/svm edit " + info.getName() + " motd <motd>");
                    return;
                }

                StringBuilder builder = new StringBuilder();

                for (int i = 3; i < args.length; i++) {
                    builder.append(args[i]).append(" ");
                }

                ServerModifiedEvent<String> modifiedEvent = new ServerModifiedEvent<>(info, sender, ServerModifiedEvent.ServerField.MOTD, ChatColor.translateAlternateColorCodes('&', builder.toString().trim()));

                BungeeServerManager.get().getProxy().getPluginManager().callEvent(modifiedEvent);

                if (modifiedEvent.isCancelled()) {
                    return;
                }

                info = modifiedEvent.getServerModified();

                ServerHelper.removeServer(info.getName());
                ServerHelper.addServer(ProxyServer.getInstance().constructServerInfo(info.getName(), info.getSocketAddress(), modifiedEvent.getNewValue(), false));
                sender.sendMessage(TextComponent.fromLegacyText(prefix + "Set the motd of " + ChatColor.GREEN + info.getName() + ChatColor.GRAY + " to " + ChatColor.GREEN + ChatColor.translateAlternateColorCodes('&', builder.toString().trim()) + ChatColor.GRAY + "."));
                return;
            }

            if (args[2].equalsIgnoreCase("restricted")) {
                if (args.length < 4) {
                    sendUsage(sender, "/svm edit " + info.getName() + " restricted <true|false>");
                    return;
                }

                ServerModifiedEvent<Boolean> modifiedEvent = new ServerModifiedEvent<>(info, sender, ServerModifiedEvent.ServerField.RESTRICTED, Boolean.parseBoolean(args[3]));

                BungeeServerManager.get().getProxy().getPluginManager().callEvent(modifiedEvent);

                if (modifiedEvent.isCancelled()) {
                    return;
                }

                info = modifiedEvent.getServerModified();

                ServerHelper.removeServer(info.getName());
                ServerHelper.addServer(ProxyServer.getInstance().constructServerInfo(info.getName(), info.getSocketAddress(), info.getMotd(), modifiedEvent.getNewValue()));
                sender.sendMessage(TextComponent.fromLegacyText(prefix + "Set the restriction of " + ChatColor.GREEN + info.getName() + ChatColor.GRAY + " to " + ChatColor.GREEN + Boolean.parseBoolean(args[3]) + ChatColor.GRAY + "."));
                return;
            }

            if (args[2].equalsIgnoreCase("domain")) {
                if (args.length < 5 || (!args[3].equalsIgnoreCase("add") && !args[3].equalsIgnoreCase("remove"))) {
                    sendUsage(sender, "/svm edit " + info.getName() + " domain <add|remove> <domain>");
                    return;
                }

                boolean addingDomain = args[3].equalsIgnoreCase("add");
                final SocketAddress address = getIp(args[4]);

                if (address == null) {
                    sender.sendMessage(TextComponent.fromLegacyText(prefix + "Invalid address " + ChatColor.GREEN + args[4] + ChatColor.GRAY + ". Here's an example: " + ChatColor.GREEN + "pvp.md-5.net"));
                    return;
                }

                String socketAddressStr = ConfigHelper.socketAddressToString(address, false);

                if (addingDomain) {
                    ConfigHelper.addForcedHost(address, info);

                    for (ListenerInfo listenerInfo : BungeeServerManager.get().getProxy().getConfig().getListeners()) {
                        listenerInfo.getForcedHosts().put(socketAddressStr, info.getName());
                    }

                    sender.sendMessage(TextComponent.fromLegacyText(prefix + "Added forced host of " + ChatColor.GREEN +
                            socketAddressStr + " " + ChatColor.GRAY + " for server " + ChatColor.GREEN +
                            info.getName() + " " + ChatColor.GRAY + "."));
                } else {
                    boolean found = false;

                    ConfigHelper.removeForcedHost(address, info);

                    for (ListenerInfo listenerInfo : BungeeServerManager.get().getProxy().getConfig().getListeners()) {
                        String targetServer = listenerInfo.getForcedHosts().get(socketAddressStr);

                        if (targetServer != null && targetServer.equalsIgnoreCase(info.getName())) {
                            listenerInfo.getForcedHosts().remove(socketAddressStr);
                            found = true;
                        }
                    }

                    if (found) {
                        sender.sendMessage(TextComponent.fromLegacyText(prefix + "Removed forced host of " +
                                ChatColor.GREEN + socketAddressStr + " " + ChatColor.GRAY + " for server " +
                                ChatColor.GREEN + info.getName() + " " + ChatColor.GRAY + "."));
                    } else {
                        sender.sendMessage(TextComponent.fromLegacyText(prefix + "Could not find forced host of " +
                                ChatColor.GREEN + socketAddressStr + " " + ChatColor.GRAY + " for server " +
                                ChatColor.GREEN + info.getName() + " " + ChatColor.GRAY + "."));
                    }
                }
                return;
            }

            sender.sendMessage(TextComponent.fromLegacyText(prefix + "Unknown argument " + ChatColor.GREEN + args[2] + ChatColor.GRAY + " use " + ChatColor.GREEN + "/svm edit " + info.getName() + ChatColor.GRAY + " for help."));
            return;
        }

        sender.sendMessage(TextComponent.fromLegacyText(prefix + "Unknown argument " + ChatColor.GREEN + args[0] + ChatColor.GRAY + " use " + ChatColor.GREEN + "/svm help" + ChatColor.GRAY + " for help."));
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(TextComponent.fromLegacyText(prefix + "Help:"));
        sender.sendMessage(getHelpString("/svm help", "Display this help menu"));
        sender.sendMessage(getHelpString("/svm list", "List servers"));
        sender.sendMessage(getHelpString("/svm info <server>", "Display info about a server"));
        sender.sendMessage(getHelpString("/svm add <name> [hostname]", "Add a server to BungeeCord"));
        sender.sendMessage(getHelpString("/svm remove <server>", "Remove a server from BungeeCord"));
        sender.sendMessage(getHelpString("/svm edit <server>", "Edit a server's information"));
    }

    private BaseComponent[] getHelpString(String command, String info) {
        return new ComponentBuilder(ChatColor.GREEN + command + ChatColor.GRAY + " - " + info)
                .event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command))
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(ChatColor.GREEN + "Click to execute this command")))
                .create();
    }

    private void sendUsage(CommandSender sender, String usage) {
        sender.sendMessage(new ComponentBuilder(prefix + "Correct usage: " + ChatColor.GREEN + usage)
                .event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "command"))
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(ChatColor.GREEN + "Click to execute this command")))
                .create());
    }

    private void sendEditMenu(CommandSender sender, String serverName) {
        sender.sendMessage(TextComponent.fromLegacyText(prefix + " Edit Help:"));
        sender.sendMessage(getHelpString("/svm edit " + serverName + " name <name>", "Change this server's name"));
        sender.sendMessage(getHelpString("/svm edit " + serverName + " ip <hostname>", "Change this server's address"));
        sender.sendMessage(getHelpString("/svm edit " + serverName + " motd <motd>", "Change this server's motd"));
        sender.sendMessage(getHelpString("/svm edit " + serverName + " restricted <true|false>", "Change this server's restricted flag"));
        sender.sendMessage(getHelpString("/svm edit " + serverName + " domain <add|remove> <domain>", "Add or remove a forced host for this server"));
    }

    private SocketAddress getIp(String input) {
        try {
            return Util.getAddr(input);
        } catch (IllegalArgumentException exec) {
            return null;
        }
    }
}
