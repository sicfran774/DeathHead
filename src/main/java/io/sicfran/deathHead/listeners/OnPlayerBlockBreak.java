package io.sicfran.deathHead.listeners;

import io.sicfran.deathHead.DeathHead;
import io.sicfran.deathHead.data.HeadData;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static net.kyori.adventure.text.Component.text;

//Considerations:
// - Close inventory if head is broken

public class OnPlayerBlockBreak implements Listener {

    private final DeathHead plugin;
    //private final Map<Location, Inventory> heads;

    public OnPlayerBlockBreak(DeathHead plugin){
        this.plugin = plugin;
        //heads = plugin.getInventoryManager().getPlayers();
    }

    @EventHandler
    public void onPlayerBlockBreak(BlockBreakEvent event){
        Block block = event.getBlock();
        Player player = event.getPlayer();

        if(!(block.getState() instanceof Skull skull)){
            return;
        }

        player.sendMessage("Broke skull.");

        UUID playerId = plugin.getPlayerIdFromSkull(skull);
        Location location = skull.getLocation();
        List<HeadData> playerData = plugin.getInventoryManager().getAllHeadData(playerId);

        if (playerData == null || playerData.isEmpty()){
            return;
        }

        event.setDropItems(false); //Prevent original head from dropping
        ItemStack skullHead = dropHeadWithLore(player.getName(), skull);
        skull.getWorld().dropItemNaturally(location, skullHead);

        for (HeadData head : playerData){
            // Find the specific skull that the player broke
            if (head.location().equals(location)){
                // Drop all items in inventory
                for(ItemStack item : head.inventory().getContents()){
                    if(item != null && !item.getType().isAir()){
                        location.getWorld().dropItemNaturally(location, item);
                    }
                }

                // Remove this inventory from the saved data
                playerData.remove(head);
                plugin.getInventoryManager().saveInventories();
                removeHeadInfo(head.location());
                break;
            }
        }
    }

    private ItemStack dropHeadWithLore(String name, Skull skull){
        ItemStack skullDrop = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) skullDrop.getItemMeta();

        meta.customName(text(name + "'s Head"));
        meta.setOwningPlayer(skull.getOwningPlayer());
        meta.lore(addLoreToSkull(skull));
        skullDrop.setItemMeta(meta);

        return skullDrop;
    }

    private List<Component> addLoreToSkull(Skull skull){
        List<Component> lore = new ArrayList<>();

        String causeOfDeath = skull.getPersistentDataContainer().get(plugin.getCauseOfDeathKey(), PersistentDataType.STRING);
        Long timeOfDeath = skull.getPersistentDataContainer().get(plugin.getTimeOfDeathKey(), PersistentDataType.LONG);

        if(causeOfDeath == null || timeOfDeath == null){
            return lore;
        }

        lore.add(text(causeOfDeath));
        lore.add(text(Instant.ofEpochMilli(timeOfDeath).toString()));

        return lore;
    }

    private void removeHeadInfo(Location location){
        World world = location.getWorld();

        for(Entity entity : world.getEntities()){
            if(!(entity instanceof ArmorStand)) continue;

            if(entity.getLocation().clone().subtract(0, DeathHead.ARMOR_STAND_HEIGHT, 0).getBlock().equals(location.getBlock())){
                entity.remove();
            }
        }
    }
}
