package damege_arow.custom_Abilitys.abilities;

import damege_arow.custom_Abilitys.Ability;
import damege_arow.custom_Abilitys.Custom_Abilitys;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Stun implements Ability, Listener {

    private final Set<UUID> chargedPlayers = new HashSet<>();

    @Override
    public String getName() {
        return "Stun";
    }

    @Override
    public String getDisplayName() {
        return "§eStun";
    }

    @Override
    public long getCooldown(Player player) {
        int seconds = Custom_Abilitys.getInstance().getConfig().getInt("Stun.cooldown", 25);
        return player.getInventory().contains(Material.DRAGON_EGG) ? seconds * 500L : seconds * 1000L;
    }

    @Override
    public void useAbility(Player player) {
        if (Custom_Abilitys.isOnCooldown(player.getUniqueId(), getName())) {
            player.sendMessage(ChatColor.RED + "Diese Fähigkeit ist noch im Cooldown!");
            return;
        }

        chargedPlayers.add(player.getUniqueId());
        Custom_Abilitys.setCooldown(player.getUniqueId(), getName(), getCooldown(player));
        player.sendMessage(ChatColor.YELLOW + "§eDein nächster Treffer wird das Ziel §lstunnen§e!");
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1.2f);
    }

    @EventHandler
    public void onAttack(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player damager)) return;
        if (!(event.getEntity() instanceof LivingEntity target)) return;
        if (!chargedPlayers.contains(damager.getUniqueId())) return;

        chargedPlayers.remove(damager.getUniqueId());

        Location stunLocation = target.getLocation().clone();
        int duration = Custom_Abilitys.getInstance().getConfig().getInt("Stun.duration", 3);

        damager.sendMessage(ChatColor.GRAY + "§7Du hast §e" + target.getName() + " §7gestunnt!");

        if (target instanceof Player pTarget) {
            pTarget.sendMessage(ChatColor.RED + "§cDu wurdest für " + duration + " Sekunden gestunnt!");
        }

        new BukkitRunnable() {
            int count = 0;

            @Override
            public void run() {
                if (count >= duration * 10 || target.isDead()) {
                    cancel();
                    return;
                }
                target.teleport(stunLocation);
                target.getWorld().spawnParticle(Particle.WITCH, stunLocation, 10, 0.3, 1, 0.3);
                target.getWorld().playSound(stunLocation, Sound.BLOCK_NOTE_BLOCK_HAT, 1f, 2f);
                count++;
            }
        }.runTaskTimer(Custom_Abilitys.getInstance(), 0L, 2L);
    }

    @Override
    public void onEquip(Player player) {
        Bukkit.getPluginManager().registerEvents(this, Custom_Abilitys.getInstance());
    }

    @Override
    public void onUnequip(Player player) {
        chargedPlayers.remove(player.getUniqueId());
        EntityDamageByEntityEvent.getHandlerList().unregister(this);
    }
}
