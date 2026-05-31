package cc.samsara.commands.commands.client;

import cc.samsara.commands.Command;
import cc.samsara.util.render.ChatUtil;

import static cc.samsara.interfaces.IAccess.mc;

public class IgnCommand extends Command {
    public IgnCommand() {
        super(new String[]{ "ign", "name","n" }, "Shows you your current name and copies it to youre clip board (.ign)");
    }

    @Override
    public void execute(String[] args, String message) {
        mc.keyboardHandler.setClipboard(mc.getUser().getName());
        ChatUtil.print("Your name is: " + mc.getUser().getName());
    }
}
