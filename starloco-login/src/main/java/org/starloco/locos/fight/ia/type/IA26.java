package org.starloco.locos.fight.ia.type;

import org.starloco.locos.fight.Fight;
import org.starloco.locos.fight.Fighter;
import org.starloco.locos.fight.ia.AbstractIA;
import org.starloco.locos.fight.ia.IAHandler;
import org.starloco.locos.fight.ia.util.Function;

/**
 * Created by Locos on 04/10/2015.
 */
public class IA26 extends AbstractIA  {

    public IA26(Fight fight, Fighter fighter, byte count) {
        super(fight, fighter, count);
    }

    @Override
    public void apply() {
        if (!this.stop && this.fighter.canPlay() && this.count > 0) {
            Fighter ennemy = Function.getNearestEnnemy(this.fight, this.fighter);
            Function.attackIfPossibleAll(this.fight, this.fighter, ennemy);

            if (!Function.invocIfPossible(this.fight, this.fighter))
                if (!Function.moveNearIfPossible(this.fight, this.fighter, ennemy))
                    if (!Function.buffIfPossibleKitsou(this.fight, this.fighter, this.fighter))
                        if(Function.attackIfPossibleAll(this.fight, this.fighter, ennemy) == 0)
                            Function.moveFarIfPossible(this.fight, this.fighter);

            addNext(this::decrementCount, 2000);
        } else {
            this.stop = true;
        }
    }
}