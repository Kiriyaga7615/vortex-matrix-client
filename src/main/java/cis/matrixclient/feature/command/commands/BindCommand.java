package cis.matrixclient.feature.command.commands;

import cis.matrixclient.feature.command.Command;
import cis.matrixclient.feature.command.arguments.ModuleArgumentType;
import cis.matrixclient.feature.module.Module;
import cis.matrixclient.util.key.KeyBinds;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class BindCommand extends Command {
    public BindCommand() {
        super("bind", "Binds a specified module to the next pressed key.");
    }

    public static boolean waiting = false;
    public static Module waitModule;

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(literal("get").then(argument("module", ModuleArgumentType.module()).executes(context -> {
            Module module = context.getArgument("module", Module.class);
            info(KeyBinds.getKeyName(module.bind.get()));
            return SINGLE_SUCCESS;
        })));
        builder.then(literal("set").then(argument("module", ModuleArgumentType.module()).executes(context -> {
            Module module = context.getArgument("module", Module.class);

            if (module == null) {
                return 0;
            }

            info("Press a key to bind the module to.");
            waitModule = module;
            waiting = true;
            return SINGLE_SUCCESS;
        })));
        builder.then(literal("remove").then(argument("module", ModuleArgumentType.module()).executes(context -> {
            Module module = context.getArgument("module", Module.class);
            module.bind.set(true, -1);

            module.info("Removed.");
            return SINGLE_SUCCESS;
        })));
    }
}
