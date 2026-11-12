package dev.ortin.houseCup.libraryLockdown;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;

enum LibraryTeams {
    BLUE,
    RED,
    GREEN,
    YELLOW,
    NULL
}

class LibraryTeamsDatatype implements PersistentDataType<String, LibraryTeams> {

    // Singleton so don't need many objects so constructor is unneeded
    LibraryTeamsDatatype() {}


    @Override
    public @NotNull Class<String> getPrimitiveType() {
        return String.class;
    }

    @Override
    public @NotNull Class<LibraryTeams> getComplexType() {
        return LibraryTeams.class;
    }

    @Override
    public @NonNull String toPrimitive(@NonNull LibraryTeams complex, @NotNull PersistentDataAdapterContext context) {
        return complex.toString();
    }

    @Override
    public @NonNull LibraryTeams fromPrimitive(@NonNull String primitive, @NotNull PersistentDataAdapterContext context) {
        return LibraryTeams.valueOf(primitive);
    }
}

class LibraryTeamsArgument implements CustomArgumentType.Converted<LibraryTeams, String> {
    private static final DynamicCommandExceptionType ERROR_INVALID_TEAM = new DynamicCommandExceptionType(team -> new LiteralMessage(team + " is not a valid team!"));

    @Override
    public @NonNull LibraryTeams convert(final String nativeType) throws CommandSyntaxException {
        try {
            return LibraryTeams.valueOf(nativeType.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw ERROR_INVALID_TEAM.create(nativeType);
        }
    }

    @Override
    public <S> @NonNull CompletableFuture<Suggestions> listSuggestions(final @NonNull CommandContext<S> context, final @NonNull SuggestionsBuilder builder) {
        for (final LibraryTeams team : LibraryTeams.values()) {
            final String name = team.toString();

            // Only suggest if the flavor name matches the user input
            if (name.startsWith(builder.getRemainingLowerCase())) {
                builder.suggest(name);
            }
        }

        return builder.buildFuture();
    }

    @Override
    public @NonNull ArgumentType<String> getNativeType() {
        return StringArgumentType.word();
    }
}
