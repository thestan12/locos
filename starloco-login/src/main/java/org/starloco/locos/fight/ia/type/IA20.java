package org.starloco.locos.fight.ia.type;

import org.starloco.locos.fight.Fight;
import org.starloco.locos.fight.Fighter;
import org.starloco.locos.fight.ia.AbstractIA;
import org.starloco.locos.fight.ia.IAHandler;
import org.starloco.locos.fight.ia.util.Function;
import org.starloco.locos.fight.traps.Glyph;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Locos on 04/10/2015.
 */
public class IA20 extends AbstractIA  {

    private byte attack = 0;

    public IA20(Fight fight, Fighter fighter, byte count) {
        super(fight, fighter, count);
    }

    @Override
    public void apply() {
        if (!this.stop && this.fighter.canPlay() && this.count > 0) {
            Fighter nearestEnnemy = Function.getNearestEnnemynbrcasemax(this.fight, this.fighter, 0, 5);
            Fighter highestEnnemy = Function.getNearestEnnemynbrcasemax(this.fight, this.fighter, 0, 30);
            List<Short> cells = new ArrayList<>();
            int attack = 0, tp = 0;
            boolean action = false;

            cells.addAll(this.fight.getAllGlyphs().stream().filter(glyph -> glyph != null && glyph.getCaster().getId() == this.fighter.getId()).map(glyph -> (short) glyph.getCell().getId()).collect(Collectors.toList()));

            if(nearestEnnemy == null)
                if(Function.moveNearIfPossible(this.fight, this.fighter, highestEnnemy))
                    action = true;
            if(this.attack == 0 && !action)
                attack = Function.attackIfPossibleKaskargo(this.fight, this.fighter, this.fighter);

            if(attack != 0) {
                this.attack++;
                action = true;
            }


            if(!action && !cells.isEmpty()) {
                if(cells.contains((short) this.fighter.getCell().getId()))
                    tp = Function.tpIfPossibleKaskargo(this.fight, this.fighter, nearestEnnemy);
            }
            if(tp != 0) action = true;
            if(!action) Function.moveNearIfPossible(this.fight, this.fighter, highestEnnemy);

            addNext(this::decrementCount, 1000);
        } else {
            this.stop = true;
        }
    }
}