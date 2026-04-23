package cc.samsara.commands.commands.client;

import cc.samsara.Samsara;
import cc.samsara.commands.Command;
import cc.samsara.friends.FriendManager;
import cc.samsara.util.render.ChatUtil;

public class FriendCommand extends Command {
    public FriendCommand() {
        super(new String[]{ "friend", "f"}, "Whitelists ppl from certain modules (.friend <add or remove> <name>");
    }

    @Override
    public void execute(String[] args, String message) {
        String[] words = message.split(" ");
        FriendManager friendManager = Samsara.getInstance().getFriendManager();

        if (words.length < 2) {
            ChatUtil.print("Wrong Usage .friend <add or remove> <name>");
            return;
        }

        switch (words[1]) {
            case "add" -> {
                friendManager.addFriend(words[2]);
                ChatUtil.print("Added " + words[2] + " As a Friend");
            }
            case "remove" -> {
                friendManager.remove(words[2]);
                ChatUtil.print("Removed " + words[2] + " From Friends List");
            }
        }
    }
}
