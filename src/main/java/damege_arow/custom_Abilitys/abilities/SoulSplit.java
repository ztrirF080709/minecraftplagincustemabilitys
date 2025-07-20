package damege_arow.custom_Abilitys.abilities;

import damege_arow.custom_Abilitys.Ability;
import damege_arow.custom_Abilitys.Custom_Abilitys;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class SoulSplit implements Ability, Listener {

    private final Set<UUID> armed = new HashSet<>();
    private final Map<UUID, ItemStack[]> storedInventory = new HashMap<>();
    private final Map<UUID, ArmorStand> soulBodies = new HashMap<>();

    @Override
    public String getName() {
        return "SoulSplit";
    }

    @Override
    public String getDisplayName() {
        return "§5SoulSplit";
    }

    @Override
    public long getCooldown(Player player) {
        int seconds = Custom_Abilitys.getInstance().getConfig().getInt("SoulSplit.cooldown", 60);
        return player.getInventory().contains(Material.DRAGON_EGG) ? seconds * 500L : seconds * 1000L;
    }

    @Override
    public void useAbility(Player player) {
        if (Custom_Abilitys.isOnCooldown(player.getUniqueId(), getName())) {
            player.sendMessage(ChatColor.RED + "Diese Fähigkeit ist noch im Cooldown!");
            return;
        }

        armed.add(player.getUniqueId());
        player.sendMessage(ChatColor.DARK_PURPLE + "§5Dein nächster Schlag wird §lSoulSplit§5 aktivieren!");
        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_AMBIENT, 1f, 1f);
        Custom_Abilitys.setCooldown(player.getUniqueId(), getName(), getCooldown(player));
    }

    @EventHandler
    public void onHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player damager)) return;
        if (!(event.getEntity() instanceof Player target)) return;
        if (!armed.contains(damager.getUniqueId())) return;

        armed.remove(damager.getUniqueId());

        // Inventar sichern
        ItemStack[] contents = target.getInventory().getContents();
        storedInventory.put(target.getUniqueId(), contents);
        target.getInventory().clear();

        // Fake-Körper spawnen
        Location loc = target.getLocation().clone();
        ArmorStand fake = loc.getWorld().spawn(loc, ArmorStand.class);
        fake.setVisible(false);
        fake.setCustomName("§7" + target.getName() + "'s Körper");
        fake.setCustomNameVisible(true);
        fake.setGravity(false);
        fake.setInvulnerable(true);
        soulBodies.put(target.getUniqueId(), fake);

        // Spieler als "Seele" — bleibt im Survival, aber kann fliegen
        target.setGameMode(GameMode.SURVIVAL);
        target.setAllowFlight(true);
        target.setFlying(true);

        // Rückstoß
        target.setVelocity(target.getLocation().getDirection().multiply(-1.5).setY(0.5));

        target.sendMessage(ChatColor.LIGHT_PURPLE + "§dDu hast deinen Körper verlassen. Kehre zurück, um dich zu vereinen.");

        // Flug beibehalten
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!target.isOnline() || !soulBodies.containsKey(target.getUniqueId())) {
                    cancel();
                    return;
                }

                target.setAllowFlight(true);
                target.setFlying(true);
            }
        }.runTaskTimer(Custom_Abilitys.getInstance(), 0L, 20L);

        // Rückkehr-Check
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!target.isOnline() || !soulBodies.containsKey(target.getUniqueId())) {
                    cancel();
                    return;
                }

                if (target.getLocation().distanceSquared(fake.getLocation()) <= 2.25) {
                    target.sendMessage(ChatColor.GREEN + "§aDu hast deinen Körper wieder betreten!");

                    // Rückgabe
                    target.setAllowFlight(false);
                    target.setFlying(false);
                    target.getInventory().setContents(storedInventory.get(target.getUniqueId()));
                    storedInventory.remove(target.getUniqueId());

                    // Fake entfernen
                    fake.remove();
                    soulBodies.remove(target.getUniqueId());

                    cancel();
                }
            }
        }.runTaskTimer(Custom_Abilitys.getInstance(), 0L, 10L);
    }

    @Override
    public void onEquip(Player player) {
        Bukkit.getPluginManager().registerEvents(this, Custom_Abilitys.getInstance());
    }

    @Override
    public void onUnequip(Player player) {
        armed.remove(player.getUniqueId());
        soulBodies.remove(player.getUniqueId());
        storedInventory.remove(player.getUniqueId());
        EntityDamageByEntityEvent.getHandlerList().unregister(this);
    }
}
