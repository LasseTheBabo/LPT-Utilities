package org.lpt.util.rcon.packet;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class PacketReader {
    private final Source source;
    private final PacketCodec codec;
    private final ByteBuffer buffer;

    public PacketReader(Source source, int bufferCapacity, PacketCodec codec) {
        this.source = source;
        this.codec = codec;
        this.buffer = ByteBuffer.allocate(bufferCapacity).order(ByteOrder.LITTLE_ENDIAN);
    }

    public Packet read() throws IOException {
        readUntilAvailable(Integer.BYTES);
        buffer.flip();
        int length = buffer.getInt();
        buffer.compact();

        readUntilAvailable(length);
        buffer.flip();
        Packet packet = codec.decode(buffer, length);
        buffer.compact();

        return packet;
    }

    private void readUntilAvailable(int bytesAvailable) throws IOException {
        while (buffer.position() < bytesAvailable) {
            if(source.read(buffer) == -1) {
                throw new EOFException();
            }
        }
    }

    @FunctionalInterface
    public interface Source {
        int read(ByteBuffer destination) throws IOException;
    }
}
