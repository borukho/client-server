package ru.oxothuk.client;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Getter
@Setter
@ToString
@Accessors(chain = true)
public class Response implements Serializable {
    private Integer id;
    private boolean success;
    private Throwable exception;
    private Object result;
}
