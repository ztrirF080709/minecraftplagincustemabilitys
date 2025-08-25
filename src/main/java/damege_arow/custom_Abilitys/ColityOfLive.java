package damege_arow.custom_Abilitys;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class ColityOfLive implements Listener {

    private static final NamespacedKey AUTO_SMELT_KEY = new NamespacedKey(Custom_Abilitys.getInstance(), "auto_smelt_pickaxe");

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        ItemStack tool = event.getPlayer().getInventory().getItemInMainHand();
        if (tool == null || tool.getType() == Material.AIR) return;
        if (!tool.getType().toString().endsWith("PICKAXE")) return;

        Material drop = switch (event.getBlock().getType()) {
            case IRON_ORE, DEEPSLATE_IRON_ORE -> Material.IRON_INGOT;
            case GOLD_ORE, DEEPSLATE_GOLD_ORE -> Material.GOLD_INGOT;
            case COPPER_ORE, DEEPSLATE_COPPER_ORE -> Material.COPPER_INGOT;
            case ANCIENT_DEBRIS -> Material.NETHERITE_SCRAP;
            default -> null;
        };

        if (drop != null) {
            event.setDropItems(false);
            event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), new ItemStack(drop));
        }
    }

    @EventHandler
    public void onCraft(PrepareItemCraftEvent event) {
        CraftingInventory inv = event.getInventory();
        if (inv.getRecipe() == null) return;

        ItemStack[] matrix = inv.getMatrix();

        // Goldapfel Rezept: /G/ GAG /G/ → Slot 1, 3, 5, 7 = GOLD; Slot 4 = APPLE
        if (matrix.length == 9 &&
                matrix[1].getType() == Material.GOLD_INGOT &&
                matrix[3].getType() == Material.GOLD_INGOT &&
                matrix[4].getType() == Material.APPLE &&
                matrix[5].getType() == Material.GOLD_INGOT &&
                matrix[7].getType() == Material.GOLD_INGOT) {
            inv.setResult(new ItemStack(Material.GOLDEN_APPLE));
        }
    }
}
