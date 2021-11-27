package org.starloco.locos.client.other;

import org.starloco.locos.client.Player;
import org.starloco.locos.common.SocketManager;

import java.util.ArrayList;

public class Party {

    private final Player chief;
    private final ArrayList<Player> players = new ArrayList<>();

    public Party(Player p1, Player p2) {
        this.chief = p1;
        this.players.add(p1);
        this.players.add(p2);
    }

    public ArrayList<Player> getPlayers() {
        return this.players;
    }

    public Player getChief() {
        return this.chief;
    }

    public boolean isChief(int id) {
        return this.chief.getId() == id;
    }

    public void addPlayer(Player player) {
        this.players.add(player);
    }

    public void leave(Player player) {
        if (!this.players.contains(player)) return;

        player.follow = null;
        player.follower.clear();
        player.setParty(null);
        this.players.remove(player);

        for(Player member : this.players) {
            if(member.follow == player) member.follow = null;
            if(member.follower.containsKey(player.getId())) member.follower.remove(player.getId());
        }

        if (this.players.size() == 1) {
            this.players.get(0).setParty(null);
            if (this.players.get(0).getAccount() == null || this.players.get(0).getGameClient() == null)
                return;
            SocketManager.GAME_SEND_PV_PACKET(this.players.get(0).getGameClient(), "");
        } else {
            SocketManager.GAME_SEND_PM_DEL_PACKET_TO_GROUP(this, player.getId());
        }
    }
}
