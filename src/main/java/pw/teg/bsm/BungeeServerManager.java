package pw.teg.bsm;

import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import pw.teg.bsm.api.ServerManagerAPI;
import pw.teg.bsm.commands.ServerManagerCommand;

public class BungeeServerManager extends Plugin {

    private static BungeeServerManager instance;
    private static ServerManagerAPI api;

    @Override
    public void onEnable() {
        instance = this;

        api = new ServerManagerAPI();

        getProxy().getPluginManager().registerCommand(this, new ServerManagerCommand());
    }

    public static BungeeServerManager get() {
        return instance;
    }

    public static ServerManagerAPI getApi() {
        return api;
    }
}
