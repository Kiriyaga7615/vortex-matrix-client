package cis.matrixclient.feature.command;

import cis.matrixclient.MatrixClient;
import cis.matrixclient.feature.command.commands.BindCommand;
import cis.matrixclient.feature.command.commands.BindSettingCommand;
import cis.matrixclient.feature.command.commands.FakePlayerCommand;
import cis.matrixclient.feature.command.commands.FriendCommand;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientCommandSource;
import net.minecraft.command.CommandSource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cis.matrixclient.MatrixClient.mc;

public class CommandManager {
    public static void init(){
        add(new BindCommand());
        add(new BindSettingCommand());
        add(new FakePlayerCommand());
        add(new FriendCommand());
    }

    private static String PREFIX = ".";

    public static String getPrefix() { return PREFIX; }
    public static void setPrefix(String prefix) { PREFIX = prefix; }
    private static final CommandDispatcher<CommandSource> DISPATCHER = new CommandDispatcher<>();
    private static List<Command> commands = new ArrayList<>();
    private static final Map<Class<? extends Command>, Command> commandInstances = new HashMap<>();
    private static final CommandSource COMMAND_SOURCE = new ChatCommandSource(mc);

    public static void dispatch(String message) throws CommandSyntaxException {
        dispatch(message, new ChatCommandSource(mc));
    }

    public static void dispatch(String message, CommandSource source) throws CommandSyntaxException {
        ParseResults<CommandSource> results = DISPATCHER.parse(message, source);
        DISPATCHER.execute(results);
    }

    public static CommandDispatcher<CommandSource> getDispatcher() {
        return DISPATCHER;
    }

    public static CommandSource getCommandSource() {
        return COMMAND_SOURCE;
    }

    private final static class ChatCommandSource extends ClientCommandSource {
        public ChatCommandSource(MinecraftClient client) {
            super(null, client);
        }
    }

    public static void add(Command command) {
        MatrixClient.EVENT_BUS.register(command);
        commands.removeIf(command1 -> command1.getName().equals(command.getName()));
        commandInstances.values().removeIf(command1 -> command1.getName().equals(command.getName()));

        command.registerTo(DISPATCHER);
        commands.add(command);
        commandInstances.put(command.getClass(), command);
    }

    public List<Command> getAll() {
        return commands;
    }
}
