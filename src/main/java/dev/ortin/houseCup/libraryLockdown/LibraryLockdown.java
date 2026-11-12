package dev.ortin.houseCup.libraryLockdown;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import io.papermc.paper.event.player.PlayerInsertLecternBookEvent;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;


class LibraryListener implements Listener {


    private final JavaPlugin plugin;

    LibraryListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerInsertLecternBook(PlayerInsertLecternBookEvent event) {
        @NotNull Player player = event.getPlayer();

        player.sendPlainMessage("You have placed a book");

        // Check if player is in team

        PersistentDataContainer pdc = player.getPersistentDataContainer();

        NamespacedKey key = new NamespacedKey(this.plugin, "house_cup_team");

        LibraryTeams team = pdc.getOrDefault(key, new LibraryTeamsDatatype(), LibraryTeams.NULL);

        player.sendPlainMessage("You are in team " + team);
    }
}



public final class LibraryLockdown extends JavaPlugin {

    String message = "Hello World!";

    @Override
    public void onEnable() {
        // Plugin startup logic

        LiteralCommandNode<CommandSourceStack> buildCommand = Commands.literal("house_cup")
                .then(Commands.literal("message")
                        .executes(context -> {
                            CommandSender sender = context.getSource().getSender(); // Retrieve the sender of the command

                            sender.sendPlainMessage(this.message + ": Done");

                            return Command.SINGLE_SUCCESS;
                        })
                )
                .then(Commands.literal("team_add")
                        .then(Commands.argument("target_player", ArgumentTypes.player())
                                .then(Commands.argument("target_team", new LibraryTeamsArgument())
                                    .executes(context -> {
                                        // Get player Argument
                                        // https://docs.papermc.io/paper/dev/command-api/arguments/entity-player/#player-argument
                                        Player player = context.getArgument("target_player", PlayerSelectorArgumentResolver.class).resolve(context.getSource()).getFirst();
                                        LibraryTeams team = context.getArgument("target_team", LibraryTeams.class);

                                        NamespacedKey key = new NamespacedKey(this, "house_cup_team");


                                        PersistentDataContainer pdc = player.getPersistentDataContainer();
                                        pdc.set(key, new LibraryTeamsDatatype(), team);

                                        return Command.SINGLE_SUCCESS;
                                    })
                                )
                        )
                )

                .build();



        // Register Events
        this.getServer().getPluginManager().registerEvents(new LibraryListener(this), this);

        // Register Command
        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> event.registrar().register(buildCommand));
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
