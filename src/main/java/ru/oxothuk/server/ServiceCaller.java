package ru.oxothuk.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.oxothuk.client.Request;
import ru.oxothuk.client.Response;
import ru.oxothuk.client.VoidResponse;
import ru.oxothuk.service.Service;
import ru.oxothuk.service.ServiceLocator;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

class ServiceCaller {
    private static Logger logger = LogManager.getLogger(ServiceCaller.class);
    private static final int THREAD_COUNT = 2;
    private ExecutorService executor;
    private ServiceLocator serviceLocator;

    ServiceCaller(ServiceLocator serviceLocator) {
        this.serviceLocator = serviceLocator;
        this.executor = Executors.newFixedThreadPool(THREAD_COUNT);
    }

    void call(Request request, ResponseCallback callback) {
        logger.info("calling {}.{}({})", request.getServiceName(), request.getMethodName(), request.getParameters());
        Optional<Service> service = serviceLocator.getServiceByName(request.getServiceName());
        if (!service.isPresent()) {
            callback.callback(new Response()
                .setId(request.getId())
                .setSuccess(false)
                .setException(new ServiceException("Service " + request.getServiceName() + " not found"))
            );
        } else {
            executor.submit(() -> {
                Response response;
                try {
                    response = callService(service.get(), request);
                } catch (Exception e) {
                    logger.warn("Error executing service method", e);
                    response = new Response()
                        .setId(request.getId())
                        .setSuccess(false)
                        .setException(e);
                }
                callback.callback(response);
            });
        }
    }

    private Response callService(Service service, Request request) throws InvocationTargetException, IllegalAccessException, ServiceException {
        String methodName = request.getMethodName();
        Object[] parameters = request.getParameters();

        Object target = service.getTarget();
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
            Object result = method.invoke(target, parameters);

            if (method.getReturnType().equals(Void.TYPE)) {
                return new VoidResponse()
                    .setId(request.getId())
                    .setSuccess(true);
            }
            return new Response()
                .setId(request.getId())
                .setSuccess(true)
                .setResult(result);

        } catch (NoSuchMethodException e) {
            throw new ServiceException("Method " + methodName + " with such signature not found");
        }
    }

    void shutdown() {
        executor.shutdownNow();
    }
}
