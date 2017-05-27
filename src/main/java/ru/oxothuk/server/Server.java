package ru.oxothuk.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Server {
    private static Logger logger = LogManager.getLogger(Server.class);
    private ServerConfiguration configuration;

    public Server(ServerConfiguration configuration) {
        this.configuration = configuration;
    }

    public void start() {
        //todo start server
    }

}
