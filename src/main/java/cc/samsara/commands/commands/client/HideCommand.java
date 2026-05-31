package cc.samsara.commands.commands.client;

import cc.samsara.Samsara;
import cc.samsara.commands.Command;
import cc.samsara.module.Module;
import cc.samsara.util.render.ChatUtil;

public class HideCommand extends Command {
    public HideCommand() {
        super(new String[]{ "hide", "h" }, "Hide Modules (.hide <module>)");
    }

    @Override
    public void execute(String[] args, String message) {
        if (args.length < 2) {
            ChatUtil.print("Wrong Usage: .hide <module>");
            return;
        }

        Module module = Samsara.getInstance().getModuleManager().getModuleBySimplifiedName(args[1]);
        if (module == null) {
            ChatUtil.print("Module not found: " + args[1]);
            return;
        }

        boolean hidden;
        if (args.length >= 3) {
            hidden = Boolean.parseBoolean(args[2].toLowerCase());
        } else {
            hidden = !module.isHidden();
        }

        module.setHidden(hidden);
        ChatUtil.print((hidden ? "Hid " : "Unhid ") + module.getName() + " module.");
    }
}
