package ru.oxothuk.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.oxothuk.client.Request;
import ru.oxothuk.client.Response;
import ru.oxothuk.service.Service;
import ru.oxothuk.service.ServiceLocator;

import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.Optional;

public class ClientHandler implements Runnable {
    private static Logger logger = LogManager.getLogger(ClientHandler.class);
    private Socket socket;
    private ServiceLocator serviceLocator;

    public ClientHandler(Socket socket, ServiceLocator serviceLocator) {
        this.socket = socket;
        this.serviceLocator = serviceLocator;
    }

    @Override
    public void run() {
        try {
            logger.info("handling client request from " + socket.getInetAddress());
            Optional<Request> optionalRequest = readRequest();
            if (optionalRequest.isPresent()) {
                handleRequest(optionalRequest.get());
            }
            //todo handle requests in thread pool
        } catch (IOException e) {
            logger.warn("Error processing client request", e);
        } finally {
            logger.info("request handling completed");
            close(socket);
        }

    }

    private void handleRequest(Request request) throws IOException {
        logger.info("handling request: " + request);
        Optional<Service> service = serviceLocator.getServiceByName(request.getServiceName());
        if (!service.isPresent()) {
            error("service " + request.getServiceName() + " not found");
        } else {
            Response response = processServiceCall(service.get(), request.getMethodName(), request.getParameters());
            writeResponse(response);
        }
    }

    private Optional<Request> readRequest() {
        try {
            ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
            Object o = inputStream.readObject();
            if (o instanceof Request) {
                return Optional.of((Request) o);
            }
        } catch (IOException | ClassNotFoundException e) {
            logger.warn("error getting request", e);
        }
        return Optional.empty();
    }

    private void writeResponse(Response response) throws IOException {
        ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
        outputStream.writeObject(response);
        outputStream.flush();
    }

    private void error(String message) {
        logger.warn(message);
    }

    private Response processServiceCall(Service service, String methodName, List<Object> parameters) {
        //todo service call
        return new Response().setSuccess(true);
    }

    private void close(Socket socket) {
        try {
            socket.close();
        } catch (IOException e) {
            logger.error("Error closing socket", e);
        }
    }
}
