package org.starloco.locos.fight.ia.type;

import org.starloco.locos.fight.Fight;
import org.starloco.locos.fight.Fighter;
import org.starloco.locos.fight.ia.AbstractIA;
import org.starloco.locos.fight.ia.IAHandler;
import org.starloco.locos.fight.ia.util.Function;

/**
 * Created by Locos on 04/10/2015.
 */
public class IA28 extends AbstractIA  {

    public IA28(Fight fight, Fighter fighter, byte count) {
        super(fight, fighter, count);
    }

    @Override
    public void apply() {
        if (!this.stop && this.fighter.canPlay() && this.count > 0) {
            Fighter firstEnnemy = Function.getNearestEnnemynbrcasemax(this.fight, this.fighter, 0, 6), secondEnnemy = Function.getNearestEnnemy(this.fight, this.fighter);

            if (!Function.invocIfPossibletortu(this.fight, this.fighter, secondEnnemy))
                if (Function.TPIfPossiblesphinctercell(this.fight, this.fighter, firstEnnemy) == 0)
                    if (!Function.moveNearIfPossible(this.fight, this.fighter, secondEnnemy))
                        if(Function.attackIfPossiblesphinctercell(this.fight, this.fighter, secondEnnemy) == 0)
                            Function.moveNearIfPossible(this.fight, this.fighter, secondEnnemy);

            addNext(this::decrementCount, 1000);
        } else {
            this.stop = true;
        }
    }
}