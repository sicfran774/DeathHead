package io.sicfran.deathHead.listeners;

import io.sicfran.deathHead.DeathHead;
import io.sicfran.deathHead.data.HeadData;
import io.sicfran.deathHead.data.InventoryManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
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

        // Prevent player drops so that items aren't duplicated
        event.getDrops().clear();

        Block block = player.getLocation().getBlock();
        Instant timeNow = Instant.now();

        Component causeOfDeathComponent = event.deathMessage();
        if (causeOfDeathComponent == null){
            return;
        }
        String causeOfDeath = PlainTextComponentSerializer.plainText().serialize(causeOfDeathComponent);

        Location location = createHead(player, block, timeNow, causeOfDeath);

        // Spawn armor stands for head information
        plugin.spawnDeathHeadInfo(location, player.getName(), timeNow.toString());

        // Add player head and its items to the InventoryManager
        plugin.getInventoryManager().getPlayers().computeIfAbsent(player.getUniqueId(),
                k -> new ArrayList<>()
        ).add(new HeadData(timeNow, location, copyInv));

        plugin.getInventoryManager().saveInventories();
    }

    private Location createHead(Player player, Block block, Instant timeNow, String causeOfDeath){
        block.setType(Material.PLAYER_HEAD);
        Skull skull = (Skull) block.getState();

        // Set player as owner to show skin's head
        skull.setOwningPlayer(player);
        // Add info to skull
        skull.getPersistentDataContainer().set(plugin.getPlayerUUIDKey(), PersistentDataType.STRING, player.getUniqueId().toString());
        skull.getPersistentDataContainer().set(plugin.getTimeOfDeathKey(), PersistentDataType.LONG, timeNow.toEpochMilli());
        skull.getPersistentDataContainer().set(plugin.getCauseOfDeathKey(), PersistentDataType.STRING, causeOfDeath);
        // Update changes in game
        skull.update();

        return skull.getLocation();
    }
}
