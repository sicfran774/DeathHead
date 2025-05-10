package io.sicfran.deathHead.data;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.time.Instant;
import java.util.*;

public record HeadData(Instant timestamp, Location location, Inventory inventory) {

    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("timestamp", timestamp.toEpochMilli());
        map.put("location", location.serialize()); // Location supports serialize()
        map.put("inventory", Arrays.asList(inventory.getContents()));
        return map;
    }

    public static HeadData deserialize(String playerId, Map<String, Object> map) {
        String playerName = Bukkit.getOfflinePlayer(UUID.fromString(playerId)).getName();

        Instant timestamp = Instant.ofEpochMilli((Long) map.get("timestamp"));
        Location location = Location.deserialize((Map<String, Object>) map.get("location"));
        ItemStack[] contents = ((List<ItemStack>) map.get("inventory")).toArray(new ItemStack[0]);
        Inventory inventory = Bukkit.createInventory(null, InventoryManager.multipleOf9(contents.length), Component.text(playerName + "'s items"));
        inventory.setContents(contents);

        return new HeadData(timestamp, location, inventory);
    }
}
