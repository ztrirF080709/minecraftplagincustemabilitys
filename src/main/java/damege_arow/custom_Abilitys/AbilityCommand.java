package damege_arow.custom_Abilitys.commands;

import damege_arow.custom_Abilitys.Ability;
import damege_arow.custom_Abilitys.Custom_Abilitys;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;

public class AbilityCommand implements CommandExecutor, TabCompleter {

    // Spieler, bei denen Shift/Maus deaktiviert ist
    public static final Set<UUID> abilityControlDisabled = new HashSet<>();
    private static final Map<UUID, Integer> selectedSlot = new HashMap<>();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage("Nur Spieler können diesen Befehl verwenden.");
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(ChatColor.RED + "Verwendung: /a <1|2|toggle|shift>");
            return true;
        }

        String sub = args[0].toLowerCase();
        UUID uuid = player.getUniqueId();
        Ability[] equipped = Custom_Abilitys.getAbilities(uuid);

        switch (sub) {
            case "1" -> {
                if (equipped.length > 0 && equipped[0] != null) {
                    equipped[0].useAbility(player);
                } else {
                    player.sendMessage(ChatColor.RED + "Keine erste Fähigkeit ausgerüstet.");
                }
            }
            case "2" -> {
                if (!player.getInventory().contains(Material.DRAGON_EGG)) {
                    player.sendMessage(ChatColor.RED + "Du brauchst ein Drachenei im Inventar für eine zweite Fähigkeit.");
                    return true;
                }
                if (equipped.length > 1 && equipped[1] != null) {
                    equipped[1].useAbility(player);
                } else {
                    player.sendMessage(ChatColor.RED + "Keine zweite Fähigkeit ausgerüstet.");
                }
            }
            case "toggle" -> {
                if (abilityControlDisabled.contains(uuid)) {
                    abilityControlDisabled.remove(uuid);
                    player.sendMessage(ChatColor.GREEN + "Shift+Rechtsklick/Maussteuerung aktiviert.");
                } else {
                    abilityControlDisabled.add(uuid);
                    player.sendMessage(ChatColor.YELLOW + "Shift+Rechtsklick/Maussteuerung deaktiviert.");
                }
            }
            case "shift" -> {
                int max = (player.getInventory().contains(Material.DRAGON_EGG)) ? 2 : 1;
                int current = selectedSlot.getOrDefault(uuid, 0);
                int next = (current + 1) % max;

                selectedSlot.put(uuid, next);
                Ability chosen = Custom_Abilitys.getAbilities(uuid)[next];
                player.sendMessage(ChatColor.GRAY + "Ausgewählte Fähigkeit: " +
                        (chosen != null ? chosen.getDisplayName() : ChatColor.RED + "keine"));
            }
            default -> {
                player.sendMessage(ChatColor.RED + "Ungültiges Argument. Nutze: 1, 2, toggle, shift");
            }
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("1", "2", "toggle", "shift");
        }
        return Collections.emptyList();
    }

    public static int getSelectedSlot(UUID uuid) {
        return selectedSlot.getOrDefault(uuid, 0);
    }
}
