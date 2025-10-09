// Datei: ParasitAbility.java
package damege_arow.custom_Abilitys.abilities;

import damege_arow.custom_Abilitys.*;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

public class ParasitAbility implements Ability, Listener {
    private final JavaPlugin plugin = Custom_Abilitys.getInstance();

    @Override
    public String getName() {
        return "Parasit";
    }

    @Override
    public String getDisplayName() {
        return ChatColor.DARK_PURPLE + "Parasit";
    }

    @Override
    public long getCooldown(Player player) {
        return 0;
    }

    @Override
    public void useAbility(Player player) {
        // NICHT benutzbar
    }

    @Override
    public void onEquip(Player player) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void onUnequip(Player player) {
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        UUID uuid = player.getUniqueId();

        Ability[] abilities = Custom_Abilitys.getAbilities(uuid);
        for (Ability ab : abilities) {
            if (ab instanceof ParasitAbility) {
                event.setCancelled(true);

                player.setHealth(1.0);
                player.setInvulnerable(true);
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 99999, 255, false, false));

                player.sendMessage(ChatColor.DARK_PURPLE + "Dein Parasit aktiviert sich...");

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (!player.isOnline()) return;
                        player.setInvulnerable(false);
                        player.removePotionEffect(PotionEffectType.SLOWNESS);

                        // Laser Effekt + Explosion
                        Location loc = player.getLocation();
                        loc.getWorld().spawnParticle(Particle.PORTAL, loc.clone().add(0, 10, 0), 200);
                        loc.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 1, 0.5f);
                        loc.getWorld().createExplosion(loc, 0, false);
                        // Loch erzeugen
                        for (int x = -2; x <= 2; x++) {
                            for (int z = -2; z <= 2; z++) {
                                for (int y = loc.getBlockY(); y > 0; y--) {
                                    Location breakLoc = loc.clone().add(x, y - loc.getBlockY(), z);
                                    breakLoc.getBlock().setType(Material.AIR);
                                }
                            }
                        }

                        // Ghost Modus aktivieren
                        player.sendMessage(ChatColor.GRAY + "Du bist jetzt ein Geist.");
                        player.setInvisible(true);
                        player.setGameMode(GameMode.SURVIVAL);

                        // Fähigkeiten setzen
                        Ability[] ghostAbilities = new Ability[2];
                        ghostAbilities[0] = new GhostCloak();
                        ghostAbilities[1] = new GhostStrike();
                        Custom_Abilitys.setAbilities(uuid, ghostAbilities);
                        ghostAbilities[0].onEquip(player);
                        ghostAbilities[1].onEquip(player);
                    }
                }.runTaskLater(plugin, 60L); // 3 Sekunden

                break;
            }
        }
    }
}
