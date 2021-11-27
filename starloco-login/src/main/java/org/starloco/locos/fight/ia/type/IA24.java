package org.starloco.locos.fight.ia.type;

import org.starloco.locos.fight.Fight;
import org.starloco.locos.fight.Fighter;
import org.starloco.locos.fight.ia.AbstractIA;
import org.starloco.locos.fight.ia.IAHandler;
import org.starloco.locos.fight.ia.util.Function;

/**
 * Created by Locos on 04/10/2015.
 */
public class IA24 extends AbstractIA  {

    public IA24(Fight fight, Fighter fighter, byte count) {
        super(fight, fighter, count);
    }

    @Override
    public void apply() {
        if (!this.stop && this.fighter.canPlay() && this.count > 0) {
            Fighter friend = Function.getNearestFriendNoInvok(this.fight, this.fighter);

            if (!Function.moveNearIfPossible(this.fight, this.fighter, friend))
                if (!Function.buffIfPossible(this.fight, this.fighter, friend))
                    Function.moveFarIfPossible(this.fight, this.fighter);

            addNext(this::decrementCount, 800);
        } else {
            this.stop = true;
        }
    }
}