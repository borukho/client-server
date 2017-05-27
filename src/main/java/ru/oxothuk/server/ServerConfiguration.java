package ru.oxothuk.server;

import lombok.Getter;

import java.util.List;

@Getter
public class ServerConfiguration {
    private String host = "localhost";
    private Integer port;
    private List<ServiceConfiguration> serviceConfigurations;

    public ServerConfiguration loadFromCommandLineArguments(String... args) throws CommandLineArgumentsConfigurationException {
        if (args.length < 2) throw new CommandLineArgumentsConfigurationException("Wrong argument count");
        for (int i = 0; i < args.length; i++) {
            if ("--host".equals(args[i])) {
                i++;
                if (i >= args.length) {
                    throw new CommandLineArgumentsConfigurationException("Wrong argument count at --host");
                }
                host = args[i];
            } else if ("--port".equals(args[i])) {
                i++;
                if (i >= args.length) {
                    throw new CommandLineArgumentsConfigurationException("Wrong argument count at --port");
                }
                String arg = args[i];
                if (!arg.matches("\\d+")) {
                    throw new CommandLineArgumentsConfigurationException("Port should be a number: " + arg);
                }
                port = Integer.parseInt(arg);
            }
        }
        if (port == null) {
            throw new CommandLineArgumentsConfigurationException("Port number is not set");
        }
        return this;
    }

    public ServerConfiguration loadFromProperties(String propertyFilePath) throws PropertiesFileConfigurationException {
        //todo load configuration from properties file
        return this;
    }
}
