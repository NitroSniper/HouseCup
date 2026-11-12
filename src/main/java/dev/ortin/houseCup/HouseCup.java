package dev.ortin.houseCup;

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
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;


class HCTeamDatatype implements PersistentDataType<Byte, HouseCupsTeams> {
    public static final HCTeamDatatype INSTANCE = new HCTeamDatatype();

    // Singleton so don't need many objects so constructor is unneeded
    private HCTeamDatatype() {}


    @Override
    public @NotNull Class<Byte> getPrimitiveType() {
        return Byte.class;
    }

    @Override
    public @NotNull Class<HouseCupsTeams> getComplexType() {
        return HouseCupsTeams.class;
    }

    @Override
    public @NonNull Byte toPrimitive(@NonNull HouseCupsTeams complex, @NotNull PersistentDataAdapterContext context) {
        return (byte) complex.ordinal();
    }

    @Override
    public @NonNull HouseCupsTeams fromPrimitive(@NonNull Byte primitive, @NotNull PersistentDataAdapterContext context) {
        return HouseCupsTeams.values()[primitive];
    }
}


enum HouseCupsTeams {
    BLUE,
    RED,
    GREEN,
    YELLOW,
    NULL
}

class ExampleListener implements Listener {


    private final JavaPlugin plugin;

    ExampleListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerInsertLecternBook(PlayerInsertLecternBookEvent event) {
        @NotNull Player player = event.getPlayer();

        player.sendPlainMessage("You have placed a book");

        // Check if player is in team

        PersistentDataContainer pdc = player.getPersistentDataContainer();

        NamespacedKey key = new NamespacedKey(this.plugin, "house_cup_team");

        HouseCupsTeams team = pdc.getOrDefault(key, HCTeamDatatype.INSTANCE, HouseCupsTeams.NULL);

        player.sendPlainMessage("You are in team " + team);
    }
}



public final class HouseCup extends JavaPlugin {

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
                                .executes(context -> {

                                    NamespacedKey key = new NamespacedKey(this, "house_cup_team");

                                    // Get player Argument
                                    // https://docs.papermc.io/paper/dev/command-api/arguments/entity-player/#player-argument
                                    Player player = context.getArgument("target_player", PlayerSelectorArgumentResolver.class).resolve(context.getSource()).getFirst();

                                    PersistentDataContainer pdc = player.getPersistentDataContainer();
                                    pdc.set(key, HCTeamDatatype.INSTANCE, HouseCupsTeams.BLUE);

                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                )

                .build();



        // Register Events
        this.getServer().getPluginManager().registerEvents(new ExampleListener(this), this);

        // Register Command
        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> event.registrar().register(buildCommand));
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
