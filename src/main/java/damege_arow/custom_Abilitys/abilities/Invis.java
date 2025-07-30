package damege_arow.custom_Abilitys.abilities;

import damege_arow.custom_Abilitys.Ability;
import damege_arow.custom_Abilitys.Custom_Abilitys;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.UUID;

public class Invis implements Ability, Listener {

    private final FileConfiguration config = Custom_Abilitys.getInstance().getConfig();

    @Override
    public String getName() {
        return "Invis";
    }

    @Override
    public String getDisplayName() {
        return "§7Invis";
    }

    @Override
    public long getCooldown(Player player) {
        return config.getLong("invis.cooldown", 60) * 1000L;
    }

    @Override
    public void useAbility(Player player) {
        UUID uuid = player.getUniqueId();

        if (Custom_Abilitys.isOnCooldown(uuid, getName())) {
            player.sendMessage(ChatColor.RED + "Diese Fähigkeit ist noch im Cooldown!");
            return;
        }

        long durationTicks = config.getLong("invis.duration", 20) * 20;
        double radius = config.getDouble("invis.blind_radius", 4.0);

        // Set Armor invisible
        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, (int) durationTicks, 1, false, false));

        // Blindness for nearby players
        for (Player target : player.getWorld().getPlayers()) {
            if (target.equals(player)) continue;

            if (target.getLocation().distanceSquared(player.getLocation()) <= radius * radius) {
                target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, (int) durationTicks, 3, false, false));
                target.sendMessage(ChatColor.DARK_GRAY + "§k--§r §cDu wurdest von Schatten geblendet! §r§k--");
            }
        }

        // Cooldown setzen
        Custom_Abilitys.setCooldown(uuid, getName(), getCooldown(player));
        player.sendMessage(ChatColor.GRAY + "§7Du bist vollständig unsichtbar...");

        // Partikel zur Visualisierung (optional)
        Location loc = player.getLocation();
        player.getWorld().spawnParticle(Particle.CLOUD, loc, 30, 0.5, 1, 0.5, 0.01);
    }

    @Override
    public void onEquip(Player player) {
        // Passive permanent invis
        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0, true, false));
    }

    @Override
    public void onUnequip(Player player) {
        player.removePotionEffect(PotionEffectType.INVISIBILITY);
    }
}
