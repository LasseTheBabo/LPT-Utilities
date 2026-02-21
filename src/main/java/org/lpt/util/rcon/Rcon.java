package org.lpt.util.rcon;

import org.lpt.util.encryption.AES;
import org.lpt.util.encryption.RSA;
import org.lpt.util.rcon.packet.*;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.EOFException;
import java.io.IOException;
import java.nio.channels.ByteChannel;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.Base64;

import static org.lpt.util.Util.LOGGER;

public class Rcon {
    private final PacketReader reader;
    private final PacketWriter writer;

    protected SecretKey aesKey;
    protected KeyPair localKey;
    protected PublicKey remoteKey;

    protected final ByteChannel channel;

    protected Rcon(ByteChannel channel, int readBufferCapacity, int writeBufferCapacity, PacketCodec codec) throws Exception {
        reader = new PacketReader(channel::read, readBufferCapacity, codec);
        writer = new PacketWriter(channel::write, writeBufferCapacity, codec);

        this.channel = channel;
        this.localKey = RSA.generate();
    }

    protected void write(int type, String payload) throws ConnectionClosedException {
        ensureConnected();
        try {
            writer.write(new Packet(0, type, payload));
        } catch (IOException e) {
            throw new ConnectionClosedException("Connection closed from other side", e);
        }
    }

    protected Packet read(int type) throws ConnectionClosedException {
        ensureConnected();
        final Packet response;

        try {
            response = reader.read();
        } catch (EOFException e) {
            throw new ConnectionClosedException("Connection closed from other side", e);
        } catch (IOException e) {
            throw new ConnectionClosedException("Read failed", e);
        }

        if (!response.isValid()) {
            throw new ConnectionClosedException("Invalid packet received");
        }

        if (response.type != type) {
            throw new ConnectionClosedException("Expected packet type " + type + " but got " + response.type);
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
        Packet ivPacket = read(PacketType.IV);
        byte[] iv = Base64.getDecoder().decode(ivPacket.payload);

        Packet encryptedPacket = read(PacketType.ENCRYPTED);
        byte[] encryptedMessage = Base64.getDecoder().decode(encryptedPacket.payload);
        byte[] decryptedMessage = AES.decrypt(encryptedMessage, aesKey, iv);

        return new String(decryptedMessage);
    }

    protected void sendRsaKey() {
        try {
            byte[] publicKey = localKey.getPublic().getEncoded();
            write(PacketType.RSA, Base64.getEncoder().encodeToString(publicKey));
        } catch (Exception e) {
            LOGGER.error("Error while sending RSA key: " + e);
        }
    }

    protected void importRsaKey() {
        try {
            Packet rsaPacket = read(PacketType.RSA);
            remoteKey = RSA.importKey(rsaPacket.payload);
        } catch (Exception e) {
            LOGGER.error("Error while receiving RSA key: " + e);
        }
    }

    protected void sendAesKey() {
        try {
            byte[] encryptedAesKey = RSA.encrypt(aesKey.getEncoded(), remoteKey);
            write(PacketType.AES, Base64.getEncoder().encodeToString(encryptedAesKey));
        } catch (Exception e) {
            LOGGER.error("Error while sending AES key: " + e);
        }
    }

    protected void importAesKey() {
        try {
            Packet aesPacket = read(PacketType.AES);
            byte[] encryptedAesKey = Base64.getDecoder().decode(aesPacket.payload);
            byte[] decryptedAesKey = RSA.decrypt(encryptedAesKey, localKey.getPrivate());
            aesKey = new SecretKeySpec(decryptedAesKey, "AES");
        } catch (Exception e) {
            LOGGER.error("Error while receiving AES key: " + e);
        }
    }

    public void close() throws ConnectionClosedException {
        if (!channel.isOpen()) return;

        LOGGER.info("Connection closed");
        try {
            channel.close();
        } catch (IOException ignored) {}
    }

    private void ensureConnected() throws ConnectionClosedException {
        if (!channel.isOpen()) {
            throw new ConnectionClosedException("Connection is closed");
        }
    }
}
