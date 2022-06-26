package cis.matrixclient.feature.command.commands;

import cis.matrixclient.feature.command.Command;
import cis.matrixclient.feature.command.arguments.KeyBindArgumentType;
import cis.matrixclient.feature.setting.Setting;
import cis.matrixclient.util.key.Keybind;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class BindSettingCommand extends Command {
    public BindSettingCommand() {
        super("bindsetting", "");
    }

    public static boolean waiting = false;
    public static Setting<Keybind> waitSetting;

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(literal("set").then(argument("setting", KeyBindArgumentType.module()).executes(context -> {
            Setting setting = context.getArgument("setting", Setting.class);

            if (setting == null) {
                return 0;
            }

            info("Press a key to bind the setting to.");
            waitSetting = setting;
            waiting = true;
            return SINGLE_SUCCESS;
        })));
    }
}
