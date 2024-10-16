package org.worldcoin.bukkit.plugin.worldid.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.worldcoin.bukkit.plugin.worldid.WorldId;
import org.worldcoin.bukkit.plugin.worldid.utils.ColorUtils;

public class ReloadConfig implements CommandExecutor {
    private final WorldId plugin = WorldId.getPlugin(WorldId.class);
    private final FileConfiguration config = plugin.getConfig();
    private final String configReloaded = config.getString("messages.config-reloaded");

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        Player player = (Player) commandSender;
        WorldId.getPlugin(WorldId.class).reloadConfig();
        player.sendMessage(ColorUtils.colorize(configReloaded));
        return true;
    }
}
