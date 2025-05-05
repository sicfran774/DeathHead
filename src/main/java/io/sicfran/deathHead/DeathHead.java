package io.sicfran.deathHead;

import io.sicfran.deathHead.data.InventoryManager;
import io.sicfran.deathHead.listeners.OnPlayerBlockBreak;
import io.sicfran.deathHead.listeners.OnPlayerDeath;
import io.sicfran.deathHead.listeners.OnPlayerInteract;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.ArmorStand;
import org.bukkit.plugin.java.JavaPlugin;

public final class DeathHead extends JavaPlugin {

    private static final String VERSION = "1.0";
    private final NamespacedKey playerUUIDKey = new NamespacedKey(this, "owner");
    private final NamespacedKey timeKey = new NamespacedKey(this, "placed_time");
    private final InventoryManager inventoryManager = new InventoryManager(this);

    @Override
    public void onEnable() {
        registerListeners();

        Bukkit.getScheduler().runTask(this, inventoryManager::loadInventories);

        getLogger().info("DeathHead " + VERSION + " successfully loaded!");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    private void registerListeners(){
        Bukkit.getPluginManager().registerEvents(new OnPlayerDeath(this), this);
        Bukkit.getPluginManager().registerEvents(new OnPlayerInteract(this), this);
        Bukkit.getPluginManager().registerEvents(new OnPlayerBlockBreak(this), this);
    }

    public NamespacedKey getPlayerUUIDKey() {
        return playerUUIDKey;
    }

    public NamespacedKey getTimeKey() {
        return timeKey;
    }

    public void spawnFloatingText(Location location, Component text){
        location = location.clone().add(0.5, 1, 0.5);

        ArmorStand stand = location.getWorld().spawn(location, ArmorStand.class);
        stand.setVisible(false);
        stand.customName(text);
        stand.setCustomNameVisible(true);
        stand.setMarker(true);
        stand.setGravity(false);
        stand.setSilent(true);
    }

    public InventoryManager getInventoryManager() {
        return inventoryManager;
    }
}
