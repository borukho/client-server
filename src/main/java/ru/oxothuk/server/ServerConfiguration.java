package ru.oxothuk.server;

import lombok.Getter;

import java.util.Map;

@Getter
public class ServerConfiguration {
    private String host;
    private int port;
    private Map<String, Service> services;

    public ServerConfiguration loadFromCommandLineArguments(String... args) throws CommandLineArgumentsConfigurationException {
        //todo load configuration from command line arguments
        return this;
    }

    public ServerConfiguration loadFromProperties(String propertyFilePath) throws PropertiesFileConfigurationException {
        //todo load configuration from properties file
        return this;
    }
}
