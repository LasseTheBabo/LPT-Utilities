package org.lpt;

import org.lpt.util.rcon.RconClient;

public class Main {
    public static void main(String[] args) throws Exception {
        RconClient client = new RconClient("localhost", 25570);
        client.connect("eulib");
        client.sendCommand("say hallo");
        client.close();
    }
}
