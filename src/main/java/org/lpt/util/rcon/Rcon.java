package org.lpt.util.rcon;

import org.lpt.util.encryption.AES;
import org.lpt.util.encryption.RSA;
import org.lpt.util.rcon.packet.*;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
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

    protected void write(int type, String payload) throws IOException {
        try {
            writer.write(new Packet(0, type, payload));
        } catch (Exception ignored) { handleClosedConnection(); }
    }

    protected Packet read(int type) throws IOException {
        try {
            Packet response = reader.read();
            if ((response.type != type) && response.isValid()) {
                LOGGER.error("Received packet type is {}. Expected: {}", response.type, type);
                close();
            }
            return response;
        } catch (Exception ignored) { handleClosedConnection(); }

        return null;
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

    protected void sendRsaKey() throws IOException {
        byte[] publicKey = localKey.getPublic().getEncoded();
        write(PacketType.RSA, Base64.getEncoder().encodeToString(publicKey));
    }

    protected void importRsaKey() throws Exception {
        Packet rsaPacket = read(PacketType.RSA);
        remoteKey = RSA.importKey(rsaPacket.payload);
    }

    protected void sendAesKey() throws Exception {
        byte[] encryptedAesKey = RSA.encrypt(aesKey.getEncoded(), remoteKey);
        write(PacketType.AES, Base64.getEncoder().encodeToString(encryptedAesKey));
    }

    protected void importAesKey() throws Exception {
        Packet aesPacket = read(PacketType.AES);
        byte[] encryptedAesKey = Base64.getDecoder().decode(aesPacket.payload);
        byte[] decryptedAesKey = RSA.decrypt(encryptedAesKey, localKey.getPrivate());
        aesKey = new SecretKeySpec(decryptedAesKey, "AES");
    }

    private void handleClosedConnection() throws IOException {
        LOGGER.info("Connection closed from other side");
        close();
    }

    public void close() throws IOException {
        LOGGER.info("Closing connection");
        channel.close();
        System.exit(0);
    }
}
