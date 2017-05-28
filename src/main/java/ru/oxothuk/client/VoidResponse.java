package ru.oxothuk.client;

import lombok.ToString;

@ToString(callSuper = true)
public class VoidResponse extends Response {
    private static final long serialVersionUID = -8302008564800019011L;

    @Override
    public Object getResult() {
        return new VoidResult();
    }
}
