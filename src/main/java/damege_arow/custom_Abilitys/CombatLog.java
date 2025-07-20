package damege_arow.custom_Abilitys;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class CombatLog implements Listener {

    private static final Map<UUID, Long> combatTags = new HashMap<>();
    private final JavaPlugin plugin;

    public CombatLog(JavaPlugin plugin) {
        this.plugin = plugin;
        startCombatTagCleaner();
    }

    // ✳ Spieler beim Angriff taggen
    @EventHandler
    public void onCombat(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;
        if (!(event.getDamager() instanceof Player damager)) return;

        tag(victim);
        tag(damager);

        damager.sendMessage(ChatColor.RED + "§cDu bist jetzt im Kampf! (15s)");
        victim.sendMessage(ChatColor.RED + "§cDu wurdest angegriffen! Kampf-Modus aktiviert (15s)");
    }

    // ✳ Elytra blockieren, wenn getaggt
    @EventHandler
    public void onElytraUse(PlayerToggleFlightEvent event) {
        Player player = event.getPlayer();
        if (isTagged(player) &&
                player.getInventory().getChestplate() != null &&
                player.getInventory().getChestplate().getType() == Material.ELYTRA) {

            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "§cDu kannst im Kampf keine Elytra benutzen!");
        }
    }

    // ✳ Spieler beim Logout töten, wenn getaggt
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (isTagged(player)) {
            player.setHealth(0);
        }
        untag(player);
    }

    // ✳ CombatTag setzen
    private void tag(Player player) {
        combatTags.put(player.getUniqueId(), System.currentTimeMillis() + 15_000);
    }

    // ✳ CombatTag prüfen
    private boolean isTagged(Player player) {
        UUID uuid = player.getUniqueId();
        return combatTags.containsKey(uuid) && combatTags.get(uuid) > System.currentTimeMillis();
    }

    // ✳ CombatTag entfernen
    private void untag(Player player) {
        combatTags.remove(player.getUniqueId());
    }

    // ✳ Aufräumtask – optional, sauber
    private void startCombatTagCleaner() {
        new BukkitRunnable() {
            @Override
            public void run() {
                long now = System.currentTimeMillis();
                combatTags.entrySet().removeIf(e -> e.getValue() <= now);
            }
        }.runTaskTimer(plugin, 20 * 10, 20 * 10); // alle 10 Sekunden
    }
}
