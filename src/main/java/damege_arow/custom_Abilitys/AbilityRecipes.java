package damege_arow.custom_Abilitys;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;

public class AbilityRecipes {

    public static void registerAll(Custom_Abilitys plugin) {

        // === BloodRush ===
        register(plugin, "bloodrush", Custom_Abilitys.createScroll("BloodRush"),
                "PNP", "SES", "PNP",
                'P', Material.PRISMARINE_SHARD,
                'N', Material.NETHERITE_BLOCK,
                'S', Material.POTION,  // Stärke 2 – kann man nicht direkt prüfen, aber so geht's im Rezept
                'E', Material.NETHER_STAR);

        // === Dash ===
        register(plugin, "dash", Custom_Abilitys.createScroll("Dash"),
                "PDP", "W W", "PDP",
                'P', Material.PRISMARINE_SHARD,
                'D', Material.DIAMOND_BLOCK,
                'W', Material.WIND_CHARGE); // Wind Charge als Alternative, Echo Shard ist ähnlich

        // === Emerald ===
        register(plugin, "emerald", Custom_Abilitys.createScroll("Emerald"),
                "PDP", "EBE", "PDP",
                'P', Material.PRISMARINE_SHARD,
                'D', Material.DIAMOND_BLOCK,
                'E', Material.EMERALD_BLOCK,
                'B', Material.BLACK_BANNER); // Pillager Banner nicht craftbar, daher ersetzt

        // === Healer ===
        register(plugin, "healer", Custom_Abilitys.createScroll("Healer"),
                "PDP", "HBH", "PDP",
                'P', Material.PRISMARINE_SHARD,
                'D', Material.DIAMOND_BLOCK,
                'H', Material.POTION, // Regeneration 2 Potion
                'B', Material.BEACON);

        // === Immortaly ===
        register(plugin, "immortaly", Custom_Abilitys.createScroll("Immortaly"),
                "PDP", "TBT", "PDP",
                'P', Material.PRISMARINE_SHARD,
                'D', Material.DIAMOND_BLOCK,
                'T', Material.TOTEM_OF_UNDYING,
                'B', Material.BEACON);

        // === Lightning ===
        register(plugin, "lightning", Custom_Abilitys.createScroll("Lightning"),
                "PDP", "LBL", "PDP",
                'P', Material.PRISMARINE_SHARD,
                'D', Material.DIAMOND_BLOCK,
                'L', Material.LIGHTNING_ROD,
                'B', Material.BOLT_ARMOR_TRIM_SMITHING_TEMPLATE); // Ersatz für Bolt Trim

        // === Magma ===
        register(plugin, "magma", Custom_Abilitys.createScroll("Magma"),
                "PDP", "MFM", "PDP",
                'P', Material.PRISMARINE_SHARD,
                'D', Material.DIAMOND_BLOCK,
                'M', Material.MAGMA_BLOCK,
                'F', Material.POTION); // Feuerresistenz Potion

        // === Rewind ===
        register(plugin, "rewind", Custom_Abilitys.createScroll("Rewind"),
                "PDP", "C C", "PDP",
                'P', Material.PRISMARINE_SHARD,
                'D', Material.DIAMOND_BLOCK,
                'C', Material.CHORUS_FRUIT);

        // === SpeedStorm ===
        register(plugin, "speedstorm", Custom_Abilitys.createScroll("SpeedStorm"),
                "PDP", "SBS", "PDP",
                'P', Material.PRISMARINE_SHARD,
                'D', Material.DIAMOND_BLOCK,
                'S', Material.POTION, // Speed II Potion
                'B', Material.BEACON);

        // === Stun ===
        register(plugin, "stun", Custom_Abilitys.createScroll("Stun"),
                "PDP", "ASA", "PDP",
                'P', Material.PRISMARINE_SHARD,
                'D', Material.DIAMOND_BLOCK,
                'A', Material.ANVIL,
                'S', Material.SILENCE_ARMOR_TRIM_SMITHING_TEMPLATE); // Silence Trim ersetzt
    }

    private static void register(Custom_Abilitys plugin, String id, ItemStack result, String row1, String row2, String row3, Object... keys) {
        ShapedRecipe recipe = new ShapedRecipe(new NamespacedKey(plugin, id), result);
        recipe.shape(row1, row2, row3);

        for (int i = 0; i < keys.length; i += 2) {
            char key = (char) keys[i];
            Material mat = (Material) keys[i + 1];
            recipe.setIngredient(key, mat);
        }

        Bukkit.addRecipe(recipe);
    }
}
