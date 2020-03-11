package pw.teg.bsm.util;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.YamlConfiguration;
import pw.teg.bsm.BungeeServerManager;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class ConfigHelper {

    private static File file;
    private static Configuration bungeeConfig;
    private static boolean locked;

    static {
        // Plugins are loaded before the config (on first run) so uhhhhh, here we go
        setupConfig();
        if (locked)
            ProxyServer.getInstance().getScheduler().schedule(plugin, ServerConfig::setupConfig, 10L, TimeUnit.SECONDS);
    }

    public static void addToConfig(ServerInfo serverInfo) {
        if (!locked) {
            bungeeConfig.set("servers." + serverInfo.getName() + ".motd", serverInfo.getMotd().replace(ChatColor.COLOR_CHAR, '&'));
            bungeeConfig.set("servers." + serverInfo.getName() + ".address", serverInfo.getAddress().getAddress().getHostAddress() + ":" + serverInfo.getAddress().getPort());
            bungeeConfig.set("servers." + serverInfo.getName() + ".restricted", false);
            saveConfig();
        }
    }

    public static void removeFromConfig(ServerInfo serverInfo) {
        removeFromConfig(serverInfo.getName());
    }

    public static void removeFromConfig(String name) {
        if (locked) {
            return;
        }

        bungeeConfig.set("servers." + name, null);
        saveConfig();
    }

    private static void saveConfig() {
        if (locked) {
            return;
        }

        try {
            YamlConfiguration.getProvider(YamlConfiguration.class).save(bungeeConfig, file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void setupConfig() {
        try {
            file = new File(ProxyServer.getInstance().getPluginsFolder().getParentFile(), "config.yml");
            bungeeConfig = YamlConfiguration.getProvider(YamlConfiguration.class).load(file);
        } catch (IOException e) {
            e.printStackTrace();
        }

        locked = bungeeConfig == null;
    }

}
