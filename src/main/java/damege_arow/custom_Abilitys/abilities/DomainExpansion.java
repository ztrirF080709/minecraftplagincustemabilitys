package damege_arow.custom_Abilitys.abilities;

import damege_arow.custom_Abilitys.Ability;
import damege_arow.custom_Abilitys.Custom_Abilitys;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class DomainExpansion implements Ability, Listener {

    private static final int RADIUS = 20;
    private static final long COOLDOWN = 5 * 60 * 1000; // 5 Minuten
    private final Map<Location, Material> replacedBlocks = new HashMap<>();

    private static final Set<Material> BLOCKS_TO_IGNORE = EnumSet.of(
            Material.CHEST, Material.TRAPPED_CHEST, Material.BARREL,
            Material.COMMAND_BLOCK, Material.REPEATING_COMMAND_BLOCK, Material.CHAIN_COMMAND_BLOCK,
            Material.SPAWNER, Material.SHULKER_BOX,
            Material.BEACON, Material.LECTERN, Material.JUKEBOX,
            Material.ENCHANTING_TABLE, Material.ANVIL, Material.GRINDSTONE,
            Material.HOPPER, Material.DROPPER, Material.DISPENSER
    );

    private boolean active = false;

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
        return COOLDOWN;
    }

    @Override
    public void useAbility(Player player) {
        UUID uuid = player.getUniqueId();
        if (Custom_Abilitys.isOnCooldown(uuid, getName())) {
            player.sendMessage(ChatColor.RED + "Diese Fähigkeit ist noch im Cooldown!");
            return;
        }

        Location center = player.getLocation().getBlock().getLocation().add(0.5, 0.5, 0.5);
        World world = player.getWorld();

        active = true;

        for (int x = -RADIUS; x <= RADIUS; x++) {
            for (int y = -RADIUS; y <= RADIUS; y++) {
                for (int z = -RADIUS; z <= RADIUS; z++) {
                    double distance = Math.sqrt(x * x + y * y + z * z);
                    if (Math.abs(distance - RADIUS) <= 0.5) {
                        Location loc = center.clone().add(x, y, z);
                        Block block = world.getBlockAt(loc);

                        if (!block.getType().isAir()) continue;
                        if (BLOCKS_TO_IGNORE.contains(block.getType())) continue;

                        replacedBlocks.put(loc, block.getType());
                        block.setType(Material.SCULK);
                    }
                }
            }
        }

        Custom_Abilitys.setCooldown(uuid, getName(), getCooldown(player));
        player.sendMessage(ChatColor.DARK_PURPLE + "Domain Expansion aktiviert!");

        new BukkitRunnable() {
            @Override
            public void run() {
                for (Map.Entry<Location, Material> entry : replacedBlocks.entrySet()) {
                    entry.getKey().getBlock().setType(entry.getValue());
                }
                replacedBlocks.clear();
                active = false;
                player.sendMessage(ChatColor.GRAY + "§7Domain Expansion ist beendet.");
            }
        }.runTaskLater(Custom_Abilitys.getInstance(), 20 * 20);
    }

    @Override
    public void onEquip(Player player) {
        Bukkit.getPluginManager().registerEvents(this, Custom_Abilitys.getInstance());
    }

    @Override
    public void onUnequip(Player player) {}

    // Verhindert das Zerstören von Domain-Blöcken
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!active) return;

        Location loc = event.getBlock().getLocation();
        if (replacedBlocks.containsKey(loc)) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "§cDu kannst die Domain-Wände nicht zerstören!");
        }
    }
}
