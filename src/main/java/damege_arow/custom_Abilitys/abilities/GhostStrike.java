package damege_arow.custom_Abilitys.abilities;

import damege_arow.custom_Abilitys.Ability;
import damege_arow.custom_Abilitys.Custom_Abilitys;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

public class GhostStrike implements Ability, Listener {

    @Override
    public String getName() {
        return "GhostStrike";
    }

    @Override
    public String getDisplayName() {
        return "§d☄️ Air Strike";
    }

    @Override
    public long getCooldown(Player player) {
        return 0;
    }

    @Override
    public void useAbility(Player player) {
        Location targetLoc = player.getTargetBlock(null, 100).getLocation().add(0.5, 0, 0.5);
        World world = player.getWorld();

        // Ankündigungseffekt
        new BukkitRunnable() {
            @Override
            public void run() {
                for (int y = targetLoc.getBlockY() + 50; y >= targetLoc.getBlockY(); y--) {
                    Location strike = new Location(world, targetLoc.getX(), y, targetLoc.getZ());
                    world.spawnParticle(Particle.DRAGON_BREATH, strike, 20, 0.1, 0.1, 0.1, 0.02);
                }

                // Explosion + Blockzerstörung
                world.createExplosion(targetLoc, 6.0F, false, false);

                for (int x = -2; x <= 2; x++) {
                    for (int y = -50; y <= 0; y++) {
                        for (int z = -2; z <= 2; z++) {
                            Location loc = targetLoc.clone().add(x, y, z);
                            if (loc.getBlock().getType() != Material.BEDROCK) {
                                loc.getBlock().setType(Material.AIR);
                            }
                        }
                    }
                }

                player.getWorld().playSound(targetLoc, Sound.ENTITY_GENERIC_EXPLODE, 1.5f, 0.6f);
            }
        }.runTaskLater(Custom_Abilitys.getInstance(), 20L); // 1s Delay für Effekt
    }

    @Override
    public void onEquip(Player player) {
    }

    @Override
    public void onUnequip(Player player) {
    }
}
