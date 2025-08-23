package damege_arow.custom_Abilitys.abilities;

import damege_arow.custom_Abilitys.Ability;
import damege_arow.custom_Abilitys.Custom_Abilitys;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class DomainExpansion implements Ability {

    private final Map<Location, Material> replacedBlocks = new HashMap<>();

    @Override
    public String getName() {
        return "DomainExpansion";
    }

    @Override
    public String getDisplayName() {
        return "§5Domain Expansion";
    }

    @Override
    public long getCooldown(Player player) {
        // Cooldown in Sekunden → umrechnen in Millisekunden
        int seconds = Custom_Abilitys.getInstance().getConfig().getInt("domain.cooldown", 300);
        return seconds * 1000L;
    }

    @Override
    public void useAbility(Player player) {
        Plugin plugin = Custom_Abilitys.getInstance();
        int radius = plugin.getConfig().getInt("domain.radius", 20);
        int durationSeconds = plugin.getConfig().getInt("domain.duration", 20);
        long durationTicks = durationSeconds * 20L;

        Location center = player.getLocation();
        World world = center.getWorld();
        if (world == null) return;

        Set<Location> changed = new HashSet<>();

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    Location loc = center.clone().add(x, y, z);
                    if (loc.distance(center) > radius) continue;

                    Block block = world.getBlockAt(loc);
                    if (block.getType().isAir()) continue;
                    if (block.getType() == Material.SCULK) continue;

                    replacedBlocks.put(block.getLocation(), block.getType());
                    block.setType(Material.SCULK);
                    changed.add(block.getLocation());
                }
            }
        }

        player.sendMessage(ChatColor.DARK_PURPLE + "Domain Expansion entfesselt!");
        player.playSound(center, Sound.BLOCK_SCULK_SHRIEKER_SHRIEK, 1f, 0.5f);

        // Rückverwandlung
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Map.Entry<Location, Material> entry : replacedBlocks.entrySet()) {
                    Block block = entry.getKey().getBlock();
                    if (block.getType() == Material.SCULK) {
                        block.setType(entry.getValue());
                    }
                }
                replacedBlocks.clear();
                player.sendMessage(ChatColor.GRAY + "§7Die Domain verschwindet...");
            }
        }.runTaskLater(plugin, durationTicks);

        Custom_Abilitys.setCooldown(player.getUniqueId(), getName(), getCooldown(player));
    }

    @Override
    public void onEquip(Player player) {
        // Kein Effekt beim Ausrüsten
    }

    @Override
    public void onUnequip(Player player) {
        // Kein Effekt beim Entfernen
    }
}
