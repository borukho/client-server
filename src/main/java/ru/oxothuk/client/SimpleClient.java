package ru.oxothuk.client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;

public class SimpleClient implements Client, Closeable {
    private static Logger logger = LogManager.getLogger(SimpleClient.class);
    private final Socket socket;
    private static AtomicInteger counter = new AtomicInteger();

    public SimpleClient(String hostname, int port) throws IOException {
        socket = new Socket(hostname, port);
    }

    @Override
    public Object remoteCall(String serviceName, String methodName, Object[] parameters) throws ClientException {
        Request request = new Request()
            .setId(counter.incrementAndGet())
            .setServiceName(serviceName)
            .setMethodName(methodName)
            .setParameters(parameters);
        logger.info("request: {}", request);
        try {
            ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
            outputStream.writeObject(request
            );
            outputStream.flush();
            ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
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
        } catch (IOException e) {
            throw new ClientException(e);
        } catch (ClassNotFoundException e) {
            throw new ClientException("unknown response");
        }
    }

    @Override
    public void close() throws IOException {
        socket.close();
    }
}
