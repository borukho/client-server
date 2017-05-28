package ru.oxothuk.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.oxothuk.model.EndSessionRequest;
import ru.oxothuk.model.Request;
import ru.oxothuk.model.Response;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ClientHandler implements Runnable, ResponseCallback {
    private static Logger logger = LogManager.getLogger(ClientHandler.class);
    private Socket socket;
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;
    private ServiceCaller serviceCaller;
    private AtomicInteger requestCounter = new AtomicInteger();
    private boolean endOfSession;
    private final Lock writeLock = new ReentrantLock();

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
            ObjectInputStream inputStream = getInputStream();
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

    private synchronized ObjectInputStream getInputStream() throws IOException {
        if (inputStream == null) {
            inputStream = new ObjectInputStream(socket.getInputStream());
        }
        return inputStream;
    }

    @Override
    public void callback(Response response) {
        try {
            writeLock.lock();
            logger.info("response: {}", response);
            ObjectOutputStream outputStream = getOutputStream();
            outputStream.writeObject(response);
            outputStream.flush();
        } catch (IOException e) {
            logger.warn("error writing response", e);
        } finally {
            writeLock.unlock();
            requestCounter.decrementAndGet();
            tryToCloseSocket();
        }
    }

    private synchronized ObjectOutputStream getOutputStream() throws IOException {
        if (outputStream == null) {
            outputStream = new ObjectOutputStream(socket.getOutputStream());
        }
        return outputStream;
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
