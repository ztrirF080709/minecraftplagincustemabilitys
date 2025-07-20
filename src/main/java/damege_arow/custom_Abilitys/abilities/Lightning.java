package damege_arow.custom_Abilitys.abilities;

import damege_arow.custom_Abilitys.Ability;
import damege_arow.custom_Abilitys.Custom_Abilitys;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.UUID;

public class Lightning implements Ability {

    @Override
    public String getName() {
        return "Lightning";
    }

    @Override
    public String getDisplayName() {
        return "§bLightning";
    }

    @Override
    public long getCooldown(Player player) {
        int seconds = Custom_Abilitys.getInstance().getConfig().getInt("Lightning.cooldown", 30);
        return player.getInventory().contains(Material.DRAGON_EGG) ? seconds * 500L : seconds * 1000L;
    }

    @Override
    public void useAbility(Player player) {
        UUID uuid = player.getUniqueId();

        if (Custom_Abilitys.isOnCooldown(uuid, getName())) {
            player.sendMessage(ChatColor.RED + "Diese Fähigkeit ist noch im Cooldown!");
            return;
        }

        int duration = Custom_Abilitys.getInstance().getConfig().getInt("Lightning.duration", 5);
        int range = Custom_Abilitys.getInstance().getConfig().getInt("Lightning.range", 10);

        Custom_Abilitys.setCooldown(uuid, getName(), getCooldown(player));
        player.sendMessage(ChatColor.AQUA + "§bBlitzsturm aktiviert!");

        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (ticks >= duration * 20) {
                    cancel();
                    player.sendMessage(ChatColor.GRAY + "§7Der Blitzsturm ist vorbei.");
                    return;
                }

                Location center = player.getLocation();
                List<Entity> nearby = center.getWorld().getEntities();

                for (Entity entity : nearby) {
                    if (entity.getUniqueId().equals(uuid)) continue;
                    if (!(entity instanceof LivingEntity target)) continue;
                    if (entity.getLocation().distanceSquared(center) > range * range) continue;

                    Location loc = target.getLocation();
                    center.getWorld().strikeLightningEffect(loc);

                    double damage = Math.max(1.0, target.getHealth() * 0.10);
                    target.damage(damage, player);
                }

                ticks += 10;
            }
        }.runTaskTimer(Custom_Abilitys.getInstance(), 0L, 10L); // alle 0.5 Sekunden
    }

    @Override
    public void onEquip(Player player) {}

    @Override
    public void onUnequip(Player player) {}
}
