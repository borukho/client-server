package ru.oxothuk.client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.oxothuk.model.EndSessionRequest;
import ru.oxothuk.model.Request;
import ru.oxothuk.model.Response;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Client implements AutoCloseable {
    private static final long TIMEOUT = 30000L;
    private static Logger logger = LogManager.getLogger(Client.class);
    private final Socket socket;
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;

    private final Map<Integer, BlockingQueue<Response>> responses = Collections.synchronizedMap(new HashMap<>());
    private final AtomicInteger counter = new AtomicInteger();
    private final Lock readLock = new ReentrantLock();
    private final Lock writeLock = new ReentrantLock();

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

        writeRequest(request);
        readResponse();
        Response response = getResponse(request.getId());
        return getRequestResult(response);
    }

    private void writeRequest(Request request) throws ClientException {
        try {
            writeLock.lock();
            logger.debug("writing request");
            ObjectOutputStream outputStream = getOutputStream();
            outputStream.writeObject(request);
            outputStream.flush();
            responses.put(request.getId(), new LinkedTransferQueue<>());
        } catch (Exception e) {
            throw new ClientException(e);
        } finally {
            logger.debug("request wrote");
            writeLock.unlock();
        }
    }

    private synchronized ObjectOutputStream getOutputStream() throws IOException {
        if (outputStream == null) {
            outputStream = new ObjectOutputStream(socket.getOutputStream());
        }
        return outputStream;
    }

    private void readResponse() throws ClientException {
        try {
            readLock.lock();
            logger.debug("reading response");
            Object o = getInputStream().readObject();
            if (o instanceof Response) {
                Response response = (Response) o;
                logger.debug("got response: {}", response);
                BlockingQueue<Response> queue = responses.get(response.getId());
                if (queue != null) queue.add(response);
            } else {
                throw new ClientException("unknown response: " + o);
            }
        } catch (ClassNotFoundException e) {
            throw new ClientException("unknown response");
        } catch (ClientException e) {
            throw e;
        } catch (Exception e) {
            throw new ClientException(e);
        } finally {
            readLock.unlock();
            logger.debug("response read");
        }
    }

    private synchronized ObjectInputStream getInputStream() throws IOException {
        if (inputStream == null) {
            inputStream = new ObjectInputStream(socket.getInputStream());
        }
        return inputStream;
    }

    private Object getRequestResult(Response response) throws ClientException {
        logger.info("response: {}", response);
        if (response.getSuccess() == null || !response.getSuccess()) {
            Throwable cause = response.getException();
            if (cause != null) {
                throw new ClientException(cause);
            } else {
                throw new ClientException("unsuccessful response");
            }
        }
        return response.getResult();
    }

    private Response getResponse(Integer id) throws ClientException {
        Response response;
        try {
            response = responses.get(id).poll(TIMEOUT, TimeUnit.MILLISECONDS);
            if (response == null) {
                throw new ClientException("timeout reached while waiting for response");
            }
        } catch (InterruptedException e) {
            throw new ClientException(e);
        } finally {
            responses.remove(id);
        }
        return response;
    }

    @Override
    public void close() throws IOException {
        try {
            writeLock.lock();
            logger.debug("closing client");
            ObjectOutputStream outputStream = getOutputStream();
            outputStream.writeObject(new EndSessionRequest());
            outputStream.flush();
        } finally {
            writeLock.unlock();
            socket.close();
        }
    }
}
