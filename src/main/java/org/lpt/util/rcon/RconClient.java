package org.lpt.util.rcon;

import org.lpt.util.Config;
import org.lpt.util.encryption.AES;
import org.lpt.util.rcon.packet.PacketCodec;
import org.lpt.util.rcon.packet.PacketType;

import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

import static org.lpt.util.Util.LOGGER;

public class RconClient extends Rcon {
    public static RconClient connect(String hostname, int port) throws NullPointerException {
        try {
            SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress(hostname, port));
            return new RconClient(socketChannel);
        } catch (Exception e) {
            LOGGER.error("Can't connect to {}:{}: {}", hostname, port, e.getMessage());
        }

        return null;
    }

    private RconClient(SocketChannel socketChannel) throws Exception {
        super(socketChannel, Config.S2C_BYTES, Config.C2S_BYTES, new PacketCodec(Config.CHARSET));
        aesKey = AES.generateKey();
    }

    public void authenticate(String password) throws Exception {
        if (!channel.isOpen()) return;

        write(PacketType.AUTH, "");
        importRsaKey();
        sendRsaKey();
        sendAesKey();

        write(PacketType.AUTH, "");
        writeEncrypted(password);
        try {
            read(PacketType.AUTH);
        } catch (ConnectionClosedException e) {
            LOGGER.error("Password rejected");
            close();
        }
    }

    public void sendCommand(String command) {
        if (!channel.isOpen()) return;

        try {
            writeEncrypted(command);
        } catch (Exception e) {
            LOGGER.error("Could not send command: {}", e.getMessage());
            return;
        }

        String line = "";
        while (!line.equals("\0")) {
            LOGGER.info(line);
            try {
                line = readEncrypted();
            } catch (Exception e) {
                LOGGER.error("Could not read command output: {}", e.getMessage());
                return;
            }
        }
    }
}
