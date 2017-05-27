package ru.oxothuk.client;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Getter
@Setter
@Accessors(chain = true)
public class Response implements Serializable {
    private Boolean success;
    private Throwable exception;
    private Object result;
}
