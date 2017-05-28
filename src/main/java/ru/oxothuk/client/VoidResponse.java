package ru.oxothuk.client;

import lombok.ToString;

@ToString(callSuper = true)
public class VoidResponse extends Response {
    @Override
    public Object getResult() {
        return new VoidResult();
    }
}
