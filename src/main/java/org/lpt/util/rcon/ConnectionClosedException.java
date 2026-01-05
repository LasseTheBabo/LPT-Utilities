package org.lpt.util.rcon;

import java.io.IOException;

public class ConnectionClosedException extends IOException {
    public ConnectionClosedException(String message) {
        super(message);
    }

    public ConnectionClosedException(String message, Throwable cause) {
        super(message, cause);
    }
}
