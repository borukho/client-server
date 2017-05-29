package ru.oxothuk.service;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ServiceCallResult {
    private Object value;
    private boolean isVoid;
}
