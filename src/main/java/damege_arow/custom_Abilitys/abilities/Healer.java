package damege_arow.custom_Abilitys.abilities;

import damege_arow.custom_Abilitys.Ability;
import damege_arow.custom_Abilitys.Custom_Abilitys;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class Healer implements Ability {

    private final Set<UUID> active = new HashSet<>();

    @Override
    public String getName() {
        return "Healer";
    }

    @Override
    public String getDisplayName() {
        return "§dHealer";
    }

    @Override
    public long getCooldown(Player player) {
        int seconds = Custom_Abilitys.getInstance().getConfig().getInt("Healer.cooldown", 45);
        return player.getInventory().contains(Material.DRAGON_EGG) ? seconds * 500L : seconds * 1000L;
    }

    @Override
    public void useAbility(Player player) {
        if (Custom_Abilitys.isOnCooldown(player.getUniqueId(), getName())) {
            player.sendMessage(ChatColor.RED + "Diese Fähigkeit ist noch im Cooldown!");
            return;
        }

        Location loc = player.getLocation().getBlock().getLocation();
        int radius = Custom_Abilitys.getInstance().getConfig().getInt("Healer.radius", 5);
        int duration = Custom_Abilitys.getInstance().getConfig().getInt("Healer.duration", 10);

        // Lagerfeuer platzieren
        Block block = loc.getBlock();
        block.setType(Material.CAMPFIRE);
        Custom_Abilitys.setCooldown(player.getUniqueId(), getName(), getCooldown(player));
        player.sendMessage(ChatColor.LIGHT_PURPLE + "§dHeilkreis aktiviert!");

        // Heil-Effekt starten (nur für den Spieler selbst)
        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (ticks >= duration * 20 || block.getType() != Material.CAMPFIRE) {
                    block.setType(Material.AIR);
                    cancel();
                    return;
                }

                if (player.getLocation().distanceSquared(loc) <= radius * radius) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 40, 2, true, false));
                }

                // Partikelkreis
                for (int i = 0; i < 360; i += 20) {
                    double angle = Math.toRadians(i);
                    double x = loc.getX() + radius * Math.cos(angle);
                    double z = loc.getZ() + radius * Math.sin(angle);
                    Location particleLoc = new Location(loc.getWorld(), x, loc.getY() + 0.2, z);
                    loc.getWorld().spawnParticle(Particle.HEART, particleLoc, 1, 0, 0, 0);
                }

                ticks += 20;
            }
        }.runTaskTimer(Custom_Abilitys.getInstance(), 0L, 20L);
    }

    @Override
    public void onEquip(Player player) {
        active.add(player.getUniqueId());
        player.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH).setBaseValue(25.0);
        player.setHealth(Math.min(player.getHealth(), 25.0));
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, Integer.MAX_VALUE, 0, true, false));
    }

    @Override
    public void onUnequip(Player player) {
        active.remove(player.getUniqueId());
        player.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH).setBaseValue(20.0);
        if (player.getHealth() > 20.0) player.setHealth(20.0);
        player.removePotionEffect(PotionEffectType.REGENERATION);
    }
}
