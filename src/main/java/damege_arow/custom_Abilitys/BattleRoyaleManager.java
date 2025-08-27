package damege_arow.custom_Abilitys;

import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
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
            case "resetworld" -> resetWorlds();
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
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 20 * 20, 1)); // 20 Sekunden
        }

        gameRunning = true;

        dropTask = new BukkitRunnable() {
            @Override
            public void run() {
                spawnSupplyDrop(world);
            }
        };
        dropTask.runTaskTimer(plugin, 0, 5 * 60 * 20); // alle 5 Minuten

        new BukkitRunnable() {
            @Override
            public void run() {
                startBorderShrink();
            }
        }.runTaskLater(plugin, 30 * 60 * 20); // nach 30 Minuten
    }

    private void spawnSupplyDrop(World world) {
        int x = random.nextInt(500) - 250;
        int z = random.nextInt(500) - 250;
        int y = world.getHighestBlockYAt(x, z) + 10;
        Location dropLoc = new Location(world, x, y, z);

        Bukkit.broadcastMessage(ChatColor.GOLD + "§lSupply Drop bei X: " + x + " Z: " + z + "!");

        ItemStack[] drops = {
                new ItemStack(Material.DIAMOND, 3),
                new ItemStack(Material.NETHERITE_SCRAP, 1),
                new ItemStack(Material.NETHER_WART, 5),
                new ItemStack(Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE, 1),
                new ItemStack(Material.GOLDEN_APPLE, 1)
        };

        for (ItemStack item : drops) {
            world.dropItemNaturally(dropLoc, item);
        }
    }

    private void startBorderShrink() {
        if (borderTask != null) borderTask.cancel();

        World world = Bukkit.getWorlds().get(0);
        WorldBorder border = world.getWorldBorder();

        Bukkit.broadcastMessage(ChatColor.RED + "§cBorder beginnt sich zu verkleinern!");

        borderTask = new BukkitRunnable() {
            double currentSize = border.getSize();
            final double targetSize = 10.0;
            final double shrinkAmount = 1.0;

            @Override
            public void run() {
                if (currentSize <= targetSize) {
                    this.cancel();
                    Bukkit.broadcastMessage(ChatColor.GOLD + "§eBorder ist nun auf minimaler Größe.");
                    return;
                }
                currentSize -= shrinkAmount;
                border.setSize(currentSize);
            }
        };
        borderTask.runTaskTimer(plugin, 0, 20 * 5); // alle 5 Sekunden
    }

    private void resetWorlds() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.kickPlayer("§cServer wird neu generiert...");
        }

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            deleteWorld("world");
            deleteWorld("world_nether");
            deleteWorld("world_the_end");

            Bukkit.spigot().restart();
        }, 60L);
    }

    private void deleteWorld(String worldName) {
        File dir = new File(Bukkit.getServer().getWorldContainer(), worldName);
        deleteDirectory(dir);
    }

    private boolean deleteDirectory(File dir) {
        if (!dir.exists()) return true;
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    if (!deleteDirectory(file)) return false;
                } else {
                    if (!file.delete()) return false;
                }
            }
        }
        return dir.delete();
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
