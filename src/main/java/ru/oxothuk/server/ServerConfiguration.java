package ru.oxothuk.server;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import ru.oxothuk.service.ServiceConfiguration;

import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
@ToString
public class ServerConfiguration {
    private int port;
    private List<ServiceConfiguration> serviceConfigurations;
}
