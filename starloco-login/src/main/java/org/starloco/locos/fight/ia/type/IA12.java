package org.starloco.locos.fight.ia.type;

import org.starloco.locos.fight.Fight;
import org.starloco.locos.fight.Fighter;
import org.starloco.locos.fight.ia.AbstractNeedSpell;
import org.starloco.locos.fight.ia.IAHandler;
import org.starloco.locos.fight.ia.util.Function;
import org.starloco.locos.fight.spells.Spell.SortStats;

import java.util.Map;

/**
 * Created by Locos on 04/10/2015.
 */
public class IA12 extends AbstractNeedSpell  {

    private byte attack = 0;
    private boolean boost = false;

    public IA12(Fight fight, Fighter fighter, byte count) {
        super(fight, fighter, count);
    }

    @Override
    public void apply() {
        if (!this.stop && this.fighter.canPlay() && this.count > 0) {
            Fighter ennemy = Function.getNearestEnnemy(this.fight, this.fighter);
            int PA = this.fighter.getCurPa(this.fight), PM = this.fighter.getCurPm(this.fight), time = 100, maxPo = 1;
            boolean action = false;

            if (this.fighter.getMob().getPa() < PA) this.boost = true;

            for (SortStats spellStats : this.highests)
                if (spellStats != null && spellStats.getMaxPO() > maxPo)
                    maxPo = spellStats.getMaxPO();

            Fighter target = Function.getNearestEnnemynbrcasemax(this.fight, this.fighter, 0, 3);

            if (target != null)
                if (target.isHide())
                    target = null;

            if (PM > 0 && target == null && this.attack == 0 || PM > 0 && target == null && this.attack == 1 && this.boost) {
                int num = Function.movediagIfPossible(this.fight, this.fighter, ennemy);
                if (num != 0) {
                    time = num;
                    action = true;
                    target = Function.getNearestEnnemynbrcasemax(this.fight, this.fighter, 0, 3);
                }
            }

            PA = this.fighter.getCurPa(this.fight);
            PM = this.fighter.getCurPm(this.fight);

            if (PA > 0 && target != null && !action) {
                int num = Function.attackIfPossible(this.fight, this.fighter, this.highests);
                if (num != 0) {
                    time = num;
                    action = true;
                    this.attack++;
                }
            }

            if (PM > 0 && !action && this.attack > 0) {
                int num = Function.moveFarIfPossible(this.fight, this.fighter);
                if (num != 0) time = num;
            }

            if (this.fighter.getCurPa(this.fight) == 0 && this.fighter.getCurPm(this.fight) == 0)
                this.stop = true;

            addNext(this::decrementCount, time);
        } else {
            this.stop = true;
        }
    }
}