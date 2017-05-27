package ru.oxothuk;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.oxothuk.server.CommandLineArgumentsConfigurationException;
import ru.oxothuk.server.PropertiesFileConfigurationException;
import ru.oxothuk.server.Server;
import ru.oxothuk.server.ServerConfiguration;

public class ServerStarter {
    private static Logger logger = LogManager.getLogger(ServerStarter.class);

    public static void main(String... args) {
        logger.info("Starting server");
        try {
            ServerConfiguration serverConfiguration = new ServerConfiguration()
                    .loadFromCommandLineArguments(args)
                    .loadFromProperties(System.getProperty("server.configuration", "server.properties"));
            new Server(serverConfiguration).start();
        } catch (CommandLineArgumentsConfigurationException e) {
            logger.error("invalid command line argument");
            printCommandLineArgumentsHelp();
        } catch (PropertiesFileConfigurationException e) {
            logger.error("invalid properties configuration");
        }
    }

    private static void printCommandLineArgumentsHelp() {
        String help = ""; //todo command line arguments help
        System.out.println(help);
    }

}
