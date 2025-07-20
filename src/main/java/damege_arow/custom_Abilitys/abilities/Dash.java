package damege_arow.custom_Abilitys.abilities;

import damege_arow.custom_Abilitys.Ability;
import damege_arow.custom_Abilitys.Custom_Abilitys;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class Dash implements Ability {

    @Override
    public String getName() {
        return "Dash";
    }

    @Override
    public String getDisplayName() {
        return "§bDash";
    }

    @Override
    public long getCooldown(Player player) {
        int seconds = Custom_Abilitys.getInstance().getConfig().getInt("Dash.cooldown", 12);
        return seconds * 1000L;
    }

    @Override
    public void useAbility(Player player) {
        if (Custom_Abilitys.isOnCooldown(player.getUniqueId(), getName())) {
            player.sendMessage(ChatColor.RED + "Diese Fähigkeit ist noch im Cooldown!");
            return;
        }

        // Dash nach vorne
        Vector direction = player.getLocation().getDirection().normalize().multiply(1.5);
        direction.setY(0.1);
        player.setVelocity(direction);

        // Sound & Partikel
        player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation(), 30, 0.2, 0.2, 0.2, 0.05);
        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);

        // Cooldown setzen
        Custom_Abilitys.setCooldown(player.getUniqueId(), getName(), getCooldown(player));
        player.sendMessage(ChatColor.AQUA + "§bDash aktiviert!");
    }

    @Override
    public void onEquip(Player player) {}

    @Override
    public void onUnequip(Player player) {}
}
