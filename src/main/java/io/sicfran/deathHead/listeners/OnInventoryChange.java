package io.sicfran.deathHead.listeners;

import io.sicfran.deathHead.DeathHead;
import io.sicfran.deathHead.data.InventoryManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

import java.util.UUID;

public class OnInventoryChange implements Listener {

    private final DeathHead plugin;

    public OnInventoryChange(DeathHead plugin){
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event){
        UUID playerId = event.getPlayer().getUniqueId();
        Inventory inventory = event.getInventory();

        InventoryManager invManager = plugin.getInventoryManager();

        // If this is one of the heads open
        if (invManager.getOpenInventories().containsKey(playerId)){
            invManager.saveInventoryChanges(playerId, inventory);
        }
    }
}
