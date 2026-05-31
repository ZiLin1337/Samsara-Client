package cc.samsara.commands.commands.client;

import cc.samsara.Samsara;
import cc.samsara.commands.Command;
import cc.samsara.module.Module;
import cc.samsara.util.render.ChatUtil;

public class ToggleModuleCommand extends Command {
    public ToggleModuleCommand() {
        super(new String[]{ "toggle", "t" }, "Toggles Modules (.toggle <module>)");
    }

    @Override
    public void execute(String[] args, String message) {
        String[] words = message.split(" ");

        if (args.length < 1) {
            ChatUtil.print("Wrong Usage .t <Module>");
            return;
        }

        Module module = Samsara.getInstance().getModuleManager().getModuleBySimplifiedName(words[1]);
        if (module != null) {
            module.toggle();
            ChatUtil.print("Toggled " + module.getName() + " module.");
        } else {
            ChatUtil.print("Module not found.");
        }
    }
}
