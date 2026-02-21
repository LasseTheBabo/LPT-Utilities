package org.lpt;

import org.lpt.util.rcon.MessageHandler;

import static org.lpt.util.Util.LOGGER;

public class CommandHandler implements MessageHandler {
    @Override
    public String[] handleMessage(String message) {
        LOGGER.info("Received message: " + message);

        return new String[] { "hello" };
    }
}
