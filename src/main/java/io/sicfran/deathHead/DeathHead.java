package io.sicfran.deathHead;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import io.sicfran.deathHead.data.InventoryManager;
import io.sicfran.deathHead.listeners.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.TileState;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.block.Skull;
import org.bukkit.inventory.meta.SkullMeta;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@SuppressWarnings("UnstableApiUsage")
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

        this.getLifecycleManager().registerEventHandler(
                LifecycleEvents.COMMANDS, commands ->
                        commands.registrar().register(new CommandTree(this).createCommand().build())
        );

        getLogger().info("DeathHead " + VERSION + " successfully loaded!");
    }

    @Override
    public void onDisable() {
        preventDupeForOpenedInv();
    }

    public int printInfo(CommandContext<CommandSourceStack> ctx){
        CommandSender sender = ctx.getSource().getSender();
        sender.sendMessage(inventoryManager.getOpenInventories().toString());

        return Command.SINGLE_SUCCESS;
    }

    public int removeBlockUnderPlayer(CommandContext<CommandSourceStack> ctx){
        try{
            final PlayerSelectorArgumentResolver resolver = ctx.getArgument("player", PlayerSelectorArgumentResolver.class);
            Player player = resolver.resolve(ctx.getSource()).getFirst();
            breakBlockAsPlayer(player, player.getLocation().getBlock());
        } catch (CommandSyntaxException e) {
            throw new RuntimeException(e);
        }
        return Command.SINGLE_SUCCESS;
    }

    public void breakBlockAsPlayer(Player player, Block block) {
        BlockBreakEvent event = new BlockBreakEvent(block, player);
        Bukkit.getPluginManager().callEvent(event);

        if (!event.isCancelled()) {
            block.setType(Material.AIR);
        }
    }

    private void registerListeners(){
        registerEvents(
                new OnPlayerDeath(this),
                new OnPlayerInteract(this),
                new OnPlayerBlockBreak(this),
                new OnPlayerBlockPlace(this),
                new BlockProtections(this),
                new OnInventoryChange(this)
        );
    }

    private void registerEvents(Listener... listeners){
        PluginManager pm = Bukkit.getPluginManager();
        for(Listener listener : listeners){
            pm.registerEvents(listener, this);
        }
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

    public String formatTime(Instant instant){
        ZonedDateTime zonedDateTime = instant.atZone(ZoneId.systemDefault());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy h:mma z");
        return zonedDateTime.format(formatter);
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

    public boolean isProtectedHead(Block block){
        BlockState state = block.getState();

        if(!(state instanceof TileState tileState)){
            return false;
        }

        PersistentDataContainer container = tileState.getPersistentDataContainer();
        return container.has(getPlayerUUIDKey(), PersistentDataType.STRING) &&
                container.has(getTimeOfDeathKey(), PersistentDataType.LONG) &&
                container.has(getCauseOfDeathKey(), PersistentDataType.STRING);
    }

    public void preventDupeForOpenedInv(){
        // Traverse each player looking inside skull chest
        for (UUID playerId : inventoryManager.getOpenInventories().keySet()){
            Player player = Bukkit.getPlayer(playerId);
            if (player != null){
                Inventory openInv = player.getOpenInventory().getTopInventory();
                player.closeInventory(); // force close inventory
                inventoryManager.saveInventoryChanges(playerId, openInv);
            }
        }
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
