package ru.oxothuk.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.oxothuk.client.EndSessionRequest;
import ru.oxothuk.client.Request;
import ru.oxothuk.client.Response;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Optional;

public class ClientHandler implements Runnable, ResponseCallback {
    private static Logger logger = LogManager.getLogger(ClientHandler.class);
    private Socket socket;
    private ServiceCaller serviceCaller;

    ClientHandler(Socket socket, ServiceCaller serviceCaller) {
        this.socket = socket;
        this.serviceCaller = serviceCaller;
    }

    @Override
    public void run() {
        try {
            logger.info("handling client session from " + socket.getInetAddress());
            boolean endOfSession = false;
            while (!endOfSession) {
                Optional<Request> request = readRequest();
                logger.info("handling client request");
                if (request.isPresent()) {
                    serviceCaller.call(request.get(), this);
                } else {
                    endOfSession = true;
                }
            }
        } finally {
            logger.info("session with {} completed", socket.getInetAddress());
        }
    }

    private Optional<Request> readRequest() {
        try {
            ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
            Object o = inputStream.readObject();
            if (o instanceof Request) {
                return Optional.of((Request) o);
            } else if (o instanceof EndSessionRequest) {
                return Optional.empty();
            }
        } catch (IOException | ClassNotFoundException e) {
            logger.warn("error getting request", e);
        }
        return Optional.empty();
    }

    @Override
    public void callback(Response response) {
        try {
            ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
            outputStream.reset();
            outputStream.writeObject(response);
            outputStream.flush();
        } catch (IOException e) {
            logger.warn("error writing response", e);
        }
    }

    private void close(Socket socket) {
        try {
            logger.info("closing socket: {}", socket);
            socket.close();
        } catch (IOException e) {
            logger.error("Error closing socket", e);
        }
    }
}
