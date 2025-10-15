package damege_arow.custom_Abilitys.abilities;

import damege_arow.custom_Abilitys.Ability;
import damege_arow.custom_Abilitys.Custom_Abilitys;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class InfernalSlash implements Ability, Listener {

    @Override
    public String getName() {
        return "InfernalSlash";
    }

    @Override
    public String getDisplayName() {
        return ChatColor.DARK_RED + "Infernal Slash";
    }

    @Override
    public long getCooldown(Player player) {
        return Custom_Abilitys.getInstance().getConfig().getLong("infernal_slash.cooldown", 20L) * 1000;
    }

    @Override
    public void useAbility(Player player) {
        Location origin = player.getEyeLocation();
        World world = origin.getWorld();
        Vector direction = origin.getDirection().normalize();

        world.playSound(origin, Sound.ENTITY_BLAZE_SHOOT, 2, 0.5f);

        // Großer horizontaler Slash „____“ der nach vorne fliegt
        new BukkitRunnable() {
            int steps = 0;
            Location loc = origin.clone();

            @Override
            public void run() {
                if (steps > 20) {
                    cancel();
                    return;
                }

                for (double x = -2.5; x <= 2.5; x += 0.5) {
                    Location line = loc.clone().add(player.getLocation().getDirection().clone().multiply(1.5));
                    line.add(player.getLocation().getDirection().clone().getCrossProduct(new Vector(0, 1, 0)).normalize().multiply(x));
                    world.spawnParticle(Particle.FLAME, line, 0);
                    world.spawnParticle(Particle.SOUL_FIRE_FLAME, line, 0);

                    // Damage
                    for (LivingEntity e : world.getNearbyLivingEntities(line, 1)) {
                        if (!e.equals(player)) {
                            e.setFireTicks(100);
                            e.damage(6); // true damage
                            e.removePotionEffect(PotionEffectType.FIRE_RESISTANCE);
                        }
                    }
                }

                loc.add(direction.clone().multiply(0.7));
                steps++;
            }
        }.runTaskTimer(Custom_Abilitys.getInstance(), 0L, 1L);
    }

    @EventHandler
    public void onHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player damager)) return;
        if (!(event.getEntity() instanceof LivingEntity target)) return;

        Ability[] abilities = Custom_Abilitys.getAbilities(damager.getUniqueId());
        for (Ability ability : abilities) {
            if (ability instanceof InfernalSlash) {
                Location loc = damager.getEyeLocation();
                World world = loc.getWorld();
                Vector forward = loc.getDirection().normalize();

                // Diagonal-Slashes (von oben links nach unten rechts)
                new BukkitRunnable() {
                    final Location base = loc.clone();
                    int step = 0;

                    @Override
                    public void run() {
                        if (step > 7) {
                            cancel();
                            return;
                        }

                        Vector offset = forward.clone().multiply(step * 0.4).add(new Vector(-0.4 * step, 0.4 * step, 0));
                        Location particleLoc = base.clone().add(offset);
                        world.spawnParticle(Particle.FLAME, particleLoc, 1);
                        world.spawnParticle(Particle.SOUL_FIRE_FLAME, particleLoc, 0);
                        step++;
                    }
                }.runTaskTimer(Custom_Abilitys.getInstance(), 0L, 1L);

                target.setFireTicks(60);
                break;
            }
        }
    }

    @Override
    public void onEquip(Player player) {
        Bukkit.getPluginManager().registerEvents(this, Custom_Abilitys.getInstance());
    }

    @Override
    public void onUnequip(Player player) {
        EntityDamageByEntityEvent.getHandlerList().unregister(this);
    }
}
