package org.lpt.rcon;

import org.lpt.Config;
import org.lpt.encryption.RSA;
import org.lpt.rcon.packet.Packet;
import org.lpt.rcon.packet.PacketCodec;
import org.lpt.rcon.packet.PacketType;

import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Base64;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.lpt.Util.LOGGER;

public class Server {
    private final int port;
    private final String password;

    private ServerSocketChannel server;
    private final ExecutorService clientPool = Executors.newCachedThreadPool();
    private volatile boolean running = false;

    private final MessageHandler handler;

    public Server(int port, String password, MessageHandler handler) {
        this.port = port;
        this.password = password;
        this.handler = handler;
    }

    public void open() throws IOException {
        if (running)
            return;
        running = true;

        server = ServerSocketChannel.open();
        server.bind(new InetSocketAddress(port));

        Thread acceptThread = new Thread(this::acceptLoop);
        acceptThread.start();
    }

    public void close() throws IOException {
        if (!running)
            return;
        running = false;

        server.close();
        clientPool.shutdown();
    }

    private void acceptLoop() {
        while (running) {
            try {
                SocketChannel client = server.accept();
                clientPool.submit(() -> {
                    try {
                        new ClientHandler(client).handle();
                    } catch (Exception e) {
                        LOGGER.error("Client error: {}", e.getMessage());
                    }
                });
            } catch (IOException e) {
                LOGGER.error("Accept error: {}", e.getMessage());
            }
        }
    }

    private class ClientHandler extends Rcon {
        SocketChannel client;

        public ClientHandler(SocketChannel client) throws Exception {
            super(client, Config.C2S_BYTES, Config.S2C_BYTES, new PacketCodec(Config.CHARSET), 0);

            this.client = client;
        }

        public void handle() throws Exception {
            readExpected(PacketType.AUTH);

            byte[] publicKey = localKey.getPublic().getEncoded();
            write(PacketType.RSA, Base64.getEncoder().encodeToString(publicKey));

            Packet rsaPacket = readExpected(PacketType.RSA);
            remoteKey = RSA.importKey(rsaPacket.payload);

            Packet aesPacket = readExpected(PacketType.AES);
            byte[] encryptedAesKey = Base64.getDecoder().decode(aesPacket.payload);
            byte[] decryptedAesKey = RSA.decrypt(encryptedAesKey, localKey.getPrivate());
            aesKey = new SecretKeySpec(decryptedAesKey, "AES");

            String pw = readEncrypted();
            if (!pw.equals(password)) {
                LOGGER.error("Wrong password from: {}", client.getLocalAddress());
                close();
                return;
            }

            write(PacketType.AUTH_RESPONSE);

            while (true) {
                try {
                    String message = readEncrypted();
                    handler.handleMessage(message);
                } catch (Exception e) {
                    LOGGER.error("Error reading message: {}", e.getMessage());
                    break;
                }
            }

            close();
        }
    }
}
