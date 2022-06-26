package cis.matrixclient.feature.command.arguments;

import cis.matrixclient.feature.command.CommandManager;
import cis.matrixclient.feature.manager.ModuleManager;
import cis.matrixclient.feature.manager.SettingManager;
import cis.matrixclient.feature.module.Module;
import cis.matrixclient.feature.setting.Setting;
import cis.matrixclient.util.key.Keybind;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.text.Text;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class KeyBindArgumentType implements ArgumentType<Setting<Keybind>> {
    private static final Collection<String> EXAMPLES = SettingManager.getSettings(Setting.Type.Keybind)
            .stream()
            .limit(3)
            .map(Setting::getName)
            .collect(Collectors.toList());

    private static final DynamicCommandExceptionType NO_SUCH_SETTING = new DynamicCommandExceptionType(o ->
            Text.of("Setting with name " + o + " doesn't exist."));

    public static KeyBindArgumentType module() {
        return new KeyBindArgumentType();
    }


    @Override
    public Setting parse(StringReader reader) throws CommandSyntaxException {
        String argument = reader.readString();

        Setting<?> setting = SettingManager.getSetting(SettingManager.getSettings(Setting.Type.Keybind), argument);

        if (setting == null) throw NO_SUCH_SETTING.create(argument);
        return setting;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return CommandSource.suggestMatching(SettingManager.getSettings().stream().map(Setting::getName), builder);
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
