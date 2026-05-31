package cc.samsara.config;

import cc.samsara.Samsara;
import java.io.File;
import net.minecraft.client.Minecraft;

public abstract class Config {
    public File moduleDirectory = new File(
            Minecraft.getInstance().gameDirectory,
            "/" + Samsara.NAME.toLowerCase() + "/Configs"
    );
    public File baseDirectory = new File(
            Minecraft.getInstance().gameDirectory,
            "/" + Samsara.NAME.toLowerCase()
    );

    public abstract void writeConfig();
    public abstract void loadConfig();
}
