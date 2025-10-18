package damege_arow.custom_Abilitys.abilities;

import damege_arow.custom_Abilitys.Ability;
import damege_arow.custom_Abilitys.Custom_Abilitys;
import org.bukkit.*;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupArrowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class RapidTripleBowAbility implements Ability, Listener {

    private final Plugin plugin = Custom_Abilitys.getInstance();
    private final Set<UUID> explosiveNextShot = new HashSet<>();

    @Override
    public String getName() {
        return "RapidTripleBow";
    }

    @Override
    public String getDisplayName() {
        return ChatColor.GOLD + "Rapid Triple Bow";
    }

    @Override
    public long getCooldown(Player player) {
        return plugin.getConfig().getLong("rapid_triple_bow.cooldown", 30L) * 1000;
    }

    @Override
    public void useAbility(Player player) {
        explosiveNextShot.add(player.getUniqueId());
        player.sendMessage(ChatColor.RED + "Dein nächster Schuss wird EXPLOSIV!");
        player.playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 1f, 1f);

        // Entfernt nach 10 Sekunden, falls nicht geschossen wurde
        new BukkitRunnable() {
            @Override
            public void run() {
                explosiveNextShot.remove(player.getUniqueId());
            }
        }.runTaskLater(plugin, 200L);
    }

    @Override
    public void onEquip(Player player) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void onUnequip(Player player) {
        HandlerList.unregisterAll(this);
    }

    // Sofortschuss bei Rechtsklick
    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (player.getInventory().getItemInMainHand().getType() == Material.BOW) {
                event.setCancelled(true);
                shootTripleArrow(player);
            }
        }
    }

    private void shootTripleArrow(Player player) {
        ItemStack bow = player.getInventory().getItemInMainHand();
        Location eye = player.getEyeLocation();
        Vector direction = eye.getDirection().normalize();

        for (int i = -1; i <= 1; i++) {
            Vector spread = direction.clone().add(new Vector(i * 0.05, 0, 0)).normalize();
            Arrow arrow = player.launchProjectile(Arrow.class);
            arrow.setVelocity(spread.multiply(3));
            arrow.setCritical(true);
            arrow.setShooter(player);

            // Keine Pfeile aufsammelbar
            arrow.setPickupStatus(Arrow.PickupStatus.DISALLOWED);

            // Verzauberungen übernehmen
            applyBowEnchantments(arrow, bow);

            // Explosionspfeil
            if (i == 0 && explosiveNextShot.contains(player.getUniqueId())) {
                arrow.setCustomName("ExplosiveArrow");
                arrow.setCustomNameVisible(false);
            }
        }

        explosiveNextShot.remove(player.getUniqueId());
        player.playSound(player.getLocation(), Sound.ENTITY_ARROW_SHOOT, 1f, 1f);
    }

    private void applyBowEnchantments(Arrow arrow, ItemStack bow) {
        if (!bow.hasItemMeta()) return;
        var enchants = bow.getItemMeta().getEnchants();

        enchants.forEach((enchant, level) -> {
            switch (enchant.getKey().getKey()) {
                case "flame" -> arrow.setFireTicks(100 * level);
                case "punch" -> arrow.setKnockbackStrength(level);
                case "power" -> arrow.setDamage(arrow.getDamage() * (1.25 + (0.25 * (level - 1))));
                // Infinity ignoriert, da keine Pfeile nötig
            }
        });
    }

    @EventHandler
    public void onArrowHit(ProjectileHitEvent event) {
        if (event.getEntity() instanceof Arrow arrow && "ExplosiveArrow".equals(arrow.getCustomName())) {
            Location loc = arrow.getLocation();
            arrow.remove();
            loc.getWorld().createExplosion(loc, 3f, false, false);

            if (arrow.getShooter() instanceof Player shooter) {
                shooter.playSound(shooter.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1f, 1f);
            }
        }
    }

    // verhindert das Aufsammeln aller Pfeile
    @EventHandler
    public void onPickupArrow(PlayerPickupArrowEvent event) {
        event.setCancelled(true);
    }
}
