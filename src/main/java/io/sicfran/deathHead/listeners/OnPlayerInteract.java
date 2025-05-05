package io.sicfran.deathHead.listeners;

import io.sicfran.deathHead.DeathHead;
import io.sicfran.deathHead.data.HeadData;
import io.sicfran.deathHead.data.InventoryManager;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class OnPlayerInteract implements Listener {

    private final DeathHead plugin;
    //private final Map<Location, Inventory> heads;

    public OnPlayerInteract(DeathHead plugin){
        this.plugin = plugin;
        //heads = plugin.getInventoryManager().getPlayers();
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event){
        if (!event.getAction().isRightClick()){
            return;
        }

        Player player = event.getPlayer();
        Block block = event.getClickedBlock();

        if (block == null || !(block.getState() instanceof Skull skull)){
            //player.sendMessage("Not a skull.");
            return;
        }

        PersistentDataContainer container = skull.getPersistentDataContainer();
        String uuidString = container.get(plugin.getPlayerUUIDKey(), PersistentDataType.STRING);
        Long timePlacedLong = container.get(plugin.getTimeKey(), PersistentDataType.LONG);

        if(uuidString == null || timePlacedLong == null){
            //player.sendMessage("No info.");
            return;
        }

        UUID playerId = UUID.fromString(uuidString);
        Instant timePlaced = Instant.ofEpochMilli(timePlacedLong);


        InventoryManager inventoryManager = plugin.getInventoryManager();
        List<HeadData> playerData = inventoryManager.getPlayers().getOrDefault(playerId, null);

        for (HeadData head : playerData){
            if (head.location().equals(skull.getLocation())){
                player.openInventory(head.inventory());
                break;
            }
        }

        player.sendMessage("Owner: " + Bukkit.getOfflinePlayer(playerId).getName());
        player.sendMessage("Placed: " + timePlaced);
    }
}
