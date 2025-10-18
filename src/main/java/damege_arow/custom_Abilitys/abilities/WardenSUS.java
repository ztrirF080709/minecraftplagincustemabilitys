package damege_arow.custom_Abilitys.abilities;

import damege_arow.custom_Abilitys.Ability;
import damege_arow.custom_Abilitys.Custom_Abilitys;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.bukkit.scheduler.BukkitRunnable;

public class WardenSUS implements Ability {

    @Override
    public String getName() {
        return "WardenSUS";
    }

    @Override
    public String getDisplayName() {
        return ChatColor.DARK_AQUA + "Warden Sonic Boom";
    }

    @Override
    public long getCooldown(Player player) {
        return Custom_Abilitys.getInstance().getConfig().getLong("warden_sus.cooldown", 15L) * 1000;
    }

    @Override
    public void useAbility(Player player) {
        World world = player.getWorld();
        Vector direction = player.getLocation().getDirection().normalize();

        new BukkitRunnable() {
            final Location loc = player.getEyeLocation().clone();
            int ticks = 0;

            @Override
            public void run() {
                if (ticks++ > 20) {
                    this.cancel();
                    return;
                }

                loc.add(direction.clone().multiply(1.2));
                world.spawnParticle(Particle.SONIC_BOOM, loc, 1);
                world.playSound(loc, Sound.ENTITY_WARDEN_SONIC_BOOM, 1.5f, 1);

                world.getNearbyEntities(loc, 1.5, 1.5, 1.5).forEach(entity -> {
                    if (entity instanceof Player target && !target.equals(player)) {
                        target.damage(6); // True damage
                        target.setVelocity(direction.multiply(1.5));
                    }
                });
            }
        }.runTaskTimer(Custom_Abilitys.getInstance(), 0L, 1L);
    }

    @Override
    public void onEquip(Player player) {}

    @Override
    public void onUnequip(Player player) {}
}
