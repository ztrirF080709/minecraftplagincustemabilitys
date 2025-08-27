package damege_arow.custom_Abilitys;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.block.TileState;
import org.bukkit.block.data.type.Barrel;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class BattleRoyaleManager implements CommandExecutor, Listener {

    private final JavaPlugin plugin;
    private boolean gameRunning = false;
    private BukkitRunnable borderTask;
    private BukkitRunnable dropTask;
    private final Random random = new Random();

    public BattleRoyaleManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        switch (label.toLowerCase()) {
            case "start" -> startGame();
            case "resetworld" -> resetWorlds(sender);
            case "testdrop" -> spawnSupplyDrop(Bukkit.getWorlds().get(0));
            case "startborder" -> startBorderShrink();
        }
        return true;
    }

    private void startGame() {
        World world = Bukkit.getWorlds().get(0);
        world.getWorldBorder().setCenter(0, 0);
        world.getWorldBorder().setSize(500);
        world.getWorldBorder().setDamageAmount(2.0);

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.teleport(new Location(world, 0, world.getHighestBlockYAt(0, 0) + 10, 0));
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 20 * 20, 1));
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, getFarmPhaseDurationTicks(), 2));
            player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, getFarmPhaseDurationTicks(), 1));
        }

        gameRunning = true;

        dropTask = new BukkitRunnable() {
            @Override
            public void run() {
                spawnSupplyDrop(world);
            }
        };
        dropTask.runTaskTimer(plugin, 0, 5 * 60 * 20);

        new BukkitRunnable() {
            @Override
            public void run() {
                startBorderShrink();
            }
        }.runTaskLater(plugin, getFarmPhaseDurationTicks());
    }

    private int getFarmPhaseDurationTicks() {
        return plugin.getConfig().getInt("farm-phase", 30) * 60 * 20;
    }

    private void spawnSupplyDrop(World world) {
        int x = random.nextInt(500) - 250;
        int z = random.nextInt(500) - 250;
        int y = world.getHighestBlockYAt(x, z) + 1;
        Location dropLoc = new Location(world, x, y, z);

        // Set barrel block
        Block block = world.getBlockAt(dropLoc);
        block.setType(Material.BARREL);

        BlockState state = block.getState();
        if (!(state instanceof Container container)) return;
        Inventory inv = container.getInventory();

        List<ItemStack> loot = getConfiguredSupplyItems();
        for (ItemStack item : loot) {
            inv.addItem(item);
        }

        Bukkit.broadcastMessage(ChatColor.GOLD + "Ein Supply Drop (Barrel) erscheint bei X: " + x + ", Z: " + z);
    }

    private List<ItemStack> getConfiguredSupplyItems() {
        List<ItemStack> items = new ArrayList<>();
        FileConfiguration config = plugin.getConfig();
        List<String> dropConfig = config.getStringList("supply-drops");

        for (String entry : dropConfig) {
            try {
                String[] parts = entry.split(";");
                Material mat = Material.valueOf(parts[0]);
                String[] amountRange = parts[1].split("-");
                int min = Integer.parseInt(amountRange[0]);
                int max = Integer.parseInt(amountRange[1]);
                double chance = Double.parseDouble(parts[2]);
                if (Math.random() <= chance) {
                    int amount = min + new Random().nextInt(max - min + 1);
                    items.add(new ItemStack(mat, amount));
                }
            } catch (Exception e) {
                Bukkit.getLogger().warning("Ungültiger Supply-Drop-Eintrag in der Config: " + entry);
            }
        }
        return items;
    }

    private void startBorderShrink() {
        if (borderTask != null) borderTask.cancel();

        World world = Bukkit.getWorlds().get(0);
        WorldBorder border = world.getWorldBorder();
        Bukkit.broadcastMessage(ChatColor.RED + "Die Border beginnt sich zu verkleinern!");

        borderTask = new BukkitRunnable() {
            double currentSize = border.getSize();
            final double targetSize = 10.0;
            final double shrinkAmount = 1.0;

            @Override
            public void run() {
                if (currentSize <= targetSize) {
                    this.cancel();
                    Bukkit.broadcastMessage(ChatColor.GOLD + "Die Border hat ihre minimale Größe erreicht.");
                    return;
                }
                currentSize -= shrinkAmount;
                border.setSize(currentSize);
            }
        };
        borderTask.runTaskTimer(plugin, 0, 20 * 5);
    }

    private void resetWorlds(CommandSender sender) {
        Bukkit.broadcastMessage(ChatColor.DARK_RED + "Server startet neu und generiert neue Welten...");
        Bukkit.getScheduler().runTaskLater(plugin, () -> Bukkit.spigot().restart(), 20L);
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        if (meta != null) {
            meta.setOwningPlayer(player);
            meta.setDisplayName(ChatColor.GOLD + player.getName() + "'s Kopf");
            head.setItemMeta(meta);
        }
        player.getWorld().dropItemNaturally(player.getLocation(), head);
    }
}
    