package org.starloco.locos.fight.ia.type;

import org.starloco.locos.fight.Fight;
import org.starloco.locos.fight.Fighter;
import org.starloco.locos.fight.ia.AbstractIA;
import org.starloco.locos.fight.ia.IAHandler;
import org.starloco.locos.fight.ia.util.Function;

/**
 * Created by Locos on 04/10/2015.
 */
public class IA21 extends AbstractIA  {

    public IA21(Fight fight, Fighter fighter, byte count) {
        super(fight, fighter, count);
    }

    @Override
    public void apply() {
        if (!this.stop && this.fighter.canPlay() && this.count > 0) {
            Function.buffIfPossibleKrala(this.fight, this.fighter, this.fighter);
            Function.invoctantaIfPossible(this.fight, this.fighter);
            addNext(this::decrementCount, 1000);
        } else {
            this.stop = true;
        }
    }
}