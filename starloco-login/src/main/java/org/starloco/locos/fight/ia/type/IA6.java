package org.starloco.locos.fight.ia.type;

import org.starloco.locos.fight.Fight;
import org.starloco.locos.fight.Fighter;
import org.starloco.locos.fight.ia.AbstractIA;
import org.starloco.locos.fight.ia.IAHandler;
import org.starloco.locos.fight.ia.util.Function;

/**
 * Created by Locos on 04/10/2015.
 */
public class IA6 extends AbstractIA  {

    public IA6(Fight fight, Fighter fighter, byte count) {
        super(fight, fighter, count);
    }

    @Override
    public void apply() {
        if (!this.stop && this.fighter.canPlay() && this.count > 0) {
            if (!Function.invocIfPossible(this.fight, this.fighter)) {
                Fighter friend = Function.getNearestFriend(this.fight, this.fighter);
                Fighter ennemy = Function.getNearestEnnemy(this.fight, this.fighter);

                if (!Function.HealIfPossible(this.fight, this.fighter, false)) {
                    if (!Function.buffIfPossible(this.fight, this.fighter, friend)) {
                        if (!Function.buffIfPossible(this.fight, this.fighter, this.fighter)) {
                            if (!Function.HealIfPossible(this.fight, this.fighter, true)) {
                                int attack = Function.attackIfPossibleAll(fight, this.fighter, ennemy);

                                if (attack != 0) {
                                    if (attack == 5) this.stop = true;
                                    if (Function.moveFarIfPossible(this.fight, this.fighter) != 0) this.stop = true;
                                }
                            }
                        }
                    }
                }
            }

            addNext(this::decrementCount, 1000);
        } else {
            this.stop = true;
        }
    }
}