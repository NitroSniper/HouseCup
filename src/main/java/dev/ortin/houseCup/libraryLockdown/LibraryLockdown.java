package dev.ortin.houseCup.libraryLockdown;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import io.papermc.paper.event.player.PlayerInsertLecternBookEvent;
import io.papermc.paper.persistence.PersistentDataContainerView;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.TextColor.color;


enum LibraryLockdownPDCKey {
    TEAM("team");

    public final String label;

    LibraryLockdownPDCKey(String label) {
        this.label = label;
    }
}

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

        NamespacedKey key = new NamespacedKey(this.plugin, LibraryLockdownPDCKey.TEAM.label);

        LibraryTeams team = pdc.getOrDefault(key, new LibraryTeamsDatatype(), LibraryTeams.NONE);

        player.sendPlainMessage("You are in team " + team);
    }
}

public final class LibraryLockdown extends JavaPlugin {


    @Override
    public void onEnable() {
        // Plugin startup logic

        LiteralCommandNode<CommandSourceStack> buildCommand = Commands.literal(this.namespace())
                .requires(source -> source.getSender().isOp())
                .then(Commands.literal("info")
                        .executes(context -> {
                            CommandSender sender = context.getSource().getSender(); // Retrieve the sender of the command
                            NamespacedKey key = new NamespacedKey(this, LibraryLockdownPDCKey.TEAM.label);
                            OfflinePlayer[] allPlayers = Bukkit.getServer().getOfflinePlayers();

                            final Component info = text("Teams:")
                                    .appendNewline()
                                    .append(
                                            Arrays.stream(LibraryTeams.values())
                                                    .map(team -> {
                                                        TextColor team_color = color(team.color);

                                                        Stream<OfflinePlayer> team_players = Arrays.stream(allPlayers).filter(
                                                                offlinePlayer -> {
                                                                    PersistentDataContainerView pdc = offlinePlayer.getPersistentDataContainer();
                                                                    return pdc.getOrDefault(key, new LibraryTeamsDatatype(), LibraryTeams.NONE) == team;
                                                                }
                                                        );
                                                        return text()
                                                                .append(text(team.name(), team_color))
                                                                .append(text(" Team: ", team_color))
                                                                .append(text(team_players.map(OfflinePlayer::getName).collect(Collectors.joining(", "))))
                                                                .build();
                                                    })
                                                    .reduce((a, b) -> a.append(Component.newline()).append(b))
                                                    .orElseThrow()
                                    );
                            sender.sendMessage(info);
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

                                            NamespacedKey key = new NamespacedKey(this, LibraryLockdownPDCKey.TEAM.label);


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
