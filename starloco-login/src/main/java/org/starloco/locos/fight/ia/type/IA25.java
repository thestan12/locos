package org.starloco.locos.fight.ia.type;

import org.starloco.locos.fight.Fight;
import org.starloco.locos.fight.Fighter;
import org.starloco.locos.fight.ia.AbstractNeedSpell;
import org.starloco.locos.fight.ia.IAHandler;
import org.starloco.locos.fight.ia.util.Function;
import org.starloco.locos.fight.spells.Spell.SortStats;

/**
 * Created by Locos on 04/10/2015.
 */
public class IA25 extends AbstractNeedSpell  {

    public IA25(Fight fight, Fighter fighter, byte count) {
        super(fight, fighter, count);
    }

    @Override
    public void apply() {
        if (!this.stop && this.fighter.canPlay() && this.count > 0) {
            int time = 100, maxPo = 1;
            boolean action = false;
            Fighter ennemy = Function.getNearestEnnemy(this.fight, this.fighter);

            for(SortStats spellStats : this.highests)
                if(spellStats != null && spellStats.getMaxPO() > maxPo)
                    maxPo = spellStats.getMaxPO();

            Fighter secondEnnemy = Function.getNearestEnnemynbrcasemax(fight, this.fighter, 0, 5);//2 = po min 1 + 1;
            Fighter target = Function.getNearestEnnemynbrcasemax(fight, this.fighter, 0, 2);//2 = po min 1 + 1;

            if(target != null) if(target.isHide()) target = null;

            if(this.fighter.getCurPm(this.fight) > 0 && secondEnnemy == null && target == null) {
                int num = Function.moveautourIfPossible(fight, this.fighter, ennemy);
                if(num != 0) {
                    time = num;
                    action = true;
                    Function.getNearestEnnemynbrcasemax(fight, this.fighter, 0, 5);//2 = po min 1 + 1;
                    target = Function.getNearestEnnemynbrcasemax(fight, this.fighter, 0, 2);//2 = po min 1 + 1;
                }
            } else if(this.fighter.getCurPm(this.fight) > 0 && secondEnnemy != null && target == null) {
                int num = Function.moveautourIfPossible(fight, this.fighter, secondEnnemy);
                if(num != 0) {
                    time = num;
                    action = true;
                    Function.getNearestEnnemynbrcasemax(fight, this.fighter, 0, 5);//2 = po min 1 + 1;
                    target = Function.getNearestEnnemynbrcasemax(fight, this.fighter, 0, 2);//2 = po min 1 + 1;
                }
            }

            if(this.fighter.getCurPa(this.fight) > 0 && target != null && !action) {
                int num = Function.attackIfPossible(this.fight, this.fighter, this.cacs);
                if(num != 0) {
                    time = num;
                    action = true;
                }
            }

            if(this.fighter.getCurPm(this.fight) > 0 && !action) {
                int num = Function.moveautourIfPossible(this.fight, this.fighter, ennemy);
                if(num != 0) time = num;
            }

            if(this.fighter.getCurPa(this.fight) == 0 && this.fighter.getCurPm(this.fight) == 0) this.stop = true;
            addNext(this::decrementCount, time);
        } else {
            this.stop = true;
        }
    }
}