package ru.oxothuk.server;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class ServiceConfiguration {
    private String name;
    private String serviceClass;
}
