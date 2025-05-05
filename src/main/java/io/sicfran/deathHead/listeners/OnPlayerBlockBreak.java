package io.sicfran.deathHead.listeners;

import io.sicfran.deathHead.DeathHead;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

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

        if(!(block.getState() instanceof Skull)){
            return;
        }

        //heads.remove(block.getLocation());
    }
}
