package org.starloco.locos.job;

import org.starloco.locos.client.Player;
import org.starloco.locos.common.Formulas;
import org.starloco.locos.common.PathFinding;
import org.starloco.locos.common.SocketManager;
import org.starloco.locos.entity.monster.Monster;
import org.starloco.locos.fight.spells.SpellEffect;
import org.starloco.locos.game.action.ExchangeAction;
import org.starloco.locos.game.action.GameAction;
import org.starloco.locos.game.world.World;
import org.starloco.locos.game.world.World.Couple;
import org.starloco.locos.kernel.Config;
import org.starloco.locos.kernel.Constant;
import org.starloco.locos.kernel.Logging;
import org.starloco.locos.area.map.GameCase;
import org.starloco.locos.area.map.entity.InteractiveObject;
import org.starloco.locos.object.GameObject;
import org.starloco.locos.object.ObjectTemplate;
import java.util.*;
import java.util.Map.Entry;

public class JobAction {

    public Map<Integer, Integer> ingredients = new TreeMap<>(), lastCraft = new TreeMap<>();
    public Player player;
    public String data = "";
    public boolean broke = false;
    public boolean broken = false;
    public boolean isRepeat = false;
    private int id;
    private int min = 1;
    private int max = 1;
    private boolean isCraft;
    private int chan = 100;
    private int time = 0;
    private int xpWin = 0;
    private JobStat SM;
    private JobCraft jobCraft;
    public JobCraft oldJobCraft;

    public JobAction(int sk, int min, int max, boolean craft, int arg, int xpWin) {
        this.id = sk;
        this.min = min;
        this.max = max;
        this.isCraft = craft;
        this.xpWin = xpWin;
        if (craft) this.chan = arg;
        else this.time = arg;
    }

    public int getId() {
        return this.id;
    }

    public int getMin() {
        return this.min;
    }

    public int getMax() {
        return this.max;
    }

    public boolean isCraft() {
        return this.isCraft;
    }

    public int getChance() {
        return this.chan;
    }

    public int getTime() {
        return this.time;
    }

    public int getXpWin() {
        return this.xpWin;
    }

    public JobStat getJobStat() {
        return this.SM;
    }

    public JobCraft getJobCraft() {
        return this.jobCraft;
    }

    public void setJobCraft(JobCraft jobCraft) {
        this.jobCraft = jobCraft;
    }

    public void startCraft(Player P) {
        this.jobCraft = new JobCraft(this, P);
    }

    public void startAction(Player P, InteractiveObject IO, GameAction GA, GameCase cell, JobStat SM) {
        this.SM = SM;
        this.player = P;

        if (P.getObjetByPos(Constant.ITEM_POS_ARME) != null && SM.getTemplate().getId() == 36) {
            if (World.world.getMetier(36).isValidTool(P.getObjetByPos(Constant.ITEM_POS_ARME).getTemplate().getId())) {
                int dist = PathFinding.getDistanceBetween(P.getCurMap(), P.getCurCell().getId(), cell.getId());
                int distItem = JobConstant.getDistCanne(P.getObjetByPos(Constant.ITEM_POS_ARME).getTemplate().getId());
                if (distItem < dist) {
                    SocketManager.GAME_SEND_MESSAGE(P, "Vous �tes trop loin pour pouvoir p�cher ce poisson !");
                    SocketManager.GAME_SEND_GA_PACKET(P.getGameClient(), "", "0", "", "");
                    P.setExchangeAction(null);
                    P.setDoAction(false);
                    return;
                }
            }
        }
        if (!this.isCraft) {
            P.getGameClient().action = System.currentTimeMillis();
            IO.setInteractive(false);
            IO.setState(JobConstant.IOBJECT_STATE_EMPTYING);
            SocketManager.GAME_SEND_GA_PACKET_TO_MAP(P.getCurMap(), "" + GA.id, 501, P.getId() + "", cell.getId() + "," + this.time);
            SocketManager.GAME_SEND_GDF_PACKET_TO_MAP(P.getCurMap(), cell);
        } else {
            P.setAway(true);
            IO.setState(JobConstant.IOBJECT_STATE_EMPTYING);
            P.setExchangeAction(new ExchangeAction<>(ExchangeAction.CRAFTING, this));
            SocketManager.GAME_SEND_ECK_PACKET(P, 3, this.min + ";" + this.id);
            SocketManager.GAME_SEND_GDF_PACKET_TO_MAP(P.getCurMap(), cell);
        }
    }

    public void startAction(Player P, InteractiveObject IO, GameAction GA, GameCase cell) {
        this.player = P;
        P.setAway(true);
        IO.setState(JobConstant.IOBJECT_STATE_EMPTYING);//FIXME trouver la bonne valeur
        P.setExchangeAction(new ExchangeAction<>(ExchangeAction.CRAFTING, this));
        SocketManager.GAME_SEND_ECK_PACKET(P, 3, this.min + ";" + this.id);//this.min => Nbr de Case de l'interface
        SocketManager.GAME_SEND_GDF_PACKET_TO_MAP(P.getCurMap(), cell);
    }

    public void endAction(Player player, InteractiveObject IO, GameAction GA, GameCase cell) {
        if(!this.isCraft && player.getGameClient().action != 0) {
            if(System.currentTimeMillis() - player.getGameClient().action < this.time - 500) {
                player.getGameClient().kick();//FIXME: Ajout� le ban si aucune plainte.
                return;
            }
        }

        player.setDoAction(false);
        if (IO == null)
            return;
        if (!this.isCraft) {
            IO.setState(3);
            IO.desactive();
            SocketManager.GAME_SEND_GDF_PACKET_TO_MAP(player.getCurMap(), cell);
            int qua = (this.max > this.min ? Formulas.getRandomValue(this.min, this.max) : this.min);

            if (SM.getTemplate().getId() == 36) {
                if (qua > 0)
                    SM.addXp(player, (long) (this.getXpWin() * Config.getInstance().rateJob * World.world.getConquestBonus(player)));
            } else
                SM.addXp(player, (long) (this.getXpWin() * Config.getInstance().rateJob  * World.world.getConquestBonus(player)));

            int tID = JobConstant.getObjectByJobSkill(this.id);

            if (SM.getTemplate().getId() == 36 && qua > 0) {
                if (Formulas.getRandomValue(1, 1000) <= 2) {
                    int _tID = JobConstant.getPoissonRare(tID);
                    if (_tID != -1) {
                        ObjectTemplate _T = World.world.getObjTemplate(_tID);
                        if (_T != null) {
                            GameObject _O = _T.createNewItem(qua, true);
                            if (player.addObjet(_O, true))
                                World.world.addGameObject(_O, true);
                        }
                    }
                }
            }


            ObjectTemplate T = World.world.getObjTemplate(tID);
            if (T == null)
                return;
            GameObject O = T.createNewItem(qua, true);

            if (player.addObjet(O, true))
                World.world.addGameObject(O, true);
            SocketManager.GAME_SEND_IQ_PACKET(player, player.getId(), qua);
            SocketManager.GAME_SEND_Ow_PACKET(player);

            if (player.getMetierBySkill(this.id).get_lvl() >= 30 && Formulas.getRandomValue(1, 40) > 39) {
                for (int[] protector : JobConstant.JOB_PROTECTORS) {
                    if (tID == protector[1]) {
                        int monsterLvl = JobConstant.getProtectorLvl(player.getLevel());
                        player.getCurMap().startFightVersusProtectors(player, new Monster.MobGroup(player.getCurMap().nextObjectId, cell.getId(), protector[0] + "," + monsterLvl + "," + monsterLvl));
                        break;
                    }
                }
            }
        }
        player.setAway(false);
    }

    public synchronized void craft(boolean isRepeat, int repeat) {
        if (!this.isCraft) return;

        if (this.id == 1 || this.id == 113 || this.id == 115 || this.id == 116 || this.id == 117 || this.id == 118 || this.id == 119 || this.id == 120 || (this.id >= 163 && this.id <= 169)) {
            this.craftMaging(isRepeat, repeat);
            return;
        }

        this.ingredients.putAll(this.lastCraft);

        Map<Integer, Integer> items = new HashMap<>();
        //on retire les items mis en ingr�dients
        for (Entry<Integer, Integer> e : this.ingredients.entrySet()) {
            if (!this.player.hasItemGuid(e.getKey())) {
                SocketManager.GAME_SEND_Ec_PACKET(this.player, "EI");
                return;
            }

            GameObject obj = World.world.getGameObject(e.getKey());

            if (obj == null) {
                SocketManager.GAME_SEND_Ec_PACKET(this.player, "EI");
                return;
            }
            if (obj.getQuantity() < e.getValue()) {
                SocketManager.GAME_SEND_Ec_PACKET(this.player, "EI");
                return;
            }

            int newQua = obj.getQuantity() - e.getValue();
            if (newQua < 0) return;

            if (newQua == 0) {
                this.player.removeItem(e.getKey());
                World.world.removeGameObject(e.getKey());
                SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(this.player, e.getKey());
            } else {
                obj.setQuantity(newQua);
                SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(this.player, obj);
            }

            items.put(obj.getTemplate().getId(), e.getValue());
        }

        boolean signed = false;

        if (items.containsKey(7508)) {
            signed = true;
            items.remove(7508);
        }

        SocketManager.GAME_SEND_Ow_PACKET(this.player);

        boolean isUnjobSkill = this.getJobStat() == null;

        if (!isUnjobSkill) {
            JobStat SM = this.player.getMetierBySkill(this.id);
            int templateId = World.world.getObjectByIngredientForJob(SM.getTemplate().getListBySkill(this.id), items);
            //Recette non existante ou pas adapt� au m�tier
            if (templateId == -1 || !SM.getTemplate().canCraft(this.id, templateId)) {
                SocketManager.GAME_SEND_Ec_PACKET(this.player, "EI");
                SocketManager.GAME_SEND_IO_PACKET_TO_MAP(this.player.getCurMap(), this.player.getId(), "-");
                this.ingredients.clear();
                return;
            }

            int chan = JobConstant.getChanceByNbrCaseByLvl(SM.get_lvl(), this.ingredients.size());
            int jet = Formulas.getRandomValue(1, 100);
            boolean success = chan >= jet;

            switch (this.id) {
                case 109:
                    success = true;
                    break;
            }

            if (Logging.USE_LOG)
                Logging.getInstance().write("Craft", this.player.getName() + " � crafter avec " + (success ? "SUCCES" : "ECHEC") + " l'item " + templateId + " (" + World.world.getObjTemplate(templateId).getName() + ")");
            if (!success) {
                SocketManager.GAME_SEND_Ec_PACKET(this.player, "EF");
                SocketManager.GAME_SEND_IO_PACKET_TO_MAP(this.player.getCurMap(), this.player.getId(), "-" + templateId);
                SocketManager.GAME_SEND_Im_PACKET(this.player, "0118");
            } else {
                GameObject newObj = World.world.getObjTemplate(templateId).createNewItemWithoutDuplication(this.player.getItems().values(), 1, true);
                int guid = newObj.getGuid();//FIXME: Ne pas recr�e un item pour l'empiler aprÃ¨s

                if(guid == -1) { // Don't exist
                    guid = newObj.setId();
                    this.player.getItems().put(guid, newObj);
                    SocketManager.GAME_SEND_OAKO_PACKET(this.player, newObj);
                    World.world.addGameObject(newObj, true);
                } else {
                    newObj.setQuantity(newObj.getQuantity() + 1);
                    SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(this.player, newObj);
                }
                SocketManager.GAME_SEND_Ow_PACKET(this.player);

                if (signed || templateId >= 15000) newObj.addTxtStat(988, this.player.getName());


                SocketManager.GAME_SEND_Em_PACKET(this.player, "KO+" + guid + "|1|" + templateId + "|" + newObj.parseStatsString().replace(";", "#"));
                SocketManager.GAME_SEND_Ec_PACKET(this.player, "K;" + templateId);
                SocketManager.GAME_SEND_IO_PACKET_TO_MAP(this.player.getCurMap(), this.player.getId(), "+" + templateId);
            }

            int winXP = 0;
            if (success)
                winXP = Formulas.calculXpWinCraft(SM.get_lvl(), this.ingredients.size()) * Config.getInstance().rateJob;
            else if (!SM.getTemplate().isMaging())
                winXP = Formulas.calculXpWinCraft(SM.get_lvl(), this.ingredients.size()) * Config.getInstance().rateJob;

            if (winXP > 0) {
                SM.addXp(this.player, winXP);
                ArrayList<JobStat> SMs = new ArrayList<>();
                SMs.add(SM);
                SocketManager.GAME_SEND_JX_PACKET(this.player, SMs);
            }
        } else {
            int templateId = World.world.getObjectByIngredientForJob(World.world.getMetier(this.id).getListBySkill(this.id), items);

            if (templateId == -1 || !World.world.getMetier(this.id).canCraft(this.id, templateId)) {
                SocketManager.GAME_SEND_Ec_PACKET(this.player, "EI");
                SocketManager.GAME_SEND_IO_PACKET_TO_MAP(this.player.getCurMap(), this.player.getId(), "-");
                this.ingredients.clear();
                return;
            }

            GameObject newObj = World.world.getObjTemplate(templateId).createNewItemWithoutDuplication(this.player.getItems().values(), 1, true);
            int guid = newObj.getGuid();//FIXME: Ne pas recr�e un item pour l'empiler aprÃ¨s

            if(guid == -1) { // Don't exist
                guid = newObj.setId();
                this.player.getItems().put(guid, newObj);
                SocketManager.GAME_SEND_OAKO_PACKET(this.player, newObj);
                World.world.addGameObject(newObj, true);
            } else {
                newObj.setQuantity(newObj.getQuantity() + 1);
                SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(this.player, newObj);
            }

            if (signed || templateId >= 15000) newObj.addTxtStat(988, this.player.getName());

            SocketManager.GAME_SEND_Ow_PACKET(this.player);
            SocketManager.GAME_SEND_Em_PACKET(this.player, "KO+" + guid + "|1|" + templateId + "|" + newObj.parseStatsString().replace(";", "#"));
            SocketManager.GAME_SEND_Ec_PACKET(this.player, "K;" + templateId);
            SocketManager.GAME_SEND_IO_PACKET_TO_MAP(this.player.getCurMap(), this.player.getId(), "+" + templateId);
        }
        this.lastCraft.clear();
        this.lastCraft.putAll(this.ingredients);
        this.ingredients.clear();

        if(!isRepeat) {
            this.oldJobCraft = this.jobCraft;
            this.jobCraft = null;
        }
    }

    private int addCraftObject(Player player, GameObject newObj) {
        for (Entry<Integer, GameObject> entry : player.getItems().entrySet()) {
            GameObject obj = entry.getValue();
            if (obj.getTemplate().getId() == newObj.getTemplate().getId() && obj.getTxtStat().equals(newObj.getTxtStat())
                    && obj.getStats().isSameStats(newObj.getStats()) && obj.getPosition() == Constant.ITEM_POS_NO_EQUIPED) {
                obj.setQuantity(obj.getQuantity() + newObj.getQuantity());//On ajoute QUA item a la quantit� de l'objet existant
                SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(player, obj);
                return obj.getGuid();
            }
        }

        player.getItems().put(newObj.getGuid(), newObj);
        SocketManager.GAME_SEND_OAKO_PACKET(player, newObj);
        World.world.addGameObject(newObj, true);
        return -1;
    }

    public boolean craftPublicMode(Player crafter, Player receiver, Map<Player, ArrayList<Couple<Integer, Integer>>> list) {
        if (!this.isCraft) return false;

        this.player = crafter;
        boolean signed = false;

        Map<Integer, Integer> items = new HashMap<>();

        for (Entry<Player, ArrayList<Couple<Integer, Integer>>> entry : list.entrySet()) {
            Player player = entry.getKey();
            Map<Integer, Integer> playerItems = new HashMap<>();

            for (Couple<Integer, Integer> couple : entry.getValue())
                playerItems.put(couple.first, couple.second);

            for (Entry<Integer, Integer> e : playerItems.entrySet()) {
                if (!player.hasItemGuid(e.getKey())) {
                    SocketManager.GAME_SEND_Ec_PACKET(player, "EI");
                    SocketManager.GAME_SEND_Ec_PACKET(this.player, "EI");
                    return false;
                }

                GameObject gameObject = World.world.getGameObject(e.getKey());
                if (gameObject == null) {
                    SocketManager.GAME_SEND_Ec_PACKET(player, "EI");
                    SocketManager.GAME_SEND_Ec_PACKET(this.player, "EI");
                    return false;
                }
                if (gameObject.getQuantity() < e.getValue()) {
                    SocketManager.GAME_SEND_Ec_PACKET(player, "EI");
                    SocketManager.GAME_SEND_Ec_PACKET(this.player, "EI");
                    return false;
                }

                int newQua = gameObject.getQuantity() - e.getValue();

                if (newQua < 0)
                    return false;

                if (newQua == 0) {
                    player.removeItem(e.getKey());
                    World.world.removeGameObject(e.getKey());
                    SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(player, e.getKey());
                } else {
                    gameObject.setQuantity(newQua);
                    SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(player, gameObject);
                }

                items.put(gameObject.getTemplate().getId(), e.getValue());
            }
        }

        SocketManager.GAME_SEND_Ow_PACKET(this.player);
        JobStat SM = this.player.getMetierBySkill(this.id);

        //Rune de signature
        if (items.containsKey(7508))
            if (SM.get_lvl() == 100)
                signed = true;
        items.remove(7508);

        int template = World.world.getObjectByIngredientForJob(SM.getTemplate().getListBySkill(this.id), items);

        if (template == -1 || !SM.getTemplate().canCraft(this.id, template)) {
            SocketManager.GAME_SEND_Ec_PACKET(this.player, "EI");
            receiver.send("EcEI");
            SocketManager.GAME_SEND_IO_PACKET_TO_MAP(this.player.getCurMap(), this.player.getId(), "-");
            items.clear();
            return false;
        }

        boolean success = JobConstant.getChanceByNbrCaseByLvl(SM.get_lvl(), items.size()) >= Formulas.getRandomValue(1, 100);

        if (Logging.USE_LOG)
            Logging.getInstance().write("SecureCraft", this.player.getName() + " � crafter avec " + (success ? "SUCCES" : "ECHEC") + " l'item " + template + " (" + World.world.getObjTemplate(template).getName() + ") pour " + receiver.getName());
        if (!success) {
            SocketManager.GAME_SEND_Ec_PACKET(this.player, "EF");
            SocketManager.GAME_SEND_Ec_PACKET(receiver, "EF");
            SocketManager.GAME_SEND_IO_PACKET_TO_MAP(this.player.getCurMap(), this.player.getId(), "-" + template);
            SocketManager.GAME_SEND_Im_PACKET(this.player, "0118");
        } else {
            GameObject newObj = World.world.getObjTemplate(template).createNewItem(1, true);
            if (signed) newObj.addTxtStat(988, this.player.getName());
            int guid = this.addCraftObject(receiver, newObj);
            if(guid == -1) guid = newObj.getGuid();
            String stats = newObj.parseStatsString();

            this.player.send("EcK;" + template + ";T" + receiver.getName() + ";" + stats);
            receiver.send("EcK;" + template + ";B" + crafter.getName() + ";" + stats);

            SocketManager.GAME_SEND_Ow_PACKET(this.player);
            SocketManager.GAME_SEND_IO_PACKET_TO_MAP(this.player.getCurMap(), this.player.getId(), "+" + template);
        }

        int winXP = Formulas.calculXpWinCraft(SM.get_lvl(), this.ingredients.size()) * Config.getInstance().rateJob;
        if (SM.getTemplate().getId() == 28 && winXP == 1)
            winXP = 10;
        if (success) {
            SM.addXp(this.player, winXP);
            ArrayList<JobStat> SMs = new ArrayList<>();
            SMs.add(SM);
            SocketManager.GAME_SEND_JX_PACKET(this.player, SMs);
        }

        this.ingredients.clear();
        return success;
    }

    public void addIngredient(Player player, int id, int quantity) {
        int oldQuantity = this.ingredients.get(id) == null ? 0 : this.ingredients.get(id);
        if(quantity < 0) if(- quantity > oldQuantity) return;

        this.ingredients.remove(id);
        oldQuantity += quantity;

        if (oldQuantity > 0) {
            this.ingredients.put(id, oldQuantity);
            SocketManager.GAME_SEND_EXCHANGE_MOVE_OK(player, 'O', "+", id + "|" + oldQuantity);
        } else {
            SocketManager.GAME_SEND_EXCHANGE_MOVE_OK(player, 'O', "-", id + "");
        }
    }

    public byte sizeList(Map<Player, ArrayList<Couple<Integer, Integer>>> list) {
        byte size = 0;

        for (ArrayList<Couple<Integer, Integer>> entry : list.values()) {
            for (Couple<Integer, Integer> couple : entry) {
                GameObject object = World.world.getGameObject(couple.first);
                if (object != null) {
                    ObjectTemplate objectTemplate = object.getTemplate();
                    if (objectTemplate != null && objectTemplate.getId() != 7508) size++;
                }
            }
        }
        return size;
    }

    public void putLastCraftIngredients() {
        if (this.player == null || this.lastCraft == null || !this.ingredients.isEmpty()) return;

        this.ingredients.clear();
        this.ingredients.putAll(this.lastCraft);
        this.ingredients.entrySet().stream().filter(e -> World.world.getGameObject(e.getKey()) != null)
                .filter(e -> !(World.world.getGameObject(e.getKey()).getQuantity() < e.getValue()))
                .forEach(e -> SocketManager.GAME_SEND_EXCHANGE_MOVE_OK(this.player, 'O', "+", e.getKey() + "|" + e.getValue()));
    }

    public void resetCraft() {
        this.ingredients.clear();
        this.lastCraft.clear();
        this.oldJobCraft = null;
        this.jobCraft = null;
    }

    //FM � chi�

    private int reConfigingRunes = -1;

    public void modifIngredient(Player P, int guid, int qua) {
        //on prend l'ancienne valeur
        int q = this.ingredients.get(guid) == null ? 0 : this.ingredients.get(guid);
        if(qua < 0) if(-qua > q) return;
        //on enleve l'entr�e dans la Map
        this.ingredients.remove(guid);
        //on ajoute (ou retire, en fct du signe) X objet
        q += qua;
        if (q > 0) {
            this.ingredients.put(guid, q);
            SocketManager.GAME_SEND_EXCHANGE_MOVE_OK(P, 'O', "+", guid + "|"
                    + q);
        } else
            SocketManager.GAME_SEND_EXCHANGE_MOVE_OK(P, 'O', "-", guid + "");
    }

    private synchronized void craftMaging(boolean isReapeat, int repeat) {
    	boolean isSigningRune = false;
        GameObject objectFm = null, signingRune = null, runeOrPotion = null;
        int lvlElementRune = 0, statsID = -1, lvlQuaStatsRune = 0, statsAdd = 0, deleteID = -1, poid = 0, idRune = 0;
        boolean bonusRune = false;
        String statsObjectFm = "-1";
        for (int idIngredient : this.ingredients.keySet()) {
        	GameObject ing = World.world.getGameObject(idIngredient);
            if (ing == null || !this.player.hasItemGuid(idIngredient)) {
                SocketManager.GAME_SEND_Ec_PACKET(this.player, "EI");
                SocketManager.GAME_SEND_IO_PACKET_TO_MAP(this.player.getCurMap(), this.player.getId(), "-");
                this.ingredients.clear();
                return;
            }
            int templateID = ing.getTemplate().getId();
            if (ing.getTemplate().getType() == 78)
                idRune = idIngredient;
            switch (templateID) {
                case 1333:
                    statsID = 99;
                    lvlElementRune = ing.getTemplate().getLevel();
                    runeOrPotion = ing;
                    break;
                case 1335:
                    statsID = 96;
                    lvlElementRune = ing.getTemplate().getLevel();
                    runeOrPotion = ing;
                    break;
                case 1337:
                    statsID = 98;
                    lvlElementRune = ing.getTemplate().getLevel();
                    runeOrPotion = ing;
                    break;
                case 1338:
                    statsID = 97;
                    lvlElementRune = ing.getTemplate().getLevel();
                    runeOrPotion = ing;
                    break;
                case 1340:
                    statsID = 97;
                    lvlElementRune = ing.getTemplate().getLevel();
                    runeOrPotion = ing;
                    break;
                case 1341:
                    statsID = 96;
                    lvlElementRune = ing.getTemplate().getLevel();
                    runeOrPotion = ing;
                    break;
                case 1342:
                    statsID = 98;
                    lvlElementRune = ing.getTemplate().getLevel();
                    runeOrPotion = ing;
                    break;
                case 1343:
                    statsID = 99;
                    lvlElementRune = ing.getTemplate().getLevel();
                    runeOrPotion = ing;
                    break;
                case 1345:
                    statsID = 99;
                    lvlElementRune = ing.getTemplate().getLevel();
                    runeOrPotion = ing;
                    break;
                case 1346:
                    statsID = 96;
                    lvlElementRune = ing.getTemplate().getLevel();
                    runeOrPotion = ing;
                    break;
                case 1347:
                    statsID = 98;
                    lvlElementRune = ing.getTemplate().getLevel();
                    runeOrPotion = ing;
                    break;
                case 1348:
                    statsID = 97;
                    lvlElementRune = ing.getTemplate().getLevel();
                    runeOrPotion = ing;
                    break;
                case 1519:
                    runeOrPotion = ing;
                    statsObjectFm = "76";
                    statsAdd = 1;
                    poid = 1;
                    lvlQuaStatsRune = ing.getTemplate().getLevel();
                    break;
                case 1521:
                    runeOrPotion = ing;
                    statsObjectFm = "7c";
                    statsAdd = 1;
                    poid = 6;
                    lvlQuaStatsRune = ing.getTemplate().getLevel();
                    break;
                case 1522:
                    runeOrPotion = ing;
                    statsObjectFm = "7e";
                    statsAdd = 1;
                    poid = 1;
                    lvlQuaStatsRune = ing.getTemplate().getLevel();
                    break;
                case 1523:
                    runeOrPotion = ing;
                    statsObjectFm = "7d";
                    statsAdd = 3;
                    poid = 1;
                    lvlQuaStatsRune = ing.getTemplate().getLevel();
                    break;
                case 1524:
                    runeOrPotion = ing;
                    statsObjectFm = "77";
                    statsAdd = 1;
                    poid = 1;
                    lvlQuaStatsRune = ing.getTemplate().getLevel();
                    break;
                case 1525:
                    runeOrPotion = ing;
                    statsObjectFm = "7b";
                    statsAdd = 1;
                    poid = 1;
                    lvlQuaStatsRune = ing.getTemplate().getLevel();
                    break;
                case 1545:
                    runeOrPotion = ing;
                    statsObjectFm = "76";
                    statsAdd = 3;
                    poid = 3;
                    lvlQuaStatsRune = ing.getTemplate().getLevel();
                    break;
                case 1546:
                    runeOrPotion = ing;
                    statsObjectFm = "7c";
                    statsAdd = 3;
                    poid = 18;
                    lvlQuaStatsRune = ing.getTemplate().getLevel();
                    break;
                case 1547:
                    runeOrPotion = ing;
                    statsObjectFm = "7e";
                    statsAdd = 3;
                    poid = 3;
                    lvlQuaStatsRune = ing.getTemplate().getLevel();
                    break;
                case 1548:
                    runeOrPotion = ing;
                    statsObjectFm = "7d";
                    statsAdd = 10;
                    poid = 10;
                    lvlQuaStatsRune = ing.getTemplate().getLevel();
                    break;
                case 1549:
                    runeOrPotion = ing;
                    statsObjectFm = "77";
                    statsAdd = 3;
                    poid = 3;
                    lvlQuaStatsRune = ing.getTemplate().getLevel();
                    break;
                case 1550:
                    runeOrPotion = ing;
                    statsObjectFm = "7b";
                    statsAdd = 3;
                    poid = 10;
                    lvlQuaStatsRune = ing.getTemplate().getLevel();
                    break;
                case 1551:
                    runeOrPotion = ing;
                    statsObjectFm = "76";
                    statsAdd = 10;
                    poid = 10;
                    lvlQuaStatsRune = ing.getTemplate().getLevel();
                    break;
                case 1552:
                    runeOrPotion = ing;
                    statsObjectFm = "7c";
                    statsAdd = 10;
                    poid = 50;
                    lvlQuaStatsRune = ing.getTemplate().getLevel();
                    break;
                case 1553:
                    runeOrPotion = ing;
                    statsObjectFm = "7e";
                    statsAdd = 10;
                    poid = 10;
                    lvlQuaStatsRune = ing.getTemplate().getLevel();
                    break;
                case 1554:
                    runeOrPotion = ing;
                    statsObjectFm = "7b";
                    statsAdd = 30;
                    poid = 10;
                    lvlQuaStatsRune = ing.getTemplate().getLevel();
                    break;
                case 1555:
                    runeOrPotion = ing;
                    statsObjectFm = "77";
                    statsAdd = 10;
                    poid = 10;
                    lvlQuaStatsRune = ing.getTemplate().getLevel();
                    break;
                case 1556:
                    runeOrPotion = ing;
                    statsObjectFm = "7d";
                    statsAdd = 10;
                    poid = 10;
                    lvlQuaStatsRune = ing.getTemplate().getLevel();
                    break;
                case 1557:
                    runeOrPotion = ing;
                    statsObjectFm = "6f";
                    statsAdd = 1;
                    poid = 100;
                    lvlQuaStatsRune = ing.getTemplate().getLevel();
                    break;
                case 1558:
                    runeOrPotion = ing;
                    statsObjectFm = "70";
                    statsAdd = 1;
                    poid = 90;
                    lvlQuaStatsRune = ing.getTemplate().getLevel();
                    break;
                case 7433:
                    runeOrPotion = ing;
                    statsObjectFm = "73";
                    statsAdd = 1;
                    poid = 30;
                    lvlQuaStatsRune = ing.getTemplate().getLevel();
                    break;
                case 7434:
                    runeOrPotion = ing;
                    statsObjectFm = "b2";
                    statsAdd = 1;
                    poid = 20;
                    lvlQuaStatsRune = ing.getTemplate().getLevel();
                    break;
                case 7435:
					runeOrPotion = ing;
					statsObjectFm = "80";
					statsAdd = 1;
					poid = 20;
					lvlQuaStatsRune = ing.getTemplate().getLevel();
                    break;
                case 7436:
                    runeOrPotion = ing;
                    statsObjectFm = "8a";
                    statsAdd = 1;
                    poid = 2;
                    lvlQuaStatsRune = ing.getTemplate().getLevel();
                    break;
                case 7437:
                    runeOrPotion = ing;
                    statsObjectFm = "dc";
                    statsAdd = 1;
                    poid = 2;
                    lvlQuaStatsRune = ing.getTemplate().getLevel();
                    break;
                case 7438:
                    runeOrPotion = ing;
                    statsObjectFm = "75";
                    statsAdd = 1;
                    poid = 50;
                    lvlQuaStatsRune = ing.getTemplate().getLevel();
                    break;
                case 7442:
                    runeOrPotion = ing;
                    statsObjectFm = "b6";
                    statsAdd = 1;
                    poid = 30;
                    lvlQuaStatsRune = ing.getTemplate().getLevel();
                    break;
                case 7443:
                    runeOrPotion = ing;
                    statsObjectFm = "9e";
                    statsAdd = 10;
                    poid = 1;
                    lvlQuaStatsRune = ing.getTemplate().getLevel();
                    break;
                case 7444:
                    runeOrPotion = ing;
                    statsObjectFm = "9e";
                    statsAdd = 30;
                    poid = 1;
                    lvlQuaStatsRune = ing.getTemplate().getLevel();
                    break;
                case 7445:
                    runeOrPotion = ing;
                    statsObjectFm = "9e";
                    statsAdd = 100;
                    poid = 1;
                    lvlQuaStatsRune = ing.getTemplate().getLevel();
                    break;
                case 7446:
                    runeOrPotion = ing;
                    statsObjectFm = "e1";
                    statsAdd = 1;
                    poid = 15;
                    lvlQuaStatsRune = ing.getTemplate().getLevel();
                    break;
                case 7447:
                    runeOrPotion = ing;
                    statsObjectFm = "e2";
                    statsAdd = 1;
                    poid = 2;
                    lvlQuaStatsRune = ing.getTemplate().getLevel();
                    break;
                case 7448:
                    runeOrPotion = ing;
                    statsObjectFm = "ae";
                    statsAdd = 10;
                    poid = 1;
                    lvlQuaStatsRune = ing.getTemplate().getLevel();
                    break;
                case 7449:
                    runeOrPotion = ing;
                    statsObjectFm = "ae";
                    statsAdd = 30;
                    poid = 3;
                    lvlQuaStatsRune = ing.getTemplate().getLevel();
                    break;
                case 7450:
                    runeOrPotion = ing;
                    statsObjectFm = "ae";
                    statsAdd = 100;
                    poid = 10;
                    lvlQuaStatsRune = ing.getTemplate().getLevel();
                    break;
                case 7451:
                    runeOrPotion = ing;
                    statsObjectFm = "b0";
                    statsAdd = 1;
                    poid = 5;
                    lvlQuaStatsRune = ing.getTemplate().getLevel();
                    break;
                case 7452:
                    runeOrPotion = ing;
                    statsObjectFm = "f3";
                    statsAdd = 1;
                    poid = 4;
                    lvlQuaStatsRune = ing.getTemplate().getLevel();
                    break;
                case 7453:
                    runeOrPotion = ing;
                    statsObjectFm = "f2";
                    statsAdd = 1;
                    poid = 4;
                    lvlQuaStatsRune = ing.getTemplate().getLevel();
                    break;
                case 7454:
                    runeOrPotion = ing;
                    statsObjectFm = "f1";
                    statsAdd = 1;
                    poid = 4;
                    lvlQuaStatsRune = ing.getTemplate().getLevel();
                    break;
                case 7455:
                    runeOrPotion = ing;
                    statsObjectFm = "f0";
                    statsAdd = 1;
                    poid = 4;
                    lvlQuaStatsRune = ing.getTemplate().getLevel();
                    break;
                case 7456:
                    runeOrPotion = ing;
                    statsObjectFm = "f4";
                    statsAdd = 1;
                    poid = 4;
                    lvlQuaStatsRune = ing.getTemplate().getLevel();
                    break;
                case 7457:
                    runeOrPotion = ing;
                    statsObjectFm = "d5";
                    statsAdd = 1;
                    poid = 5;
                    lvlQuaStatsRune = ing.getTemplate().getLevel();
                    break;
                case 7458:
                    runeOrPotion = ing;
                    statsObjectFm = "d4";
                    statsAdd = 1;
                    poid = 5;
                    lvlQuaStatsRune = ing.getTemplate().getLevel();
                    break;
                case 7459:
                    runeOrPotion = ing;
                    statsObjectFm = "d2";
                    statsAdd = 1;
                    poid = 5;
                    lvlQuaStatsRune = ing.getTemplate().getLevel();
                    break;
                case 7460:
                    runeOrPotion = ing;
                    statsObjectFm = "d3";
                    statsAdd = 1;
                    poid = 5;
                    lvlQuaStatsRune = ing.getTemplate().getLevel();
                    break;
                case 7560:
                    runeOrPotion = ing;
                    statsObjectFm = "d6";
                    statsAdd = 1;
                    poid = 5;
                    lvlQuaStatsRune = ing.getTemplate().getLevel();
                    break;
                case 8379:
                    runeOrPotion = ing;
                    statsObjectFm = "7d";
                    statsAdd = 10;
                    poid = 10;
                    lvlQuaStatsRune = ing.getTemplate().getLevel();
                    break;
                case 10662:
                    runeOrPotion = ing;
                    statsObjectFm = "b0";
                    statsAdd = 3;
                    poid = 15;
                    lvlQuaStatsRune = ing.getTemplate().getLevel();
                    break;
                case 7508:
                    isSigningRune = true;
                    signingRune = ing;
                    break;
                case 11118:
                    bonusRune = true;
                    runeOrPotion = ing;
                    statsObjectFm = "76";
                    statsAdd = 15;
                    poid = 1;
                    lvlQuaStatsRune = ing.getTemplate().getLevel();
                    break;
                case 11119:
                    bonusRune = true;
                    runeOrPotion = ing;
                    statsObjectFm = "7c";
                    statsAdd = 15;
                    poid = 1;
                    lvlQuaStatsRune = ing.getTemplate().getLevel();
                    break;
                case 11120:
                    bonusRune = true;
                    runeOrPotion = ing;
                    statsObjectFm = "7e";
                    statsAdd = 15;
                    poid = 1;
                    lvlQuaStatsRune = ing.getTemplate().getLevel();
                    break;
                case 11121:
                    bonusRune = true;
                    runeOrPotion = ing;
                    statsObjectFm = "7d";
                    statsAdd = 45;
                    poid = 1;
                    lvlQuaStatsRune = ing.getTemplate().getLevel();
                    break;
                case 11122:
                    bonusRune = true;
                    runeOrPotion = ing;
                    statsObjectFm = "77";
                    statsAdd = 15;
                    poid = 1;
                    lvlQuaStatsRune = ing.getTemplate().getLevel();
                    break;
                case 11123:
                    bonusRune = true;
                    runeOrPotion = ing;
                    statsObjectFm = "7b";
                    statsAdd = 15;
                    poid = 1;
                    lvlQuaStatsRune = ing.getTemplate().getLevel();
                    break;
                case 11124:
                    bonusRune = true;
                    runeOrPotion = ing;
                    statsObjectFm = "b0";
                    statsAdd = 10;
                    poid = 1;
                    lvlQuaStatsRune = ing.getTemplate().getLevel();
                    break;
                case 11125:
                    bonusRune = true;
                    runeOrPotion = ing;
                    statsObjectFm = "73";
                    statsAdd = 3;
                    poid = 1;
                    lvlQuaStatsRune = ing.getTemplate().getLevel();
                    break;
                case 11126:
                    bonusRune = true;
                    runeOrPotion = ing;
                    statsObjectFm = "b2";
                    statsAdd = 5;
                    poid = 1;
                    lvlQuaStatsRune = ing.getTemplate().getLevel();
                    break;
                case 11127:
                    bonusRune = true;
                    runeOrPotion = ing;
                    statsObjectFm = "70";
                    statsAdd = 5;
                    poid = 1;
                    lvlQuaStatsRune = ing.getTemplate().getLevel();
                    break;
                case 11128:
                    bonusRune = true;
                    runeOrPotion = ing;
                    statsObjectFm = "8a";
                    statsAdd = 10;
                    poid = 1;
                    lvlQuaStatsRune = ing.getTemplate().getLevel();
                    break;
                case 11129:
                    bonusRune = true;
                    runeOrPotion = ing;
                    statsObjectFm = "dc";
                    statsAdd = 5;
                    poid = 1;
                    lvlQuaStatsRune = ing.getTemplate().getLevel();
                    break;
                default:
                    int type = ing.getTemplate().getType();
                    if ((type >= 1 && type <= 11) || (type >= 16 && type <= 22)
                            || type == 81 || type == 102 || type == 114
                            || ing.getTemplate().getPACost() > 0) {
                        objectFm = ing;
                        SocketManager.GAME_SEND_EXCHANGE_OTHER_MOVE_OK_FM(this.player.getGameClient(), 'O', "+", objectFm.getGuid()
                                + "|" + 1);
                        deleteID = idIngredient;
                        GameObject newObj = GameObject.getCloneObjet(objectFm, 1); // Cr�ation d'un clone avec un nouveau identifiant
                        if (objectFm.getQuantity() > 1) { // S'il y avait plus d'un objet
                            int newQuant = objectFm.getQuantity() - 1; // On supprime celui que l'on a ajout�
                            objectFm.setQuantity(newQuant);
                            SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(this.player, objectFm);
                        } else {
                            World.world.removeGameObject(idIngredient);
                            this.player.removeItem(idIngredient);
                            SocketManager.GAME_SEND_DELETE_STATS_ITEM_FM(this.player, idIngredient);
                        }
                        objectFm = newObj; // Tout neuf avec un nouveau identifiant
                        break;
                    }
            }
        }
        double poid2 = World.getPwrPerEffet(Integer.parseInt(statsObjectFm, 16));
        if (poid2 > 0.0)
            poid = statsAdd * ((int) poid2);

        if (SM == null || objectFm == null || runeOrPotion == null) {
            if (objectFm != null) {
                World.world.addGameObject(objectFm, true);
                this.player.addObjet(objectFm);
            }
            SocketManager.GAME_SEND_Ec_PACKET(this.player, "EI");
            SocketManager.GAME_SEND_IO_PACKET_TO_MAP(this.player.getCurMap(), this.player.getId(), "-");
            this.ingredients.clear();
            return;
        }
        if (deleteID != -1) {
            this.ingredients.remove(deleteID);
        }
        ObjectTemplate objTemplate = objectFm.getTemplate();
        int chance = 0;
        int lvlJob = SM.get_lvl();
        int currentWeightTotal = 1;
        int pwrPerte = 0;
        ArrayList<Integer> chances = new ArrayList<Integer>();
        int objTemplateID = objTemplate.getId();
        String statStringObj = objectFm.parseStatsString();
        if (lvlElementRune > 0 && lvlQuaStatsRune == 0) {
            chance = Formulas.calculChanceByElement(lvlJob, objTemplate.getLevel(), lvlElementRune);
            if (chance > 100 - (lvlJob / 20))
                chance = 100 - (lvlJob / 20);
            if (chance < (lvlJob / 20))
                chance = (lvlJob / 20);
            chances.add(0, chance);
            chances.add(1, 0);
            chances.add(2, 100 - chance);
        } else if (lvlQuaStatsRune > 0 && lvlElementRune == 0) {
            int currentWeightStats = 1;
            if (!statStringObj.isEmpty()) {
                currentWeightTotal = currentTotalWeigthBase(statStringObj, objectFm); // Poids total de l'objet : PWRg
                currentWeightStats = currentWeithStats(objectFm, statsObjectFm); // Poids � ajouter : PWRcarac
            }
            int currentTotalBase = WeithTotalBase(objTemplateID); // Poids maximum de l'objet : PWRmax
            int currentMinBase = WeithTotalBaseMin(objTemplateID);
            if (currentTotalBase < 0) {
                currentTotalBase = 0;
            }
            if (currentWeightStats < 0) {
                currentWeightStats = 0;
            }
            if (currentWeightTotal < 0) {
                currentWeightTotal = 0;
            }
            float coef = 1;
            int baseStats = Job.viewBaseStatsItem(objectFm, statsObjectFm);
            int currentStats = Job.viewActualStatsItem(objectFm, statsObjectFm);
            if (baseStats == 1 && currentStats == 1 || baseStats == 1
                    && currentStats == 0) {
                coef = 1.0f;
            } else if (baseStats == 2 && currentStats == 2) {
                coef = 0.50f;
            } else if (baseStats == 0 && currentStats == 0 || baseStats == 0
                    && currentStats == 1) {
                coef = 0.25f;
            }
            float x = 1;
            boolean canFM = true;
            int statMax = getStatBaseMaxs(objectFm.getTemplate(), statsObjectFm);
            int actuelJet = Job.getActualJet(objectFm, statsObjectFm);
            if (actuelJet > statMax) {
                x = 0.8F;
                int overPerEffect = (int) World.getOverPerEffet(Integer.parseInt(statsObjectFm, 16));
                if (statMax == 0)
                    if (actuelJet >= (statMax + overPerEffect + 1))
                        canFM = false;
                    else if (actuelJet >= (statMax + overPerEffect))
                        canFM = false;
            }
            if (lvlJob < (int) Math.floor(objTemplate.getLevel() / 2))
                canFM = false; // On rate le FM si le m�tier n'est pas suffidant

            int diff = (int) Math.abs((currentTotalBase * 1.3f)
                    - currentWeightTotal);
            if (canFM) {
                /*if (objectFm.getTemplate().getId() == 2469 && poid == 100) // Si c'est un gelano et que l'on essaie de remettre le PA
                {
                    chances.add(0, 49);
                    chances.add(1, 18);
                } else*/
                    chances = Formulas.chanceFM(currentTotalBase, currentMinBase, currentWeightTotal, currentWeightStats, poid, diff, coef, statMax, getStatBaseMins(objectFm.getTemplate(), statsObjectFm), currentStats(objectFm, statsObjectFm), x, bonusRune, statsAdd);
            } else
            // Si l'objet est au dessus de l'over (impossible statistiquement ... mais evite un gelano 2 PA :p)
            {
                chances.add(0, 0);
                chances.add(1, 0);
            }
        }
        int aleatoryChance = Formulas.getRandomValue(1, 100);
        int SC = chances.get(0);
        int SN = chances.get(1);
        boolean successC = (aleatoryChance <= SC);
        boolean successN = (aleatoryChance <= (SC + SN));

        if (successC || successN) {
            int winXP = Formulas.calculXpWinFm(objectFm.getTemplate().getLevel(), poid)
                    * Config.getInstance().rateJob;
            if (winXP > 0) {
                SM.addXp(this.player, winXP);
                ArrayList<JobStat> SMs = new ArrayList<JobStat>();
                SMs.add(SM);
                SocketManager.GAME_SEND_JX_PACKET(this.player, SMs);
            }
        }

        if (successC) // SC
        {
            int coef = 0;
            pwrPerte = 0;
            if (lvlElementRune == 1)
                coef = 50;
            else if (lvlElementRune == 25)
                coef = 65;
            else if (lvlElementRune == 50)
                coef = 85;
            if (isSigningRune) {
                objectFm.addTxtStat(985, this.player.getName());
            }
            if (lvlElementRune > 0 && lvlQuaStatsRune == 0) {
                for (SpellEffect effect : objectFm.getEffects()) {
                    if (effect.getEffectID() != 100)
                        continue;
                    String[] infos = effect.getArgs().split(";");
                    try {
                        int min = Integer.parseInt(infos[0], 16);
                        int max = Integer.parseInt(infos[1], 16);
                        int newMin = (int) ((min * coef) / 100);
                        int newMax = (int) ((max * coef) / 100);
                        if (newMin == 0)
                            newMin = 1;
                        String newRange = "1d" + (newMax - newMin + 1) + "+"
                                + (newMin - 1);
                        String newArgs = Integer.toHexString(newMin) + ";"
                                + Integer.toHexString(newMax) + ";-1;-1;0;"
                                + newRange;
                        effect.setArgs(newArgs);
                        effect.setEffectID(statsID);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else if (lvlQuaStatsRune > 0 && lvlElementRune == 0) {
                boolean negative = false;
                int currentStats = Job.viewActualStatsItem(objectFm, statsObjectFm);
                if (currentStats == 2) {
                    if (statsObjectFm.compareTo("7b") == 0) {
                        statsObjectFm = "98";
                        negative = true;
                    }
                    if (statsObjectFm.compareTo("77") == 0) {
                        statsObjectFm = "9a";
                        negative = true;
                    }
                    if (statsObjectFm.compareTo("7e") == 0) {
                        statsObjectFm = "9b";
                        negative = true;
                    }
                    if (statsObjectFm.compareTo("76") == 0) {
                        statsObjectFm = "9d";
                        negative = true;
                    }
                    if (statsObjectFm.compareTo("7c") == 0) {
                        statsObjectFm = "9c";
                        negative = true;
                    }
                    if (statsObjectFm.compareTo("7d") == 0) {
                        statsObjectFm = "99";
                        negative = true;
                    }
                }
                if (currentStats == 1 || currentStats == 2) {
                    if (statStringObj.isEmpty()) {
                        String statsStr = statsObjectFm + "#"
                                + Integer.toHexString(statsAdd) + "#0#0#0d0+"
                                + statsAdd;
                        objectFm.clearStats();
                        objectFm.parseStringToStats(statsStr, false);
                    } else {
                        String statsStr = objectFm.parseFMStatsString(statsObjectFm, objectFm, statsAdd, negative);
                        objectFm.clearStats();
                        objectFm.parseStringToStats(statsStr, false);
                    }
                } else {
                    if (statStringObj.isEmpty()) {
                        String statsStr = statsObjectFm + "#"
                                + Integer.toHexString(statsAdd) + "#0#0#0d0+"
                                + statsAdd;
                        objectFm.clearStats();
                        objectFm.parseStringToStats(statsStr, false);
                    } else {
                        String statsStr = objectFm.parseFMStatsString(statsObjectFm, objectFm, statsAdd, negative)
                                + ","
                                + statsObjectFm
                                + "#"
                                + Integer.toHexString(statsAdd)
                                + "#0#0#0d0+"
                                + statsAdd;
                        objectFm.clearStats();
                        objectFm.parseStringToStats(statsStr, false);
                    }
                }
            }
            if (signingRune != null) {
                int newQua = signingRune.getQuantity() - 1;
                if (newQua <= 0) {
                    this.player.removeItem(signingRune.getGuid());
                    World.world.removeGameObject(signingRune.getGuid());
                    SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(this.player, signingRune.getGuid());
                } else {
                    signingRune.setQuantity(newQua);
                    SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(this.player, signingRune);
                }
            }
            if (runeOrPotion != null) {
                int newQua = runeOrPotion.getQuantity() - 1;
                if (newQua <= 0) {
                    this.player.removeItem(runeOrPotion.getGuid());
                    World.world.removeGameObject(runeOrPotion.getGuid());
                    SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(this.player, runeOrPotion.getGuid());
                } else {
                    runeOrPotion.setQuantity(newQua);
                    SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(this.player, runeOrPotion);
                }
            }
            World.world.addGameObject(objectFm, true);
            this.player.addObjet(objectFm);
            SocketManager.GAME_SEND_Ow_PACKET(this.player);
            String data = objectFm.getGuid() + "|1|"
                    + objectFm.getTemplate().getId() + "|"
                    + objectFm.parseStatsString();
            if (!this.isRepeat)
                this.reConfigingRunes = -1;
            if (this.reConfigingRunes != 0 || this.broken)
                SocketManager.GAME_SEND_EXCHANGE_MOVE_OK_FM(this.player, 'O', "+", data);
            this.data = data;
            SocketManager.GAME_SEND_IO_PACKET_TO_MAP(this.player.getCurMap(), this.player.getId(), "+"
                    + objTemplateID);
            SocketManager.GAME_SEND_Ec_PACKET(this.player, "K;" + objTemplateID);
        } else if (successN) {
            pwrPerte = 0;
            if (isSigningRune) {
                objectFm.addTxtStat(985, this.player.getName());
            }

            boolean negative = false;
            int currentStats = Job.viewActualStatsItem(objectFm, statsObjectFm);
            if (currentStats == 2) {
                if (statsObjectFm.compareTo("7b") == 0) {
                    statsObjectFm = "98";
                    negative = true;
                }
                if (statsObjectFm.compareTo("77") == 0) {
                    statsObjectFm = "9a";
                    negative = true;
                }
                if (statsObjectFm.compareTo("7e") == 0) {
                    statsObjectFm = "9b";
                    negative = true;
                }
                if (statsObjectFm.compareTo("76") == 0) {
                    statsObjectFm = "9d";
                    negative = true;
                }
                if (statsObjectFm.compareTo("7c") == 0) {
                    statsObjectFm = "9c";
                    negative = true;
                }
                if (statsObjectFm.compareTo("7d") == 0) {
                    statsObjectFm = "99";
                    negative = true;
                }
            }
            if (currentStats == 1 || currentStats == 2) {
                if (statStringObj.isEmpty()) {
                    String statsStr = statsObjectFm + "#"
                            + Integer.toHexString(statsAdd) + "#0#0#0d0+"
                            + statsAdd;
                    objectFm.clearStats();
                    objectFm.parseStringToStats(statsStr, false);
                } else {
                    String statsStr = "";
                    if (objectFm.getPuit() <= 0) // EC en premier s'il n'y a pas de puits
                    {
                        statsStr = objectFm.parseStringStatsEC_FM(objectFm, poid, Integer.parseInt(statsObjectFm, 16));
                        objectFm.clearStats();
                        objectFm.parseStringToStats(statsStr, false);
                        pwrPerte = currentWeightTotal
                                - currentTotalWeigthBase(statsStr, objectFm);
                    }
                    statsStr = objectFm.parseFMStatsString(statsObjectFm, objectFm, statsAdd, negative);
                    objectFm.clearStats();
                    objectFm.parseStringToStats(statsStr, false);
                }
            } else {
                if (statStringObj.isEmpty()) {
                    String statsStr = statsObjectFm + "#"
                            + Integer.toHexString(statsAdd) + "#0#0#0d0+"
                            + statsAdd;
                    objectFm.clearStats();
                    objectFm.parseStringToStats(statsStr, false);
                } else {
                    String statsStr = "";
                    if (objectFm.getPuit() <= 0) // EC en premier s'il n'y a pas de puits
                    {
                        statsStr = objectFm.parseStringStatsEC_FM(objectFm, poid, Integer.parseInt(statsObjectFm, 16));
                        objectFm.clearStats();
                        objectFm.parseStringToStats(statsStr, true);
                        pwrPerte = currentWeightTotal
                                - currentTotalWeigthBase(statsStr, objectFm);
                    }
                    statsStr = objectFm.parseFMStatsString(statsObjectFm, objectFm, statsAdd, negative)
                            + ","
                            + statsObjectFm
                            + "#"
                            + Integer.toHexString(statsAdd)
                            + "#0#0#0d0+"
                            + statsAdd;
                    objectFm.clearStats();
                    objectFm.parseStringToStats(statsStr, false);
                }
            }

            if (signingRune != null) {
                int newQua = signingRune.getQuantity() - 1;
                if (newQua <= 0) {
                    this.player.removeItem(signingRune.getGuid());
                    World.world.removeGameObject(signingRune.getGuid());
                    SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(this.player, signingRune.getGuid());
                } else {
                    signingRune.setQuantity(newQua);
                    SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(this.player, signingRune);
                }
            }
            if (runeOrPotion != null) {
                int newQua = runeOrPotion.getQuantity() - 1;
                if (newQua <= 0) {
                    this.player.removeItem(runeOrPotion.getGuid());
                    World.world.removeGameObject(runeOrPotion.getGuid());
                    SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(this.player, runeOrPotion.getGuid());
                } else {
                    runeOrPotion.setQuantity(newQua);
                    SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(this.player, runeOrPotion);
                }
            }

            World.world.addGameObject(objectFm, true);
            this.player.addObjet(objectFm);
            SocketManager.GAME_SEND_Ow_PACKET(this.player);
            String data = objectFm.getGuid() + "|1|"
                    + objectFm.getTemplate().getId() + "|"
                    + objectFm.parseStatsString();
            if (!this.isRepeat)
                this.reConfigingRunes = -1;
            if (this.reConfigingRunes != 0 || this.broken)
                SocketManager.GAME_SEND_EXCHANGE_MOVE_OK_FM(this.player, 'O', "+", data);
            this.data = data;
            SocketManager.GAME_SEND_IO_PACKET_TO_MAP(this.player.getCurMap(), this.player.getId(), "+"
                    + objTemplateID);
            if (pwrPerte > 0) {
                SocketManager.GAME_SEND_Ec_PACKET(this.player, "EF");
                SocketManager.GAME_SEND_Im_PACKET(this.player, "0194");
            } else
                SocketManager.GAME_SEND_Ec_PACKET(this.player, "K;"
                        + objTemplateID);
        } else
        // EC
        {
            pwrPerte = 0;
            if (signingRune != null) {
                int newQua = signingRune.getQuantity() - 1;
                if (newQua <= 0) {
                    this.player.removeItem(signingRune.getGuid());
                    World.world.removeGameObject(signingRune.getGuid());
                    SocketManager.GAME_SEND_DELETE_STATS_ITEM_FM(this.player, signingRune.getGuid());
                } else {
                    signingRune.setQuantity(newQua);
                    SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(this.player, signingRune);
                }
            }
            if (runeOrPotion != null) {
                int newQua = runeOrPotion.getQuantity() - 1;
                if (newQua <= 0) {
                    this.player.removeItem(runeOrPotion.getGuid());
                    World.world.removeGameObject(runeOrPotion.getGuid());
                    SocketManager.GAME_SEND_DELETE_STATS_ITEM_FM(this.player, runeOrPotion.getGuid());
                } else {
                    runeOrPotion.setQuantity(newQua);
                    SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(this.player, runeOrPotion);
                }
            }
            String statsStr = "";
            if (!statStringObj.isEmpty()) {
                statsStr = objectFm.parseStringStatsEC_FM(objectFm, poid, -1);
                objectFm.clearStats();
                objectFm.parseStringToStats(statsStr, false);
                pwrPerte = currentWeightTotal
                        - currentTotalWeigthBase(statsStr, objectFm);
            }
            World.world.addGameObject(objectFm, true);
            this.player.addObjet(objectFm);
            SocketManager.GAME_SEND_Ow_PACKET(this.player);

            String data = objectFm.getGuid() + "|1|"
                    + objectFm.getTemplate().getId() + "|"
                    + objectFm.parseStatsString();
            if (!this.isRepeat)
                this.reConfigingRunes = -1;
            if (this.reConfigingRunes != 0 || this.broken)
                SocketManager.GAME_SEND_EXCHANGE_MOVE_OK_FM(this.player, 'O', "+", data);
            this.data = data;

            SocketManager.GAME_SEND_IO_PACKET_TO_MAP(this.player.getCurMap(), this.player.getId(), "-"
                    + objTemplateID);
            SocketManager.GAME_SEND_Ec_PACKET(this.player, "EF");
            if (pwrPerte > 0)
                SocketManager.GAME_SEND_Im_PACKET(this.player, "0117");
            else
                SocketManager.GAME_SEND_Im_PACKET(this.player, "0183");

        }
        objectFm.setPuit((objectFm.getPuit() + pwrPerte) - poid);
        this.lastCraft.clear();
        this.lastCraft.putAll(this.ingredients);
        this.lastCraft.put(objectFm.getGuid(), 1);
        int nbRunes = 0;
        if (!this.ingredients.isEmpty() && this.ingredients.get(idRune) != null) {
            if (isRepeat)
                nbRunes = this.ingredients.get(idRune) - repeat;
            else
                nbRunes = this.ingredients.get(idRune) - 1;
        }
        this.ingredients.clear();
        if (nbRunes > 0)
            this.modifIngredient(this.player, idRune, nbRunes); // Rajout des runes moins une
        ((JobAction) this.player.getExchangeAction().getValue()).modifIngredient(this.player, objectFm.getGuid(), 1); // On remet l'item dans la case de FM
    }


    public static int getStatBaseMaxs(ObjectTemplate objMod, String statsModif) {
        String[] split = objMod.getStrTemplate().split(",");
        for (String s : split) {
            String[] stats = s.split("#");
            if (stats[0].toLowerCase().compareTo(statsModif.toLowerCase()) > 0) {
            } else if (stats[0].toLowerCase().compareTo(statsModif.toLowerCase()) == 0) {
                int max = Integer.parseInt(stats[2], 16);
                if (max == 0)
                    max = Integer.parseInt(stats[1], 16);
                return max;
            }
        }
        return 0;
    }

    public static int getStatBaseMins(ObjectTemplate objMod, String statsModif) {
        String[] split = objMod.getStrTemplate().split(",");
        for (String s : split) {
            String[] stats = s.split("#");
            if (stats[0].toLowerCase().compareTo(statsModif.toLowerCase()) > 0) {
            } else if (stats[0].toLowerCase().compareTo(statsModif.toLowerCase()) == 0) {
                return Integer.parseInt(stats[1], 16);
            }
        }
        return 0;
    }

    public static int WeithTotalBaseMin(int objTemplateID) {
        int weight = 0;
        int alt = 0;
        String statsTemplate = "";
        statsTemplate = World.world.getObjTemplate(objTemplateID).getStrTemplate();
        if (statsTemplate == null || statsTemplate.isEmpty())
            return 0;
        String[] split = statsTemplate.split(",");
        for (String s : split) {
            String[] stats = s.split("#");
            int statID = Integer.parseInt(stats[0], 16);
            boolean sig = true;
            for (int a : Constant.ARMES_EFFECT_IDS)
                if (a == statID)
                    sig = false;
            if (!sig)
                continue;
            String jet = "";
            int value = 1;
            try {
                jet = stats[4];
                value = Formulas.getRandomJet(jet);
                try {
                    int min = Integer.parseInt(stats[1], 16);
                    value = min;
                } catch (Exception e) {
                    value = Formulas.getRandomJet(jet);
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            int statX = 1;
            if (statID == 125 || statID == 158 || statID == 174) {
                statX = 1;
            } else if (statID == 118 || statID == 126 || statID == 119
                    || statID == 123) {
                statX = 2;
            } else if (statID == 138 || statID == 666 || statID == 226
                    || statID == 220) // de
            // da�os,Trampas %
            {
                statX = 3;
            } else if (statID == 124 || statID == 176) {
                statX = 5;
            } else if (statID == 240 || statID == 241 || statID == 242
                    || statID == 243 || statID == 244)

            {
                statX = 7;
            } else if (statID == 210 || statID == 211 || statID == 212
                    || statID == 213 || statID == 214)

            {
                statX = 8;
            } else if (statID == 225) {
                statX = 15;
            } else if (statID == 178 || statID == 112) {
                statX = 20;
            } else if (statID == 115 || statID == 182) {
                statX = 30;
            } else if (statID == 117) {
                statX = 50;
            } else if (statID == 128) {
                statX = 90;
            } else if (statID == 111) {
                statX = 100;
            }
            weight = value * statX;
            alt += weight;
        }
        return alt;
    }

    public static int WeithTotalBase(int objTemplateID) {
        int weight = 0;
        int alt = 0;
        String statsTemplate = "";
        statsTemplate = World.world.getObjTemplate(objTemplateID).getStrTemplate();
        if (statsTemplate == null || statsTemplate.isEmpty())
            return 0;
        String[] split = statsTemplate.split(",");
        for (String s : split) {
            String[] stats = s.split("#");
            int statID = Integer.parseInt(stats[0], 16);
            boolean sig = true;
            for (int a : Constant.ARMES_EFFECT_IDS)
                if (a == statID)
                    sig = false;
            if (!sig)
                continue;
            String jet = "";
            int value = 1;
            try {
                jet = stats[4];
                value = Formulas.getRandomJet(jet);
                try {
                    int min = Integer.parseInt(stats[1], 16);
                    int max = Integer.parseInt(stats[2], 16);
                    value = min;
                    if (max != 0)
                        value = max;
                } catch (Exception e) {
                    e.printStackTrace();
                    value = Formulas.getRandomJet(jet);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            int statX = 1;
            if (statID == 125 || statID == 158 || statID == 174) {
                statX = 1;
            } else if (statID == 118 || statID == 126 || statID == 119
                    || statID == 123) {
                statX = 2;
            } else if (statID == 138 || statID == 666 || statID == 226
                    || statID == 220) // de
            // da�os,Trampas %
            {
                statX = 3;
            } else if (statID == 124 || statID == 176) {
                statX = 5;
            } else if (statID == 240 || statID == 241 || statID == 242
                    || statID == 243 || statID == 244)

            {
                statX = 7;
            } else if (statID == 210 || statID == 211 || statID == 212
                    || statID == 213 || statID == 214)

            {
                statX = 8;
            } else if (statID == 225) {
                statX = 15;
            } else if (statID == 178 || statID == 112) {
                statX = 20;
            } else if (statID == 115 || statID == 182) {
                statX = 30;
            } else if (statID == 117) {
                statX = 50;
            } else if (statID == 128) {
                statX = 90;
            } else if (statID == 111) {
                statX = 100;
            }
            weight = value * statX;
            alt += weight;
        }
        return alt;
    }

    public static int currentWeithStats(GameObject obj, String statsModif) {
        for (Entry<Integer, Integer> entry : obj.getStats().getMap().entrySet()) {
            int statID = entry.getKey();
            if (Integer.toHexString(statID).toLowerCase().compareTo(statsModif.toLowerCase()) > 0) {
            } else if (Integer.toHexString(statID).toLowerCase().compareTo(statsModif.toLowerCase()) == 0) {
                int statX = 1;
                int coef = 1;
                int BaseStats = viewBaseStatsItem(obj, Integer.toHexString(statID));
                if (BaseStats == 2) {
                    coef = 3;
                } else if (BaseStats == 0) {
                    coef = 8;
                }
                if (statID == 125 || statID == 158 || statID == 174) {
                    statX = 1;
                } else if (statID == 118 || statID == 126 || statID == 119
                        || statID == 123)

                {
                    statX = 2;
                } else if (statID == 138 || statID == 666 || statID == 226
                        || statID == 220) // da�os,Trampas
                // %
                {
                    statX = 3;
                } else if (statID == 124 || statID == 176) {
                    statX = 5;
                } else if (statID == 240 || statID == 241 || statID == 242
                        || statID == 243 || statID == 244)

                {
                    statX = 7;
                } else if (statID == 210 || statID == 211 || statID == 212
                        || statID == 213 || statID == 214) {
                    statX = 8;
                } else if (statID == 225) {
                    statX = 15;
                } else if (statID == 178 || statID == 112) {
                    statX = 20;
                } else if (statID == 115 || statID == 182) {
                    statX = 30;
                } else if (statID == 117) {
                    statX = 50;
                } else if (statID == 128) {
                    statX = 90;
                } else if (statID == 111) {
                    statX = 100;
                }
                int Weight = entry.getValue() * statX * coef;
                return Weight;
            }
        }
        return 0;
    }

    public static int currentStats(GameObject obj, String statsModif) {
        for (Entry<Integer, Integer> entry : obj.getStats().getMap().entrySet()) {
            int statID = entry.getKey();
            if (Integer.toHexString(statID).toLowerCase().compareTo(statsModif.toLowerCase()) > 0) {
            } else if (Integer.toHexString(statID).toLowerCase().compareTo(statsModif.toLowerCase()) == 0) {
                return entry.getValue();
            }
        }
        return 0;
    }

    public static int currentTotalWeigthBase(String statsModelo, GameObject obj) {
        if (statsModelo.equalsIgnoreCase(""))
            return 0;
        int Weigth = 0;
        int Alto = 0;
        String[] split = statsModelo.split(",");
        for (String s : split) {
            String[] stats = s.split("#");
            int statID = Integer.parseInt(stats[0], 16);
            if (statID == 985 || statID == 988)
                continue;
            boolean xy = false;
            for (int a : Constant.ARMES_EFFECT_IDS)
                if (a == statID)
                    xy = true;
            if (xy)
                continue;
            String jet = "";
            int qua = 1;
            try {
                jet = stats[4];
                qua = Formulas.getRandomJet(jet);
                try {
                    int min = Integer.parseInt(stats[1], 16);
                    int max = Integer.parseInt(stats[2], 16);
                    qua = min;
                    if (max != 0)
                        qua = max;
                } catch (Exception e) {
                    e.printStackTrace();
                    qua = Formulas.getRandomJet(jet);
                }
            } catch (Exception e) {
                // Ok :/
            }
            int statX = 1;
            int coef = 1;
            int statsBase = viewBaseStatsItem(obj, stats[0]);
            if (statsBase == 2) {
                coef = 3;
            } else if (statsBase == 0) {
                coef = 2;
            }
            if (statID == 125 || statID == 158 || statID == 174) {
                statX = 1;
            } else if (statID == 118 || statID == 126 || statID == 119
                    || statID == 123) {
                statX = 2;
            } else if (statID == 138 || statID == 666 || statID == 226
                    || statID == 220) // de
            // da�os,Trampas %
            {
                statX = 3;
            } else if (statID == 124 || statID == 176) {
                statX = 5;
            } else if (statID == 240 || statID == 241 || statID == 242
                    || statID == 243 || statID == 244) {
                statX = 7;
            } else if (statID == 210 || statID == 211 || statID == 212
                    || statID == 213 || statID == 214)

            {
                statX = 8;
            } else if (statID == 225) {
                statX = 15;
            } else if (statID == 178 || statID == 112) {
                statX = 20;
            } else if (statID == 115 || statID == 182) {
                statX = 30;
            } else if (statID == 117) {
                statX = 50;
            } else if (statID == 128) {
                statX = 90;
            } else if (statID == 111) {
                statX = 100;
            }
            Weigth = qua * statX * coef;
            Alto += Weigth;
        }
        return Alto;
    }

    public static double getPwrPerEffet(int effect) {
        double r = 0.0;
        switch (effect) {
            case Constant.STATS_ADD_PA:
                r = 100.0;
                break;
            case Constant.STATS_ADD_PM2:
                r = 90.0;
                break;
            case Constant.STATS_ADD_VIE:
                r = 0.25;
                break;
            case Constant.STATS_MULTIPLY_DOMMAGE:
                r = 100.0;
                break;
            case Constant.STATS_ADD_CC:
                r = 30.0;
                break;
            case Constant.STATS_ADD_PO:
                r = 51.0;
                break;
            case Constant.STATS_ADD_FORC:
                r = 1.0;
                break;
            case Constant.STATS_ADD_AGIL:
                r = 1.0;
                break;
            case Constant.STATS_ADD_PA2:
                r = 100.0;
                break;
            case Constant.STATS_ADD_DOMA:
                r = 20.0;
                break;
            case Constant.STATS_ADD_EC:
                r = 1.0;
                break;
            case Constant.STATS_ADD_CHAN:
                r = 1.0;
                break;
            case Constant.STATS_ADD_SAGE:
                r = 3.0;
                break;
            case Constant.STATS_ADD_VITA:
                r = 0.25;
                break;
            case Constant.STATS_ADD_INTE:
                r = 1.0;
                break;
            case Constant.STATS_ADD_PM:
                r = 90.0;
                break;
            case Constant.STATS_ADD_PERDOM:
                r = 2.0;
                break;
            case Constant.STATS_ADD_PDOM:
                r = 2.0;
                break;
            case Constant.STATS_ADD_PODS:
                r = 0.25;
                break;
            case Constant.STATS_ADD_AFLEE:
                r = 1.0;
                break;
            case Constant.STATS_ADD_MFLEE:
                r = 1.0;
                break;
            case Constant.STATS_ADD_INIT:
                r = 0.1;
                break;
            case Constant.STATS_ADD_PROS:
                r = 3.0;
                break;
            case Constant.STATS_ADD_SOIN:
                r = 20.0;
                break;
            case Constant.STATS_CREATURE:
                r = 30.0;
                break;
            case Constant.STATS_ADD_RP_TER:
                r = 6.0;
                break;
            case Constant.STATS_ADD_RP_EAU:
                r = 6.0;
                break;
            case Constant.STATS_ADD_RP_AIR:
                r = 6.0;
                break;
            case Constant.STATS_ADD_RP_FEU:
                r = 6.0;
                break;
            case Constant.STATS_ADD_RP_NEU:
                r = 6.0;
                break;
            case Constant.STATS_TRAPDOM:
                r = 15.0;
                break;
            case Constant.STATS_TRAPPER:
                r = 2.0;
                break;
            case Constant.STATS_ADD_R_FEU:
                r = 2.0;
                break;
            case Constant.STATS_ADD_R_NEU:
                r = 2.0;
                break;
            case Constant.STATS_ADD_R_TER:
                r = 2.0;
                break;
            case Constant.STATS_ADD_R_EAU:
                r = 2.0;
                break;
            case Constant.STATS_ADD_R_AIR:
                r = 2.0;
                break;
            case Constant.STATS_ADD_RP_PVP_TER:
                r = 6.0;
                break;
            case Constant.STATS_ADD_RP_PVP_EAU:
                r = 6.0;
                break;
            case Constant.STATS_ADD_RP_PVP_AIR:
                r = 6.0;
                break;
            case Constant.STATS_ADD_RP_PVP_FEU:
                r = 6.0;
                break;
            case Constant.STATS_ADD_RP_PVP_NEU:
                r = 6.0;
                break;
            case Constant.STATS_ADD_R_PVP_TER:
                r = 2.0;
                break;
            case Constant.STATS_ADD_R_PVP_EAU:
                r = 2.0;
                break;
            case Constant.STATS_ADD_R_PVP_AIR:
                r = 2.0;
                break;
            case Constant.STATS_ADD_R_PVP_FEU:
                r = 2.0;
                break;
            case Constant.STATS_ADD_R_PVP_NEU:
                r = 2.0;
                break;
        }
        return r;
    }

    public static double getOverPerEffet(int effect) {
        double r = 0.0;
        switch (effect) {
            case Constant.STATS_ADD_PA:
                r = 0.0;
                break;
            case Constant.STATS_ADD_PM2:
                r = 404.0;
                break;
            case Constant.STATS_ADD_VIE:
                r = 404.0;
                break;
            case Constant.STATS_MULTIPLY_DOMMAGE:
                r = 0.0;
                break;
            case Constant.STATS_ADD_CC:
                r = 3.0;
                break;
            case Constant.STATS_ADD_PO:
                r = 0.0;
                break;
            case Constant.STATS_ADD_FORC:
                r = 101.0;
                break;
            case Constant.STATS_ADD_AGIL:
                r = 101.0;
                break;
            case Constant.STATS_ADD_PA2:
                r = 0.0;
                break;
            case Constant.STATS_ADD_DOMA:
                r = 5.0;
                break;
            case Constant.STATS_ADD_EC:
                r = 0.0;
                break;
            case Constant.STATS_ADD_CHAN:
                r = 101.0;
                break;
            case Constant.STATS_ADD_SAGE:
                r = 33.0;
                break;
            case Constant.STATS_ADD_VITA:
                r = 404.0;
                break;
            case Constant.STATS_ADD_INTE:
                r = 101.0;
                break;
            case Constant.STATS_ADD_PM:
                r = 0.0;
                break;
            case Constant.STATS_ADD_PERDOM:
                r = 50.0;
                break;
            case Constant.STATS_ADD_PDOM:
                r = 50.0;
                break;
            case Constant.STATS_ADD_PODS:
                r = 404.0;
                break;
            case Constant.STATS_ADD_AFLEE:
                r = 0.0;
                break;
            case Constant.STATS_ADD_MFLEE:
                r = 0.0;
                break;
            case Constant.STATS_ADD_INIT:
                r = 1010.0;
                break;
            case Constant.STATS_ADD_PROS:
                r = 33.0;
                break;
            case Constant.STATS_ADD_SOIN:
                r = 5.0;
                break;
            case Constant.STATS_CREATURE:
                r = 3.0;
                break;
            case Constant.STATS_ADD_RP_TER:
                r = 16.0;
                break;
            case Constant.STATS_ADD_RP_EAU:
                r = 16.0;
                break;
            case Constant.STATS_ADD_RP_AIR:
                r = 16.0;
                break;
            case Constant.STATS_ADD_RP_FEU:
                r = 16.0;
                break;
            case Constant.STATS_ADD_RP_NEU:
                r = 16.0;
                break;
            case Constant.STATS_TRAPDOM:
                r = 6.0;
                break;
            case Constant.STATS_TRAPPER:
                r = 50.0;
                break;
            case Constant.STATS_ADD_R_FEU:
                r = 50.0;
                break;
            case Constant.STATS_ADD_R_NEU:
                r = 50.0;
                break;
            case Constant.STATS_ADD_R_TER:
                r = 50.0;
                break;
            case Constant.STATS_ADD_R_EAU:
                r = 50.0;
                break;
            case Constant.STATS_ADD_R_AIR:
                r = 50.0;
                break;
            case Constant.STATS_ADD_RP_PVP_TER:
                r = 16.0;
                break;
            case Constant.STATS_ADD_RP_PVP_EAU:
                r = 16.0;
                break;
            case Constant.STATS_ADD_RP_PVP_AIR:
                r = 16.0;
                break;
            case Constant.STATS_ADD_RP_PVP_FEU:
                r = 16.0;
                break;
            case Constant.STATS_ADD_RP_PVP_NEU:
                r = 16.0;
                break;
            case Constant.STATS_ADD_R_PVP_TER:
                r = 50.0;
                break;
            case Constant.STATS_ADD_R_PVP_EAU:
                r = 50.0;
                break;
            case Constant.STATS_ADD_R_PVP_AIR:
                r = 50.0;
                break;
            case Constant.STATS_ADD_R_PVP_FEU:
                r = 50.0;
                break;
            case Constant.STATS_ADD_R_PVP_NEU:
                r = 50.0;
                break;
        }
        return r;
    }

    public static int getBaseMaxJet(int templateID, String statsModif) {
        ObjectTemplate t = World.world.getObjTemplate(templateID);
        String[] splitted = t.getStrTemplate().split(",");
        for (String s : splitted) {
            String[] stats = s.split("#");
            if (stats[0].compareTo(statsModif) > 0)//Effets n'existe pas de base
            {
            } else if (stats[0].compareTo(statsModif) == 0)//L'effet existe bien !
            {
                int max = Integer.parseInt(stats[2], 16);
                if (max == 0)
                    max = Integer.parseInt(stats[1], 16);//Pas de jet maximum on prend le minimum
                return max;
            }
        }
        return 0;
    }

    public static int getActualJet(GameObject obj, String statsModif) {
        for (Entry<Integer, Integer> entry : obj.getStats().getMap().entrySet()) {
            if (Integer.toHexString(entry.getKey()).compareTo(statsModif) > 0)//Effets inutiles
            {
            } else if (Integer.toHexString(entry.getKey()).compareTo(statsModif) == 0)//L'effet existe bien !
            {
                int JetActual = entry.getValue();
                return JetActual;
            }
        }
        return 0;
    }

    public static byte viewActualStatsItem(GameObject obj, String stats)//retourne vrai si le stats est actuellement sur l'item
    {
        if (!obj.parseStatsString().isEmpty()) {
            for (Entry<Integer, Integer> entry : obj.getStats().getMap().entrySet()) {
                if (Integer.toHexString(entry.getKey()).compareTo(stats) > 0)//Effets inutiles
                {
                    if (Integer.toHexString(entry.getKey()).compareTo("98") == 0
                            && stats.compareTo("7b") == 0) {
                        return 2;
                    } else if (Integer.toHexString(entry.getKey()).compareTo("9a") == 0
                            && stats.compareTo("77") == 0) {
                        return 2;
                    } else if (Integer.toHexString(entry.getKey()).compareTo("9b") == 0
                            && stats.compareTo("7e") == 0) {
                        return 2;
                    } else if (Integer.toHexString(entry.getKey()).compareTo("9d") == 0
                            && stats.compareTo("76") == 0) {
                        return 2;
                    } else if (Integer.toHexString(entry.getKey()).compareTo("74") == 0
                            && stats.compareTo("75") == 0) {
                        return 2;
                    } else if (Integer.toHexString(entry.getKey()).compareTo("99") == 0
                            && stats.compareTo("7d") == 0) {
                        return 2;
                    } else {
                    }
                } else if (Integer.toHexString(entry.getKey()).compareTo(stats) == 0)//L'effet existe bien !
                {
                    return 1;
                }
            }
            return 0;
        } else {
            return 0;
        }
    }

    public static byte viewBaseStatsItem(GameObject obj, String ItemStats)//retourne vrai si le stats existe de base sur l'item
    {

        String[] splitted = obj.getTemplate().getStrTemplate().split(",");
        for (String s : splitted) {
            String[] stats = s.split("#");
            if (stats[0].compareTo(ItemStats) > 0)//Effets n'existe pas de base
            {
                if (stats[0].compareTo("98") == 0
                        && ItemStats.compareTo("7b") == 0) {
                    return 2;
                } else if (stats[0].compareTo("9a") == 0
                        && ItemStats.compareTo("77") == 0) {
                    return 2;
                } else if (stats[0].compareTo("9b") == 0
                        && ItemStats.compareTo("7e") == 0) {
                    return 2;
                } else if (stats[0].compareTo("9d") == 0
                        && ItemStats.compareTo("76") == 0) {
                    return 2;
                } else if (stats[0].compareTo("74") == 0
                        && ItemStats.compareTo("75") == 0) {
                    return 2;
                } else if (stats[0].compareTo("99") == 0
                        && ItemStats.compareTo("7d") == 0) {
                    return 2;
                } else {
                }
            } else if (stats[0].compareTo(ItemStats) == 0)//L'effet existe bien !
            {
                return 1;
            }
        }
        return 0;
    }
}