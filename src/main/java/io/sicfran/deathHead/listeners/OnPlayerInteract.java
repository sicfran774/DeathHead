package io.sicfran.deathHead.listeners;

import io.sicfran.deathHead.DeathHead;
import io.sicfran.deathHead.data.HeadData;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.List;
import java.util.UUID;

public class OnPlayerInteract implements Listener {

    private final DeathHead plugin;

    public OnPlayerInteract(DeathHead plugin){
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event){
        // Only allow main hand right click
        if (!event.getAction().isRightClick()) return;
        if (event.getHand() != EquipmentSlot.HAND) return;


        Player player = event.getPlayer();
        Block block = event.getClickedBlock();

        if (block == null || !(block.getState() instanceof Skull skull)){
            return;
        }

        UUID playerId = plugin.getPlayerIdFromSkull(skull);
        List<HeadData> playerData = plugin.getInventoryManager().getAllHeadData(playerId);

        if (playerData == null || playerData.isEmpty()){
            return;
        }

        event.setCancelled(true); //Prevent any accidental actions, only allow opening head inventory

        for (HeadData head : playerData){
            // Find specific head inventory at this location
            if (head.location().equals(skull.getLocation())){
                player.openInventory(head.inventory());
                break;
            }
        }
    }
}
