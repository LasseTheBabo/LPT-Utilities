package org.lpt.rcon;

import org.lpt.encryption.AES;
import org.lpt.encryption.RSA;
import org.lpt.rcon.packet.*;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.channels.ByteChannel;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.Base64;

import static org.lpt.Util.LOGGER;

public class Rcon {
    final private PacketReader reader;
    final private PacketWriter writer;
    final private int id;

    protected SecretKey aesKey;
    protected KeyPair localKey;
    protected PublicKey remoteKey;

    protected final ByteChannel channel;

    protected Rcon(ByteChannel channel, int readBufferCapacity, int writeBufferCapacity, PacketCodec codec, int id) throws Exception {
        reader = new PacketReader(channel::read, readBufferCapacity, codec);
        writer = new PacketWriter(channel::write, writeBufferCapacity, codec);

        this.id = id;
        this.channel = channel;
        localKey = RSA.generate();
    }

    protected void write(int packetType, String payload) throws IOException {
        writer.write(new Packet(id, packetType, payload));
    }

    protected void write(int packetType) throws IOException {
        writer.write(new Packet(id, packetType));
    }

    protected Packet read() throws IOException {
        return reader.read();
    }

    protected Packet readExpected(int packetType) throws IOException {
        Packet response = read();
        if (response.type != packetType) {
            LOGGER.error("Received packet type is {}. Expected: {}", response.type, packetType);
            close();
        }

        return response;
    }

    protected void writeEncrypted(String payload) throws Exception {
        byte[] iv = AES.generateIV();
        write(PacketType.IV, Base64.getEncoder().encodeToString(iv));

        byte[] encryptedMessage = AES.encrypt(payload.getBytes(), aesKey, iv);
        write(PacketType.ENCRYPTED, Base64.getEncoder().encodeToString(encryptedMessage));
    }

    protected String readEncrypted() throws Exception {
        Packet ivPacket = readExpected(PacketType.IV);
        byte[] iv = Base64.getDecoder().decode(ivPacket.payload);

        Packet encryptedPacket = readExpected(PacketType.ENCRYPTED);
        byte[] encryptedMessage = Base64.getDecoder().decode(encryptedPacket.payload);
        byte[] decryptedMessage = AES.decrypt(encryptedMessage, aesKey, iv);

        return new String(decryptedMessage);
    }

    public void close() throws IOException {
        LOGGER.debug("Closing connection");
        channel.close();
    }
}
