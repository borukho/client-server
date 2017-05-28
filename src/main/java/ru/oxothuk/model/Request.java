package ru.oxothuk.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Getter
@Setter
@Accessors(chain = true)
@ToString
public class Request implements Serializable {
    private static final long serialVersionUID = 8404332301010677214L;

    private Integer id;
    private String serviceName;
    private String methodName;
    private Object[] parameters;
}
