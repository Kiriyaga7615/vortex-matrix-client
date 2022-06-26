package cis.matrixclient.feature.command.commands;

import cis.matrixclient.feature.command.Command;
import cis.matrixclient.feature.command.arguments.PlayerArgumentType;
import cis.matrixclient.feature.manager.FriendManager;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.PlayerEntity;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class FriendCommand extends Command {

    public FriendCommand() {
        super("friend", "Friends");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(literal("add").then(argument("player", PlayerArgumentType.player()).executes(context -> {
            PlayerEntity player = PlayerArgumentType.getPlayer(context);

            if (FriendManager.isFriend(player.getEntityName())){
                error("That person is already your friend.");
            }
            else {
                info(player.getEntityName() + " added to friend list");
                FriendManager.addFriend(player.getEntityName());
            }
            return SINGLE_SUCCESS;
        })));
        builder.then(literal("remove").then(argument("player", PlayerArgumentType.player()).executes(context -> {
            PlayerEntity player = PlayerArgumentType.getPlayer(context);

            if (FriendManager.isFriend(player.getEntityName())){
                FriendManager.removeFriend(player.getEntityName());
                info(player.getEntityName() + " removed from friend list");

            }
            else {
                error("That person is already your friend.");
            }
            return SINGLE_SUCCESS;
        })));
        builder.then(literal("list").executes(context -> {
            int i = 1;
            for (String s : FriendManager.getFriends()){
                info(i + ". " + s);
                i += 1;
            }
            return SINGLE_SUCCESS;
        }));
    }

}
