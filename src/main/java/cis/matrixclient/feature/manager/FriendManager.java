package cis.matrixclient.feature.manager;

import net.minecraft.entity.player.PlayerEntity;

import java.util.ArrayList;
import java.util.List;

public class FriendManager {
    private static List<String> friends = new ArrayList<>();

    public static void addFriend(String name) {
        friends.add(name);
    }

    public static boolean removeFriend(String name) {
        if (isFriend(name)) friends.remove(name);
        else return false;
        return true;
    }

    public static boolean isFriend(String name) {
        return friends.contains(name);
    }

    public static boolean isFriend(PlayerEntity player) {
        return friends.contains(player.getGameProfile().getName());
    }

    public static List<String> getFriends() {
        return friends;
    }
}
