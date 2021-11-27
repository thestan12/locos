package org.starloco.locos.util.lang.type;

import org.starloco.locos.kernel.Config;
import org.starloco.locos.util.lang.AbstractLang;

/**
 * Created by Locos on 09/12/2015.
 */
public class French extends AbstractLang {

    public final static French singleton = new French();

    public static French getInstance() {
        return singleton;
    }

    public void initialize() {
        int index = 0;
        this.sentences.add(index, "Votre canal g�n�ral est d�sactiv�."); index++;
        this.sentences.add(index, "Les caract�res point virgule, chevrons et tild� sont d�sactiv�s."); index++;
        this.sentences.add(index, "Tu dois attendre encore #1 seconde(s)."); index++;
        this.sentences.add(index, "Vous avez activ� le canal g�n�ral."); index++;
        this.sentences.add(index, "Vous avez d�sactiv� le canal g�n�ral."); index++;
        this.sentences.add(index, "Liste des membres du staff connect�s :"); index++;
        this.sentences.add(index, "Il n'y a aucun membre du staff connect� ou peut-�tre y a t'il la pr�sence de Locos ?"); index++;
        this.sentences.add(index, "Vous n'�tes pas bloqu�.."); index++;
        this.sentences.add(index, "<b>StarLoco - " + Config.getInstance().NAME + "</b>\nEn ligne depuis : #1j #2h #3m #4s."); index++;
        this.sentences.add(index, "\nJoueurs en ligne : #1"); index++;
        this.sentences.add(index, "\nJoueurs uniques en ligne : #1"); index++;
        this.sentences.add(index, "\nRecord de connexion : #1"); index++;
        this.sentences.add(index, "Les commandes disponnibles sont :\n"
                + "<b>.infos</b> - Permet d'obtenir des informations sur le serveur.\n"
                + "<b>.deblo</b> - Permet de vous d�bloquer en vous t�l�portant � une cellule libre.\n"
                + "<b>.staff</b> - Permet de voir les membres du staff connect�s.\n"
                + "<b>.reset</b> - Permet de remettre ses caract�ristiques � 0.\n"
                + "<b>.parcho</b> - Permet de se parcho 101 dans tous les �l�ments.\n"
                + "<b>.level</b> - Permet de fixer son level � une valeur. (Ex : .level 100)\n"
                + "<b>.vie</b> - Permet de restaurer 100% de sa vie.\n"
                + "<b>.fmcac</b> - Permet d'FM son cac dans un �l�ment d'attaque. (ex : .fmcac feu)\n"
                + "<b>.exo</b> - Permet d'exo un item PA ou PM. (ex : .exo cape pa)\n"
                + "<b>.start</b> - Permet de se t�l�porter � la map de d�part.\n"
                + "<b>.poutch</b> - Permet de se t�l�porter au poutch.\n"
                + "<b>.enclos</b> - Permet de se t�l�porter � un enclos.\n"
                + "<b>.pvp</b> - Permet de se t�l�porter � la map pvp.\n"
                + "<b>.pvm</b> - Permet de se t�l�porter � la map pvm.\n"
                + "<b>.maitre</b> - Permet de cr�er une escouade.\n"
                + "<b>.tp</b> - Permet de t�l�porter votre escouade.\n"
                + "<b>.banque</b> - Permet d'ouvrir l'interface de sa banque.\n"
                + "<b>.phoenix</b> - Permet de se t�l�porter � la statue du Phoenix.\n"
                + "<b>.shop</b> - Permet de se t�l�porter � la zone shop.\n"
                + "<b>.ange</b> - Permet de passer en Alignement Bontarien.\n"
                + "<b>.demon</b> - Permet de passer en Alignement Brakmarien.\n"
                + "<b>.neutre</b> - Permet de passer en Alignement Neutre.\n"
                + "<b>.all</b> - Permet d'envoyer un message � tous les joueurs. (ex : .all Hey!)\n"
                + "<b>.noall</b> - Permet de ne plus recevoir les messages du canal g�n�ral."); index++;
        this.sentences.add(index, "Retrouvez les commandes en tapant .commandes ou .x dans le chat.");
    }
}
