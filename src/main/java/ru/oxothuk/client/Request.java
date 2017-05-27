package ru.oxothuk.client;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@ToString
public class Request implements Serializable {
    private Integer id;
    private String serviceName;
    private String methodName;
    private List<Object> parameters;
}
