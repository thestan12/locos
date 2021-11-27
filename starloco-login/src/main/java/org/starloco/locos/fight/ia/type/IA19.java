package org.starloco.locos.fight.ia.type;

import org.starloco.locos.fight.Fight;
import org.starloco.locos.fight.Fighter;
import org.starloco.locos.fight.ia.AbstractIA;
import org.starloco.locos.fight.ia.IAHandler;
import org.starloco.locos.fight.ia.util.Function;

/**
 * Created by Locos on 04/10/2015.
 */
public class IA19 extends AbstractIA  {

    public IA19(Fight fight, Fighter fighter, byte count) {
        super(fight, fighter, count);
    }

    @Override
    public void apply() {
        if (!this.stop && this.fighter.canPlay() && this.count > 0) {
            Fighter friend = Function.getNearestFriend(this.fight, this.fighter), ennemy = Function.getNearestEnnemy(this.fight, this.fighter);

            if (!Function.moveNearIfPossible(this.fight, this.fighter, ennemy))
                if (!Function.HealIfPossiblefriend(fight, this.fighter, friend))
                    if(Function.attackIfPossibleTynril(this.fight, this.fighter, ennemy) == 0)
                        Function.tpIfPossibleTynril(this.fight, this.fighter, friend);

            addNext(this::decrementCount, 1000);
        } else {
            this.stop = true;
        }
    }
}