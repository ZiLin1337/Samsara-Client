package cc.samsara.commands.commands.client;

import cc.samsara.Samsara;
import cc.samsara.commands.Command;
import cc.samsara.util.io.HwidUtil;
import cc.samsara.util.render.ChatUtil;
import java.io.File;
import net.minecraft.client.Minecraft;

public class ConfigCommand extends Command {
    public ConfigCommand() {
        super(new String[]{"config", "c"}, "Manages configurations: .config <save|relod|load|delete> <name>");
    }

    @Override
    public void execute(String[] args, String message) {
        String[] words = message.split(" ");

        if (words.length < 2) {
            ChatUtil.print("Usage: #config <save|load|delete|reload|folder|list> <name>");
            return;
        }

        String action = words[1].toLowerCase();
        String configName = words.length > 2 ? message.substring(message.indexOf(action) + action.length()).trim() : null;

        Samsara.getInstance().getConfigManager().addConfigsToHashMap();

        switch (action) {
            case "folder":
                if (HwidUtil.PlatformInfo.detect() == HwidUtil.PlatformInfo.OS.WIN) {
                    File moduleDirectory = new File(
                            Minecraft.getInstance().gameDirectory,
                            "/" + Samsara.NAME.toLowerCase() + "/Configs"
                    );
                    try {
                        Runtime.getRuntime().exec("explorer.exe  /select," + moduleDirectory.getAbsolutePath());
                    } catch (Exception ex) {
                        System.out.println("Error - " + ex);
                    }
                } else {
                    ChatUtil.print("This command is only available for windows users.");
                }
                break;
            case "list":
                ChatUtil.print("available configs: ");
                for (String cfg : Samsara.getInstance().getConfigManager().configs.keySet()) {
                    ChatUtil.print(cfg);
                }
                break;
            case "save":
                if (configName == null || configName.isEmpty()) {
                    ChatUtil.print("Usage: #config save <name>");
                    return;
                }

                Samsara.getInstance().getConfigManager().saveConfig(configName);
                ChatUtil.print("Saved config: " + configName);
                break;
            case "load":
                if (configName == null || configName.isEmpty()) {
                    ChatUtil.print("Usage: #config load <name>");
                    return;
                }
                Samsara.getInstance().getConfigManager().loadConfig(configName);
                break;
            case "delete":
                if (configName == null || configName.isEmpty()) {
                    ChatUtil.print("Usage: #config delete <name>");
                    return;
                }
                Samsara.getInstance().getConfigManager().deleteConfig(configName);
                ChatUtil.print("Deleted config: " + configName);
                break;
          /*  case "reload":
                Samsara.getInstance().getConfigManager().addConfigsToHashMap();
                break;*/
            default:
                ChatUtil.print("Invalid action. Use save, load, delete, folder, or list");
                break;
        }
    }
}
