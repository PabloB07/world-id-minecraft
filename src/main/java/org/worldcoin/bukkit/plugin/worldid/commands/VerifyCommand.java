package org.worldcoin.bukkit.plugin.worldid.commands;

import java.util.UUID;

import com.posthog.java.shaded.org.jetbrains.annotations.NotNull;
import org.bukkit.Color;
import org.worldcoin.bukkit.plugin.worldid.WorldId;
import org.worldcoin.bukkit.plugin.worldid.tasks.CheckVerified;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.worldcoin.bukkit.plugin.worldid.utils.ColorUtils;

public class VerifyCommand implements CommandExecutor {

    private final WorldId plugin = WorldId.getPlugin(WorldId.class);

    private final FileConfiguration config = plugin.getConfig();

    private final String verifyAlreadyVerified = config.getString("messages.verify-already-verified");
    private final String verifyClickHere = config.getString("messages.verify-click-here");
    private final String verifyNow = config.getString("messages.verify-now");

    private final String orbGroupName = config.getString("orb-group-name");
    private final String deviceGroupName = config.getString("device-group-name");
    private final String baseUrl = config.getString("web-url");
    private final String serverUUID = config.getString("server-uuid");

    @Override
        public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        if (sender instanceof Player player) {

            if (player.hasPermission("group." + orbGroupName) || player.hasPermission("group." + deviceGroupName)) {
                player.sendMessage(ColorUtils.colorize(verifyAlreadyVerified));
                return true;
            }
            UUID reqUUID = UUID.randomUUID();
            String url = baseUrl + "/verify?reqUUID=" + reqUUID + "&serverUUID=" + serverUUID;

            player.sendMessage(ColorUtils.colorize(verifyClickHere));
            player.sendMessage("----------------------------------------");

            // player.spigot().sendMessage( new TextComponent(" "));

            TextComponent button = new TextComponent(ColorUtils.colorize(verifyNow));
            // button.setColor(ChatColor.WHITE);
            button.setBold(true);
            button.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url));

            player.spigot().sendMessage( button );
            player.sendMessage("----------------------------------------");

            new CheckVerified(player, reqUUID, 20).runTaskTimerAsynchronously(plugin, 100, 200);
            return true;
        } else {
            sender.sendMessage(ColorUtils.colorize("You must be a player to use this!"));
            return false;
        }
    }
}
