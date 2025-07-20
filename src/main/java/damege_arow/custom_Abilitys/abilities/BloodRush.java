package damege_arow.custom_Abilitys.abilities;

import damege_arow.custom_Abilitys.Ability;
import damege_arow.custom_Abilitys.Custom_Abilitys;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class BloodRush implements Ability {

    @Override
    public String getName() {
        return "BloodRush";
    }

    @Override
    public String getDisplayName() {
        return "§cBloodRush";
    }

    @Override
    public long getCooldown(Player player) {
        int seconds = Custom_Abilitys.getInstance().getConfig().getInt("BloodRush.cooldown", 25);
        return player.getInventory().contains(Material.DRAGON_EGG) ? seconds * 500L : seconds * 1000L;
    }

    @Override
    public void useAbility(Player player) {
        if (Custom_Abilitys.isOnCooldown(player.getUniqueId(), getName())) {
            player.sendMessage(ChatColor.RED + "Diese Fähigkeit ist noch im Cooldown!");
            return;
        }

        Custom_Abilitys.setCooldown(player.getUniqueId(), getName(), getCooldown(player));

        // Stärke III für 5 Sekunden
        player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 20 * 5 , 2));
        player.sendMessage(ChatColor.DARK_RED + "§cDu verfällst in Blutrausch! §7(Stärke III)");

        // Partikel & Sound
        player.getWorld().spawnParticle(Particle.DUST, player.getLocation(), 40, 0.6, 1, 0.6,
                new Particle.DustOptions(Color.RED, 1));
        player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 1f, 0.6f);

        // Nach 5 Sekunden: Rückwirkung
        Bukkit.getScheduler().runTaskLater(Custom_Abilitys.getInstance(), () -> {
            player.damage(4.0); // 2 Herzen
            player.sendMessage(ChatColor.GRAY + "§7Der Blutrausch endet... du spürst den Schmerz.");
        }, 20L * 5);
    }

    @Override
    public void onEquip(Player player) {}

    @Override
    public void onUnequip(Player player) {}
}
