package damege_arow.custom_Abilitys;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class ScrollListener implements Listener {

    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() != Material.PAPER) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        NamespacedKey key = new NamespacedKey(Custom_Abilitys.getInstance(), "scroll_ability");
        if (!meta.getPersistentDataContainer().has(key, PersistentDataType.STRING)) return;

        String abilityName = meta.getPersistentDataContainer().get(key, PersistentDataType.STRING);
        Ability ability = Custom_Abilitys.getAbilities().stream()
                .filter(a -> a.getName().equalsIgnoreCase(abilityName))
                .findFirst().orElse(null);

        if (ability == null) {
            player.sendMessage(ChatColor.RED + "Unbekannte Fähigkeit auf Scroll.");
            return;
        }

        Ability[] current = Custom_Abilitys.getAbilities(player.getUniqueId());
        int max = player.getInventory().contains(Material.DRAGON_EGG) ? 2 : 1;

        for (int i = 0; i < max; i++) {
            if (i >= current.length || current[i] == null) {
                if (i >= current.length) {
                    Ability[] expanded = new Ability[max];
                    System.arraycopy(current, 0, expanded, 0, current.length);
                    current = expanded;
                }
                current[i] = ability;
                ability.onEquip(player);
                player.getInventory().removeItem(item);
                Custom_Abilitys.setAbilities(player.getUniqueId(), current);
                player.sendMessage(ChatColor.GREEN + "Fähigkeit ausgerüstet: " + ability.getDisplayName());
                return;
            }
        }

        player.sendMessage(ChatColor.YELLOW + "Du hast bereits alle Fähigkeitsslots belegt.");
    }
}
