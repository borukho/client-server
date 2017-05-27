package ru.oxothuk.server;

import lombok.Getter;

import java.util.List;

@Getter
public class ServerConfiguration {
    private String host;
    private int port;
    private List<ServiceConfiguration> serviceConfigurations;

    public ServerConfiguration loadFromCommandLineArguments(String... args) throws CommandLineArgumentsConfigurationException {
        //todo load configuration from command line arguments
        return this;
    }

    public ServerConfiguration loadFromProperties(String propertyFilePath) throws PropertiesFileConfigurationException {
        //todo load configuration from properties file
        return this;
    }
}
