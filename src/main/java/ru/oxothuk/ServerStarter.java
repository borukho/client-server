package ru.oxothuk;

import org.apache.commons.cli.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.oxothuk.server.Server;
import ru.oxothuk.server.ServerConfiguration;
import ru.oxothuk.service.ServiceConfiguration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

public class ServerStarter {
    private static Logger logger = LogManager.getLogger(ServerStarter.class);

    public static void main(String... args) throws ParseException {
        logger.info("Starting server");
        Options options = createOptions();
        try {
            CommandLine commandLine = parseCommandLine(args, options);
            ServerConfiguration serverConfiguration = new ServerConfiguration()
                .setPort(Integer.parseInt(commandLine.getOptionValue("port")))
                .setServiceCallerThreadCount(Integer.parseInt(commandLine.getOptionValue("service-caller-threads", "1")))
                .setServiceConfigurations(loadServiceConfigurations(commandLine.getOptionValue("services-config")));
            new Server(serverConfiguration).start(false);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            printCommandLineArgumentsHelp(options);
        } catch (IOException | InterruptedException e) {
            System.out.println(e.getMessage());
        }
    }

    private static Options createOptions() {
        Options options = new Options();
        options.addOption(Option.builder().longOpt("port").required()
            .hasArg().argName("port").type(Integer.class)
            .desc("server port")
            .build());
        options.addOption(Option.builder().longOpt("service-caller-threads").required()
            .hasArg().argName("threads amount").type(Integer.class)
            .desc("threads amount for service caller")
            .build());
        options.addOption(Option.builder().longOpt("services-config").required()
            .hasArg().argName("file")
            .desc("services config file location")
            .build());
        return options;
    }

    private static CommandLine parseCommandLine(String[] args, Options options) throws ParseException {
        CommandLineParser parser = new DefaultParser();
        return parser.parse(options, args);
    }

    private static List<ServiceConfiguration> loadServiceConfigurations(String fileLocation) throws IOException {
        Properties properties = new Properties();
        try (FileInputStream inputStream = new FileInputStream(new File(fileLocation))) {
            properties.load(inputStream);
        }
        return properties.entrySet().stream()
            .map(e -> new ServiceConfiguration()
                .setName((String) e.getKey())
                .setServiceClass((String) e.getValue())
            )
            .collect(Collectors.toList());
    }

    private static void printCommandLineArgumentsHelp(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(ServerStarter.class.getName(), options);
    }

}
