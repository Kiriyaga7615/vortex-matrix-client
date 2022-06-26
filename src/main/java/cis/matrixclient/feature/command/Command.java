package cis.matrixclient.feature.command;

import cis.matrixclient.util.player.ChatUtils;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandSource;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public abstract class Command {
    protected static MinecraftClient mc;

    private final String name, description;
    private final List<String> aliases = new ArrayList<>();

    public Command(String name, String description, String... aliases){
        this.name = name;
        this.description = description;
        Collections.addAll(this.aliases, aliases);
        mc = MinecraftClient.getInstance();
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public List<String> getAliases() { return aliases; }

    protected static <T> RequiredArgumentBuilder<CommandSource, T> argument(final String name, final ArgumentType<T> type) {
        return RequiredArgumentBuilder.argument(name, type);
    }

    protected static LiteralArgumentBuilder<CommandSource> literal(final String name) {
        return LiteralArgumentBuilder.literal(name);
    }

    public final void registerTo(CommandDispatcher<CommandSource> dispatcher) {
        register(dispatcher, name);
        for (String alias : aliases) register(dispatcher, alias);
    }

    public void register(CommandDispatcher<CommandSource> dispatcher, String name) {
        LiteralArgumentBuilder<CommandSource> builder = LiteralArgumentBuilder.literal(name);
        builder.then(literal("help").executes(context -> {
            info(description);
            return SINGLE_SUCCESS;
        }));
        build(builder);
        dispatcher.register(builder);
    }

    public abstract void build(LiteralArgumentBuilder<CommandSource> builder);

    public void info(String message) {
        ChatUtils.info(Formatting.WHITE + message);
    }

    public void warning(String message) {
        ChatUtils.info(name + Formatting.YELLOW + message);
    }

    public void error(String message) {
        ChatUtils.info(name + Formatting.RED + message);
    }
}
