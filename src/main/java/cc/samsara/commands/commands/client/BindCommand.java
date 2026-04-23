package cc.samsara.commands.commands.client;

import cc.samsara.Samsara;
import cc.samsara.commands.Command;
import cc.samsara.module.Module;
import cc.samsara.util.io.KeyBoardUtil;
import cc.samsara.util.render.ChatUtil;

import java.awt.event.KeyEvent;

public class BindCommand extends Command {

    public BindCommand() {
        super(new String[]{ "bind", "b", "keyboard" }, "Binds Modules (.bind <module> <bind>)");
    }

    @Override
    public void execute(String[] args, String message) {
        String[] words = message.split(" ");

        if (words.length < 2) {
            ChatUtil.print("Wrong Usage .bind <module> <bind>");
            return;
        }

        String userInput = words[1].toLowerCase();

        if (userInput.equalsIgnoreCase("list")) {
            for (Module module : Samsara.getInstance().getModuleManager().getModules()) {
                String keybindName = KeyEvent.getKeyText(module.getKeyCode());
                if (module.getKeyCode() != 0 && !keybindName.startsWith("Unknown")) {
                    ChatUtil.print(module.getName() + " is bound to " + keybindName);
                }
            }
            return;
        }

        if (words.length < 3) {
            ChatUtil.print("Wrong Usage .bind <module> <bind>");
            return;
        }

        String bind = words[2];
        Module module = Samsara.getInstance().getModuleManager().getModuleBySimplifiedName(userInput);

        if (module == null) {
            ChatUtil.print("Non-Existing Module " + userInput);
            return;
        }

        final int keyCode = KeyBoardUtil.getKeyCode(bind);
        module.setKeyCode(keyCode);
        ChatUtil.print("Bound " + module.getName() + " to " + bind.toUpperCase());
    }
}
