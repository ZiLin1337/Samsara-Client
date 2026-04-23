package cc.samsara.commands.commands.client;

import cc.samsara.Samsara;
import cc.samsara.commands.Command;
import cc.samsara.util.render.ChatUtil;

public class HelpCommand extends Command {
    public HelpCommand() {
        super(new String[]{ "help", "h"}, "Shows Information And Usages About Other Commands");
    }

    @Override
    public void execute(String[] args, String message) {
        for (Command command : Samsara.getInstance().getCommandManager().getObjects()) {
            ChatUtil.print("Expresion: " + command.getExpressions()[0]);
            ChatUtil.print("Description: " + command.getDescription());
        }
    }
}
