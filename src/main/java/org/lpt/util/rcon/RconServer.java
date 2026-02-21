package org.lpt.util.rcon;

import org.lpt.util.Config;
import org.lpt.util.rcon.packet.PacketCodec;
import org.lpt.util.rcon.packet.PacketType;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.lpt.util.Util.LOGGER;

public class RconServer {
    private final int port;
    private final String password;

    private ServerSocketChannel server;
    private final ExecutorService clientPool = Executors.newCachedThreadPool();
    private volatile boolean running = false;

    private final MessageHandler handler;

    public RconServer(int port, String password, MessageHandler handler) {
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
                        LOGGER.error("Client error: " + e);
                    }
                });
            } catch (AsynchronousCloseException ignored) {
            } catch (IOException e) {
                LOGGER.error("Accept error: " + e);
            }
        }
    }

    private class ClientHandler extends Rcon {
        SocketChannel client;

        public ClientHandler(SocketChannel client) throws Exception {
            super(client, Config.C2S_BYTES, Config.S2C_BYTES, new PacketCodec(Config.CHARSET));

            this.client = client;
        }

        void handle() throws Exception {
            read(PacketType.AUTH);
            sendRsaKey();
            importRsaKey();
            importAesKey();

            read(PacketType.AUTH);
            if (!readEncrypted().equals(password)) {
                LOGGER.error("Wrong password from: " + client.getRemoteAddress());
                close();
                return;
            }
            write(PacketType.AUTH, "");

            while (running) {
                String message;
                try {
                    message = readEncrypted();
                } catch (ConnectionClosedException e) {
                    LOGGER.error(e.getMessage());
                    break;
                }
                String[] response = handler.handleMessage(message);

                for (String line : response) {
                    writeEncrypted(line);
                }

                writeEncrypted("\0");
            }

            close();
        }
    }
}
