package cc.samsara.module.impl.client;

import cc.samsara.Samsara;
import cc.samsara.event.EventTarget;
import cc.samsara.event.events.impl.network.BackendMessageEvent;
import cc.samsara.module.Category;
import cc.samsara.module.Module;
import cc.samsara.util.render.ChatUtil;

public class IrcModule extends Module {

    public IrcModule() {
        super(Category.VISUAL);
    }

    @EventTarget
    public void onBackendMessage(BackendMessageEvent event) {
        mc.gui.getChat().addMessage(ChatUtil.translateToGradient("IRC",
                Samsara.getInstance().getFirstColor().getRGB(), Samsara.getInstance().getSecondColor().getRGB())
                .append(" » " + event.getMessage())
        );
    }
}
