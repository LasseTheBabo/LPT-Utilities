package org.lpt.rcon;

import org.lpt.Config;
import org.lpt.encryption.AES;
import org.lpt.encryption.RSA;
import org.lpt.rcon.packet.Packet;
import org.lpt.rcon.packet.PacketCodec;
import org.lpt.rcon.packet.PacketType;

import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.Base64;

import static org.lpt.Util.LOGGER;

public class RconClient extends Rcon {
    private boolean connected = false;

    public RconClient(String hostname, int port) throws Exception {
        super(SocketChannel.open(new InetSocketAddress(hostname, port)), Config.S2C_BYTES, Config.C2S_BYTES, new PacketCodec(Config.CHARSET), 1);

        aesKey = AES.generateKey();
    }

    public void connect(String password) throws Exception {
        if (connected)
            return;
        connected = true;

        write(PacketType.AUTH);

        Packet rsaPacket = readExpected(PacketType.RSA);
        remoteKey = RSA.importKey(rsaPacket.payload);

        byte[] publicKey = localKey.getPublic().getEncoded();
        write(PacketType.RSA, Base64.getEncoder().encodeToString(publicKey));

        byte[] encryptedAesKey = RSA.encrypt(aesKey.getEncoded(), remoteKey);
        write(PacketType.AES, Base64.getEncoder().encodeToString(encryptedAesKey));

        writeEncrypted(password);

        readExpected(PacketType.AUTH_RESPONSE);
    }

    public void sendCommand(String command) throws Exception {
        writeEncrypted(command);
        String line = "";
        while (!line.equals("\0")) {
            LOGGER.info(line);
            line = readEncrypted();
        }
    }
}
