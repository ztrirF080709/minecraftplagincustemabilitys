package damege_arow.custom_Abilitys;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class PlayerDeathListener implements Listener {

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Ability[] abilities = Custom_Abilitys.getAbilities(player.getUniqueId());

        for (Ability ability : abilities) {
            if (ability == null) continue;

            // Scroll droppen
            ItemStack scroll = Custom_Abilitys.createScroll(ability.getName());
            ItemMeta meta = scroll.getItemMeta();
            meta.setDisplayName(ChatColor.RED + "Scroll: " + ability.getName());
            meta.setUnbreakable(true);
            scroll.setItemMeta(meta);

            // Drop mit Glowing
            player.getWorld().dropItemNaturally(player.getLocation(), scroll);

            // Entfernen der Fähigkeit
            ability.onUnequip(player);
        }

        // Fähigkeiten-Slots leeren
        Custom_Abilitys.setAbilities(player.getUniqueId(), new Ability[1]);
    }
}
