package io.sicfran.deathHead.listeners;

import io.sicfran.deathHead.DeathHead;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityExplodeEvent;

public class BlockProtections implements Listener {
    private final DeathHead plugin;

    public BlockProtections(DeathHead plugin){
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockBurn(BlockBurnEvent event) {
        Block block = event.getBlock();
        if (block.getType() == Material.PLAYER_HEAD && plugin.isProtectedHead(block)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockIgnite(BlockIgniteEvent event) {
        Block block = event.getBlock();
        if (block.getType() == Material.PLAYER_HEAD && plugin.isProtectedHead(block)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event) {
        event.blockList().removeIf(block -> block.getType() == Material.PLAYER_HEAD && plugin.isProtectedHead(block));
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        event.blockList().removeIf(block -> block.getType() == Material.PLAYER_HEAD && plugin.isProtectedHead(block));
    }

    @EventHandler
    public void onPistonExtend(BlockPistonExtendEvent event) {
        for (Block block : event.getBlocks()) {
            if (block.getType() == Material.PLAYER_HEAD && plugin.isProtectedHead(block)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onPistonRetract(BlockPistonRetractEvent event) {
        for (Block block : event.getBlocks()) {
            if (block.getType() == Material.PLAYER_HEAD && plugin.isProtectedHead(block)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onBlockFromTo(BlockFromToEvent event) {
        Block toBlock = event.getToBlock();
        if (toBlock.getType() == Material.PLAYER_HEAD && plugin.isProtectedHead(toBlock)) {
            event.setCancelled(true);
        }
    }
}
