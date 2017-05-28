package ru.oxothuk.client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Client implements AutoCloseable {
    private static Logger logger = LogManager.getLogger(Client.class);
    private static final int TIMEOUT = 30000;

    private final Socket socket;
    private final AtomicInteger counter = new AtomicInteger();
    private final Lock lock = new ReentrantLock();

    public Client(String hostname, int port) throws IOException {
        socket = new Socket(hostname, port);
    }

    public Object remoteCall(String serviceName, String methodName, Object[] parameters) throws ClientException {
        Request request = new Request()
            .setId(counter.incrementAndGet())
            .setServiceName(serviceName)
            .setMethodName(methodName)
            .setParameters(parameters);
        logger.info("request: {}", request);
        boolean locked = false;
        try {
            locked = lock.tryLock(TIMEOUT, TimeUnit.MILLISECONDS);
            if (!locked) {
                throw new ClientException("resource await timeout reached");
            }
            ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
            logger.debug("writing request");
            outputStream.reset();
            outputStream.writeObject(request);
            outputStream.flush();
            ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
            logger.debug("reading response");
            Object o = inputStream.readObject();
            if (o instanceof Response) {
                Response response = (Response) o;
                if (response.getSuccess() == null || !response.getSuccess()) {
                    logger.info("response: {}", response);
                    Throwable cause = response.getException();
                    if (cause != null) {
                        throw new ClientException(cause);
                    } else {
                        throw new ClientException("unsuccessful response");
                    }
                }
                return response.getResult();
            } else {
                throw new ClientException("unknown response");
            }
        } catch (ClassNotFoundException e) {
            throw new ClientException("unknown response");
        } catch (ClientException e) {
            throw e;
        } catch (Exception e) {
            throw new ClientException(e);
        } finally {
            if (locked) lock.unlock();
        }
    }

    @Override
    public void close() throws IOException {
        ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
        outputStream.writeObject(new EndSessionRequest());
        outputStream.flush();
        socket.close();
    }
}
