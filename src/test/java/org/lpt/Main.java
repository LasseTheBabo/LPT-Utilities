package org.lpt;

import org.lpt.util.rcon.RconClient;

public class Main {
    public static void main(String[] args) throws Exception {
        //RconServer rconServer = new RconServer(25570, "minecraft", new CommandHandler());
        //rconServer.open();
        //Thread.sleep(100);

        RconClient client = RconClient.connect("localhost", 25570);
        if (client == null)
            return;

        client.authenticate("Ch4ng3-M3");
        client.sendCommand("say hallo");
        client.close();

        //rconServer.close();
    }
}
