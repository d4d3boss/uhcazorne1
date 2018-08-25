package me.raauhh.uhc.manager.combatlogger;

import lombok.Getter;
import lombok.Setter;
import me.raauhh.uhc.UHC;
import me.raauhh.uhc.manager.player.UHCPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

@Getter
@Setter
public class CombatLogger {

    public enum Remove {
        DEAD, JOIN
    }

    private UUID uuid;
    private Player player;
    private String name;

    private Zombie entity;

    private int experience;

    private ItemStack[] armor;
    private ItemStack[] inventory;

    private long createdTime;

    public CombatLogger(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);

        this.uuid = uuid;
        this.player = player;
        this.name = player.getName();

        this.entity = (Zombie) player.getWorld().spawnEntity(player.getLocation(), EntityType.ZOMBIE);
        this.entity.setCustomName(player.getName());
        this.entity.setCustomNameVisible(true);
        this.entity.setHealth(player.getHealth());
        this.entity.setBaby(false);
        this.entity.setVillager(false);

        this.armor = player.getInventory().getArmorContents();
        this.inventory = player.getInventory().getContents();
        this.experience = player.getTotalExperience();

        this.entity.getEquipment().setArmorContents(this.armor);
        this.entity.getEquipment().setItemInHand(player.getItemInHand());
        this.entity.setCanPickupItems(false);

        this.createdTime = System.currentTimeMillis();
    }

    public void remove(Remove remove) {
        UHC.getInstance().getCombatLoggerManager().remove(player.getUniqueId());
        switch (remove) {
            case DEAD:
                if(!UHC.getInstance().getGamemodeManager().exists("TimeBomb")) {
                    World world = this.entity.getWorld();
                    for (ItemStack item : this.armor) {
                        if (item == null || item.getType() == Material.AIR) continue;
                        world.dropItemNaturally(this.entity.getLocation(), item);
                    }
                    for (ItemStack item : this.inventory) {
                        if (item == null || item.getType() == Material.AIR) continue;
                        world.dropItemNaturally(this.entity.getLocation(), item);
                    }
                }

                UHCPlayer uhcPlayer = UHC.getInstance().getUhcPlayerManager().getPlayer(player.getUniqueId());
                String kills = "§7[§f" + uhcPlayer.getKills() + "§7]";

                UHC.getInstance().getGameManager().formatMessage(player, "%1" + this.name + kills + " §ewas disconnected for a long time and died.");
                UHC.getInstance().getGameManager().checkWinners();
                break;
            case JOIN:
                if (!this.entity.isDead()) this.entity.remove();
                break;
        }
    }

}
