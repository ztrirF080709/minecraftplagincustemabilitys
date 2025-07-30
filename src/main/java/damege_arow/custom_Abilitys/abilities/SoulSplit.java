package damege_arow.custom_Abilitys.abilities;

import damege_arow.custom_Abilitys.Ability;
import damege_arow.custom_Abilitys.Custom_Abilitys;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.*;

public class SoulSplit implements Ability, Listener {

    private static final Set<UUID> activeSouls = new HashSet<>();
    private final FileConfiguration config = Custom_Abilitys.getInstance().getConfig();

    public SoulSplit() {
        Bukkit.getPluginManager().registerEvents(this, Custom_Abilitys.getInstance());
    }

    @Override
    public String getName() {
        return "SoulSplit";
    }

    @Override
    public String getDisplayName() {
        return "§5SoulSplit";
    }

    @Override
    public long getCooldown(Player player) {
        return config.getLong("soulsplit.cooldown", 30) * 1000L; // in Sekunden
    }

    @Override
    public void useAbility(Player player) {
        UUID uuid = player.getUniqueId();

        if (Custom_Abilitys.isOnCooldown(uuid, getName())) {
            player.sendMessage(ChatColor.RED + "Diese Fähigkeit ist noch im Cooldown!");
            return;
        }

        // Wenn schon aktiv → zurückwechseln
        if (activeSouls.contains(uuid)) {
            player.setGameMode(GameMode.SURVIVAL);
            activeSouls.remove(uuid);
            player.sendMessage(ChatColor.GRAY + "§7Du bist in deinen Körper zurückgekehrt.");
            return;
        }

        // Spectator aktivieren
        activeSouls.add(uuid);
        player.setGameMode(GameMode.SPECTATOR);
        player.sendMessage(ChatColor.DARK_PURPLE + "§oDu hast deinen Körper verlassen...");

        Custom_Abilitys.setCooldown(uuid, getName(), getCooldown(player));

        long durationTicks = config.getLong("soulsplit.duration", 5) * 20;

        Bukkit.getScheduler().runTaskLater(Custom_Abilitys.getInstance(), () -> {
            if (activeSouls.contains(uuid)) {
                player.setGameMode(GameMode.SURVIVAL);
                player.sendMessage(ChatColor.GRAY + "§7SoulSplit ist vorbei.");
                activeSouls.remove(uuid);
            }
        }, durationTicks);
    }

    @Override
    public void onEquip(Player player) {
        // nichts
    }

    @Override
    public void onUnequip(Player player) {
        UUID uuid = player.getUniqueId();
        if (activeSouls.contains(uuid)) {
            player.setGameMode(GameMode.SURVIVAL);
            activeSouls.remove(uuid);
        }
    }

    @EventHandler
    public void onSpectatorTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();

        if (player.getGameMode() == GameMode.SPECTATOR
                && event.getCause() == PlayerTeleportEvent.TeleportCause.SPECTATE
                && !player.isOp()) {

            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "§cTeleportieren im SoulSplit-Modus ist blockiert.");
        }
    }
}
