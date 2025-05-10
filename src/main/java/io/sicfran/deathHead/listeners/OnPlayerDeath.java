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
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.Inventory;

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
        Inventory copyInv = Bukkit.createInventory(null, InventoryManager.multipleOf9(originalInv.getSize()), Component.text(player.getName() + "'s items"));
        copyInv.setContents(originalInv.getContents());

        // Prevent player drops so that items aren't duplicated
        event.getDrops().clear();

        Block block = player.getLocation().getBlock();

        // Prevent block from generating on an existing head
        // Loop until there's a free block above
        while(!(block.getType() == Material.AIR) && block.getY() < player.getWorld().getMaxHeight()){
            block = player.getWorld().getBlockAt(block.getLocation().clone().add(0, 1, 0));
        }

        Instant timeNow = Instant.now();

        Component causeOfDeathComponent = event.deathMessage();
        if (causeOfDeathComponent == null){
            return;
        }
        String causeOfDeath = PlainTextComponentSerializer.plainText().serialize(causeOfDeathComponent);

        Location location = createHead(player, block, timeNow, causeOfDeath);

        // Spawn armor stands for head information
        spawnDeathHeadInfo(location, player.getName(), plugin.formatTime(timeNow), causeOfDeath);

        // Add player head and its items to the InventoryManager
        plugin.getInventoryManager().getPlayers().computeIfAbsent(player.getUniqueId(),
                k -> new ArrayList<>()
        ).add(new HeadData(timeNow, location, copyInv));

        plugin.getInventoryManager().saveInventories();
    }

    private void spawnDeathHeadInfo(Location location, String name, String time, String causeOfDeath){
        spawnFloatingText(location.clone().add(0, 0.5, 0), text(name, color(0x00d0ff)));
        spawnFloatingText(location.clone().add(0, 0.25, 0), text(time, color(0x00d0ff)));
        spawnFloatingText(location, text(causeOfDeath));
    }

    private void spawnFloatingText(Location location, Component text){
        location = location.clone().add(0.5, DeathHead.ARMOR_STAND_HEIGHT, 0.5);

        ArmorStand stand = location.getWorld().spawn(location, ArmorStand.class);
        stand.setVisible(false);
        stand.customName(text);
        stand.setCustomNameVisible(true);
        stand.setMarker(true);
        stand.setGravity(false);
        stand.setSilent(true);
    }

    private Location createHead(Player player, Block block, Instant timeNow, String causeOfDeath){
        block.setType(Material.PLAYER_HEAD);
        Skull skull = (Skull) block.getState();

        // Set player as owner to show skin's head
        skull.setOwningPlayer(player);
        // Add info to skull
        plugin.setPDCtoSkull(skull, player, timeNow, causeOfDeath);

        return skull.getLocation();
    }
}
