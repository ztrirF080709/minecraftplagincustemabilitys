package damege_arow.custom_Abilitys.abilities;

import damege_arow.custom_Abilitys.Ability;
import damege_arow.custom_Abilitys.Custom_Abilitys;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class Immortaly implements Ability, Listener {

    private final Set<UUID> active = new HashSet<>();

    @Override
    public String getName() {
        return "Immortaly";
    }

    @Override
    public String getDisplayName() {
        return "§6Immortaly";
    }

    @Override
    public long getCooldown(Player player) {
        return 90_000; // 90 Sekunden Cooldown
    }

    @Override
    public void useAbility(Player player) {
        UUID uuid = player.getUniqueId();

        if (Custom_Abilitys.isOnCooldown(uuid, getName())) {
            player.sendMessage(ChatColor.RED + "Diese Fähigkeit ist noch im Cooldown!");
            return;
        }

        active.add(uuid);
        player.sendMessage(ChatColor.GOLD + "Du bist jetzt 15 Sekunden lang unverwundbar!");

        Custom_Abilitys.setCooldown(uuid, getName(), getCooldown(player));

        Bukkit.getScheduler().runTaskLater(Custom_Abilitys.getInstance(), () -> {
            active.remove(uuid);
            player.sendMessage(ChatColor.GRAY + "§7Immortaly ist vorbei.");
        }, 20 * 15); // 15 Sekunden
    }

    @Override
    public void onEquip(Player player) {
        Bukkit.getPluginManager().registerEvents(this, Custom_Abilitys.getInstance());
    }

    @Override
    public void onUnequip(Player player) {
        active.remove(player.getUniqueId());
        EntityDamageByEntityEvent.getHandlerList().unregister(this);
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        Player target = (Player) event.getEntity();
        if (!active.contains(target.getUniqueId())) return;

        event.setCancelled(true);

        // Amboss-Sound beim Angreifer
        Entity damager = event.getDamager();
        damager.getWorld().playSound(damager.getLocation(), Sound.BLOCK_ANVIL_LAND, 1f, 1f);
    }
}
