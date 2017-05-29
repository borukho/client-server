package ru.oxothuk.service;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.stream.Stream;

public class Service {
    private ServiceConfiguration configuration;
    private final Object target;

    public Service(ServiceConfiguration configuration) {
        this.configuration = configuration;
        try {
            target = Class.forName(configuration.getServiceClass()).newInstance();
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            throw new RuntimeException(e);
        }
    }

    public String getName() {
        return configuration.getName();
    }

    ServiceCallResult call(String methodName, Object[] parameters) throws ServiceException {
        ServiceCallResult serviceCallResult = new ServiceCallResult();
        boolean hasMethodWithSameName = Stream.of(target.getClass().getMethods())
            .anyMatch(method -> method.getName().equals(methodName));
        if (!hasMethodWithSameName) {
            throw new ServiceException("Method " + methodName + " not found");
        }
        try {
            Class[] parameterClasses = Stream.of(parameters)
                .map(Object::getClass)
                .toArray(Class[]::new);
            Method method = target.getClass().getMethod(methodName, parameterClasses);
            serviceCallResult.setValue(method.invoke(target, parameters));
            serviceCallResult.setVoid(method.getReturnType().equals(Void.TYPE));
            return serviceCallResult;
        } catch (NoSuchMethodException e) {
            throw new ServiceException("Method " + methodName + " with such signature not found");
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new ServiceException(e.getMessage(), e);
        }
    }
}
