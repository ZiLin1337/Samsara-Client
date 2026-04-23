package cc.samsara.module.impl.client.cheaterfinder.checks;

import cc.samsara.event.events.impl.game.MotionEvent;
import cc.samsara.module.impl.client.cheaterfinder.Check;
import cc.samsara.module.impl.client.cheaterfinder.PlayerData;
import net.minecraft.world.entity.player.Player;

public class AutoblockCheck extends Check {

    public AutoblockCheck() {
        super("Auto Block");
    }

    @Override
    public void onMotion(MotionEvent event) {
        Player player = getPlayerData().player;
        if (player == null )  {
            return;
        }

        if(getPlayerData().autoBlockTicks >= 8) {
            warn();
        }
    }
}
