package org.starloco.locos.command;


import org.starloco.locos.area.map.entity.House;

import org.starloco.locos.client.Player;
import org.starloco.locos.client.other.Maitre; // pour .maitre
import org.starloco.locos.common.PathFinding;
import org.starloco.locos.common.SocketManager;
import org.starloco.locos.database.Database; // Pour .banque
import org.starloco.locos.game.action.ExchangeAction;
import org.starloco.locos.game.world.World;
import org.starloco.locos.kernel.*;			// Ajouté pour .fmcac
import org.starloco.locos.kernel.Config;
import org.starloco.locos.kernel.Logging;
import org.starloco.locos.kernel.Main;
import org.starloco.locos.util.lang.Lang;
import org.starloco.locos.fight.spells.*; // Ajouté pour .fmcac
import org.starloco.locos.object.GameObject; // Ajouté pour .exo
import org.starloco.locos.job.JobAction;
import org.starloco.locos.job.*;




import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map.Entry;

public class CommandPlayer {

    public final static String canal = "Général";
    

//    public void apply(String packet) {  // Ajouté pour commande .level
//    	String msg = packet.substring(2);
//    	String[] infos = msg.split(" ");  
    
//    	if (infos.length == 0) return;
//    	String command = infos[0];
//    }
    

    	

    public static boolean analyse(Player player, String msg) {
        if (msg.charAt(0) == '.' && msg.charAt(1) != '.') {
            if (command(msg, "all") && msg.length() > 5) {
                if (player.isInPrison())
                    return true;
                if (player.noall) {
                    SocketManager.GAME_SEND_MESSAGE(player, Lang.get(player, 0), "C35617");
                    return true;
                }
                if(player.getGroupe() == null && System.currentTimeMillis() - player.getGameClient().timeLastTaverne < 10000) {
                    SocketManager.GAME_SEND_MESSAGE(player, Lang.get(player, 2).replace("#1", String.valueOf(10 - ((System.currentTimeMillis() - player.getGameClient().timeLastTaverne) / 1000))), "C35617");
                    return true;
                }

                player.getGameClient().timeLastTaverne = System.currentTimeMillis();

                String prefix = "<font color='#C35617'>[" + (new SimpleDateFormat("HH:mm").format(new Date(System.currentTimeMillis()))) + "] (" + canal + ") (" + getNameServerById(Main.serverId) + ") <b><a href='asfunction:onHref,ShowPlayerPopupMenu," + player.getName() + "'>" + player.getName() + "</a></b>";

                Logging.getInstance().write("AllMessage", "[" + (new SimpleDateFormat("HH:mm").format(new Date(System.currentTimeMillis()))) + "] : " + player.getName() + " : " + msg.substring(5, msg.length() - 1));

                final String message = "Im116;" + prefix + "~" + msg.substring(5, msg.length() - 1).replace(";", ":").replace("~", "").replace("|", "").replace("<", "").replace(">", "") + "</font>";

                World.world.getOnlinePlayers().stream().filter(p -> !p.noall).forEach(p -> p.send(message));
                Main.exchangeClient.send("DM" + player.getName() + "|" + getNameServerById(Main.serverId) + "|" + msg.substring(5, msg.length() - 1).replace("\n", "").replace("\r", "").replace(";", ":").replace("~", "").replace("|", "").replace("<", "").replace(">", "") + "|");
                return true;
            } else if (command(msg, "noall")) {
                if (player.noall) {
                    player.noall = false;
                    SocketManager.GAME_SEND_MESSAGE(player, Lang.get(player, 3), "C35617");
                } else {
                    player.noall = true;
                    SocketManager.GAME_SEND_MESSAGE(player, Lang.get(player, 4), "C35617");
                }
                return true;
            } else if (command(msg, "staff")) {
                String message = Lang.get(player, 5);
                boolean vide = true;
                for (Player target : World.world.getOnlinePlayers()) {
                    if (target == null)
                        continue;
                    if (target.getGroupe() == null || target.isInvisible())
                        continue;

                    message += "\n- <b><a href='asfunction:onHref,ShowPlayerPopupMenu," + target.getName() + "'>[" + target.getGroupe().getName() + "] " + target.getName() + "</a></b>";
                    vide = false;
                }
                if (vide)
                    message = Lang.get(player, 6);
                SocketManager.GAME_SEND_MESSAGE(player, message);
                return true;
            }  else if (command(msg, "house")) {
                String message = "";
                if(!msg.contains("all")) {
                    message = "L'id de la maison la plus proche est : ";
                    short lstDist = 999;
                    House nearest = null;
                    for (House house : World.world.getHouses().values()) {
                        if (house.getMapId() == player.getCurMap().getId()) {
                            short dist = (short) PathFinding.getDistanceBetween(player.getCurMap(), house.getCellId(), player.getCurCell().getId());
                            if (dist < lstDist) {
                                nearest = house;
                                lstDist = dist;
                            }
                        }
                    }
                    if (nearest != null) message += nearest.getId();
                } else {
                    for (House house : World.world.getHouses().values()) {
                        if (house.getMapId() == player.getCurMap().getId()) {
                            message += "Maison " + house.getId() + " | cellId : " + house.getId();
                        }
                    }
                    if(message.isEmpty()) message = "Aucune maison sur cette carte.";
                }
                SocketManager.GAME_SEND_MESSAGE(player, message);
                return true;
            } else if (command(msg, "deblo")) {//180min
                if (player.isInPrison())
                    return true;
                if (player.cantTP())
                    return true;
                if (player.getFight() != null)
                    return true;
                if(player.getCurCell().isWalkable(true)) {
                    SocketManager.GAME_SEND_MESSAGE(player, Lang.get(player, 7));
                    return true;
                }
                player.teleport(player.getCurMap().getId(), player.getCurMap().getRandomFreeCellId());
                return true;
            } else if (command(msg, "infos")) {
                long uptime = System.currentTimeMillis()
                        - Config.getInstance().startTime;
                int jour = (int) (uptime / (1000 * 3600 * 24));
                uptime %= (1000 * 3600 * 24);
                int hour = (int) (uptime / (1000 * 3600));
                uptime %= (1000 * 3600);
                int min = (int) (uptime / (1000 * 60));
                uptime %= (1000 * 60);
                int sec = (int) (uptime / (1000));
                int nbPlayer = Main.gameServer.getClients().size();
                int nbPlayerIp = Main.gameServer.getPlayersNumberByIp();

                String mess = Lang.get(player, 8).replace("#1", String.valueOf(jour)).replace("#2", String.valueOf(hour)).replace("#3", String.valueOf(min)).replace("#4", String.valueOf(sec));
                if (nbPlayer > 0)
                    mess +=  Lang.get(player, 9).replace("#1", String.valueOf(nbPlayer));
                if (nbPlayerIp > 0)
                    mess +=  Lang.get(player, 10).replace("#1", String.valueOf(nbPlayerIp));
                SocketManager.GAME_SEND_MESSAGE(player, mess);
                return true;
            } else if(command(msg, "banque")) { // commande .banque (complètement copier-coller)
                if (player.getFight() != null){
                    SocketManager.GAME_SEND_MESSAGE(player, "Vous ne pouvez pas utiliser cette commande en combat.");
                    return true;
                }
                boolean ok = false;
                        ok = true;

                if (ok) {
                    Database.getStatics().getPlayerData().update(player);
                    if (player.getDeshonor() >= 1) {
                        SocketManager.GAME_SEND_Im_PACKET(player, "183");
                        return true;
                    }
                    final int cost = player.getBankCost();
                    if (cost > 0) {
                        final long playerKamas = player.getKamas();
                        final long kamasRemaining = playerKamas - cost;
                        final long bankKamas = player.getAccount().getBankKamas();
                        final long totalKamas = bankKamas + playerKamas;
                        if (kamasRemaining < 0)//Si le joueur n'a pas assez de kamas SUR LUI pour ouvrir la banque
                        {
                            if (bankKamas >= cost) {
                                player.setBankKamas(bankKamas - cost); //On modifie les kamas de la banque
                            } else if (totalKamas >= cost) {
                                player.setKamas(0); //On puise l'entiï¿½reter des kamas du joueurs. Ankalike ?
                                player.setBankKamas(totalKamas - cost); //On modifie les kamas de la banque
                                SocketManager.GAME_SEND_STATS_PACKET(player);
                                SocketManager.GAME_SEND_Im_PACKET(player, "020;"
                                        + playerKamas);
                            } else {
                                SocketManager.GAME_SEND_MESSAGE_SERVER(player, "10|"
                                        + cost);
                                return true;
                            }
                        } else
                        //Si le joueur a les kamas sur lui on lui retire directement
                        {
                            player.setKamas(kamasRemaining);
                            SocketManager.GAME_SEND_STATS_PACKET(player);
                            SocketManager.GAME_SEND_Im_PACKET(player, "020;"
                                    + cost);
                        }
                    }
                    SocketManager.GAME_SEND_ECK_PACKET(player.getGameClient(), 5, "");
                    SocketManager.GAME_SEND_EL_BANK_PACKET(player);
                    player.setAway(true);
                    player.setExchangeAction(new ExchangeAction<>(ExchangeAction.IN_BANK, 0));
                }
                return true;
            } else if(command(msg, "maitre")) {    // commande .maitre (copier/coller + adaptation de différentes parties du code)
                if (player.getFight() != null){
                    SocketManager.GAME_SEND_MESSAGE(player, "Vous ne pouvez pas utiliser cette commande en combat.");
                    return true;
                }
        		if(player.isEsclave() == true) {
        			SocketManager.GAME_SEND_MESSAGE(player, "Action impossible, vous êtes un héro.");
					return true;
        		}
                
            	if(player.get_maitre() != null)
            	{
            	
                	player.get_maitre()._esclaves.forEach(esclave -> esclave.setEsclave(false));
            		player.set_maitre(null);
            		SocketManager.GAME_SEND_MESSAGE(player, "Commande désactivée.");
            		
            	}else if(player.get_maitre() != null && player.isEsclave() == true)
                	{
                	
                    	player.get_maitre()._esclaves.forEach(esclave -> esclave.setEsclave(false));
                		player.set_maitre(null);
                		SocketManager.GAME_SEND_MESSAGE(player, "Commande désactivée.");
                		
                	}else{
    				player.set_maitre(new Maitre(player));
    				if(player.get_maitre() != null){
    					SocketManager.GAME_SEND_MESSAGE(player, "Commande activée, vous avez "+player.get_maitre().getEsclaves().size()+" héros. Faîte .tp pour téléporter votre escouade.");
    				}else
    					SocketManager.GAME_SEND_MESSAGE(player, "Aucun héro n'a été trouvé.");
            	}
    			return true;
        	} else if(command(msg, "tp")) {     // commande .tp pour téléporter ses esclaves
                if(System.currentTimeMillis() - player.getGameClient().timeLastTP < 10000) {
                    SocketManager.GAME_SEND_MESSAGE(player, "Cette commande est disponible toutes les 10 secondes.");
                    return true;
                }
                if (player.getCurMap().haveMobFix()) {
                    SocketManager.GAME_SEND_MESSAGE(player, "Vous ne pouvez pas utiliser cette commande en donjon.");
                    return true;
                }
                if (player.isInDungeon()) {
                    SocketManager.GAME_SEND_MESSAGE(player, "Vous ne pouvez pas utiliser cette commande en donjon.");
                    return true;
                }
                if (player.getFight() != null){
                    SocketManager.GAME_SEND_MESSAGE(player, "Vous ne pouvez pas utiliser cette commande en combat.");
                    return true;
                }
                if (player.getExchangeAction() != null){
                    SocketManager.GAME_SEND_MESSAGE(player, "Vous ne pouvez pas utiliser cette commande car vous êtes occupé.");
                    return true;
                }
    		    if(player.get_maitre() != null){
    		    player.getGameClient().timeLastTP = System.currentTimeMillis();
    			player.get_maitre().teleportAllEsclaves();
    			SocketManager.GAME_SEND_MESSAGE(player, "Vous avez téléporté "+player.get_maitre().getEsclaves().size()+" héros.");
    			}
    		    else
    			SocketManager.GAME_SEND_MESSAGE(player, "Aucun héros n'a été trouvé pour la téléportation.");
    			return true;
        	} else if(command(msg, "parcho")) //Commande .parcho
			{
			if(player.getFight() != null)
			return true;

			String element = "";
			int nbreElement = 0;
			if(player.getStats().getEffect(125) < 101)
			{
			player.getStats().addOneStat(125, 101 - player.getStats().getEffect(125));
			element += "vitalité";
			nbreElement++;
			}

			if(player.getStats().getEffect(124) < 101)
			{
			player.getStats().addOneStat(124, 101 - player.getStats().getEffect(124));
			if(nbreElement == 0)
			element += "sagesse";
			else
			element += ", sagesse";
			nbreElement++;
			}

			if(player.getStats().getEffect(118) < 101)
			{
			player.getStats().addOneStat(118, 101 - player.getStats().getEffect(118));
			if(nbreElement == 0)
			element += "force";
			else
			element += ", force";
			nbreElement++;
			}

			if(player.getStats().getEffect(126) < 101)
			{
			player.getStats().addOneStat(126, 101 - player.getStats().getEffect(126));
			if(nbreElement == 0)
			element += "intelligence";
			else
			element += ", intelligence";
			nbreElement++;
			}

			if(player.getStats().getEffect(119) < 101)
			{
			player.getStats().addOneStat(119, 101 - player.getStats().getEffect(119));
			if(nbreElement == 0)
			element += "agilité";
			else
			element += ", agilité";
			nbreElement++;
			}

			if(player.getStats().getEffect(123) < 101)
			{
			player.getStats().addOneStat(123, 101 - player.getStats().getEffect(123));
			if(nbreElement == 0)
			element += "chance";
			else
			element += ", chance";
			nbreElement++;
			}

			if(nbreElement == 0)
			{
			SocketManager.GAME_SEND_Im_PACKET(player, "116;<i>Serveur:</i>Vous avez déjà plus de 100 partout !");
			}
			else
			{
			SocketManager.GAME_SEND_STATS_PACKET(player);
			SocketManager.GAME_SEND_Im_PACKET(player, "116;<i>Serveur:</i>Vous êtes parcho 101 en " + element + " !");
			}
			return true;
			} else if(command(msg, "vie"))//Commande .vie
			{
				int count = 100;
				Player perso = player;
				int newPDV = (perso.getMaxPdv() * count) / 100;
				perso.setPdv(newPDV);
				if(perso.isOnline())
				{
				SocketManager.GAME_SEND_STATS_PACKET(perso);
				}
				SocketManager.GAME_SEND_MESSAGE(player, "Vos points de vie sont au maximum.");
				return true;
			} else if(command(msg, "start")) //Commande .start
			   {
				if (player.isInPrison()) 
					return true;
				if (player.cantTP()) 
					return true;
				if (player.getFight() != null) 
					return true;
				Player perso = player;
		        if (player.getFight() != null) return true;
		        perso.teleport((short) 164, 298);
		        return true;
			   } else if(command(msg, "poutch")) //Commande .poutch
			   {  
				if (player.isInPrison()) 
					return true;
				if (player.cantTP()) 
					return true;
				if (player.getFight() != null) 
					return true;
				Player perso = player;
		        if (player.getFight() != null) return true;
		        perso.teleport((short) 534, 372);
		        return true;
			   } else if(command(msg, "phoenix")) //Commande .phoenix
			   {
				if (player.isInPrison()) 
				return true;
				if (player.cantTP()) 
					return true;
				if (player.getFight() != null) 
					return true;
				Player perso = player;
		        if (player.getFight() != null) return true;
		        perso.teleport((short) 8534, 267);
		        return true;
			   } else if(command(msg, "enclos")) //Commande .enclos
			   {
				if (player.isInPrison()) 
					return true;
				if (player.cantTP()) 
					return true;
				if (player.getFight() != null) 
					return true;
				Player perso = player;
		        if (player.getFight() != null) return true;
		        perso.teleport((short) 8747, 633);
		        return true;
			   } else if(command(msg, "pvp")) //Commande .pvp
			   {
				if (player.isInPrison()) 
					return true;
				if (player.cantTP()) 
					return true;
				if (player.getFight() != null) 
					return true;
				Player perso = player;
		        if (player.getFight() != null) return true;
		        perso.teleport((short) 952, 295);
		        return true;
			   } else if(command(msg, "pvm")) //Commande .pvm
			   {
				if (player.isInPrison()) 
					return true;
				if (player.cantTP()) 
					return true;
				if (player.getFight() != null) 
					return true;
				Player perso = player;
		        if (player.getFight() != null) return true;
		        perso.teleport((short) 957, 223);
		        return true;
			   } else if(command(msg, "shop")) //Commande .shop
			   {
				if (player.isInPrison()) 
					return true;
				if (player.cantTP()) 
					return true;
				if (player.getFight() != null) 
					return true;
				Player perso = player;
		        if (player.getFight() != null) return true;
		        perso.teleport((short) 164, 298);
		        return true;
			   } else if (command(msg, "fmcac")) { // Commande .fmcac (eau, terre, feu, air)
				   // Cette commande est bug sur le deuxième jet neutre de suite ex : Kukri Kura
				   // Le deuxième jet est fm à 100% de son jet initial.
                   GameObject obj = player.getObjetByPos(Constant.ITEM_POS_ARME); // obj
                   if (player.getFight() != null) {
                       SocketManager.GAME_SEND_MESSAGE(player,
                               "Action impossible : vous ne devez pas être en combat");
                       return true;
                   } else if (obj == null) {
                       SocketManager.GAME_SEND_MESSAGE(player,
                               "Action impossible : vous ne portez pas d'arme");
                       return true;
                   }
                   boolean containNeutre = false;
                   for (SpellEffect effect : obj.getEffects()) {
                       if (effect.getEffectID() == 100 || effect.getEffectID() == 95) {
                           containNeutre = true;
                       }
                   }
                   if (!containNeutre) {
                       SocketManager.GAME_SEND_MESSAGE(player,
                               "Action impossible : votre arme n'a pas de dégats neutre");
                       return true;
                   }

                   String answer;

                   try {
                       answer = msg.substring(7, msg.length() - 1);
                   } catch (Exception e) {
                       SocketManager.GAME_SEND_MESSAGE(player,
                               "Action impossible : vous n'avez pas spécifié l'élément (air, feu, terre, eau) qui remplacera les dégats/vols de vies neutres");
                       return true;
                   }

                   if (!answer.equalsIgnoreCase("air") && !answer.equalsIgnoreCase(
                           "terre") && !answer.equalsIgnoreCase("feu") && !answer.equalsIgnoreCase(
                           "eau")) {
                       SocketManager.GAME_SEND_MESSAGE(player,
                               "Action impossible : l'élément " + answer + " est incorrect. (Disponible : air, feu, terre, eau)");
                       return true;
                   }
                   
                   // Ajout des 85% du jet (pas 100% debug)
                       for (SpellEffect effect : obj.getEffects()) {
                           if (effect.getEffectID() != 100)
                               continue;
                           String[] infos = effect.getArgs().split(";");
                           try {                      	   
                        	   int coef = 85;  // fm à 85%
                               int min = Integer.parseInt(infos[0], 16);
                               int max = Integer.parseInt(infos[1], 16);
                               int newMin = (min * coef) / 100;
                               int newMax = (max * coef) / 100;
                               if (newMin == 0)
                                   newMin = 1;
                               String newRange = "1d" + (newMax - newMin + 1) + "+"
                                       + (newMin - 1);
                               String newArgs = Integer.toHexString(newMin) + ";"
                                       + Integer.toHexString(newMax) + ";-1;-1;0;"
                                       + newRange;
                               effect.setArgs(newArgs);
                               
                               
                               /*
                               if (effect.getEffectID() != 95)
                                   continue;
                               effect.setArgs(newArgs); // Souci ; les jets sont ok mais restent neutre (Kukri Kura)
                               */
                               
                               for (int i = 0; i < obj.getEffects().size(); i++) {
                                   if (obj.getEffects().get(i).getEffectID() == 100) {
                                       if (answer.equalsIgnoreCase("air")) {
                                           obj.getEffects().get(i).setEffectID(98);
                                       }
                                       if (answer.equalsIgnoreCase("feu")) {
                                           obj.getEffects().get(i).setEffectID(99);
                                       }
                                       if (answer.equalsIgnoreCase("terre")) {
                                           obj.getEffects().get(i).setEffectID(97);
                                       }
                                       if (answer.equalsIgnoreCase("eau")) {
                                           obj.getEffects().get(i).setEffectID(96);
                                       }
                                   }

                                   if (obj.getEffects().get(i).getEffectID() == 95) {                             	   
                                       if (answer.equalsIgnoreCase("air")) {
                                           obj.getEffects().get(i).setEffectID(93);
                                       }
                                       if (answer.equalsIgnoreCase("feu")) {
                                           obj.getEffects().get(i).setEffectID(94);
                                       }
                                       if (answer.equalsIgnoreCase("terre")) {
                                           obj.getEffects().get(i).setEffectID(92);
                                       }
                                       if (answer.equalsIgnoreCase("eau")) {
                                           obj.getEffects().get(i).setEffectID(91);
                                       }
                                   }
                               }
                           } catch (Exception e) {
                               e.printStackTrace();
                           }
                       } 
                   long new_kamas = player.getKamas();
                   if (new_kamas < 0) //Ne devrait pas arriver...
                   {
                       new_kamas = 0;
                   }
                   player.setKamas(new_kamas);
                   
                   SocketManager.GAME_SEND_STATS_PACKET(player);

                   SocketManager.GAME_SEND_MESSAGE(player,
                           "Votre item : " + obj.getTemplate().getName() + " a été FM avec succès en " + answer + ".");
                   SocketManager.GAME_SEND_MESSAGE(player,
                           " Pensez à vous deco/reco pour voir les changements !");
                   return true;
                       
			   } else if (command(msg, "exo")) { // Commande .exo (coiffe ,cape)(pa, pm)
				   
                   if (player.getFight() != null) {
                       SocketManager.GAME_SEND_MESSAGE(player,
                               "Action impossible : vous ne devez pas être en combat");
                       return true;
                   } 
                   String answer;
                   try {
                       answer = msg.substring(5, msg.length() - 1); // 5 = nbr carac après ".exo " (avec espace) 
                   } catch (Exception e) {
                       SocketManager.GAME_SEND_MESSAGE(player,
                               "Action impossible : vous n'avez pas spécifié l'item à exo.");
                       return true;
                   }
                   
                   
                   if (!answer.equalsIgnoreCase("coiffe pa") && !answer.equalsIgnoreCase(
                           "coiffe pm") && !answer.equalsIgnoreCase("cape pa") && !answer.equalsIgnoreCase(
                                   "cape pm") && !answer.equalsIgnoreCase("ceinture pa") && !answer.equalsIgnoreCase(
                                           "ceinture pm") && !answer.equalsIgnoreCase("bottes pa") && !answer.equalsIgnoreCase(
                                                   "bottes pm") && !answer.equalsIgnoreCase("amulette pa") && !answer.equalsIgnoreCase(
                                                           "amulette pm") && !answer.equalsIgnoreCase("anneauG pa") && !answer.equalsIgnoreCase(
                                                                   "anneauG pm") && !answer.equalsIgnoreCase("anneauD pa") && !answer.equalsIgnoreCase(
                                                                           "anneauD pm") && !answer.equalsIgnoreCase("cac pa") && !answer.equalsIgnoreCase(
                                                                                   "cac pm") ) {
                       SocketManager.GAME_SEND_MESSAGE(player,
                               "Action impossible : l'option " + answer + " est incomplète ou incorrecte. "
                               		+ "(Disponible : coiffe pa, coiffe pm, cape pa , cape pm, ceinture pa, ceinture pm,"
                               		+ " bottes pa, bottes pm, amulette pa, amulette pm, anneauG pa, anneauG pm, anneauD pa, anneauD pm, "
                               		+ "cac pa, cac pm)");
                       return true;
                   }
                   
                   
                   String statsObjectFmPa = "6f";
                   String statsObjectFmPm = "80";
                   int statsAdd = 1;
                   boolean negative = false;
                   
                   // Cas d'une coiffe pa
                   if (answer.equalsIgnoreCase("coiffe pa")) {
                	   
                	   GameObject obj = player.getObjetByPos(Constant.ITEM_POS_COIFFE);
                       if (obj == null) {
                           SocketManager.GAME_SEND_MESSAGE(player,
                                   "Action impossible : vous ne portez pas de coiffe.");
                           return true;
                       }
                       // Pour éviter d'avoir des items 2Pa/2Pm
                       int currentStats = viewActualStatsItem(obj, statsObjectFmPa);
                       if(currentStats == 1) {
                    	   SocketManager.GAME_SEND_MESSAGE(player,
                                   "Cette coiffe possède déjà 1 PA, action impossible.");
                    	   return true;
                       }
                       // Pour éviter l'ajout de PA + PM sur un item qui n'en possède aucun de base
                       int currentStats2 = viewActualStatsItem(obj, statsObjectFmPm);
                       
                       if(currentStats2 == 1) {
                    	   int baseStats = viewBaseStatsItem(obj, statsObjectFmPm);
                    	   if(baseStats == 0) {
                    	   SocketManager.GAME_SEND_MESSAGE(player,
                                   "Cette cape est déjà exo.");
                    	   return true;
                    	   }
                       }
                	   // Ajout de l'exo PA
                       String statsStr = obj.parseFMStatsString(statsObjectFmPa, obj, statsAdd, negative)
                              + ","
                              + statsObjectFmPa
                              + "#"
                              + Integer.toHexString(statsAdd)
                              + "#0#0#0d0+"
                              + statsAdd;
                       obj.clearStats();
                       obj.refreshStatsObjet(statsStr);
                       
                       SocketManager.GAME_SEND_MESSAGE(player,
                               "Votre item : " + obj.getTemplate().getName() + " a été exo avec succès.");
                       SocketManager.GAME_SEND_MESSAGE(player,
                               " Pensez à vous deco/reco pour voir les changements !");
                       
                	   return true;
                   }
                   
                   // Cas coiffe pm
                   if (answer.equalsIgnoreCase("coiffe pm")) {
                	   
                	   GameObject obj = player.getObjetByPos(Constant.ITEM_POS_COIFFE);
                       if (obj == null) {
                           SocketManager.GAME_SEND_MESSAGE(player,
                                   "Action impossible : vous ne portez pas de coiffe.");
                           return true;
                       }

                       // Pour éviter d'avoir des items 2Pa/2Pm
                       int currentStats = viewActualStatsItem(obj, statsObjectFmPm);
                       if(currentStats == 1) {
                    	   SocketManager.GAME_SEND_MESSAGE(player,
                                   "Cette coiffe possède déjà 1 PM, action impossible.");
                    	   return true;
                       }
                       // Pour éviter l'ajout de PA + PM sur un item qui n'en possède aucun de base
                       int currentStats2 = viewActualStatsItem(obj, statsObjectFmPa);
                       
                       if(currentStats2 == 1) {
                    	   int baseStats = viewBaseStatsItem(obj, statsObjectFmPa);
                    	   if(baseStats == 0) {
                    	   SocketManager.GAME_SEND_MESSAGE(player,
                                   "Cette coiffe est déjà exo.");
                    	   return true;
                    	   }
                       }
                	   // Ajout de l'exo PM
                       String statsStr = obj.parseFMStatsString(statsObjectFmPm, obj, statsAdd, negative)
                              + ","
                              + statsObjectFmPm
                              + "#"
                              + Integer.toHexString(statsAdd)
                              + "#0#0#0d0+"
                              + statsAdd;
                       obj.clearStats();
                       obj.refreshStatsObjet(statsStr);
                       
                       SocketManager.GAME_SEND_MESSAGE(player,
                               "Votre item : " + obj.getTemplate().getName() + " a été exo avec succès.");
                       SocketManager.GAME_SEND_MESSAGE(player,
                               " Pensez à vous deco/reco pour voir les changements !");
                       
                	   return true;
                   }
                   
                   // Cas d'une cape pa
                   if (answer.equalsIgnoreCase("cape pa")) {
                	   
                	   GameObject obj = player.getObjetByPos(Constant.ITEM_POS_CAPE);
                       if (obj == null) {
                           SocketManager.GAME_SEND_MESSAGE(player,
                                   "Action impossible : vous ne portez pas de cape.");
                           return true;
                       }
                       // Pour éviter d'avoir des items 2Pa/2Pm
                       int currentStats = viewActualStatsItem(obj, statsObjectFmPa);
                       if(currentStats == 1) {
                    	   SocketManager.GAME_SEND_MESSAGE(player,
                                   "Cette cape possède déjà 1 PA, action impossible.");
                    	   return true;
                       }
                       // Pour éviter l'ajout de PA + PM sur un item qui n'en possède aucun de base
                       int currentStats2 = viewActualStatsItem(obj, statsObjectFmPm);
                       
                       if(currentStats2 == 1) {
                    	   int baseStats = viewBaseStatsItem(obj, statsObjectFmPm);
                    	   if(baseStats == 0) {
                    	   SocketManager.GAME_SEND_MESSAGE(player,
                                   "Cette cape est déjà exo.");
                    	   
                    	   SocketManager.GAME_SEND_MESSAGE(player,
                                   "Votre item : " + obj.getTemplate().getName() + " a été exo avec succès.");
                           SocketManager.GAME_SEND_MESSAGE(player,
                                   " Pensez à vous deco/reco pour voir les changements !");
                    	   
                    	   return true;
                    	   }
                       }
                	   // Ajout de l'exo PA
                       String statsStr = obj.parseFMStatsString(statsObjectFmPa, obj, statsAdd, negative)
                              + ","
                              + statsObjectFmPa
                              + "#"
                              + Integer.toHexString(statsAdd)
                              + "#0#0#0d0+"
                              + statsAdd;
                       obj.clearStats();
                       obj.refreshStatsObjet(statsStr);
                       
                       SocketManager.GAME_SEND_MESSAGE(player,
                               "Votre item : " + obj.getTemplate().getName() + " a été exo avec succès.");
                       SocketManager.GAME_SEND_MESSAGE(player,
                               " Pensez à vous deco/reco pour voir les changements !");
                       
                	   return true;
                   }
                   
                   // Cas cape pm
                   if (answer.equalsIgnoreCase("cape pm")) {
                	   
                	   GameObject obj = player.getObjetByPos(Constant.ITEM_POS_CAPE);
                       if (obj == null) {
                           SocketManager.GAME_SEND_MESSAGE(player,
                                   "Action impossible : vous ne portez pas de cape.");
                           return true;
                       }

                       // Pour éviter d'avoir des items 2Pa/2Pm
                       int currentStats = viewActualStatsItem(obj, statsObjectFmPm);
                       if(currentStats == 1) {
                    	   SocketManager.GAME_SEND_MESSAGE(player,
                                   "Cette cape possède déjà 1 PM, action impossible.");
                    	   return true;
                       }
                       // Pour éviter l'ajout de PA + PM sur un item qui n'en possède aucun de base
                       int currentStats2 = viewActualStatsItem(obj, statsObjectFmPa);
                       
                       if(currentStats2 == 1) {
                    	   int baseStats = viewBaseStatsItem(obj, statsObjectFmPa);
                    	   if(baseStats == 0) {
                    	   SocketManager.GAME_SEND_MESSAGE(player,
                                   "Cette cape est déjà exo.");
                    	   return true;
                    	   }
                       }
                	   // Ajout de l'exo PM
                       String statsStr = obj.parseFMStatsString(statsObjectFmPm, obj, statsAdd, negative)
                              + ","
                              + statsObjectFmPm
                              + "#"
                              + Integer.toHexString(statsAdd)
                              + "#0#0#0d0+"
                              + statsAdd;
                       obj.clearStats();
                       obj.refreshStatsObjet(statsStr);
                       
                       SocketManager.GAME_SEND_MESSAGE(player,
                               "Votre item : " + obj.getTemplate().getName() + " a été exo avec succès.");
                       SocketManager.GAME_SEND_MESSAGE(player,
                               " Pensez à vous deco/reco pour voir les changements !");
                       
                	   return true;
                   }
                   
                   
                // Cas de bottes pa
                   if (answer.equalsIgnoreCase("bottes pa")) {
                	   
                	   GameObject obj = player.getObjetByPos(Constant.ITEM_POS_BOTTES);
                       if (obj == null) {
                           SocketManager.GAME_SEND_MESSAGE(player,
                                   "Action impossible : vous ne portez pas de bottes.");
                           return true;
                       }
                       // Pour éviter d'avoir des items 2Pa/2Pm
                       int currentStats = viewActualStatsItem(obj, statsObjectFmPa);
                       if(currentStats == 1) {
                    	   SocketManager.GAME_SEND_MESSAGE(player,
                                   "Ces bottes possèdent déjà 1 PA, action impossible.");
                    	   return true;
                       }
                       // Pour éviter l'ajout de PA + PM sur un item qui n'en possède aucun de base
                       int currentStats2 = viewActualStatsItem(obj, statsObjectFmPm);
                       
                       if(currentStats2 == 1) {
                    	   int baseStats = viewBaseStatsItem(obj, statsObjectFmPm);
                    	   if(baseStats == 0) {
                    	   SocketManager.GAME_SEND_MESSAGE(player,
                                   "Ces bottes sont déjà exo.");
                    	   return true;
                    	   }
                       }
                	   // Ajout de l'exo PA
                       String statsStr = obj.parseFMStatsString(statsObjectFmPa, obj, statsAdd, negative)
                              + ","
                              + statsObjectFmPa
                              + "#"
                              + Integer.toHexString(statsAdd)
                              + "#0#0#0d0+"
                              + statsAdd;
                       obj.clearStats();
                       obj.refreshStatsObjet(statsStr);
                       
                       SocketManager.GAME_SEND_MESSAGE(player,
                               "Votre item : " + obj.getTemplate().getName() + " a été exo avec succès.");
                       SocketManager.GAME_SEND_MESSAGE(player,
                               " Pensez à vous deco/reco pour voir les changements !");
                       
                	   return true;
                   }
                   
                   
                   // Cas bottes pm
                   if (answer.equalsIgnoreCase("bottes pm")) {
                	   
                	   GameObject obj = player.getObjetByPos(Constant.ITEM_POS_BOTTES);
                       if (obj == null) {
                           SocketManager.GAME_SEND_MESSAGE(player,
                                   "Action impossible : vous ne portez pas de bottes.");
                           return true;
                       }

                       // Pour éviter d'avoir des items 2Pa/2Pm
                       int currentStats = viewActualStatsItem(obj, statsObjectFmPm);
                       if(currentStats == 1) {
                    	   SocketManager.GAME_SEND_MESSAGE(player,
                                   "Ces bottes possèdent déjà 1 PM, action impossible.");
                    	   return true;
                       }
                       // Pour éviter l'ajout de PA + PM sur un item qui n'en possède aucun de base
                       int currentStats2 = viewActualStatsItem(obj, statsObjectFmPa);
                       
                       if(currentStats2 == 1) {
                    	   int baseStats = viewBaseStatsItem(obj, statsObjectFmPa);
                    	   if(baseStats == 0) {
                    	   SocketManager.GAME_SEND_MESSAGE(player,
                                   "Ces bottes sont déjà exo.");
                    	   return true;
                    	   }
                       }
                	   // Ajout de l'exo PM
                       String statsStr = obj.parseFMStatsString(statsObjectFmPm, obj, statsAdd, negative)
                              + ","
                              + statsObjectFmPm
                              + "#"
                              + Integer.toHexString(statsAdd)
                              + "#0#0#0d0+"
                              + statsAdd;
                       obj.clearStats();
                       obj.refreshStatsObjet(statsStr);
                       
                       SocketManager.GAME_SEND_MESSAGE(player,
                               "Votre item : " + obj.getTemplate().getName() + " a été exo avec succès.");
                       SocketManager.GAME_SEND_MESSAGE(player,
                               " Pensez à vous deco/reco pour voir les changements !");
                       
                	   return true;
                   }
                   
                   
                // Cas ceinture pa
                   if (answer.equalsIgnoreCase("ceinture pa")) {
                	   
                	   GameObject obj = player.getObjetByPos(Constant.ITEM_POS_CEINTURE);
                       if (obj == null) {
                           SocketManager.GAME_SEND_MESSAGE(player,
                                   "Action impossible : vous ne portez pas de ceinture.");
                           return true;
                       }
                       // Pour éviter d'avoir des items 2Pa/2Pm
                       int currentStats = viewActualStatsItem(obj, statsObjectFmPa);
                       if(currentStats == 1) {
                    	   SocketManager.GAME_SEND_MESSAGE(player,
                                   "Cette ceinture possède déjà 1 PA, action impossible.");
                    	   return true;
                       }
                       // Pour éviter l'ajout de PA + PM sur un item qui n'en possède aucun de base
                       int currentStats2 = viewActualStatsItem(obj, statsObjectFmPm);
                       
                       if(currentStats2 == 1) {
                    	   int baseStats = viewBaseStatsItem(obj, statsObjectFmPm);
                    	   if(baseStats == 0) {
                    	   SocketManager.GAME_SEND_MESSAGE(player,
                                   "Cette ceinture est déjà exo.");
                    	   return true;
                    	   }
                       }
                	   // Ajout de l'exo PA
                       String statsStr = obj.parseFMStatsString(statsObjectFmPa, obj, statsAdd, negative)
                              + ","
                              + statsObjectFmPa
                              + "#"
                              + Integer.toHexString(statsAdd)
                              + "#0#0#0d0+"
                              + statsAdd;
                       obj.clearStats();
                       obj.refreshStatsObjet(statsStr);
                       
                       SocketManager.GAME_SEND_MESSAGE(player,
                               "Votre item : " + obj.getTemplate().getName() + " a été exo avec succès.");
                       SocketManager.GAME_SEND_MESSAGE(player,
                               " Pensez à vous deco/reco pour voir les changements !");
                       
                	   return true;
                   }
                   
                   
                   
                // Cas ceinture pm
                   if (answer.equalsIgnoreCase("ceinture pm")) {
                	   
                	   GameObject obj = player.getObjetByPos(Constant.ITEM_POS_CEINTURE);
                       if (obj == null) {
                           SocketManager.GAME_SEND_MESSAGE(player,
                                   "Action impossible : vous ne portez pas de ceinture.");
                           return true;
                       }

                       // Pour éviter d'avoir des items 2Pa/2Pm
                       int currentStats = viewActualStatsItem(obj, statsObjectFmPm);
                       if(currentStats == 1) {
                    	   SocketManager.GAME_SEND_MESSAGE(player,
                                   "Cette ceinture possède déjà 1 PM, action impossible.");
                    	   return true;
                       }
                       // Pour éviter l'ajout de PA + PM sur un item qui n'en possède aucun de base
                       int currentStats2 = viewActualStatsItem(obj, statsObjectFmPa);
                       
                       if(currentStats2 == 1) {
                    	   int baseStats = viewBaseStatsItem(obj, statsObjectFmPa);
                    	   if(baseStats == 0) {
                    	   SocketManager.GAME_SEND_MESSAGE(player,
                                   "Cette ceinture est déjà exo.");
                    	   return true;
                    	   }
                       }
                	   // Ajout de l'exo PM
                       String statsStr = obj.parseFMStatsString(statsObjectFmPm, obj, statsAdd, negative)
                              + ","
                              + statsObjectFmPm
                              + "#"
                              + Integer.toHexString(statsAdd)
                              + "#0#0#0d0+"
                              + statsAdd;
                       obj.clearStats();
                       obj.refreshStatsObjet(statsStr);
                       
                       SocketManager.GAME_SEND_MESSAGE(player,
                               "Votre item : " + obj.getTemplate().getName() + " a été exo avec succès.");
                       SocketManager.GAME_SEND_MESSAGE(player,
                               " Pensez à vous deco/reco pour voir les changements !");
                       
                	   return true;
                   }
                   
                   
                // Cas amulette pa
                   if (answer.equalsIgnoreCase("amulette pa")) {
                	   
                	   GameObject obj = player.getObjetByPos(Constant.ITEM_POS_AMULETTE);
                       if (obj == null) {
                           SocketManager.GAME_SEND_MESSAGE(player,
                                   "Action impossible : vous ne portez pas d'amulette.");
                           return true;
                       }
                       // Pour éviter d'avoir des items 2Pa/2Pm
                       int currentStats = viewActualStatsItem(obj, statsObjectFmPa);
                       if(currentStats == 1) {
                    	   SocketManager.GAME_SEND_MESSAGE(player,
                                   "Cette amulette possède déjà 1 PA, action impossible.");
                    	   return true;
                       }
                       // Pour éviter l'ajout de PA + PM sur un item qui n'en possède aucun de base
                       int currentStats2 = viewActualStatsItem(obj, statsObjectFmPm);
                       
                       if(currentStats2 == 1) {
                    	   int baseStats = viewBaseStatsItem(obj, statsObjectFmPm);
                    	   if(baseStats == 0) {
                    	   SocketManager.GAME_SEND_MESSAGE(player,
                                   "Cette amulette est déjà exo.");
                    	   return true;
                    	   }
                       }
                	   // Ajout de l'exo PA
                       String statsStr = obj.parseFMStatsString(statsObjectFmPa, obj, statsAdd, negative)
                              + ","
                              + statsObjectFmPa
                              + "#"
                              + Integer.toHexString(statsAdd)
                              + "#0#0#0d0+"
                              + statsAdd;
                       obj.clearStats();
                       obj.refreshStatsObjet(statsStr);
                       
                       SocketManager.GAME_SEND_MESSAGE(player,
                               "Votre item : " + obj.getTemplate().getName() + " a été exo avec succès.");
                       SocketManager.GAME_SEND_MESSAGE(player,
                               " Pensez à vous deco/reco pour voir les changements !");
                       
                	   return true;
                   }
                   
                   
                   // Cas amulette pm
                   if (answer.equalsIgnoreCase("amulette pm")) {
                	   
                	   GameObject obj = player.getObjetByPos(Constant.ITEM_POS_AMULETTE);
                       if (obj == null) {
                           SocketManager.GAME_SEND_MESSAGE(player,
                                   "Action impossible : vous ne portez pas d'amulette.");
                           return true;
                       }

                       // Pour éviter d'avoir des items 2Pa/2Pm
                       int currentStats = viewActualStatsItem(obj, statsObjectFmPm);
                       if(currentStats == 1) {
                    	   SocketManager.GAME_SEND_MESSAGE(player,
                                   "Cette amulette possède déjà 1 PM, action impossible.");
                    	   return true;
                       }
                       // Pour éviter l'ajout de PA + PM sur un item qui n'en possède aucun de base
                       int currentStats2 = viewActualStatsItem(obj, statsObjectFmPa);
                       
                       if(currentStats2 == 1) {
                    	   int baseStats = viewBaseStatsItem(obj, statsObjectFmPa);
                    	   if(baseStats == 0) {
                    	   SocketManager.GAME_SEND_MESSAGE(player,
                                   "Cette amulette est déjà exo.");
                    	   return true;
                    	   }
                       }
                	   // Ajout de l'exo PM
                       String statsStr = obj.parseFMStatsString(statsObjectFmPm, obj, statsAdd, negative)
                              + ","
                              + statsObjectFmPm
                              + "#"
                              + Integer.toHexString(statsAdd)
                              + "#0#0#0d0+"
                              + statsAdd;
                       obj.clearStats();
                       obj.refreshStatsObjet(statsStr);
                       
                       SocketManager.GAME_SEND_MESSAGE(player,
                               "Votre item : " + obj.getTemplate().getName() + " a été exo avec succès.");
                       SocketManager.GAME_SEND_MESSAGE(player,
                               " Pensez à vous deco/reco pour voir les changements !");
                       
                	   return true;
                   }
                   
                   
                   // Cas anneau gauche pa
                   if (answer.equalsIgnoreCase("anneauG pa")) {
                	   
                	   GameObject obj = player.getObjetByPos(Constant.ITEM_POS_ANNEAU1);
                       if (obj == null) {
                           SocketManager.GAME_SEND_MESSAGE(player,
                                   "Action impossible : vous ne portez pas d'anneau gauche.");
                           return true;
                       }
                       // Pour éviter d'avoir des items 2Pa/2Pm
                       int currentStats = viewActualStatsItem(obj, statsObjectFmPa);
                       if(currentStats == 1) {
                    	   SocketManager.GAME_SEND_MESSAGE(player,
                                   "Cet anneau possède déjà 1 PA, action impossible.");
                    	   return true;
                       }
                       // Pour éviter l'ajout de PA + PM sur un item qui n'en possède aucun de base
                       int currentStats2 = viewActualStatsItem(obj, statsObjectFmPm);
                       
                       if(currentStats2 == 1) {
                    	   int baseStats = viewBaseStatsItem(obj, statsObjectFmPm);
                    	   if(baseStats == 0) {
                    	   SocketManager.GAME_SEND_MESSAGE(player,
                                   "Cet anneau est déjà exo.");
                    	   return true;
                    	   }
                       }
                	   // Ajout de l'exo PA
                       String statsStr = obj.parseFMStatsString(statsObjectFmPa, obj, statsAdd, negative)
                              + ","
                              + statsObjectFmPa
                              + "#"
                              + Integer.toHexString(statsAdd)
                              + "#0#0#0d0+"
                              + statsAdd;
                       obj.clearStats();
                       obj.refreshStatsObjet(statsStr);
                       
                       SocketManager.GAME_SEND_MESSAGE(player,
                               "Votre item : " + obj.getTemplate().getName() + " a été exo avec succès.");
                       SocketManager.GAME_SEND_MESSAGE(player,
                               " Pensez à vous deco/reco pour voir les changements !");
                       
                	   return true;
                   }
                   
                   
                // Cas anneau gauche pm
                   if (answer.equalsIgnoreCase("anneauG pm")) {
                	   
                	   GameObject obj = player.getObjetByPos(Constant.ITEM_POS_ANNEAU1);
                       if (obj == null) {
                           SocketManager.GAME_SEND_MESSAGE(player,
                                   "Action impossible : vous ne portez pas d'anneau gauche.");
                           return true;
                       }

                       // Pour éviter d'avoir des items 2Pa/2Pm
                       int currentStats = viewActualStatsItem(obj, statsObjectFmPm);
                       if(currentStats == 1) {
                    	   SocketManager.GAME_SEND_MESSAGE(player,
                                   "Cet anneau possède déjà 1 PM, action impossible.");
                    	   return true;
                       }
                       // Pour éviter l'ajout de PA + PM sur un item qui n'en possède aucun de base
                       int currentStats2 = viewActualStatsItem(obj, statsObjectFmPa);
                       
                       if(currentStats2 == 1) {
                    	   int baseStats = viewBaseStatsItem(obj, statsObjectFmPa);
                    	   if(baseStats == 0) {
                    	   SocketManager.GAME_SEND_MESSAGE(player,
                                   "Cet anneau est déjà exo.");
                    	   return true;
                    	   }
                       }
                	   // Ajout de l'exo PM
                       String statsStr = obj.parseFMStatsString(statsObjectFmPm, obj, statsAdd, negative)
                              + ","
                              + statsObjectFmPm
                              + "#"
                              + Integer.toHexString(statsAdd)
                              + "#0#0#0d0+"
                              + statsAdd;
                       obj.clearStats();
                       obj.refreshStatsObjet(statsStr);
                       
                       SocketManager.GAME_SEND_MESSAGE(player,
                               "Votre item : " + obj.getTemplate().getName() + " a été exo avec succès.");
                       SocketManager.GAME_SEND_MESSAGE(player,
                               " Pensez à vous deco/reco pour voir les changements !");
                       
                	   return true;
                   }
                   
                   
                // Cas anneau droit pa
                   if (answer.equalsIgnoreCase("anneauD pa")) {
                	   
                	   GameObject obj = player.getObjetByPos(Constant.ITEM_POS_ANNEAU2);
                       if (obj == null) {
                           SocketManager.GAME_SEND_MESSAGE(player,
                                   "Action impossible : vous ne portez pas d'anneau droit.");
                           return true;
                       }
                       // Pour éviter d'avoir des items 2Pa/2Pm
                       int currentStats = viewActualStatsItem(obj, statsObjectFmPa);
                       if(currentStats == 1) {
                    	   SocketManager.GAME_SEND_MESSAGE(player,
                                   "Cet anneau possède déjà 1 PA, action impossible.");
                    	   return true;
                       }
                       // Pour éviter l'ajout de PA + PM sur un item qui n'en possède aucun de base
                       int currentStats2 = viewActualStatsItem(obj, statsObjectFmPm);
                       
                       if(currentStats2 == 1) {
                    	   int baseStats = viewBaseStatsItem(obj, statsObjectFmPm);
                    	   if(baseStats == 0) {
                    	   SocketManager.GAME_SEND_MESSAGE(player,
                                   "Cet anneau est déjà exo.");
                    	   return true;
                    	   }
                       }
                	   // Ajout de l'exo PA
                       String statsStr = obj.parseFMStatsString(statsObjectFmPa, obj, statsAdd, negative)
                              + ","
                              + statsObjectFmPa
                              + "#"
                              + Integer.toHexString(statsAdd)
                              + "#0#0#0d0+"
                              + statsAdd;
                       obj.clearStats();
                       obj.refreshStatsObjet(statsStr);
                       
                       SocketManager.GAME_SEND_MESSAGE(player,
                               "Votre item : " + obj.getTemplate().getName() + " a été exo avec succès.");
                       SocketManager.GAME_SEND_MESSAGE(player,
                               " Pensez à vous deco/reco pour voir les changements !");
                       
                	   return true;
                   }
                   
                   
                // Cas anneau droit pm
                   if (answer.equalsIgnoreCase("anneauD pm")) {
                	   
                	   GameObject obj = player.getObjetByPos(Constant.ITEM_POS_ANNEAU2);
                       if (obj == null) {
                           SocketManager.GAME_SEND_MESSAGE(player,
                                   "Action impossible : vous ne portez pas d'anneau droit.");
                           return true;
                       }

                       // Pour éviter d'avoir des items 2Pa/2Pm
                       int currentStats = viewActualStatsItem(obj, statsObjectFmPm);
                       if(currentStats == 1) {
                    	   SocketManager.GAME_SEND_MESSAGE(player,
                                   "Cet anneau possède déjà 1 PM, action impossible.");
                    	   return true;
                       }
                       // Pour éviter l'ajout de PA + PM sur un item qui n'en possède aucun de base
                       int currentStats2 = viewActualStatsItem(obj, statsObjectFmPa);
                       
                       if(currentStats2 == 1) {
                    	   int baseStats = viewBaseStatsItem(obj, statsObjectFmPa);
                    	   if(baseStats == 0) {
                    	   SocketManager.GAME_SEND_MESSAGE(player,
                                   "Cet anneau est déjà exo.");
                    	   return true;
                    	   }
                       }
                	   // Ajout de l'exo PM
                       String statsStr = obj.parseFMStatsString(statsObjectFmPm, obj, statsAdd, negative)
                              + ","
                              + statsObjectFmPm
                              + "#"
                              + Integer.toHexString(statsAdd)
                              + "#0#0#0d0+"
                              + statsAdd;
                       obj.clearStats();
                       obj.refreshStatsObjet(statsStr);
                       
                       SocketManager.GAME_SEND_MESSAGE(player,
                               "Votre item : " + obj.getTemplate().getName() + " a été exo avec succès.");
                       SocketManager.GAME_SEND_MESSAGE(player,
                               " Pensez à vous deco/reco pour voir les changements !");
                       
                	   return true;
                   }
                   
                   
                // Cas cac pa
                   if (answer.equalsIgnoreCase("cac pa")) {
                	   
                	   GameObject obj = player.getObjetByPos(Constant.ITEM_POS_ARME);
                       if (obj == null) {
                           SocketManager.GAME_SEND_MESSAGE(player,
                                   "Action impossible : vous ne portez pas de CAC.");
                           return true;
                       }
                       // Pour éviter d'avoir des items 2Pa/2Pm
                       int currentStats = viewActualStatsItem(obj, statsObjectFmPa);
                       if(currentStats == 1) {
                    	   SocketManager.GAME_SEND_MESSAGE(player,
                                   "Ce CAC possède déjà 1 PA, action impossible.");
                    	   return true;
                       }
                       // Pour éviter l'ajout de PA + PM sur un item qui n'en possède aucun de base
                       int currentStats2 = viewActualStatsItem(obj, statsObjectFmPm);
                       
                       if(currentStats2 == 1) {
                    	   int baseStats = viewBaseStatsItem(obj, statsObjectFmPm);
                    	   if(baseStats == 0) {
                    	   SocketManager.GAME_SEND_MESSAGE(player,
                                   "Ce CAC est déjà exo.");
                    	   return true;
                    	   }
                       }
                	   // Ajout de l'exo PA
                       String statsStr = obj.parseFMStatsString(statsObjectFmPa, obj, statsAdd, negative)
                              + ","
                              + statsObjectFmPa
                              + "#"
                              + Integer.toHexString(statsAdd)
                              + "#0#0#0d0+"
                              + statsAdd;
                       obj.clearStats();
                       obj.refreshStatsObjet(statsStr);
                       
                       SocketManager.GAME_SEND_MESSAGE(player,
                               "Votre item : " + obj.getTemplate().getName() + " a été exo avec succès.");
                       SocketManager.GAME_SEND_MESSAGE(player,
                               " Pensez à vous deco/reco pour voir les changements !");
                       
                	   return true;
                   }
                   
                   
                    // Cac pm
                   if (answer.equalsIgnoreCase("cac pm")) {
                	   
                	   GameObject obj = player.getObjetByPos(Constant.ITEM_POS_ARME);
                       if (obj == null) {
                           SocketManager.GAME_SEND_MESSAGE(player,
                                   "Action impossible : vous ne portez pas de CAC.");
                           return true;
                       }

                       // Pour éviter d'avoir des items 2Pa/2Pm
                       int currentStats = viewActualStatsItem(obj, statsObjectFmPm);
                       if(currentStats == 1) {
                    	   SocketManager.GAME_SEND_MESSAGE(player,
                                   "Ce CAC possède déjà 1 PM, action impossible.");
                    	   return true;
                       }
                       // Pour éviter l'ajout de PA + PM sur un item qui n'en possède aucun de base
                       int currentStats2 = viewActualStatsItem(obj, statsObjectFmPa);
                       
                       if(currentStats2 == 1) {
                    	   int baseStats = viewBaseStatsItem(obj, statsObjectFmPa);
                    	   if(baseStats == 0) {
                    	   SocketManager.GAME_SEND_MESSAGE(player,
                                   "Ce CAC est déjà exo.");
                    	   return true;
                    	   }
                       }
                	   // Ajout de l'exo PM
                       String statsStr = obj.parseFMStatsString(statsObjectFmPm, obj, statsAdd, negative)
                              + ","
                              + statsObjectFmPm
                              + "#"
                              + Integer.toHexString(statsAdd)
                              + "#0#0#0d0+"
                              + statsAdd;
                       obj.clearStats();
                       obj.refreshStatsObjet(statsStr);
                       
                       SocketManager.GAME_SEND_MESSAGE(player,
                               "Votre item : " + obj.getTemplate().getName() + " a été exo avec succès.");
                       SocketManager.GAME_SEND_MESSAGE(player,
                               " Pensez à vous deco/reco pour voir les changements !");
                       
                	   return true;
                   }
                   
                   
                   
			   
			   	} else if (command(msg, "reset")) { //Commande .reset
				    Player perso = player;
		            perso.getStats().addOneStat(125, -perso.getStats().getEffect(125));
		            perso.getStats().addOneStat(124, -perso.getStats().getEffect(124));
		            perso.getStats().addOneStat(118, -perso.getStats().getEffect(118));
		            perso.getStats().addOneStat(123, -perso.getStats().getEffect(123));
		            perso.getStats().addOneStat(119, -perso.getStats().getEffect(119));
		            perso.getStats().addOneStat(126, -perso.getStats().getEffect(126));
		            perso.getStatsParcho().getMap().clear();
		            perso.addCapital((perso.getLevel() - 1) * 5 - perso.get_capital());
		            SocketManager.GAME_SEND_STATS_PACKET(perso);
		            SocketManager.GAME_SEND_MESSAGE(player, "Vous vous êtes restat.");
		            return true;
		            } else if (command(msg, "level")) { //Commande .level
		            String answer;
		            answer = msg.substring(7, msg.length() - 1);
                    int count = 0;
                    try {
						count = Integer.parseInt(answer);
						
						
						if(count == player.getLevel())
			            {
			            	SocketManager.GAME_SEND_MESSAGE(player, 
			            			"Le level demandé est identique à votre level actuel.");
			            	return true;
			            }
						if(count < player.getLevel())
			            {
			            	SocketManager.GAME_SEND_MESSAGE(player, 
			            			"Vous ne pouvez pas vous donner un niveau inférieur à votre niveau actuel.");
			            	return true;
			            }
						
                        if (count < 1)
                            count = 1;
                        if (count > World.world.getExpLevelSize())
                            count = World.world.getExpLevelSize();
                            Player perso = player;
                        if (perso.getLevel() < count) {
                            while (perso.getLevel() < count)
                                perso.levelUp(false, true);
                            if (perso.isOnline()) {
                                SocketManager.GAME_SEND_SPELL_LIST(perso);
                                SocketManager.GAME_SEND_NEW_LVL_PACKET(perso.getGameClient(), perso.getLevel());
                                SocketManager.GAME_SEND_STATS_PACKET(perso);
                            }
                        }
                        String mess = "Vous avez fixé le niveau de " + perso.getName()
                                + " à " + count + ".";
                        SocketManager.GAME_SEND_MESSAGE(player, mess);
                        
                    } catch (Exception e) {
                        // ok
                    	SocketManager.GAME_SEND_MESSAGE(player, "Valeur incorrecte.");
                        return true;
                    }
                    return true;
			     
			   } else if(command(msg, "demon")) //Commande Brakmarien
	              {
		              byte align = 2;
		              Player target = player;
		              target.modifAlignement(align);
		              if(target.isOnline())
		              SocketManager.GAME_SEND_STATS_PACKET(target);
		              SocketManager.GAME_SEND_MESSAGE(player, "Tu es désormais Brâkmarien.");
		              return true;
		              
		            } else if(command(msg, "ange")) //Commande bontarien
		              {
		              byte align = 1;
		              Player target = player;
		              target.modifAlignement(align);
		              if(target.isOnline())
		              SocketManager.GAME_SEND_STATS_PACKET(target);
		              SocketManager.GAME_SEND_MESSAGE(player, "Tu es désormais Bontarien.");
		              return true;
		              
		            } else  if(command(msg, "neutre")) //Commande neutre
		              {
		              byte align = 0;
		              Player target = player;
		              target.modifAlignement(align);
		              if(target.isOnline())
		              SocketManager.GAME_SEND_STATS_PACKET(target);
		              SocketManager.GAME_SEND_MESSAGE(player, "Tu es désormais Neutre.");
		              return true;
		              
			    } else {
                SocketManager.GAME_SEND_MESSAGE(player, Lang.get(player, 12));
                return true;
            }
        }
        return false;
    }

    private static boolean command(String msg, String command) {
        return msg.length() > command.length() && msg.substring(1, command.length() + 1).equalsIgnoreCase(command);
    }

    private static String getNameServerById(int id) {
        switch(id) {
            case 13: return "Silouate";
            case 19: return "Allister";
            case 22: return "Oto Mustam";
            case 1: return "Jiva";
            case 37: return "Nostalgy";
            case 4001: return "Alma";
            case 4002: return "Aguabrial";
        }
        return "Unknown";
    }
    
    // .exo 
    // Marche pas autrement qu'en faisant un gros copier coller de noob
    // Cette partie de code provient de locos.job.JobAction
    public static byte viewActualStatsItem(GameObject obj, String stats)//retourne vrai si la stats est actuellement sur l'item
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






















