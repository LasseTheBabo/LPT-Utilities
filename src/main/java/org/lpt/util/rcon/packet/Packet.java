package org.lpt.util.rcon.packet;

public class Packet {
    public final int requestId;
    public final int type;
    public final String payload;

    public Packet(int requestId, int type, String payload) {
        this.requestId = requestId;
        this.type = type;
        this.payload = payload;
    }

    public boolean isValid() {
        return (type != -1) && (requestId != -1);
    }
}
