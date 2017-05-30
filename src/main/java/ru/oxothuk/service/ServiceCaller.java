package ru.oxothuk.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.oxothuk.model.Request;
import ru.oxothuk.model.Response;
import ru.oxothuk.model.VoidResponse;

import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServiceCaller {
    private static Logger logger = LogManager.getLogger(ServiceCaller.class);
    private ExecutorService executor;
    private ServiceLocator serviceLocator;

    public ServiceCaller(ServiceLocator serviceLocator, int threads) {
        this.serviceLocator = serviceLocator;
        this.executor = Executors.newFixedThreadPool(Math.max(1, threads));
    }

    public void call(Request request, ResponseCallback callback) {
        executor.submit(() -> {
            Response response;
            Optional<Service> service = serviceLocator.getServiceByName(request.getServiceName());
            if (!service.isPresent()) {
                callback.callback(new Response()
                    .setId(request.getId())
                    .setSuccess(false)
                    .setException(new WrongMethodException("Service " + request.getServiceName() + " not found"))
                );
            } else {
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
            }
        });
    }

    private Response callService(Service service, Request request) throws ServiceException {
        logger.info("calling {}.{}({})", request.getServiceName(), request.getMethodName(), request.getParameters());
        String methodName = request.getMethodName();
        Object[] parameters = request.getParameters();
        ServiceCallResult serviceCallResult = service.call(methodName, parameters);
        if (serviceCallResult.isVoid()) {
            return new VoidResponse()
                .setId(request.getId())
                .setSuccess(true);
        }
        return new Response()
            .setId(request.getId())
            .setSuccess(true)
            .setResult(serviceCallResult.getValue());

    }

    public void shutdown() {
        executor.shutdownNow();
    }
}
