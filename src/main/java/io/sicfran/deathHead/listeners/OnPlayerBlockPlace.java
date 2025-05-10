package io.sicfran.deathHead.listeners;

import io.sicfran.deathHead.DeathHead;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.time.Instant;
import java.util.UUID;

public class OnPlayerBlockPlace implements Listener {

    private final DeathHead plugin;

    public OnPlayerBlockPlace(DeathHead plugin){
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerBlockPlace(BlockPlaceEvent event){
        ItemStack item = event.getItemInHand();
        Block block = event.getBlock();

        if(!(item.getItemMeta() instanceof SkullMeta skullMeta) || !(block.getState() instanceof Skull skull)){
            return;
        }

        UUID playerId = plugin.getPlayerIdFromSkull(skullMeta);
        Long timeOfDeath = plugin.getTimeOfDeathFromSkull(skullMeta);
        String causeOfDeath = plugin.getCauseOfDeathFromSkull(skullMeta);

        if(playerId == null || causeOfDeath == null || timeOfDeath == null){
            return;
        }

        Player player = Bukkit.getPlayer(playerId);
        if(player == null){
            return;
        }

        plugin.setPDCtoSkull(skull, player, Instant.ofEpochMilli(timeOfDeath), causeOfDeath);
    }
}
