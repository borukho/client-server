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
    private static final long serialVersionUID = -4864012918432335074L;

    private Integer id;
    private Boolean success;
    private Throwable exception;
    private Object result;
}
