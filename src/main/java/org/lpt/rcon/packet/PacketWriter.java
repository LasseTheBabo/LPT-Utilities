package org.lpt.rcon.packet;

import org.lpt.Config;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static org.lpt.Util.LOGGER;

public class PacketWriter {
    final private Destination destination;
    final private PacketCodec codec;
    final private ByteBuffer buffer;

    public PacketWriter(final Destination destination, final int bufferCapacity, final PacketCodec codec) {
        this.destination = destination;
        this.codec = codec;
        this.buffer = ByteBuffer.allocate(bufferCapacity).order(ByteOrder.LITTLE_ENDIAN);
    }

    public int write(final Packet packet) throws IOException {
        int length = packet.payload.length();
        if (length > Config.C2S_BYTES - 14) {
            LOGGER.error("Packet payload too big: {}", length);
            throw new EOFException();
        }

        buffer.clear();
        buffer.position(Integer.BYTES);
        codec.encode(packet, buffer);
        buffer.putInt(0, buffer.position() - Integer.BYTES);
        buffer.flip();

        return destination.write(buffer);
    }

    @FunctionalInterface
    public interface Destination {
        int write(ByteBuffer source) throws IOException;
    }
}
