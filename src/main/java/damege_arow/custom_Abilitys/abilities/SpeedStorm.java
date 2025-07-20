package damege_arow.custom_Abilitys.abilities;

import damege_arow.custom_Abilitys.Ability;
import damege_arow.custom_Abilitys.Custom_Abilitys;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class SpeedStorm implements Ability {

    private final Map<UUID, Double> savedBaseAttackSpeed = new HashMap<>();

    @Override
    public String getName() {
        return "SpeedStorm";
    }

    @Override
    public String getDisplayName() {
        return "§aSpeedStorm";
    }

    @Override
    public long getCooldown(Player player) {
        return player.getInventory().contains(Material.DRAGON_EGG) ? 15_000 : 30_000;
    }

    @Override
    public void useAbility(Player player) {
        UUID uuid = player.getUniqueId();

        if (Custom_Abilitys.isOnCooldown(uuid, getName())) {
            player.sendMessage(ChatColor.RED + "Diese Fähigkeit ist noch im Cooldown!");
            return;
        }

        // Speed II
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * 10, 1));

        // Angriffsgeschwindigkeit direkt setzen (betrifft ALLE Waffen)
        AttributeInstance attr = player.getAttribute(Attribute.ATTACK_SPEED);
        if (attr != null) {
            // Ursprungswert sichern (nur einmal)
            if (!savedBaseAttackSpeed.containsKey(uuid)) {
                savedBaseAttackSpeed.put(uuid, attr.getBaseValue());
            }

            // Auf schnellen Wert setzen (z. B. 4.0 = Schwert-Geschwindigkeit)
            attr.setBaseValue(4.0);
        }

        // Sound & Cooldown
        player.playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1f, 1f);
        Custom_Abilitys.setCooldown(uuid, getName(), getCooldown(player));

        // Nach 10 Sekunden zurücksetzen
        Bukkit.getScheduler().runTaskLater(Custom_Abilitys.getInstance(), () -> {
            AttributeInstance attr2 = player.getAttribute(Attribute.ATTACK_SPEED);
            if (attr2 != null && savedBaseAttackSpeed.containsKey(uuid)) {
                attr2.setBaseValue(savedBaseAttackSpeed.get(uuid));
                savedBaseAttackSpeed.remove(uuid);
            }
            player.sendMessage(ChatColor.GRAY + "§7SpeedStorm ist vorbei.");
        }, 20 * 10);
    }

    @Override
    public void onEquip(Player player) {
        // nichts nötig
    }

    @Override
    public void onUnequip(Player player) {
        UUID uuid = player.getUniqueId();
        AttributeInstance attr = player.getAttribute(Attribute.ATTACK_SPEED);
        if (attr != null && savedBaseAttackSpeed.containsKey(uuid)) {
            attr.setBaseValue(savedBaseAttackSpeed.get(uuid));
            savedBaseAttackSpeed.remove(uuid);
        }
    }
}
