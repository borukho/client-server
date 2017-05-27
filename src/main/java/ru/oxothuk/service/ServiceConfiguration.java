package ru.oxothuk.service;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
@ToString
public class ServiceConfiguration {
    private String name;
    private String serviceClass;
}
