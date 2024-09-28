package org.worldcoin.bukkit.plugin.worldid.event;

import org.worldcoin.bukkit.plugin.worldid.WorldId;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.worldcoin.bukkit.plugin.worldid.utils.ColorUtils;

public class JoinListener implements Listener {

    private final WorldId plugin = WorldId.getPlugin(WorldId.class);

    private final FileConfiguration config = plugin.getConfig();
    private final String orbGroupName = config.getString("orb-group-name");
    private final String deviceGroupName = config.getString("device-group-name");
    private final String welcomeMsg = config.getString("listener-messages.welcome-msg");
    private final String verifyWelcomeMsg = config.getString("listener-messages.verify-welcome-msg");
    private final String verifyToMsg = config.getString("listener-messages.verify-to-msg");
    private final String verifyMsg = config.getString("listener-messages.verify-msg");

    public JoinListener(WorldId plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    

    @EventHandler
    public void join(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        player.sendMessage(ColorUtils.colorize(welcomeMsg));
        if (player.hasPermission("group."+orbGroupName) || player.hasPermission("group."+deviceGroupName)) {
            player.sendMessage(ColorUtils.colorize(verifyWelcomeMsg));
        } else {
            player.sendMessage(ColorUtils.colorize(verifyToMsg));
            player.sendMessage(ColorUtils.colorize(verifyMsg));
        }
    }
}
