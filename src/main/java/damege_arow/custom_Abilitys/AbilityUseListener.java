package damege_arow.custom_Abilitys;

import damege_arow.custom_Abilitys.AbilityCommand;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AbilityUseListener implements Listener {

    private final Map<UUID, Integer> selectedSlot = new HashMap<>();

    @EventHandler
    public void onHotbarScroll(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        // ⛔ Shift + Scroll blockieren, wenn deaktiviert
        if (player.isSneaking() && AbilityCommand.abilityControlDisabled.contains(uuid)) return;
        if (!player.isSneaking()) return;

        Ability[] abilities = Custom_Abilitys.getAbilities(uuid);
        if (abilities.length <= 1) return;

        int current = selectedSlot.getOrDefault(uuid, 0);
        int newIndex = (current + 1) % abilities.length;
        selectedSlot.put(uuid, newIndex);

        Ability selected = abilities[newIndex];
        player.sendMessage(ChatColor.AQUA + "Aktive Fähigkeit: " + (selected != null ? selected.getDisplayName() : "Keine"));
    }

    @EventHandler
    public void onShiftRightClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (!player.isSneaking()) return;

        // ⛔ Blockieren, wenn man nur per Command aktivieren darf
        if (AbilityCommand.abilityControlDisabled.contains(uuid)) return;

        Ability[] abilities = Custom_Abilitys.getAbilities(uuid);
        if (abilities.length == 0) {
            player.sendMessage(ChatColor.RED + "Keine Fähigkeit ausgerüstet.");
            return;
        }

        int index = selectedSlot.getOrDefault(uuid, 0);
        if (index >= abilities.length || abilities[index] == null) {
            player.sendMessage(ChatColor.RED + "Keine Fähigkeit im aktiven Slot.");
            return;
        }

        abilities[index].useAbility(player);
    }

    // Optional: Getter, falls du den Slot von außen brauchst
    public int getSelectedSlot(UUID uuid) {
        return selectedSlot.getOrDefault(uuid, 0);
    }
}
