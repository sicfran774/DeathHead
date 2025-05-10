package io.sicfran.deathHead.data;

import io.sicfran.deathHead.DeathHead;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class InventoryManager {

    private final Map<UUID, List<HeadData>> players = new HashMap<>();
    private final Map<UUID, HeadData> openInventories = new HashMap<>();
    private final DeathHead plugin;
    private final File playerDataFile;
    private final YamlConfiguration config;

    public InventoryManager(DeathHead plugin){
        this.plugin = plugin;
        playerDataFile = new File(plugin.getDataFolder(), "inventories.yml");
        config = YamlConfiguration.loadConfiguration(playerDataFile);
        initializeFiles();
    }

    private void initializeFiles(){
        if(!plugin.getDataFolder().exists()){
            boolean success = plugin.getDataFolder().mkdirs();
        }
        if(!playerDataFile.exists()){
            try{
                boolean success = playerDataFile.createNewFile();
            } catch (IOException e){
                plugin.getLogger().severe("Failed to create inventories.yml");
            }
        }
    }

    public void saveInventories(){
        initializeFiles();

        for (Map.Entry<UUID, List<HeadData>> player : players.entrySet()){
            List<HeadData> playerData = player.getValue();
            List<Map<String, Object>> serializedPlayerData = playerData.stream()
                            .map(HeadData::serialize)
                                    .toList();

            config.set(player.getKey().toString(), serializedPlayerData);
        }

        try{
            config.save(playerDataFile);
        } catch (IOException e){
            plugin.getLogger().severe("Failed to save inventories.yml");
        }
    }

    public void loadInventories(){
        initializeFiles();

        players.clear();
        for (String key : config.getKeys(false)){
            List<Map<?,?>> rawData = (List<Map<?, ?>>) config.getList(key);
            List<HeadData> headData = new ArrayList<>();

            assert rawData != null;
            for (Map<?,?> rawMap : rawData){
                Map<String, Object> serialized = (Map<String, Object>) rawMap;
                HeadData deserialized = HeadData.deserialize(key, serialized);
                headData.add(deserialized);
            }

            players.put(UUID.fromString(key), headData);
        }
    }

    public void addToOpenInventories(UUID playerId, HeadData head){
        openInventories.put(playerId, head);
    }

    public HeadData removeFromOpenInventories(UUID playerId){
        return openInventories.remove(playerId);
    }

    public void saveInventoryChanges(UUID playerId, Inventory inventory){
        HeadData headData = removeFromOpenInventories(playerId);
        headData.inventory().setContents(inventory.getContents());
        saveInventories();
    }

    public Map<UUID, HeadData> getOpenInventories() {
        return openInventories;
    }

    public List<HeadData> getAllHeadData(UUID playerId){
        return players.getOrDefault(playerId, null);
    }

    public static int multipleOf9(int num){
        return num % 9 == 0 ? num : num + (9 - (num % 9));
    }

    public Map<UUID, List<HeadData>> getPlayers() {
        return players;
    }


}
