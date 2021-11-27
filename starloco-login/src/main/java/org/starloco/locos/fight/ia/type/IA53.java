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
public class IA53 extends AbstractNeedSpell  {

    private byte attack = 0;
    
    public IA53(Fight fight, Fighter fighter, byte count) {
        super(fight, fighter, count);
    }

    @Override
    public void apply() {
        if (!this.stop && this.fighter.canPlay() && this.count > 0) {
            int time = 100, maxPo = 1;
            boolean action = false;

            for(Spell.SortStats spellStats : this.highests)
                if(spellStats.getMaxPO() > maxPo)
                    maxPo = spellStats.getMaxPO();

            Fighter ennemy = Function.getNearestEnnemy(this.fight, this.fighter);
            Fighter L = Function.getNearestEnnemynbrcasemax(this.fight, this.fighter, 1, maxPo + 1);// pomax +1;
            Fighter C = Function.getNearestEnnemynbrcasemax(this.fight, this.fighter, 0, 2);//2 = po min 1 + 1;

            if(maxPo == 1) L = null;
            if(C != null && C.isHide()) C = null;
            if(L != null && L.isHide()) L = null;
            
            if(this.attack >= 4) {
                int value = Function.moveFarIfPossible(this.fight, this.fighter);
                if(value != 0) {
                    time = value;
                    action = true;
                    this.stop = true;
                }
            }
            if(this.attack >= 3) {
                int value = Function.attackIfPossiblePeki(this.fight, this.fighter, L);
                if(value != 0) {
                    time = 3000;
                    action = true;
                    this.attack++;
                }
            }
            if(this.fighter.getCurPm(this.fight) > 0 && C == null && this.attack < 3) {
                int value = Function.moveautourIfPossible(this.fight, this.fighter, ennemy);
                if(value != 0) {
                    time = value;
                    action = true;
                    L = Function.getNearestEnnemynbrcasemax(this.fight, this.fighter, 1, maxPo + 1);// pomax +1;
                    C = Function.getNearestEnnemynbrcasemax(this.fight, this.fighter, 0, 2);//2 = po min 1 + 1;
                    if(maxPo == 1) L = null;
                }
            }
            if(this.fighter.getCurPa(this.fight) > 0 && !action && this.attack >= 3) {
                if (Function.buffIfPossible(this.fight, this.fighter, this.fighter, this.buffs)) {
                    time = 1500;
                    action = true;
                }
            }
        
            if(this.fighter.getCurPa(this.fight) > 0 && L != null && C == null && !action) {
                int value = Function.attackIfPossible(this.fight, this.fighter, this.highests);
                if(value != 0) {
                    time = value * 2;
                    action = true;
                    this.attack++;
                }
            } else if(this.fighter.getCurPa(this.fight) > 0 && C != null && !action) {
                int value = Function.attackIfPossible(this.fight, this.fighter, this.cacs);
                if(value != 0) {
                    time = value*2;
                    action = true;
                    this.attack++;
                }
            }
            if(this.fighter.getCurPa(this.fight) > 0 && C != null && !action) {
                int value = Function.attackIfPossible(this.fight, this.fighter, this.highests);
                if(value != 0) {
                    time = value * 2;
                    this.attack++;
                }
            }
           
            if(this.fighter.getCurPa(this.fight) == 0 && this.fighter.getCurPm(this.fight) == 0) this.stop = true;
            addNext(this::decrementCount, time);
        } else {
            this.stop = true;
        }
    }
}