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
    private static final long serialVersionUID = -7812063207400096667L;

    private Integer id;
    private boolean success;
    private Throwable exception;
    private Object result;
}
