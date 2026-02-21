package org.lpt;

import org.junit.jupiter.api.Test;
import org.lpt.util.Logger;
import org.lpt.util.Util;
import org.lpt.util.rcon.RconClient;
import org.lpt.util.rcon.RconServer;

public class ServerClientTest {
    @Test
    public void test() throws Exception {
        Util.LOGGER = new Logger() {
            @Override
            public void info(String message) {
                System.out.println(message);
            }

            @Override
            public void error(String message) {
                System.err.println(message);
            }
        };

        RconServer rconServer = new RconServer(
                25570,
                "Ch4ng3-M3",
                new CommandHandler()
        );
        rconServer.open();
        Thread.sleep(100);

        try (RconClient client = RconClient.connect("localhost", 25570)) {
            if (client.authenticate("Ch4ng3-M3")) {
                client.sendCommand("help").forEach(System.out::println);
            } else {
                System.out.println("Failed to authenticate");
            }
        }
    }
}
