package ru.oxothuk.server;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
public class ServerConfiguration {
    private String host;
    private Integer port;
    private List<ServiceConfiguration> serviceConfigurations;
}
