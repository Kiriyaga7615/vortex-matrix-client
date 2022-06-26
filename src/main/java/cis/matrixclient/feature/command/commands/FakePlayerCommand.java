package cis.matrixclient.feature.command.commands;

import cis.matrixclient.feature.command.Command;
import cis.matrixclient.feature.manager.ModuleManager;
import cis.matrixclient.feature.module.modules.client.FakePlayer;
import cis.matrixclient.util.player.ChatUtils;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class FakePlayerCommand extends Command {
    public FakePlayerCommand() {
        super("fakeplayer", "");
    }

    private FakePlayer fakePlayer = ModuleManager.getModule(FakePlayer.class);

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(literal("spawn").executes(context -> {
            if (!fakePlayer.enabled) fakePlayer.toggle();
            return SINGLE_SUCCESS;
        }));
        builder.then(literal("remove").executes(context -> {
            if (fakePlayer.enabled) fakePlayer.toggle();
            return SINGLE_SUCCESS;
        }));
        builder.then(literal("play").executes(context -> {
            FakePlayer.instance.action = FakePlayer.Action.Play;
            FakePlayer.instance.startPlaying = true;
            ChatUtils.info("Playing...", fakePlayer);
            return SINGLE_SUCCESS;
        }));
        builder.then(literal("record").executes(context -> {
            FakePlayer.instance.action = FakePlayer.Action.Record;
            FakePlayer.instance.startRecording = true;
            ChatUtils.info("Recording...", fakePlayer);
            return SINGLE_SUCCESS;
        }));
        builder.then(literal("stop").executes(context -> {
            FakePlayer.instance.action = FakePlayer.Action.Stop;
            ChatUtils.info("Recording was stopped...", fakePlayer);
            return SINGLE_SUCCESS;
        }));
    }
}
