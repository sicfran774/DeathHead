package io.sicfran.deathHead;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;

@SuppressWarnings("UnstableApiUsage")
public class CommandTree {

    private final DeathHead plugin;

    public CommandTree(DeathHead plugin){
        this.plugin = plugin;
    }

    public LiteralArgumentBuilder<CommandSourceStack> createCommand(){
        return Commands.literal("head")
                .executes(plugin::printInfo)
                .then(Commands.argument("player", ArgumentTypes.player())
                                .executes(plugin::removeBlockUnderPlayer));
    }
}
