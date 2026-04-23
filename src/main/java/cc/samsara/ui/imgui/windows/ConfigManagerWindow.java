package cc.samsara.ui.imgui.windows;

import imgui.ImGui;
import imgui.flag.ImGuiInputTextFlags;
import imgui.type.ImString;
import cc.samsara.Samsara;
import lombok.AllArgsConstructor;

import java.io.File;
import java.util.Objects;

@AllArgsConstructor
public class ConfigManagerWindow {
    private ImString configName;

    public void showInline() {
        if (ImGui.beginChild("ConfigFilesChild", 200, 200, true)) {
            File configDir = Samsara.getInstance().getConfigManager().directory;
            if (configDir.exists() && configDir.isDirectory()) {
                for (File file : Objects.requireNonNull(configDir.listFiles())) {
                    if (file.isFile() && file.toString().endsWith(".samsara")) {
                        String configName = file.getName().replace(".samsara", "");
                        if (ImGui.button(configName + "##" + configName)) {
                            Samsara.getInstance().getConfigManager().loadConfig(configName);
                        }

                        ImGui.sameLine();
                        if (ImGui.button("Delete##" + configName)) {
                            Samsara.getInstance().getConfigManager().deleteConfig(configName);
                        }
                    }
                }
            }
            ImGui.endChild();
        }

        ImGui.inputTextWithHint("##ConfigName", "Config Name", this.configName, ImGuiInputTextFlags.None);
        
        ImGui.sameLine();
        if (ImGui.button("Save")) {
            Samsara.getInstance().getConfigManager().saveConfig(configName.get());
            Samsara.getInstance().getConfigManager().addConfigsToHashMap();
        }

        ImGui.sameLine();
        if (ImGui.button("Load")) {
            Samsara.getInstance().getConfigManager().loadConfig(configName.get());
        }
    }
}

