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
import java.util.concurrent.atomic.AtomicInteger;

public class ClientHandler implements Runnable, ResponseCallback {
    private static Logger logger = LogManager.getLogger(ClientHandler.class);
    private Socket socket;
    private ServiceCaller serviceCaller;
    private AtomicInteger requestCounter = new AtomicInteger();
    private boolean endOfSession;

    ClientHandler(Socket socket, ServiceCaller serviceCaller) {
        this.socket = socket;
        this.serviceCaller = serviceCaller;
    }

    @Override
    public void run() {
        try {
            logger.info("handling client session from " + socket.getInetAddress());
            while (!endOfSession) {
                Optional<Request> optionalRequest = readRequest();
                if (optionalRequest.isPresent()) {
                    Request request = optionalRequest.get();
                    logger.info("request: {}", request);
                    requestCounter.incrementAndGet();
                    serviceCaller.call(request, this);
                } else {
                    logger.debug("got end of session event");
                    endOfSession = true;
                    tryToCloseSocket();
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
            logger.info("response: {}", response);
            ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
            outputStream.reset();
            outputStream.writeObject(response);
            outputStream.flush();
        } catch (IOException e) {
            logger.warn("error writing response", e);
        } finally {
            requestCounter.decrementAndGet();
            tryToCloseSocket();
        }
    }

    private void tryToCloseSocket() {
        if (endOfSession && requestCounter.get() == 0) {
            try {
                logger.debug("closing socket: {}", socket);
                socket.close();
            } catch (IOException e) {
                logger.error("Error closing socket", e);
            }
        }
    }
}
