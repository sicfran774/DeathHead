package io.sicfran.deathHead.listeners;

import io.sicfran.deathHead.DeathHead;
import io.sicfran.deathHead.data.HeadData;
import io.sicfran.deathHead.data.InventoryManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.persistence.PersistentDataType;

import java.time.Instant;
import java.util.ArrayList;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.TextColor.color;

public class OnPlayerDeath implements Listener {

    private final DeathHead plugin;

    public OnPlayerDeath(DeathHead plugin){
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event){
        Player player = event.getPlayer();
        Inventory originalInv = player.getInventory();
        Inventory copyInv = Bukkit.createInventory(null, InventoryManager.multipleOf9(originalInv.getSize()));
        copyInv.setContents(originalInv.getContents());

        event.getDrops().clear();
        Block block = player.getLocation().getBlock();
        Instant timeNow = Instant.now();
        Location location = createHead(player, block, timeNow);

        // Add player head and its items to the InventoryManager
        plugin.getInventoryManager().getPlayers().computeIfAbsent(player.getUniqueId(),
                k -> new ArrayList<>()
        ).add(new HeadData(timeNow, location, copyInv));

        plugin.getInventoryManager().saveInventories();
    }

    private Location createHead(Player player, Block block, Instant timeNow){
        block.setType(Material.PLAYER_HEAD);
        Skull skull = (Skull) block.getState();

        // Set player as owner to show skin's head
        skull.setOwningPlayer(player);

        // Set player info
        plugin.spawnFloatingText(skull.getLocation(), text(player.getName(), color(0x00d0ff)));
        plugin.spawnFloatingText(skull.getLocation().clone().add(0, 0.25, 0), text(timeNow.toString(), color(0x00d0ff)));

        skull.getPersistentDataContainer().set(plugin.getPlayerUUIDKey(), PersistentDataType.STRING, player.getUniqueId().toString());
        skull.getPersistentDataContainer().set(plugin.getTimeKey(), PersistentDataType.LONG, timeNow.toEpochMilli());

        skull.update();

        return skull.getLocation();
    }
}
