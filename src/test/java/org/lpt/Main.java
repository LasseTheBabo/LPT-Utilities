package org.lpt;

import org.lpt.util.rcon.RconClient;
import org.lpt.util.rcon.RconServer;

public class Main {
    public static void main(String[] args) throws Exception {
        RconServer rconServer = new RconServer(25570, "eulibr", new CommandHandler());
        rconServer.open();
        Thread.sleep(100);

        RconClient client = RconClient.connect("localhost", 25570);
        if (client == null)
            return;

        client.authenticate("eulibr");
        client.sendCommand("kill Dev");
        client.close();

        rconServer.close();
    }
}
