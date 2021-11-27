package org.starloco.locos.fight.ia.type;

import org.starloco.locos.fight.Fight;
import org.starloco.locos.fight.Fighter;
import org.starloco.locos.fight.ia.AbstractIA;
import org.starloco.locos.fight.ia.IAHandler;
import org.starloco.locos.fight.ia.util.Function;

/**
 * Created by Locos on 04/10/2015.
 */
public class IA5 extends AbstractIA  {

    public IA5(Fight fight, Fighter fighter, byte count) {
        super(fight, fighter, count);
    }

    @Override
    public void apply() {
        if (!this.stop && this.fighter.canPlay() && this.count > 0) {
            Fighter target = Function.getNearestEnnemy(this.fight, this.fighter);

            if (target == null) return;
            if (!Function.moveNearIfPossible(this.fight, this.fighter, target)) this.stop = true;

            addNext(this::decrementCount, 1000);
        } else {
            this.stop = true;
        }
    }
}