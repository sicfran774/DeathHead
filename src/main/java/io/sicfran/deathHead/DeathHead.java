package io.sicfran.deathHead;

import io.sicfran.deathHead.data.InventoryManager;
import io.sicfran.deathHead.listeners.OnPlayerBlockBreak;
import io.sicfran.deathHead.listeners.OnPlayerBlockPlace;
import io.sicfran.deathHead.listeners.OnPlayerDeath;
import io.sicfran.deathHead.listeners.OnPlayerInteract;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.block.Skull;
import org.bukkit.inventory.meta.SkullMeta;

import java.time.Instant;
import java.util.UUID;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.TextColor.color;

public final class DeathHead extends JavaPlugin {

    private static final String VERSION = "1.0";
    private final NamespacedKey playerUUIDKey = new NamespacedKey(this, "owner");
    private final NamespacedKey timeOfDeathKey = new NamespacedKey(this, "time_death");
    private final NamespacedKey causeOfDeathKey = new NamespacedKey(this, "cause_death");
    private final InventoryManager inventoryManager = new InventoryManager(this);
    public static float ARMOR_STAND_HEIGHT = 1;

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
        Bukkit.getPluginManager().registerEvents(new OnPlayerBlockPlace(this), this);
    }

    public UUID getPlayerIdFromSkull(PersistentDataHolder holder){
        PersistentDataContainer container = holder.getPersistentDataContainer();
        String uuidString = container.get(getPlayerUUIDKey(), PersistentDataType.STRING);

        if (uuidString == null) return null;

        return UUID.fromString(uuidString);
    }

    public String getCauseOfDeathFromSkull(PersistentDataHolder holder){
        PersistentDataContainer container = holder.getPersistentDataContainer();
        return container.get(getCauseOfDeathKey(), PersistentDataType.STRING);
    }

    public Long getTimeOfDeathFromSkull(PersistentDataHolder holder){
        PersistentDataContainer container = holder.getPersistentDataContainer();
        return container.get(getTimeOfDeathKey(), PersistentDataType.LONG);
    }

    public void setPDCtoSkull(Skull skull, Player player, Instant timeOfDeath, String causeOfDeath){
        skull.getPersistentDataContainer().set(getPlayerUUIDKey(), PersistentDataType.STRING, player.getUniqueId().toString());
        skull.getPersistentDataContainer().set(getTimeOfDeathKey(), PersistentDataType.LONG, timeOfDeath.toEpochMilli());
        skull.getPersistentDataContainer().set(getCauseOfDeathKey(), PersistentDataType.STRING, causeOfDeath);

        // Update changes in game
        skull.update();
    }

    public void setPDCtoSkull(SkullMeta skullMeta, Player player, Instant timeOfDeath, String causeOfDeath){
        skullMeta.getPersistentDataContainer().set(getPlayerUUIDKey(), PersistentDataType.STRING, player.getUniqueId().toString());
        skullMeta.getPersistentDataContainer().set(getTimeOfDeathKey(), PersistentDataType.LONG, timeOfDeath.toEpochMilli());
        skullMeta.getPersistentDataContainer().set(getCauseOfDeathKey(), PersistentDataType.STRING, causeOfDeath);
    }

    public void spawnDeathHeadInfo(Location location, String name, String time){
        spawnFloatingText(location.clone().add(0, 0.25, 0), text(name, color(0x00d0ff)));
        spawnFloatingText(location, text(time, color(0x00d0ff)));
    }

    private void spawnFloatingText(Location location, Component text){
        location = location.clone().add(0.5, ARMOR_STAND_HEIGHT, 0.5);

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

    public NamespacedKey getPlayerUUIDKey() {
        return playerUUIDKey;
    }

    public NamespacedKey getTimeOfDeathKey() {
        return timeOfDeathKey;
    }

    public NamespacedKey getCauseOfDeathKey() {
        return causeOfDeathKey;
    }
}
