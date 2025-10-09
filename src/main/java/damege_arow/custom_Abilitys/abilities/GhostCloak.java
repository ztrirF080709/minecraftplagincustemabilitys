// GhostCloak.java
package damege_arow.custom_Abilitys.abilities;

import damege_arow.custom_Abilitys.Ability;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class GhostCloak implements Ability {
    private boolean invisible = false;

    @Override
    public String getName() {
        return "GhostCloak";
    }

    @Override
    public String getDisplayName() {
        return ChatColor.GRAY + "Ghost Cloak";
    }

    @Override
    public long getCooldown(Player player) {
        return 0; // Kein Cooldown, sofort toggelbar
    }

    @Override
    public void useAbility(Player player) {
        invisible = !invisible;

        if (invisible) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0, false, false));
            player.sendMessage(ChatColor.DARK_GRAY + "[GHOST] Unsichtbarkeit aktiviert.");
        } else {
            player.removePotionEffect(PotionEffectType.INVISIBILITY);
            player.sendMessage(ChatColor.GRAY + "[GHOST] Unsichtbarkeit deaktiviert.");
        }
    }

    @Override
    public void onEquip(Player player) {}
    @Override
    public void onUnequip(Player player) {}
}
