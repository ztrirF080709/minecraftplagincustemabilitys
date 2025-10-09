// Datei: Custom_Abilitys.java (vollständig aktualisiert)
package damege_arow.custom_Abilitys;

import damege_arow.custom_Abilitys.abilities.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class Custom_Abilitys extends JavaPlugin implements TabExecutor {

    private static final Map<UUID, Ability[]> playerAbilities = new HashMap<>();
    private static final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();
    private static final Map<String, Ability> abilities = new HashMap<>();
    private static Custom_Abilitys instance;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();
        getCommand("giveability").setExecutor(this);
        getCommand("giveability").setTabCompleter(this);
        getCommand("withdraw").setExecutor(this);
        getCommand("test").setExecutor(new TestCommand());
        getCommand("test").setTabCompleter(new TestCommand());
        getCommand("a").setExecutor(new AbilityCommand());
        getCommand("a").setTabCompleter(new AbilityCommand());

        // Fähigkeiten registrieren
        registerAbility(new SpeedStorm());
        registerAbility(new Dash());
        registerAbility(new Rewind());
        registerAbility(new BloodRush());
        registerAbility(new Stun());
        registerAbility(new Lightning());
        registerAbility(new Magma());
        registerAbility(new Healer());
        registerAbility(new Emerald());
        registerAbility(new Immortaly());






        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    sendActionBar(player);
                }
            }
        }.runTaskTimer(this, 0, 20);
    }

    public void registerAbility(Ability ability) {
        abilities.put(ability.getName(), ability);
    }

    public static Ability getAbility(String name) {
        return abilities.get(name);
    }

    public static Set<String> getAbilityNames() {
        return abilities.keySet();
    }

    public static Custom_Abilitys getInstance() {
        return instance;
    }

    public static Ability[] getAbilities(UUID uuid) {
        return playerAbilities.getOrDefault(uuid, new Ability[1]);
    }

    public static void setAbilities(UUID uuid, Ability[] a) {
        playerAbilities.put(uuid, a);
    }

    public static Map<String, Long> getPlayerCooldowns(UUID uuid) {
        return cooldowns.computeIfAbsent(uuid, k -> new HashMap<>());
    }

    public static long getCooldown(UUID uuid, String abilityName) {
        return getPlayerCooldowns(uuid).getOrDefault(abilityName, 0L) - System.currentTimeMillis();
    }

    public static boolean isOnCooldown(UUID uuid, String abilityName) {
        return getCooldown(uuid, abilityName) > 0;
    }

    public static void setCooldown(UUID uuid, String abilityName, long millis) {
        getPlayerCooldowns(uuid).put(abilityName, System.currentTimeMillis() + millis);
    }

    public static ItemStack createScroll(String name) {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.RED + "Scroll: " + name);
        meta.addEnchant(Enchantment.UNBREAKING, 1, true);
        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE);

        NamespacedKey key = new NamespacedKey(getInstance(), "scroll_ability");
        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, name);

        item.setItemMeta(meta);
        return item;
    }

    private void sendActionBar(Player player) {
        StringBuilder sb = new StringBuilder();
        Ability[] list = getAbilities(player.getUniqueId());
        boolean hasAbility = false;

        for (Ability ability : list) {
            if (ability == null) continue;
            hasAbility = true;
            long remaining = getCooldown(player.getUniqueId(), ability.getName());
            if (remaining <= 0) {
                sb.append(ChatColor.GREEN).append(ability.getDisplayName()).append(ChatColor.DARK_GRAY)
                        .append(" [").append(ChatColor.GREEN).append("Bereit").append(ChatColor.DARK_GRAY).append("] ");
            } else {
                sb.append(ChatColor.YELLOW).append(ability.getDisplayName()).append(ChatColor.DARK_GRAY)
                        .append(" [").append(ChatColor.RED).append(remaining / 1000).append("s").append(ChatColor.DARK_GRAY).append("] ");
            }
        }

        if (!hasAbility) {
            player.sendActionBar(ChatColor.RED + "Keine Fähigkeit ausgerüstet");
        } else {
            player.sendActionBar(sb.toString().trim());
        }
    }

    public static List<Ability> getAllAbilities() {
        return new ArrayList<>(abilities.values());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) return true;

        if (command.getName().equalsIgnoreCase("giveability")) {
            if (args.length != 1) {
                player.sendMessage(ChatColor.RED + "Verwendung: /giveability <name>");
                return true;
            }
            String name = args[0];
            Ability ability = getAbility(name);
            if (ability == null) {
                player.sendMessage(ChatColor.RED + "Fähigkeit nicht gefunden.");
                return true;
            }
            player.getInventory().addItem(createScroll(ability.getName()));
            player.sendMessage(ChatColor.GREEN + "Scroll gegeben: " + ability.getDisplayName());
            return true;
        }

        if (command.getName().equalsIgnoreCase("withdraw")) {
            Ability[] current = getAbilities(player.getUniqueId());
            for (int i = 0; i < current.length; i++) {
                if (current[i] != null) {
                    current[i].onUnequip(player);
                    player.getInventory().addItem(createScroll(current[i].getName()));
                    current[i] = null;
                }
            }
            setAbilities(player.getUniqueId(), current);
            player.sendMessage(ChatColor.YELLOW + "Fähigkeiten entfernt.");
            return true;
        }

        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (command.getName().equalsIgnoreCase("giveability") && args.length == 1) {
            String input = args[0].toLowerCase();
            List<String> completions = new ArrayList<>();
            for (String name : getAbilityNames()) {
                if (name.toLowerCase().startsWith(input)) {
                    completions.add(name);
                }
            }
            return completions;
        }
        return Collections.emptyList();
    }
}
