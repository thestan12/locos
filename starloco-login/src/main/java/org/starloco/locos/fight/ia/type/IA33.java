package org.starloco.locos.fight.ia.type;

import org.starloco.locos.fight.Fight;
import org.starloco.locos.fight.Fighter;
import org.starloco.locos.fight.ia.AbstractNeedSpell;
import org.starloco.locos.fight.ia.IAHandler;
import org.starloco.locos.fight.ia.util.Function;
import org.starloco.locos.fight.spells.Spell;

import java.util.Map;

/**
 * Created by Locos on 04/10/2015.
 */
public class IA33 extends AbstractNeedSpell  {

    public IA33(Fight fight, Fighter fighter, byte count) {
        super(fight, fighter, count);
    }

    @Override
    public void apply() {
        if (!this.stop && this.fighter.canPlay() && this.count > 0) {
            boolean action = false;
            int time = 100, maxPo = 1, maxPoBuff = 1;

            for(Spell.SortStats S : this.highests)
                if(S.getMaxPO() > maxPo)
                    maxPo = S.getMaxPO();
            for(Spell.SortStats S : this.buffs)
                if(S.getMaxPO() > maxPo)
                    maxPoBuff = S.getMaxPO();

            Fighter nearestEnnemy = Function.getNearestEnnemy(this.fight, this.fighter);
            Fighter ennemy1 = Function.getNearestEnnemynbrcasemax(this.fight, this.fighter, 1, maxPo + 1);// pomax +1;
            Fighter ennemy2 = Function.getNearestEnnemynbrcasemax(this.fight, this.fighter, 0, 2);//2 = po min 1 + 1;
            Fighter ennemy3 = Function.getNearestAminbrcasemax(this.fight, this.fighter, 0, maxPoBuff + 1);//2 = po min 1 + 1;

            if(maxPo == 1) ennemy1 = null;
            if(ennemy2 != null) if(ennemy2.isHide()) ennemy2 = null;
            if(ennemy1 != null) if(ennemy1.isHide()) ennemy1 = null;

            if(this.fighter.getCurPm(this.fight) > 0 && ennemy1 == null && ennemy2 == null) {
                int value = Function.moveautourIfPossible(this.fight, this.fighter, nearestEnnemy);
                if(value != 0) {
                    time = value;
                    action = true;
                    ennemy1 = Function.getNearestEnnemynbrcasemax(this.fight, this.fighter, 1, maxPo + 1);// pomax +1;
                    ennemy2 = Function.getNearestEnnemynbrcasemax(this.fight, this.fighter, 0, 2);//2 = po min 1 + 1;
                    ennemy3 = Function.getNearestAminbrcasemax(this.fight, this.fighter, 0, maxPoBuff+1);//2 = po min 1 + 1;
                    if(maxPo == 1) ennemy1 = null;
                }
            }

            if(this.fighter.getCurPa(this.fight) > 0 && !action) {
                if (Function.invocIfPossible(this.fight, this.fighter, this.invocations)) {
                    time = 2000;
                    action = true;
                }
            }

            if(this.fighter.getCurPa(this.fight) > 0 && !action && ennemy3 != null) {
                if (Function.buffIfPossible(this.fight, this.fighter, ennemy3, this.buffs)) {
                    time = 2000;
                    action = true;
                }
            }

            if(this.fighter.getCurPa(this.fight) > 0 && ennemy1 != null && ennemy2 == null && !action) {
                int value = Function.attackIfPossible(this.fight, this.fighter, this.highests);
                if(value != 0) {
                    time = value;
                    action = true;
                }
            } else if(this.fighter.getCurPa(this.fight) > 0 && ennemy2 != null && !action) {
                int value = Function.attackIfPossible(this.fight, this.fighter, this.cacs);
                if(value != 0) {
                    time = value;
                    action = true;
                }
            }

            if(this.fighter.getCurPa(this.fight) > 0 && ennemy2 != null && !action) {
                int value = Function.attackIfPossible(this.fight, this.fighter, this.highests);
                if(value != 0) {
                    time = value;
                    action = true;
                }
            }

            if(this.fighter.getCurPm(this.fight) > 0 && !action) {
                int value = Function.moveautourIfPossible(this.fight, this.fighter, nearestEnnemy);
                if(value != 0) time = value;
            }

            if(this.fighter.getCurPa(this.fight) == 0 && this.fighter.getCurPm(this.fight) == 0) this.stop = true;
            addNext(this::decrementCount, time);
        } else {
            this.stop = true;
        }
    }
}