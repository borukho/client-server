package ru.oxothuk.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.oxothuk.client.Request;
import ru.oxothuk.client.Response;
import ru.oxothuk.service.Service;
import ru.oxothuk.service.ServiceLocator;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

public class ServiceCaller {
    private static Logger logger = LogManager.getLogger(ServiceCaller.class);
    private static final int THREAD_COUNT = 2;
    private ExecutorService executor;
    private ServiceLocator serviceLocator;

    public ServiceCaller(ServiceLocator serviceLocator) {
        this.serviceLocator = serviceLocator;
        this.executor = Executors.newFixedThreadPool(THREAD_COUNT);
    }

    public void call(Request request, ResponseCallback callback) {
        logger.info("request: {}", request);
        Optional<Service> service = serviceLocator.getServiceByName(request.getServiceName());
        if (!service.isPresent()) {
            callback.callback(new Response()
                .setId(request.getId())
                .setSuccess(false)
                .setException(new Exception("Service " + request.getServiceName() + " not found"))
            );
        } else {
            executor.submit(() -> {
                Response response;
                try {
                    Object result = processServiceCall(service.get(), request.getMethodName(), request.getParameters());
                    response = new Response()
                        .setId(request.getId())
                        .setSuccess(true)
                        .setResult(result);
                } catch (Exception e) {
                    logger.warn("Error executing service method", e);
                    response = new Response()
                        .setId(request.getId())
                        .setSuccess(false)
                        .setException(e);
                }
                logger.info("response: {}", response);
                callback.callback(response);
            });
        }
    }

    private Object processServiceCall(Service service, String methodName, Object[] parameters) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Class[] parameterClasses = Stream.of(parameters)
            .map(Object::getClass)
            .toArray(Class[]::new);
        Method method = service.getClass().getMethod(methodName, parameterClasses);
        return method.invoke(service, parameters);
    }

    public void shutdown() {
        executor.shutdown();
    }
}
