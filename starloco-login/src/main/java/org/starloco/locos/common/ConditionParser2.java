package org.starloco.locos.common;


import com.singularsys.jep.Jep;
import com.singularsys.jep.JepException;
import org.starloco.locos.client.Player;
import org.starloco.locos.game.world.World;
import org.starloco.locos.kernel.Constant;

public class ConditionParser2 {
    public static boolean               test() {
        World.world.logger.info("test222");
        return true;
    }
    public static boolean validConditions(Player perso, String req) {
        if (req == null || req.equals(""))
            return true;
        if (req.contains("BI"))
            return false;
        if (perso == null)
            return false;

        Jep jep = new Jep();
        req = req.replace("&", "&&").replace("=", "==").replace("|", "||").replace("!", "!=").replace("~", "==");
        if (req.contains("Sc"))
            return true;
        if (req.contains("Pg")) // C'est les dons que l'on gagne lors des qu�tes d'alignement, connaissance des potions etc ... ce n'est pas encore cod� !
            return false;

        try {
            //Stats stuff compris
            jep.addVariable("CI", perso.getTotalStats().getEffect(Constant.STATS_ADD_INTE));

        } catch (JepException e) {
            e.printStackTrace();
        }

        return true;
    }

}