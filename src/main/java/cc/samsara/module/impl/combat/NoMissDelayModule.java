package cc.samsara.module.impl.combat;

import cc.samsara.event.EventTarget;
import cc.samsara.event.events.impl.game.EntityInteractEvent;
import cc.samsara.event.types.EventModes;
import cc.samsara.module.Category;
import cc.samsara.module.Module;
import cc.samsara.property.properties.BooleanProperty;

public class NoMissDelayModule extends Module {
    public final BooleanProperty attackcooldown = new BooleanProperty("Removes Attack Cooldown",true);
    public final BooleanProperty attackonMiss = new BooleanProperty("Removes your attack on miss",true);
    public final BooleanProperty weaponOnly = new BooleanProperty("Weapon Only", true);
    public final BooleanProperty allowBlockHit = new BooleanProperty("Allow Block Hit", false);
    public final BooleanProperty allowAirHit = new BooleanProperty("Allow Air Hit", false);

    public NoMissDelayModule() {
        super(Category.COMBAT);
        registerProperties(attackcooldown, attackonMiss, weaponOnly, allowBlockHit, allowAirHit);
    }
}
