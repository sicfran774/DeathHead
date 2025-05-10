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

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static net.kyori.adventure.text.Component.text;

public class OnPlayerBlockBreak implements Listener {

    private final DeathHead plugin;

    public OnPlayerBlockBreak(DeathHead plugin){
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerBlockBreak(BlockBreakEvent event){
        Block block = event.getBlock();
        Player player = event.getPlayer();

        if(!(block.getState() instanceof Skull skull)){
            return;
        }

        UUID playerId = plugin.getPlayerIdFromSkull(skull);
        Long timeOfDeath = plugin.getTimeOfDeathFromSkull(skull);
        String causeOfDeath = plugin.getCauseOfDeathFromSkull(skull);

        Location location = skull.getLocation();
        List<HeadData> playerData = plugin.getInventoryManager().getAllHeadData(playerId);

        if(playerId == null || timeOfDeath == null || causeOfDeath == null){
            return;
        }

        event.setDropItems(false); //Prevent original head from dropping
        ItemStack skullHead = dropHeadWithLore(player, skull);
        skull.getWorld().dropItemNaturally(location, skullHead);

        if (playerData == null || playerData.isEmpty()){
            return;
        }

        plugin.preventDupeForOpenedInv();

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

    private ItemStack dropHeadWithLore(Player player, Skull skull){
        ItemStack skullDrop = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) skullDrop.getItemMeta();

        plugin.setPDCtoSkull(meta, player, Instant.ofEpochMilli(plugin.getTimeOfDeathFromSkull(skull)), plugin.getCauseOfDeathFromSkull(skull));
        meta.customName(text(player.getName() + "'s Head"));
        meta.setOwningPlayer(skull.getOwningPlayer());
        meta.lore(addLoreToSkull(skull));
        skullDrop.setItemMeta(meta);

        return skullDrop;
    }

    private List<Component> addLoreToSkull(Skull skull){
        List<Component> lore = new ArrayList<>();

        lore.add(text(plugin.getCauseOfDeathFromSkull(skull)));

        Instant instant = Instant.ofEpochMilli(plugin.getTimeOfDeathFromSkull(skull));
        lore.add(text(plugin.formatTime(instant)));

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
