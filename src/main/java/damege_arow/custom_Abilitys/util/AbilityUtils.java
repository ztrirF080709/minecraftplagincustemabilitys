package damege_arow.custom_Abilitys.util;

import damege_arow.custom_Abilitys.Ability;
import damege_arow.custom_Abilitys.Custom_Abilitys;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AbilityUtils {

    public static Ability[] getEquippedAbilities(Player player) {
        return Custom_Abilitys.getAbilities(player.getUniqueId());
    }

    public static <T extends Ability> T getAbilityByClass(Player player, Class<T> clazz) {
        for (Ability a : getEquippedAbilities(player)) {
            if (clazz.isInstance(a)) return clazz.cast(a);
        }
        return null;
    }

    public static long getCooldownRemaining(Player player, String abilityName) {
        return Math.max(Custom_Abilitys.getCooldown(player.getUniqueId(), abilityName), 0) / 1000;
    }

    public static boolean isAbilityReady(Player player, String name) {
        return getCooldownRemaining(player, name) == 0;
    }

    public static void setCooldownSeconds(Player player, String abilityName, long seconds) {
        Custom_Abilitys.setCooldown(player.getUniqueId(), abilityName, seconds * 1000);
    }

    public static int getSlotOfAbility(Player player, String name) {
        Ability[] abilities = getEquippedAbilities(player);
        for (int i = 0; i < abilities.length; i++) {
            if (abilities[i] != null && abilities[i].getName().equalsIgnoreCase(name)) {
                return i;
            }
        }
        return -1;
    }

    public static void unequipAbility(Player player, String name) {
        Ability[] current = getEquippedAbilities(player);
        for (int i = 0; i < current.length; i++) {
            if (current[i] != null && current[i].getName().equalsIgnoreCase(name)) {
                current[i].onUnequip(player);
                current[i] = null;
            }
        }
        Custom_Abilitys.setAbilities(player.getUniqueId(), current);
    }

    public static String getAbilityDisplayName(String name) {
        Ability a = Custom_Abilitys.getAbility(name);
        return (a != null) ? a.getDisplayName() : name;
    }

    public static boolean hasAbility(Player player, String name) {
        return getSlotOfAbility(player, name) != -1;
    }

    public static void swapAbilitySlots(Player player) {
        Ability[] current = getEquippedAbilities(player);
        Ability temp = current[0];
        current[0] = current[1];
        current[1] = temp;
        Custom_Abilitys.setAbilities(player.getUniqueId(), current);
    }

    public static void applyCooldownToAll(Player player, long seconds) {
        for (Ability a : getEquippedAbilities(player)) {
            if (a != null) {
                setCooldownSeconds(player, a.getName(), seconds);
            }
        }
    }

    public static List<String> getEquippedAbilityNames(Player player) {
        List<String> list = new ArrayList<>();
        for (Ability a : getEquippedAbilities(player)) {
            if (a != null) list.add(a.getName());
        }
        return list;
    }
}
