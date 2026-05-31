package cc.samsara;

import cc.samsara.commands.CommandManager;
import cc.samsara.component.ComponentManager;
import cc.samsara.config.ConfigManager;
import cc.samsara.config.impl.AltConfig;
import cc.samsara.config.impl.DraggableConfig;
import cc.samsara.event.EventManager;
import cc.samsara.friends.FriendManager;
import cc.samsara.interfaces.IAccess;
import cc.samsara.module.ModuleManager;
import cc.samsara.module.impl.visual.HudModule;
import cc.samsara.media.MediaPlayerAccessor;
import cc.samsara.skija.SkijaImpl;
import cc.samsara.ui.animations.AnimationManager;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;

@Getter
public class Samsara implements IAccess {
    @Getter
    private static final Samsara instance = new Samsara();
    public static final Logger LOGGER = LoggerFactory.getLogger("samsara");
    public static final String NAME = "Samsara", BY = "Developed by Kawase, Badaiim & IHasseDich";
    public static final double VERSION = 1.27;

    private final IClientProfileMeta clientProfileMeta = new ClientProfileMeta(ClientProfileType.SAMSARA, Samsara.VERSION);

    public static String commitInfo, commitID;

    private ModuleManager moduleManager;
    private EventManager eventManager;
    private ComponentManager componentManager;
    private AnimationManager animationManager;
    private ConfigManager configManager;
    private CommandManager commandManager;
    private FriendManager friendManager;
    private AltConfig altConfig;
    private DraggableConfig draggableConfig;
    private SkijaImpl skija;

    public void init() {
        File samsaraDir = new File(mc.gameDirectory, "/" + NAME.toLowerCase());
        if (!samsaraDir.exists()) {
            if (samsaraDir.mkdirs()) {
                LOGGER.info("/samsara directory created successfully.");
            } else {
                LOGGER.error("Failed to create /samsara directory.");
            }
        }

        eventManager = new EventManager();
        animationManager = new AnimationManager();
        moduleManager = new ModuleManager();
        componentManager = new ComponentManager();
        configManager = new ConfigManager();
        commandManager = new CommandManager();
        friendManager = new FriendManager();
        skija = new SkijaImpl();

        altConfig = new AltConfig(false);
        draggableConfig = new DraggableConfig();

        MediaPlayerAccessor.run();

        // Initialize all modules, commands, and components immediately
        componentManager.registerComponents();
        moduleManager.registerModules();
        commandManager.registerCommands();
        animationManager.start();
        altConfig.loadConfig();
        draggableConfig.loadConfig();
        configManager.init();

        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown, "Shutdown Thread"));
    }

    public void shutdown() {
        //Samsara.LOGGER.error("shut");
        altConfig.writeConfig();
        configManager.writeDefaultConfig();
        draggableConfig.writeConfig();
    }

    public Color getFirstColor() {
        return getModuleManager().getModule(HudModule.class).firstColor.getProperty();
    }

    public Color getSecondColor() {
        return getModuleManager().getModule(HudModule.class).secondColor.getProperty();
    }
}