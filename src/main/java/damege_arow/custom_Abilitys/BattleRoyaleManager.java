package damege_arow.custom_Abilitys;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

public class BattleRoyaleManager implements CommandExecutor, Listener {

    private final JavaPlugin plugin;
    private boolean gameRunning = false;
    private BukkitRunnable borderTask;
    private BukkitRunnable dropTask;
    private final Random random = new Random();
    private final Set<String> excludedAbilities = Set.of("DomainExpansion", "SoulSplit", "Invis");

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
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 20 * 60 * 30, 2));
            player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 20 * 60 * 30, 0));
            giveRandomAbility(player);
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
        }.runTaskLater(plugin, 30 * 60 * 20);
    }

    private void spawnSupplyDrop(World world) {
        int x = random.nextInt(500) - 250;
        int z = random.nextInt(500) - 250;
        int y = world.getHighestBlockYAt(x, z) + 1;
        Location dropLoc = new Location(world, x, y, z);

        Bukkit.broadcastMessage(ChatColor.GOLD + "Ein Supply Drop erscheint bei X: " + x + ", Z: " + z + "!");

        world.getBlockAt(dropLoc).setType(Material.BARREL);
        if (world.getBlockAt(dropLoc).getState() instanceof org.bukkit.block.Container container) {
            ItemStack[] drops = {
                    new ItemStack(Material.DIAMOND, 3),
                    new ItemStack(Material.NETHERITE_SCRAP, 1),
                    new ItemStack(Material.NETHER_WART, 5),
                    new ItemStack(Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE, 1),
                    new ItemStack(Material.GOLDEN_APPLE, 1)
            };
            container.getInventory().addItem(drops);
        }
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

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() != Material.PLAYER_HEAD) return;

        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 20 * 30, 2));
        player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 20 * 30, 2));
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 20 * 30, 1));

        item.setAmount(item.getAmount() - 1);
    }

    private void giveRandomAbility(Player player) {
        List<Ability> pool = new ArrayList<>();
        for (Ability a : Custom_Abilitys.getAllAbilities()) {
            if (!excludedAbilities.contains(a.getName())) {
                pool.add(a);
            }
        }

        if (pool.isEmpty()) return;

        Ability selected = pool.get(new Random().nextInt(pool.size()));
        Ability[] abilities = Custom_Abilitys.getAbilities(player.getUniqueId());

        for (int i = 0; i < abilities.length; i++) {
            if (abilities[i] == null) {
                abilities[i] = selected;
                Custom_Abilitys.setAbilities(player.getUniqueId(), abilities);
                selected.onEquip(player);
                player.spigot().sendMessage(
                        net.md_5.bungee.api.ChatMessageType.ACTION_BAR,
                        new net.md_5.bungee.api.chat.TextComponent(ChatColor.GREEN + "Fähigkeit erhalten: " + selected.getDisplayName())
                );
                return;
            }
        }

        abilities[0] = selected;
        Custom_Abilitys.setAbilities(player.getUniqueId(), abilities);
        selected.onEquip(player);
        player.spigot().sendMessage(
                net.md_5.bungee.api.ChatMessageType.ACTION_BAR,
                new net.md_5.bungee.api.chat.TextComponent(ChatColor.GREEN + "Fähigkeit erhalten: " + selected.getDisplayName())
        );
    }
}
