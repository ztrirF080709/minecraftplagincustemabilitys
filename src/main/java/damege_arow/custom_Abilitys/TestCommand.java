package damege_arow.custom_Abilitys;

import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TestCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Dieser Befehl kann nur von einem Spieler ausgeführt werden.");
            return true;
        }

        if (!player.isOp()) {
            player.sendMessage(ChatColor.RED + "Du hast keine Berechtigung, diesen Befehl zu verwenden.");
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(ChatColor.YELLOW + "Verwendung: /test <ability>");
            return true;
        }

        Ability ability = Custom_Abilitys.getAbility(args[0]);
        if (ability == null) {
            player.sendMessage(ChatColor.RED + "Unbekannte Fähigkeit: " + args[0]);
            return true;
        }

        ability.useAbility(player);
        player.sendMessage(ChatColor.GREEN + "Fähigkeit " + ability.getDisplayName() + " angewendet!");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            String input = args[0].toLowerCase();
            List<String> completions = new ArrayList<>();
            for (String name : Custom_Abilitys.getAbilityNames()) {
                if (name.toLowerCase().startsWith(input)) {
                    completions.add(name);
                }
            }
            return completions;
        }
        return Collections.emptyList();
    }
}
