package damege_arow.custom_Abilitys.abilities;

import damege_arow.custom_Abilitys.Ability;
import damege_arow.custom_Abilitys.Custom_Abilitys;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class Emerald implements Ability, Listener {

    private final Map<UUID, Long> activeDebuff = new HashMap<>();
    private final Set<UUID> waitingForHit = new HashSet<>();
    private final FileConfiguration config = Custom_Abilitys.getInstance().getConfig();

    public Emerald() {
        Bukkit.getPluginManager().registerEvents(this, Custom_Abilitys.getInstance());
    }

    @Override
    public String getName() {
        return "Emerald";
    }

    @Override
    public String getDisplayName() {
        return "§aEmerald";
    }

    @Override
    public long getCooldown(Player player) {
        return config.getLong("emerald.cooldown", 30) * 1000L;
    }

    @Override
    public void useAbility(Player player) {
        if (Custom_Abilitys.isOnCooldown(player.getUniqueId(), getName())) {
            player.sendMessage(ChatColor.RED + "Diese Fähigkeit ist noch im Cooldown!");
            return;
        }

        waitingForHit.add(player.getUniqueId());
        Custom_Abilitys.setCooldown(player.getUniqueId(), getName(), getCooldown(player));
        player.sendMessage(ChatColor.GREEN + "§aEmerald-Effekt aktiviert. Schlage einen Spieler, um ihn zu verfluchen.");
    }

    @Override
    public void onEquip(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.HERO_OF_THE_VILLAGE, Integer.MAX_VALUE, 254, true, false));
    }

    @Override
    public void onUnequip(Player player) {
        player.removePotionEffect(PotionEffectType.HERO_OF_THE_VILLAGE);
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player damager)) return;
        if (!(event.getEntity() instanceof Player target)) return;

        UUID uuid = damager.getUniqueId();
        if (!waitingForHit.contains(uuid)) return;

        waitingForHit.remove(uuid);

        long debuffDuration = config.getLong("emerald.debuff_duration", 5) * 1000L;
        activeDebuff.put(target.getUniqueId(), System.currentTimeMillis() + debuffDuration);

        target.sendMessage(ChatColor.DARK_RED + "§lDu wurdest verflucht! Fähigkeiten blockiert.");
        damager.sendMessage(ChatColor.GREEN + "§aDu hast " + target.getName() + " verflucht.");
    }

    @EventHandler
    public void onActionBarUpdate(org.bukkit.event.player.PlayerMoveEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        long until = activeDebuff.getOrDefault(uuid, 0L);
        if (System.currentTimeMillis() < until) {
            player.sendActionBar("§c⛔ Geloggt");
        }
    }

    @EventHandler
    public void onAnyUse(PlayerInteractEvent event) {
        cancelIfDebuffed(event.getPlayer(), event);
    }

    @EventHandler
    public void onUseItem(PlayerItemConsumeEvent event) {
        cancelIfDebuffed(event.getPlayer(), event);
    }

    @EventHandler
    public void onItemHeld(PlayerItemHeldEvent event) {
        cancelIfDebuffed(event.getPlayer(), event);
    }

    @EventHandler
    public void onSwap(PlayerSwapHandItemsEvent event) {
        cancelIfDebuffed(event.getPlayer(), event);
    }

    private void cancelIfDebuffed(Player player, Cancellable event) {
        long until = activeDebuff.getOrDefault(player.getUniqueId(), 0L);
        if (System.currentTimeMillis() > until) return;

        double chance = config.getDouble("emerald.fail_chance", 0.3);
        if (Math.random() < chance) {
            event.setCancelled(true);
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.5f, 0.7f);
            player.sendMessage(ChatColor.RED + "§cDein Versuch ist fehlgeschlagen! (Verfluchung)");
        }
    }

    public static boolean isDebuffed(Player player) {
        long until = Custom_Abilitys.getInstance().getConfig().getLong("emerald.debuff_duration", 5) * 1000L;
        return System.currentTimeMillis() < until;
    }
}
