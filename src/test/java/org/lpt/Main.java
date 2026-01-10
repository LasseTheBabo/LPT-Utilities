package org.lpt;

import org.lpt.util.rcon.RconClient;
import org.lpt.util.rcon.RconServer;

public class Main {
    public static void main(String[] args) throws Exception {
        RconServer rconServer = new RconServer(
                25570,
                "Ch4ng3-M3",
                new CommandHandler()
        );
        rconServer.open();
        Thread.sleep(100);

        try (RconClient client = RconClient.connect("localhost", 25570)) {
            if (client.authenticate("Ch4ng3-M3")) {
                client.sendCommand("say hallo");
            } else {
                System.out.println("Failed to authenticate");
            }
        }
    }
}
