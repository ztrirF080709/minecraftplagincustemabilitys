package damege_arow.custom_Abilitys;

import org.bukkit.entity.Player;

public interface Ability {
    String getName();
    String getDisplayName();
    long getCooldown(Player player);
    void useAbility(Player player);
    void onEquip(Player player);
    void onUnequip(Player player);
}
