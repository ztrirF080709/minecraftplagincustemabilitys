package damege_arow.custom_Abilitys.abilities;

import damege_arow.custom_Abilitys.Ability;
import damege_arow.custom_Abilitys.Custom_Abilitys;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class Rewind implements Ability {

    @Override
    public String getName() {
        return "Rewind";
    }

    @Override
    public String getDisplayName() {
        return "§bRewind";
    }

    @Override
    public long getCooldown(Player player) {
        int seconds = Custom_Abilitys.getInstance().getConfig().getInt("Rewind.cooldown", 20);
        return player.getInventory().contains(Material.DRAGON_EGG) ? seconds * 500L : seconds * 1000L;
    }

    @Override
    public void useAbility(Player player) {
        if (Custom_Abilitys.isOnCooldown(player.getUniqueId(), getName())) {
            player.sendMessage(ChatColor.RED + "Diese Fähigkeit ist noch im Cooldown!");
            return;
        }

        // Position merken
        Location start = player.getLocation().clone();
        player.sendMessage(ChatColor.AQUA + "§bRewind wird vorbereitet...");

        // Partikel und Sound bei Aktivierung
        player.getWorld().spawnParticle(Particle.PORTAL, start, 30, 0.5, 1, 0.5, 0.1);
        player.playSound(start, Sound.ITEM_CHORUS_FRUIT_TELEPORT, 1f, 1.5f);

        // Cooldown starten
        Custom_Abilitys.setCooldown(player.getUniqueId(), getName(), getCooldown(player));

        // Nach 3 Sekunden zurückteleportieren
        new BukkitRunnable() {
            @Override
            public void run() {
                player.teleport(start);
                player.sendMessage(ChatColor.GRAY + "§7Du wurdest zurückgesetzt!");
                player.getWorld().spawnParticle(Particle.REVERSE_PORTAL, player.getLocation(), 40, 0.5, 1, 0.5, 0.1);
                player.playSound(player.getLocation(), Sound.ITEM_CHORUS_FRUIT_TELEPORT, 1f, 0.8f);
            }
        }.runTaskLater(Custom_Abilitys.getInstance(), 20 * 3); // 3 Sekunden
    }

    @Override
    public void onEquip(Player player) {}

    @Override
    public void onUnequip(Player player) {}
}
