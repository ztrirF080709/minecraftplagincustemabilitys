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

public class Magma implements Ability {

    private final Set<UUID> equipped = new HashSet<>();

    @Override
    public String getName() {
        return "Magma";
    }

    @Override
    public String getDisplayName() {
        return "§cMagma";
    }

    @Override
    public long getCooldown(Player player) {
        int seconds = Custom_Abilitys.getInstance().getConfig().getInt("Magma.cooldown", 30);
        return player.getInventory().contains(Material.DRAGON_EGG) ? seconds * 500L : seconds * 1000L;
    }

    @Override
    public void useAbility(Player player) {
        UUID uuid = player.getUniqueId();
        if (Custom_Abilitys.isOnCooldown(uuid, getName())) {
            player.sendMessage(ChatColor.RED + "Diese Fähigkeit ist noch im Cooldown!");
            return;
        }

        int radius = Custom_Abilitys.getInstance().getConfig().getInt("Magma.radius", 5);
        Location center = player.getLocation();
        World world = player.getWorld();

        int removed = 0;

        for (int x = -radius; x <= radius; x++) {
            for (int y = -2; y <= 2; y++) {
                for (int z = -radius; z <= radius; z++) {
                    Location loc = center.clone().add(x, y, z);
                    Block block = world.getBlockAt(loc);
                    if (block.getType() == Material.WATER || block.getType() == Material.WATER_CAULDRON) {
                        block.setType(Material.AIR);
                        removed++;
                        world.spawnParticle(Particle.SMOKE, loc.add(0.5, 0.5, 0.5), 5, 0.2, 0.2, 0.2, 0);
                        world.playSound(loc, Sound.BLOCK_FIRE_EXTINGUISH, 0.6f, 1.2f);
                    }
                }
            }
        }

        player.sendMessage(ChatColor.GOLD + "§6Verdampftes Wasser: " + removed);
        Custom_Abilitys.setCooldown(uuid, getName(), getCooldown(player));
    }

    @Override
    public void onEquip(Player player) {
        equipped.add(player.getUniqueId());

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!equipped.contains(player.getUniqueId()) || !player.isOnline()) {
                    cancel();
                    return;
                }

                // Dauerhafte Feuerresistenz
                player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 60, 0, true, false));

                if (player.getFireTicks() > 0) {
                    // Zusätzliche Effekte beim Brennen
                    player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 40, 0, true, false));
                    player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 40, 1, true, false));
                }
            }
        }.runTaskTimer(Custom_Abilitys.getInstance(), 0L, 20L);
    }

    @Override
    public void onUnequip(Player player) {
        equipped.remove(player.getUniqueId());
        player.removePotionEffect(PotionEffectType.FIRE_RESISTANCE);
        player.removePotionEffect(PotionEffectType.REGENERATION);
        player.removePotionEffect(PotionEffectType.RESISTANCE);
    }
}
