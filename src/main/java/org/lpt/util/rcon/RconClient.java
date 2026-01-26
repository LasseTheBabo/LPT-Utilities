package org.lpt.util.rcon;

import org.lpt.util.Config;
import org.lpt.util.encryption.AES;
import org.lpt.util.rcon.packet.PacketCodec;
import org.lpt.util.rcon.packet.PacketType;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

import static org.lpt.util.Util.LOGGER;

public class RconClient extends Rcon implements AutoCloseable {
    public static RconClient connect(String hostname, int port) throws Exception {
        SocketAddress remote = new InetSocketAddress(hostname, port);
        SocketChannel socketChannel = SocketChannel.open(remote);

        return new RconClient(socketChannel);
    }

    private RconClient(SocketChannel socketChannel) throws Exception {
        super(
                socketChannel,
                Config.S2C_BYTES,
                Config.C2S_BYTES,
                new PacketCodec(Config.CHARSET)
        );

        aesKey = AES.generateKey();
    }

    public boolean authenticate(String password) throws Exception {
        if (!channel.isOpen()) return false;

        write(PacketType.AUTH, "");
        importRsaKey();
        sendRsaKey();
        sendAesKey();

        write(PacketType.AUTH, "");
        writeEncrypted(password);
        try {
            read(PacketType.AUTH);
            return true;
        } catch (ConnectionClosedException e) {
            LOGGER.error("Password rejected");
            close();
        }

        return false;
    }

    public List<String> sendCommand(String command) {
        List<String> output = new ArrayList<>();

        if (!channel.isOpen()) return output;

        try {
            writeEncrypted(command);
        } catch (Exception e) {
            output.add("Could not send command: " + e.getMessage());
            return output;
        }

        String line = "";
        while (!line.equals("\0")) {
            output.add(line);
            try {
                line = readEncrypted();
            } catch (Exception e) {
                output.add("Could not read command output: " + e.getMessage());
                return output;
            }
        }

        return output;
    }
}
