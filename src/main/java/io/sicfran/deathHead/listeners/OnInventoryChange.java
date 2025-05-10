package io.sicfran.deathHead.listeners;

import io.sicfran.deathHead.DeathHead;
import io.sicfran.deathHead.data.InventoryManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

public class OnInventoryChange implements Listener {

    private final DeathHead plugin;

    public OnInventoryChange(DeathHead plugin){
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event){
        Inventory inventory = event.getInventory();
        InventoryManager invManager = plugin.getInventoryManager();

        // If this is one of the heads open
        if (invManager.getOpenInventories().containsKey(inventory)){
            invManager.saveInventoryChanges(inventory, event.getPlayer().getUniqueId());
        }
    }
}
