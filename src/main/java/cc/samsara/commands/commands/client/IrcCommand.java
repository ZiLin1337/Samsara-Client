package cc.samsara.commands.commands.client;

import cc.samsara.Samsara;
import cc.samsara.commands.Command;
import cc.samsara.util.render.ChatUtil;
import club.serenityutils.packets.impl.BroadcastMessagePacket;
import club.serenityutils.packets.impl.GlobalIRCMessagePacket;

import java.util.Arrays;

public class IrcCommand extends Command {
    public IrcCommand() {
        super(new String[]{ "irc", "i" }, "Sends a message to all client users (.irc <message>");
    }

    @Override
    public void execute(String[] args, String message) {
        if (args.length < 1) {
            ChatUtil.print("Wrong Usage .irc <Message>");
            return;
        }

        // test 2
        String joined = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        new BroadcastMessagePacket(joined).sendPacket(Samsara.getInstance().getClient());
        new GlobalIRCMessagePacket(joined).sendPacket(Samsara.getInstance().getClient());
    }
}
