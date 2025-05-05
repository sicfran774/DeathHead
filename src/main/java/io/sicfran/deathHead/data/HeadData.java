package io.sicfran.deathHead.data;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record HeadData(Instant timestamp, Location location, Inventory inventory) {

    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("timestamp", timestamp.toEpochMilli());
        map.put("location", location.serialize()); // Location supports serialize()
        map.put("inventory", Arrays.asList(inventory.getContents()));
        return map;
    }

    public static HeadData deserialize(Map<String, Object> map) {
        Instant timestamp = Instant.ofEpochMilli((Long) map.get("timestamp"));
        Location location = Location.deserialize((Map<String, Object>) map.get("location"));
        ItemStack[] contents = ((List<ItemStack>) map.get("inventory")).toArray(new ItemStack[0]);
        Inventory inventory = Bukkit.createInventory(null, InventoryManager.multipleOf9(contents.length));
        inventory.setContents(contents);
        return new HeadData(timestamp, location, inventory);
    }
}
