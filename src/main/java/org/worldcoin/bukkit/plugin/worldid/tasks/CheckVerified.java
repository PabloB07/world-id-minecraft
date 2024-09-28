package org.worldcoin.bukkit.plugin.worldid.tasks;

import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

import org.apache.hc.client5.http.fluent.Request;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.worldcoin.bukkit.plugin.worldid.WorldId;

import com.posthog.java.PostHog;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.node.types.InheritanceNode;
import net.luckperms.api.model.user.User;
import org.worldcoin.bukkit.plugin.worldid.utils.ColorUtils;

public class CheckVerified extends BukkitRunnable {

    private final WorldId plugin = WorldId.getPlugin(WorldId.class);

    private final Player player;
    private final String webUrl;
    private int counter;

    private final FileConfiguration config = plugin.getConfig();

    private final String verifySuccess = config.getString("messages.verify-success");
    private final String verifyFailed = config.getString("messages.verify-failure");
    private final String verifyError = config.getString("messages.verify-error");

    private final String verifyLevel = config.getString("messages.verify-level");
    private final String verifyInvalid = config.getString("messages.verify-invalid");

    private final String orbGroupName = config.getString("orb-group-name");
    private final String deviceGroupName = config.getString("device-group-name");

    PostHog posthog = new PostHog.Builder(WorldId.POSTHOG_API_KEY).host(WorldId.POSTHOG_HOST).build();

    public CheckVerified(Player player, UUID uuid, int counter) {
        this.player = player;
        String baseUrl = config.getString("web-url");
        this.webUrl = baseUrl +"/api/isVerified?id="+uuid;
        if (counter <= 0) {
            throw new IllegalArgumentException("counter must be greater than 0");
        } else {
            this.counter = counter;
        }
    }

    @Override
    public void run() {
        if (counter > 0) { 
            try {
                Request.get(webUrl).execute().handleResponse(new HttpClientResponseHandler<Boolean>() {
                    @Override
                    public Boolean handleResponse(final ClassicHttpResponse response) throws IOException {
                        final int status = response.getCode();

                        if (status == 200) {
                            String verification_level = new String(response.getEntity().getContent().readAllBytes());
                            String groupName;
                            
                            switch (verification_level) {
                                case "orb":
                                    groupName = orbGroupName.isBlank() ? deviceGroupName : orbGroupName;
                                    break;
                                case "device":
                                    groupName = deviceGroupName;
                                    if (groupName == null) {
                                        player.sendMessage(ColorUtils.colorize(verifyLevel));
                                        CheckVerified.this.cancel();
                                        return false;
                                    }
                                    break;
                                default:
                                    groupName = null;
                                    player.sendMessage(ColorUtils.colorize(verifyInvalid));
                                    return false;
                            }

                            if (player.hasPermission("group." + groupName)) {
                                throw new IllegalStateException("Player is already verified.");
                            }

                            final LuckPerms api = LuckPermsProvider.get();
                            final User user = api.getPlayerAdapter(Player.class).getUser(player);
                            final InheritanceNode node = InheritanceNode.builder(groupName).value(true).build();
                            user.data().add(node);
                            user.setPrimaryGroup(groupName);
                            api.getUserManager().saveUser(user);
                            player.sendMessage(ColorUtils.colorize(verifySuccess));
                            posthog.capture(player.getUniqueId().toString(), "minecraft integration verification", new HashMap<String, Object>() {
                                {
                                  put("verification_level", verification_level);
                                  put("server_uuid", config.getString("server-uuid"));
                                }
                            });
                            CheckVerified.this.cancel();
                            return true;
                        } else {
                            return false;
                        }
                    }
                });
            } catch (Exception e) {
                player.sendMessage(ColorUtils.colorize(verifyError + e.getMessage()));
                this.cancel();
            } finally {
                counter--;
            }   
        } else {
            player.sendMessage(ColorUtils.colorize(verifyFailed));
            this.cancel();
        }
    }
}