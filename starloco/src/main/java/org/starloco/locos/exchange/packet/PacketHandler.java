package org.starloco.locos.exchange.packet;

import org.starloco.locos.exchange.ExchangeClient;
import org.starloco.locos.kernel.Main;
import org.starloco.locos.object.Server;

import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;

public class PacketHandler {

    public static void parser(ExchangeClient client, String packet) {
        try {
            switch (packet.charAt(0)) {
                case 'F': // Free places
                    int freePlaces = Integer.parseInt(packet.substring(1));
                    client.getServer().setFreePlaces(freePlaces);
                    break;

                case 'S': // Server
                    switch (packet.charAt(1)) {
                        case 'H': // Host
                            Server server = client.getServer();
                            String[] s = packet.substring(2).split(";");
                            server.setIp(s[0]);
                            server.setPort(Integer.parseInt(s[1]));
                            client.send("SHK#");
                            break;

                        case 'K': // Key
                            s = packet.substring(2).split(";");
                            int id = Integer.parseInt(s[0]);
                            String key = s[1];
                            freePlaces = Integer.parseInt(s[2]);

                            server = Server.get(id);

                            if(server != null) {
                                if (!server.getKey().equals(key)) {
                                    client.send("SKR#");
                                    client.kick();
                                }

                                server.setClient(client);
                                client.setServer(server);
                                server.setFreePlaces(freePlaces);
                                client.send("SKK#");
                            }
                            break;

                        case 'S': // Statut
                            if (client.getServer() != null)
                                client.getServer().setState(Integer.parseInt(packet.substring(2)));
                            break;
                    }
                    break;

                case 'D' : // Data
                    switch(packet.charAt(1)) {
                        case 'I': // Id
                            switch(Byte.parseByte(String.valueOf(packet.charAt(2)))) {
                                case 1: // Mount
                                    int id = Main.database.getWorldEntityData().getNextMountId();
                                    client.send("DI1" + packet.substring(3) + ";" + id + "#");
                                    break;
                                case 2: // Object
                                    id = Main.database.getWorldEntityData().getNextObjectId();
                                    client.send("DI2" + packet.substring(3) + ";" + id+ "#");
                                    break;
                                case 3: // QuestPlayer
                                    id = Main.database.getWorldEntityData().getNextQuestPlayerId();
                                    client.send("DI3" + packet.substring(3) + ";" + id+ "#");
                                    break;
                                case 4: // Guild
                                    id = Main.database.getWorldEntityData().getNextGuildId();
                                    client.send("DI4" + packet.substring(3) + ";" + id+ "#");
                                    break;
                                case 5: // Pet
                                    id = Main.database.getWorldEntityData().getNextPetId();
                                    client.send("DI5" + packet.substring(3) + ";" + id+ "#");
                                    break;
                            }
                            break;

                        case 'M':
                            Server.servers.values().stream()
                                    .filter(server -> server != null && server.getClient() != null && server.getId() != client.getServer().getId())
                                    .forEach(server -> server.send(packet+ "#"));
                            break;
                    }
                    break;

                default:
                    client.send("Packet undefined\"" + packet + "\"#");
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            client.kick();
        }
    }
}
